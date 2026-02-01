package org.crowdwatch.rtm.application;

import org.crowdwatch.rtm.domain.models.BusState;
import org.crowdwatch.rtm.domain.repositories.BusStateManagementRepository;
import org.crowdwatch.rtm.domain.service.MonitoringService;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class MonitoringServiceImpl implements MonitoringService {
    final BusStateManagementRepository busStateManagementRepository;

    @Inject
    public MonitoringServiceImpl(
        BusStateManagementRepository busStateManagementRepository
    ) {
        this.busStateManagementRepository = busStateManagementRepository;
    }

    @Override
    public Uni<String> changeBusStateMonitoringState(
        String busCode,
        boolean monitoringIsActivated
    ) {
        return busStateManagementRepository.getBusStateByBusCode(busCode)
            .map(existingBusState -> {
                if(existingBusState.isEmpty()) {
                    return BusState.builder()
                        .busCode(busCode)
                        .currentOccupancy(0)
                        .monitoringIsActivated(monitoringIsActivated)
                        .build();
                }
                BusState busState = existingBusState.get();
                busState.setMonitoringIsActivated(monitoringIsActivated);
                return existingBusState.get();
            })
            .flatMap(busState -> busStateManagementRepository
                .saveBusState(busState)
            )
            .replaceWith(
                "Bus state monitoring enabled was changed to " + monitoringIsActivated
            );
    } 
}
