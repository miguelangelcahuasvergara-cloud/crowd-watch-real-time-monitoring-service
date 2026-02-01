package org.crowdwatch.rtm.infrastructure.repositories;

import java.util.List;
import java.util.Optional;
import static java.util.Objects.isNull;

import org.crowdwatch.rtm.domain.models.BusState;
import org.crowdwatch.rtm.domain.repositories.BusStateManagementRepository;
import org.crowdwatch.rtm.infrastructure.config.RedisConfig.CustomRedisQualifier;
import org.crowdwatch.rtm.infrastructure.utils.JsonService;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.redis.client.Redis;
import io.vertx.mutiny.redis.client.RedisAPI;
import io.vertx.mutiny.redis.client.Response;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class RedisBusStateManagementRepository implements BusStateManagementRepository {
    final RedisAPI redisAPI;
    final JsonService jsonService;

    @Inject
    public RedisBusStateManagementRepository(
        @CustomRedisQualifier Redis redis,
        JsonService jsonService
    ) {
        redisAPI = RedisAPI.api(redis);
        this.jsonService = jsonService;
    }

    @Override
    public Uni<BusState> saveBusState(BusState busState) {
        String key = "Bus: " + busState.getBusCode();
        return jsonService.serializeToJsonAsync(busState)
            .flatMap(json -> redisAPI.set(
                List.of(key, json)
            ))
            .map(_response -> busState);
    }

    @Override
    public Uni<Boolean> existsBusStateByBusCode(String busCode) {
        String key = "Bus: " + busCode;
        return redisAPI.exists(List.of(key))
            .map(response -> response.toLong() > 0);
    }

    @Override
    public Uni<Optional<BusState>> getBusStateByBusCode(String busCode) {
        String key = "Bus: " + busCode;
        return redisAPI.get(key)
            .flatMap(response -> {
                if(isNull(response)) {
                    return Uni.createFrom().item(Optional.empty());
                }
                return jsonService.deserializeFromJsonAsync(
                    response.toString(), BusState.class
                ).map(busState -> Optional.of(busState));
            });
    }

    @Override
    public Uni<Boolean> deleteBusState(String busCode) {
        String key = "Bus: " + busCode;
        return redisAPI.del(List.of(key))
            .map(response -> response.toLong() == 1);
    }

    @Override
    public Uni<Optional<String>> getBusStateJsonByBusCode(String busCode) {
        String key = "Bus: " + busCode;
        return redisAPI.get(key)
            .map(response -> {
                if(isNull(response)) {
                    return Optional.empty();
                }
                return Optional.of(response.toString());
            });
    }

    public Multi<String> getAllBusStatesJson() {
        return redisAPI.keys("*")
            .onItem()
            .transformToMulti(Response::toMulti)
            .flatMap(key -> {
                return redisAPI.get(key.toString())
                    .onItem().ifNull()
                    .failWith(() -> new RuntimeException(
                        key + " key does not exists but it should"
                    ))
                    .map(res -> res.toString())
                    .toMulti();
            });

    }

    // FIXME: Este método se debe optimizar para mejorar su performance
    @Override
    public Multi<BusState> getBusStatesByRouteCode(String routeCode) {
        return redisAPI.keys("*")
            .onItem().transformToMulti(response -> {
                if(response == null) {
                    return Multi.createFrom().empty();
                }
                return response.toMulti();
            })
            .map(key -> key.toString())
            .filter(key -> key.startsWith("Bus: "))
            .flatMap(key -> {
                return redisAPI.get(key).toMulti();
            })
            .flatMap(busStateJson -> {
                return jsonService.deserializeFromJsonAsync(busStateJson.toString(), BusState.class).toMulti();
            })
            .filter(busState -> {
                return busState.getRouteCode().equals(routeCode);
            });
    }
    
}
