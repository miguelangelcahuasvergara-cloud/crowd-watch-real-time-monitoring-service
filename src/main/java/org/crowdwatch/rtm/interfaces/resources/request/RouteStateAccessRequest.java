package org.crowdwatch.rtm.interfaces.resources.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RouteStateAccessRequest(
    @NotNull(message = "organizationCode must not be null, empty or blank")
    String organizationCode,
    @NotBlank(message = "routeCode must not be null, empty or blank")
    String routeCode
) {}
