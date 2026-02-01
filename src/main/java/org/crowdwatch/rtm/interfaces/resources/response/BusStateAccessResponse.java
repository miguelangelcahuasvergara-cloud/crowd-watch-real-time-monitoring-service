package org.crowdwatch.rtm.interfaces.resources.response;

import org.crowdwatch.rtm.domain.models.BusState;

import lombok.Builder;

@Builder
public record BusStateAccessResponse(
    String url,
    String userId,
    BusState currentBusState
) {}