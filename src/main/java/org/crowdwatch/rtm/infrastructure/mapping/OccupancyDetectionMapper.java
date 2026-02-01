package org.crowdwatch.rtm.infrastructure.mapping;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.crowdwatch.rtm.domain.models.BusState;
import org.crowdwatch.rtm.domain.models.OccupancyDetection;
import org.crowdwatch.rtm.domain.models.OccupancyUpdate;

public final class OccupancyDetectionMapper {
    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private OccupancyDetectionMapper() {}
    public static OccupancyDetection createFromOccupancyUpdateAndBusState(
        OccupancyUpdate occupancyUpdate,
        BusState busState
    ) {
        return OccupancyDetection.builder()
                    .busCode(occupancyUpdate.busCode())
                    .organizationCode(busState.getOrganizationCode())
                    .routeCode(busState.getRouteCode())
                    .busCode(busState.getBusCode())
                    .detectionType(occupancyUpdate.eventType())
                    .detectionSendAt(LocalDateTime.parse(occupancyUpdate.timestamp(), dateTimeFormatter))
                    .detectionReceivedAt(LocalDateTime.now())
                    .detectionProcessingStartedAt(LocalDateTime.now())
                    .build();
    }
}
