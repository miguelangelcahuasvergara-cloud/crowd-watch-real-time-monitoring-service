package org.crowdwatch.rtm.infrastructure.clients.resources.response;

public record RouteResponse(
    String code,
    String name,
    String description,
    String organizationCode,
    String centerControlOperatorCode
) {}
