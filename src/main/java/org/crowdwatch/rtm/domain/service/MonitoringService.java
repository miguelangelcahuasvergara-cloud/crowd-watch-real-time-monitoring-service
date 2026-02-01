package org.crowdwatch.rtm.domain.service;

import io.smallrye.mutiny.Uni;

public interface MonitoringService {
    Uni<String> changeBusStateMonitoringState(
        String busCode,
        boolean monitoringIsActivated
    );
}