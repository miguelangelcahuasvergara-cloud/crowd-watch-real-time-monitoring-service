package org.crowdwatch.rtm.domain.models;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public record GPSLocation(
   String latitude,
   String longitude
) {}
