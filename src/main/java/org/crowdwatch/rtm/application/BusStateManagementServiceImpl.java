package org.crowdwatch.rtm.application;

import org.crowdwatch.rtm.domain.models.BusState;
import org.crowdwatch.rtm.domain.models.BusStateNotification;
import org.crowdwatch.rtm.domain.repositories.BusStateManagementRepository;
import org.crowdwatch.rtm.domain.service.BusStateManagementService;
import org.crowdwatch.rtm.infrastructure.clients.BarmClient;
import org.crowdwatch.rtm.infrastructure.messaging.OperatorNotificationsMessagingProcessor;
import org.crowdwatch.rtm.infrastructure.security.JwtManager;
import org.crowdwatch.rtm.infrastructure.utils.JsonService;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

@ApplicationScoped
@Slf4j
public class BusStateManagementServiceImpl implements BusStateManagementService {
    final BusStateManagementRepository busStateManagementRepository;
    final JsonService jsonService;
    final OperatorNotificationsMessagingProcessor operatorNotificationsMessagingProcessor;
    final JwtManager jwtManager;
    @Inject
    @RestClient
    BarmClient barmClient;
    
    public BusStateManagementServiceImpl(
        BusStateManagementRepository busStateManagementRepository,
        JsonService jsonService,
        OperatorNotificationsMessagingProcessor operatorNotificationsMessagingProcessor,
        JwtManager jwtManager
    ) {
        this.busStateManagementRepository = busStateManagementRepository;
        this.jsonService = jsonService;
        this.operatorNotificationsMessagingProcessor = operatorNotificationsMessagingProcessor;
        this.jwtManager = jwtManager;
    }

    @Override
    public Uni<BusState> saveBusState(
        String busCode,
        BusState busState
    ) {
        return busStateManagementRepository
            .getBusStateByBusCode(busCode)
            .map(existingBusState -> {
                if(existingBusState.isEmpty()) {
                    busState.setCurrentOccupancy(0);
                    return busState;
                }
                return existingBusState.get();
            })
            .flatMap(state -> {
                state.update(busState);
                if(state.capacityWasExceeded()) {
                    return sendBusCapacityExceededNotificationAndSaveBusState(state);
                }
                if(state.currentOccupancyIsNegative()) {
                    return sendInconsistentBusCapacityDetectedNotification(state);
                }
                return busStateManagementRepository.saveBusState(state);
            });
    }

    Uni<BusState> sendBusCapacityExceededNotificationAndSaveBusState(
        BusState busState
    ) {
        return operatorNotificationsMessagingProcessor
            .sendMessage(
                BusStateNotification.createBusCapacityExceededNotificationJson(
                    busState.getBusCode(),
                    busState.getCenterControlOperatorCode()
                )
            ).flatMap(r -> busStateManagementRepository.saveBusState(busState));
    }

    Uni<BusState> sendInconsistentBusCapacityDetectedNotification(BusState busState) {
        return operatorNotificationsMessagingProcessor
            .sendMessage(
                BusStateNotification.createInconsistentBusCapacityDetectedAlert(
                    busState.getBusCode(),
                    busState.getCenterControlOperatorCode()
                )
            ).flatMap(r -> busStateManagementRepository.saveBusState(busState));
    }

    @Override
    public Uni<Boolean> deleteBusState(String busCode) {
        return busStateManagementRepository.deleteBusState(busCode);
    }
}
