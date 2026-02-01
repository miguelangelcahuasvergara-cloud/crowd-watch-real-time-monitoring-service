package org.crowdwatch.rtm.domain.service;

import org.crowdwatch.rtm.domain.models.BusState;

import io.smallrye.mutiny.Uni;

public interface BusStateManagementService {
    Uni<BusState> saveBusState(String busCode, BusState busState);
    Uni<Boolean> deleteBusState(String busCode);
}
