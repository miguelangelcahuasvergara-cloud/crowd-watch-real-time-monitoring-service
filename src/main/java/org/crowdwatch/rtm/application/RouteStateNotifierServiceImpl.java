package org.crowdwatch.rtm.application;

import org.crowdwatch.rtm.domain.external.RealtimeMessagingClient;
import org.crowdwatch.rtm.domain.models.BusState;
import org.crowdwatch.rtm.domain.repositories.BusStateManagementRepository;
import org.crowdwatch.rtm.domain.service.RouteStateNotifierService;
import org.crowdwatch.rtm.infrastructure.utils.JsonService;
import org.crowdwatch.rtm.interfaces.resources.response.RouteStateAccessResponse;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class RouteStateNotifierServiceImpl implements RouteStateNotifierService {
    private final RealtimeMessagingClient realtimeMessagingPort;
    private final BusStateManagementRepository busStateManagementRepository;
    private final JsonService jsonService;

    @Inject
    public RouteStateNotifierServiceImpl(
        RealtimeMessagingClient realtimeMessagingPort,
        BusStateManagementRepository busStateManagementRepository,
        JsonService jsonService
    ) {
        this.realtimeMessagingPort = realtimeMessagingPort;
        this.busStateManagementRepository = busStateManagementRepository;
        this.jsonService = jsonService;
    }

    @Override
    public Uni<String> removeRegisteredListener(String organizationCode, String routeCode, String userId) {
        final String groupName = organizationCode + routeCode;
        return realtimeMessagingPort.removeExistingListener(groupName, userId);
    }

    @Override
    public Uni<RouteStateAccessResponse> registerNewListenerToNotifyRouteState(String organizationCode, String routeCode, String userId) {
        final String groupName = organizationCode + routeCode;
        return realtimeMessagingPort.generateNewListenerAccessUrl(groupName, userId)
            .chain(url -> busStateManagementRepository
                .getBusStatesByRouteCode(routeCode)
                .collect().asList().map(busesStates -> new RouteStateAccessResponse(url, userId, busesStates))
            );
    }

    @Override
    public Uni<Void> notifyBusStateToRouteListeners(BusState busState) {
        final String routeGroupName = busState.getOrganizationCode() + busState.getRouteCode();
        return jsonService.serializeToJsonAsync(busState)
            .chain(json -> realtimeMessagingPort.notifyBusState(routeGroupName, json));
    }
    
}
