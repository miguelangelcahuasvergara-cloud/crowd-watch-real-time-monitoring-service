package org.crowdwatch.rtm.interfaces.REST;

import org.crowdwatch.rtm.domain.models.BusState;
import org.crowdwatch.rtm.domain.service.BusStateManagementService;
import org.crowdwatch.rtm.domain.service.BusStateNotifierService;
import org.crowdwatch.rtm.domain.service.RouteStateNotifierService;
import org.crowdwatch.rtm.infrastructure.utils.JsonService;
import org.crowdwatch.rtm.interfaces.Result;

import io.smallrye.mutiny.Uni;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;

@Path("/crowd-watch/realtime-monitoring-service/event-handler")
@Slf4j
public class BusStateEventHandler {
    private static final String BUS_DRIVER_EVENT_NAME = "bus-driver-bus-state-event";
    final BusStateManagementService busStateManagementService;
    final RouteStateNotifierService routeStateNotifierService;
    final BusStateNotifierService busStateNotifierService;
    final JsonService jsonService;

    @Inject
    public BusStateEventHandler(
        BusStateManagementService busStateManagementService,
        RouteStateNotifierService routeStateNotifierService,
        BusStateNotifierService busStateNotifierService,
        JsonService jsonService
    ) {
        this.busStateManagementService = busStateManagementService;
        this.routeStateNotifierService = routeStateNotifierService;
        this.busStateNotifierService = busStateNotifierService;
        this.jsonService = jsonService;
    }
    /*
     * El valor del header ce-eventName debería ser bus-driver-bus-state-event
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @PermitAll
    public Uni<Response> receiveBusStateFromBusDriver(
        @HeaderParam("ce-eventName") String eventName,
        String json
    ) {
        if(!BUS_DRIVER_EVENT_NAME.equals(eventName)) {
            log.info("Event name: {}", eventName);
            return Uni.createFrom().item(
                Response.ok(Result.success("This message is skipped")).build()
            );
        }
        log.info("Se invoca el endpoint para interceptar mensajes al web hook");
        return jsonService.deserializeFromJsonAsync(json, BusState.class)
            .chain(busState -> busStateManagementService.saveBusState(busState.getBusCode(), busState))
            .call(savedBusState -> busStateNotifierService.notifyBusStateToBusListeners(savedBusState))
            .call(savedBusState -> routeStateNotifierService.notifyBusStateToRouteListeners(savedBusState))
            .map(savedBusState -> {
                var result = Result.success("Bus with with code " + savedBusState.getBusCode() + " updated its state");
                return Response.status(result.httpStatus()).entity(result).build();
            })
            .onFailure().recoverWithItem(failure -> {
                log.error("Failure in azure web pub sub web hook invoked endpoint", failure);
                var result = Result.fromException(failure);
                return Response.status(result.httpStatus()).entity(result).build();
            });
    }
}
