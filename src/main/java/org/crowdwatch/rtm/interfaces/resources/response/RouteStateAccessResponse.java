package org.crowdwatch.rtm.interfaces.resources.response;

import java.util.List;

import org.crowdwatch.rtm.domain.models.BusState;

public record RouteStateAccessResponse(
    String url,
    String userId,
    List<BusState> currentBusesStates
) {}