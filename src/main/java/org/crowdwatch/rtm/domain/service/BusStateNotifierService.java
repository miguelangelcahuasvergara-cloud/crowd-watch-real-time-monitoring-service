package org.crowdwatch.rtm.domain.service;

import org.crowdwatch.rtm.domain.models.BusState;
import org.crowdwatch.rtm.interfaces.resources.response.BusStateAccessResponse;

import io.smallrye.mutiny.Uni;

public interface BusStateNotifierService {
    Uni<String> removeRegisteredListener(String organizationCode, String routeCode, String busCode, String userId);
    Uni<BusStateAccessResponse> registerNewListenerToNotifyBusState(String organizationCode, String routeCode, String busCode, String userId);
    Uni<Void> notifyBusStateToBusListeners(BusState busState);
}
