package org.crowdwatch.rtm.domain.service;

import org.crowdwatch.rtm.domain.models.OccupancyUpdate;

public interface OccupancyDetectionService {
    void processBusOccupancyDetectionAndNotifyToListeners(OccupancyUpdate occupancyUpdate);
}
