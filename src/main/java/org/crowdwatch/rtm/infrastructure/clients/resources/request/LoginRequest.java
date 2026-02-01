package org.crowdwatch.rtm.infrastructure.clients.resources.request;

import lombok.Builder;

@Builder
public record LoginRequest(
    String emailAddress,
    String password
) {}