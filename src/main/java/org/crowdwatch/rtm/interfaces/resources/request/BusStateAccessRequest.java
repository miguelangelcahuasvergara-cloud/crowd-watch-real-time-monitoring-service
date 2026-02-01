package org.crowdwatch.rtm.interfaces.resources.request;

import jakarta.validation.constraints.NotBlank;

public record BusStateAccessRequest(
    @NotBlank(message = "organizationCode must not be null, empty or blank")
    String organizationCode,
    @NotBlank(message = "routeCode must not be null, empty or blank")
    String routeCode,
    @NotBlank(message = "busCode must not be null, empty or blank")
    String busCode
) {}