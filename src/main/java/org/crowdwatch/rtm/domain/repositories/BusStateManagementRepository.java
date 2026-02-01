package org.crowdwatch.rtm.domain.repositories;

import org.crowdwatch.rtm.domain.models.BusState;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import java.util.Optional;

public interface BusStateManagementRepository {
    Uni<BusState> saveBusState(BusState busState);
    Uni<Boolean> existsBusStateByBusCode(String busCode);
    Uni<Optional<BusState>> getBusStateByBusCode(String busCode);
    Uni<Optional<String>> getBusStateJsonByBusCode(String busCode);
    Uni<Boolean> deleteBusState(String busCode);
    Multi<BusState> getBusStatesByRouteCode(String routeCode);
}
