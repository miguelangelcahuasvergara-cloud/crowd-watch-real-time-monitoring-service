package org.crowdwatch.rtm.infrastructure.clients.resources.response;

public record BusResponse(
    String code,
    String licencePlate,
    Long capacity,
    RouteResponse route,
    String status,
    String organizationCode
) {}