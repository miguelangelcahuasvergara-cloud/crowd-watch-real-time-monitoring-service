package org.crowdwatch.rtm.application;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

import org.crowdwatch.rtm.domain.external.RealtimeMessagingClient;
import org.crowdwatch.rtm.domain.models.BusState;
import org.crowdwatch.rtm.domain.models.BusStateNotification;
import org.crowdwatch.rtm.domain.models.OccupancyDetection;
import org.crowdwatch.rtm.domain.models.OccupancyUpdate;
import org.crowdwatch.rtm.domain.repositories.BusStateManagementRepository;
import org.crowdwatch.rtm.domain.service.OccupancyDetectionService;
import org.crowdwatch.rtm.infrastructure.clients.BarmClient;
import org.crowdwatch.rtm.infrastructure.clients.resources.response.BusResponse;
import org.crowdwatch.rtm.infrastructure.clients.resources.response.RouteResponse;
import org.crowdwatch.rtm.infrastructure.exceptions.MappingException;
import org.crowdwatch.rtm.infrastructure.mapping.OccupancyDetectionMapper;
import org.crowdwatch.rtm.infrastructure.messaging.OperatorNotificationsMessagingProcessor;
import org.crowdwatch.rtm.infrastructure.security.JwtManager;
import org.crowdwatch.rtm.infrastructure.utils.JsonService;
import org.crowdwatch.rtm.infrastructure.utils.ReactiveUtils;
import org.crowdwatch.rtm.interfaces.Result;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.vertx.ConsumeEvent;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.core.Context;
import io.vertx.mutiny.core.Vertx;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.GenericType;
import lombok.extern.slf4j.Slf4j;

@ApplicationScoped
@Slf4j
public class OccupancyDetectionServiceImpl implements OccupancyDetectionService {
    final BusStateManagementRepository busStateManagementRepository;
    final OperatorNotificationsMessagingProcessor operatorNotificationsMessagingProcessor;
    final JwtManager jwtManager;
    final RealtimeMessagingClient realtimeMessagingClient;
    final JsonService jsonService;

    @Inject
    @RestClient
    BarmClient barmClient;

    @Inject
    public OccupancyDetectionServiceImpl(
        BusStateManagementRepository busStateManagementRepository,
        OperatorNotificationsMessagingProcessor operatorNotificationsMessagingProcessor,
        JwtManager jwtManager,
        RealtimeMessagingClient realtimeMessagingClient,
        JsonService jsonService
    ) {
        this.busStateManagementRepository = busStateManagementRepository;
        this.operatorNotificationsMessagingProcessor = operatorNotificationsMessagingProcessor;
        this.jwtManager = jwtManager;
        this.realtimeMessagingClient = realtimeMessagingClient;
        this.jsonService = jsonService;
    }

    Uni<BusState> getBusStateIfNotExists(String busCode) {
        return jwtManager.getJwt()
            .chain(jwt -> barmClient.getBusByCode(jwt, busCode))
            .map(response -> response.readEntity(new GenericType<Result<BusResponse>>() {}))
            .map(result -> {
                BusResponse busResponse = result.data();
                RouteResponse routeResponse = busResponse.route();
                return BusState.builder()
                    .organizationCode(routeResponse.organizationCode())
                    .centerControlOperatorCode(routeResponse.centerControlOperatorCode())
                    .routeCode(routeResponse.code())
                    .busCode(busResponse.code())
                    .currentOccupancy(0)
                    .capacity(busResponse.capacity().intValue())
                    .build();
            });
    }

    Uni<BusState> updateOccupancyAndSendEventsConditionally(BusState busState, OccupancyDetection occupancyDetection) {
        busState.updateOccupancy(occupancyDetection);
        List<Uni<Void>> notifications = new ArrayList<>();
        if(busState.capacityWasExceeded()) {
            notifications.add(operatorNotificationsMessagingProcessor.sendMessage(
                BusStateNotification.createBusCapacityExceededNotificationJson(
                    busState.getBusCode(),
                    busState.getCenterControlOperatorCode()
                )
            ));
        }
        if(busState.currentOccupancyIsNegative()) {
            notifications.add(operatorNotificationsMessagingProcessor.sendMessage(
                BusStateNotification.createInconsistentBusCapacityDetectedAlert(
                    busState.getBusCode(),
                    busState.getCenterControlOperatorCode()
                )
            ));
        }
        Uni<Void> combinedNotifications = notifications.isEmpty() ? Uni.createFrom().voidItem() 
            : Uni.combine().all().unis(notifications).discardItems();
        return combinedNotifications.chain(() -> busStateManagementRepository.saveBusState(busState));
    }

    Uni<?> updateBusStateAndNotify(
        final OccupancyDetection occupancyDetection,
        final BusState busState,
        final Context currentEventLoopContext
    ) {
        final Executor eventLoopExecutor = ReactiveUtils.createEventLoopExecutor(currentEventLoopContext);
        return updateOccupancyAndSendEventsConditionally(busState, occupancyDetection)
            .emitOn(eventLoopExecutor)
            .chain(state -> jsonService.serializeToJsonAsync(state)
                .call(json -> realtimeMessagingClient.notifyBusState(
                    state.getOrganizationCode() + state.getRouteCode() + state.getBusCode(), json
                ))
                .call(json -> realtimeMessagingClient.notifyBusState(
                    state.getOrganizationCode() + state.getRouteCode(), json
                ))
            )
            .emitOn(eventLoopExecutor)
            .chain(() -> {
                occupancyDetection.setDetectionProcessingFinishedAt(LocalDateTime.now());
                occupancyDetection.setCreatedAt(LocalDateTime.now());
                return Panache.withTransaction(() -> occupancyDetection.persistAndFlush());
            })
            .onFailure().call(exception -> {
                log.error("An error occured: ", exception);
                occupancyDetection.setCreatedAt(LocalDateTime.now());
                return Panache.withTransaction(() -> occupancyDetection.persist());
            });
    }

    @ConsumeEvent("process.occupancy-detection")
    @Override
    public void processBusOccupancyDetectionAndNotifyToListeners(OccupancyUpdate occupancyUpdate) {
        final Context currentEventLoopContext = Vertx.currentContext();

        busStateManagementRepository
            .getBusStateByBusCode(occupancyUpdate.busCode())
            .chain(existingBusState -> 
                existingBusState.isEmpty() ? getBusStateIfNotExists(occupancyUpdate.busCode()) : 
                Uni.createFrom().item(existingBusState.get())
            )
            .flatMap(busState -> {
                try {
                    OccupancyDetection occupancyDetection = OccupancyDetectionMapper.createFromOccupancyUpdateAndBusState(
                        occupancyUpdate, busState
                    );
                    return Uni.combine().all().unis(
                        Uni.createFrom().item(occupancyDetection),
                        Uni.createFrom().item(busState)
                    ).asTuple();
                }catch (Exception e) {
                    log.error("An error occured while mapping to OccupancyDetection", e);
                    return Uni.createFrom().failure(new MappingException(busState, OccupancyDetection.class));
                }
            })
            .onFailure().call(exception -> {
                if(!(exception instanceof MappingException)) {
                    return Uni.createFrom().voidItem();
                }
                log.info("Sending passenger detector receiving error alert");
                MappingException mappingException = (MappingException) exception;
                BusState busState = BusState.class.cast(mappingException.getSource());
                return operatorNotificationsMessagingProcessor.sendMessage(
                    BusStateNotification.createPassengerDetectionReceivingAlert(
                        busState.getBusCode(),
                        busState.getCenterControlOperatorCode()
                    )
                );
            })
            .flatMap(result -> {
                OccupancyDetection occupancyDetection = result.getItem1();
                BusState busState = result.getItem2();
                return updateBusStateAndNotify(
                    occupancyDetection,
                    busState,
                    currentEventLoopContext
                );
            })
            .subscribe()
            .with(
                success -> log.info("Processed successfully an occupancy detection"),
                failure -> log.error("An error occured while processing an occupancy detection", failure)
            );
    }
    
}
