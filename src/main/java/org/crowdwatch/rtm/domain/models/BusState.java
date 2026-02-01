package org.crowdwatch.rtm.domain.models;

import org.crowdwatch.rtm.domain.enums.BusEvent;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.Builder.Default;

import static java.util.Objects.requireNonNullElse;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@RegisterForReflection
@Setter
@ToString
public class BusState {
    private String busCode;
    private String routeCode;
    private String centerControlOperatorCode;
    private GPSLocation location;
    private Integer currentOccupancy;
    private Integer capacity;
    @Default
    private boolean monitoringIsActivated = true;
    private String organizationCode;

    public static BusState fromOccupancyUpdate(OccupancyUpdate occupancyUpdate) {
        BusEvent busEvent = occupancyUpdate.eventType();
        int currentOccupancy = BusEvent.PASSENGER_ENTRY.equals(busEvent) ? 1 : 0;
        return BusState.builder()
            .busCode(occupancyUpdate.busCode())
            .currentOccupancy(currentOccupancy)
            .build();
    }

    public static BusState emptyBusState() {
        return BusState.builder().build();
    }

    public void updateOccupancy(OccupancyUpdate occupancyUpdate) {
        BusEvent busEvent = occupancyUpdate.eventType();
        
        if(BusEvent.PASSENGER_ENTRY.equals(busEvent)) {
            ++currentOccupancy;
        }
        if(BusEvent.PASSENGER_EXIT.equals(busEvent)) {
            --currentOccupancy;
        }
    }

    public void updateOccupancy(OccupancyDetection occupancyDetection) {
       BusEvent busEvent = occupancyDetection.getDetectionType();
        
        if(BusEvent.PASSENGER_ENTRY.equals(busEvent)) {
            ++currentOccupancy;
        }
        if(BusEvent.PASSENGER_EXIT.equals(busEvent)) {
            --currentOccupancy;
        }
    }

    public void updateLocation(GPSLocation newLocation) {
        location = newLocation;
    }

    public void update(BusState busState) {
        routeCode = requireNonNullElse(busState.routeCode, routeCode);
        location = requireNonNullElse(busState.location, location);
        capacity = requireNonNullElse(busState.capacity, capacity);
        organizationCode = requireNonNullElse(busState.organizationCode, organizationCode);
        centerControlOperatorCode = requireNonNullElse(busState.centerControlOperatorCode, centerControlOperatorCode);
    }

    public boolean capacityWasExceeded() {
        return currentOccupancy > capacity;
    }
    public boolean currentOccupancyIsNegative() {
        return currentOccupancy < 0;
    }
}
