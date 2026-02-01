package org.crowdwatch.rtm.domain.service;

import org.crowdwatch.rtm.domain.models.BusState;
import org.crowdwatch.rtm.interfaces.resources.response.RouteStateAccessResponse;

import io.smallrye.mutiny.Uni;

public interface RouteStateNotifierService {
    Uni<String> removeRegisteredListener(String organizationCode, String routeCode, String userId);
    Uni<Void> notifyBusStateToRouteListeners(BusState busState);
    Uni<RouteStateAccessResponse> registerNewListenerToNotifyRouteState(String organizationCode, String routeCode, String userId);
}
