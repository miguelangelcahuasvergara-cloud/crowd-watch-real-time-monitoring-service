package org.crowdwatch.rtm.infrastructure.jobs;

import org.crowdwatch.rtm.infrastructure.config.RedisConfig.CustomRedisQualifier;
import org.crowdwatch.rtm.infrastructure.jobs.resources.ScanResult;
import org.crowdwatch.rtm.infrastructure.messaging.BusStateSnapshotMessagingProcessor;

import io.quarkus.scheduler.Scheduled;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.redis.client.Redis;
import io.vertx.mutiny.redis.client.RedisAPI;
import io.vertx.mutiny.redis.client.Response;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicReference;
import java.time.Duration;

@ApplicationScoped
@Slf4j
public class BusStateSnapshotJob {

    final RedisAPI redisAPI;
    final BusStateSnapshotMessagingProcessor busStateSnapshotMessagingProcessor;

    @Inject
    public BusStateSnapshotJob(
        @CustomRedisQualifier Redis redis,
        BusStateSnapshotMessagingProcessor busStateSnapshotMessagingProcessor
    ) {
        redisAPI = RedisAPI.api(redis);
        this.busStateSnapshotMessagingProcessor = busStateSnapshotMessagingProcessor;
    }

    @Scheduled(every = "${bus.snapshot.interval}")
    public void takeSnapshotEveryHour() {
        log.info("Taking buses states snapshot");
        AtomicReference<String> cursorRef = new AtomicReference<>("0"); // Cursor inicial

        Multi.createBy().repeating()
            .uni(() -> {
                String currentCursor = cursorRef.get(); // Obtener el cursor actual
                log.info("Scanning with cursor: " + currentCursor);
                return scan(currentCursor)
                    .onItem().invoke(scanResult -> {
                        log.info("Iteration scan result: {}", scanResult);
                        cursorRef.set(scanResult.getCursor()); // Actualizar el cursor para la próxima iteración
                    })
                    .ifNoItem().after(Duration.ofSeconds(10)).fail() // Tiempo de espera para evitar bloqueo
                    .onFailure().invoke(failure -> log.error("Scan failed", failure));
            })
            .whilst(scanResult -> !scanResult.getCursor().equals("0")) // Completa cuando el cursor vuelve a 0
            .onItem().invoke(scanResult -> log.info("Scan completed with cursor: " + scanResult.getCursor()))
            .onItem().transformToMulti(scanResult -> Multi.createFrom().iterable(scanResult.getKeys()))
            .merge()
            .onFailure().invoke(failure -> log.error("Error during scanning keys", failure))
            .flatMap(key -> redisAPI.get(key)
                .onItem().ifNotNull().transform(response -> response.toString())
                .onFailure().recoverWithNull() // Omitir valores en caso de error
                .toMulti()
            )
            .onFailure().invoke(failure -> log.error("Error while retrieving value from Redis", failure))
            .flatMap(json -> Multi.createFrom().completionStage(busStateSnapshotMessagingProcessor.sendMessage(json)))
            .onFailure().invoke(failure -> log.error("Error when sending message to queue", failure))
            .subscribe().with(
                success -> log.info("Success sending snapshot to queue"),
                failure -> log.error("Error in the whole process", failure)
            );
    }

    Uni<ScanResult> scan(String cursor) {
        return redisAPI.scan(List.of(cursor, "MATCH", "Bus:*"))
            .map(response -> {
                String nextCursor = response.get(0).toString();
                List<String> keys = new ArrayList<>();
                Response keysResponse = response.get(1);
                for (int i = 0; i < keysResponse.size(); i++) {
                    keys.add(keysResponse.get(i).toString());
                }
                boolean isComplete = "0".equals(nextCursor);
                return new ScanResult(nextCursor, keys, isComplete);
            });
    }
}
