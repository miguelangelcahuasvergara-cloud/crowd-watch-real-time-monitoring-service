package org.crowdwatch.rtm.application;

import org.crowdwatch.rtm.domain.external.RealtimeMessagingClient;
import org.crowdwatch.rtm.domain.models.BusState;
import org.crowdwatch.rtm.domain.repositories.BusStateManagementRepository;
import org.crowdwatch.rtm.domain.service.BusStateNotifierService;
import org.crowdwatch.rtm.infrastructure.utils.JsonService;
import org.crowdwatch.rtm.interfaces.resources.response.BusStateAccessResponse;

import io.smallrye.mutiny.Uni;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

@ApplicationScoped
@Slf4j
public class BusStateNotifierServiceImpl implements BusStateNotifierService {
    private final RealtimeMessagingClient realtimeMessagingPort;
    private final BusStateManagementRepository busStateManagementRepository;
    private final JsonService jsonService;

    @Inject
    public BusStateNotifierServiceImpl(
        RealtimeMessagingClient realtimeMessagingPort,
        BusStateManagementRepository busStateManagementRepository,
        JsonService jsonService
    ) {
        this.realtimeMessagingPort = realtimeMessagingPort;
        this.busStateManagementRepository = busStateManagementRepository;
        this.jsonService = jsonService;
    }

    @Override
    public Uni<String> removeRegisteredListener(
        String organizationCode, String routeCode,
        String busCode, String userId
    ) {
        final String groupName = organizationCode + routeCode + busCode;
        return realtimeMessagingPort.removeExistingListener(groupName, userId);
    }

    @Override
    public Uni<BusStateAccessResponse> registerNewListenerToNotifyBusState(
        String organizationCode, String routeCode,
        String busCode, String userId
    ) {
        final String groupName = organizationCode + routeCode + busCode;
        return realtimeMessagingPort.generateNewListenerAccessUrl(groupName, userId)
            .chain(url -> 
                busStateManagementRepository.getBusStateByBusCode(busCode)
                    .map(existingBusState -> existingBusState.orElse(null))
                    .map(busState -> new BusStateAccessResponse(url, userId, busState))
            );
    }

    @Override
    public Uni<Void> notifyBusStateToBusListeners(BusState busState) {
        final String groupName = busState.getOrganizationCode() + busState.getRouteCode() + busState.getBusCode();
        return jsonService.serializeToJsonAsync(busState)
            .chain(json -> realtimeMessagingPort.notifyBusState(groupName, json));
    }
}
