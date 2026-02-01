package org.crowdwatch.rtm.domain.models;

import org.crowdwatch.rtm.domain.enums.BusEvent;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public record OccupancyUpdate(
    String busCode,
    BusEvent eventType,
    String timestamp
) {}
