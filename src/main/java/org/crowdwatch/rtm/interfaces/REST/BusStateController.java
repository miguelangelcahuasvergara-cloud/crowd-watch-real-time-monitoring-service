package org.crowdwatch.rtm.interfaces.REST;

import java.util.UUID;

import org.crowdwatch.rtm.domain.service.BusStateManagementService;
import org.crowdwatch.rtm.domain.service.BusStateNotifierService;
import org.crowdwatch.rtm.domain.service.MonitoringService;
import org.crowdwatch.rtm.domain.service.RouteStateNotifierService;
import org.crowdwatch.rtm.interfaces.Result;
import org.crowdwatch.rtm.interfaces.resources.request.BusStateAccessRequest;

import io.smallrye.mutiny.Uni;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/crowd-watch/realtime-monitoring-service/buses/{busCode}/monitoring")
public class BusStateController {
    final MonitoringService monitoringService;
    final BusStateNotifierService busStateNotifierService;
    final BusStateManagementService busStateManagementService;
    final RouteStateNotifierService routeStateNotifierService;

    @Inject
    public BusStateController(
        MonitoringService monitoringService,
        BusStateNotifierService busStateNotifierService,
        BusStateManagementService busStateManagementService,
        RouteStateNotifierService routeStateNotifierService
    ) {
        this.monitoringService = monitoringService;
        this.busStateNotifierService = busStateNotifierService;
        this.busStateManagementService = busStateManagementService;
        this.routeStateNotifierService = routeStateNotifierService;
    }

    @Path("/activate")
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed(value = {"Admin", "Transport manager", "Control center operator"})
    public Uni<Response> activateBusStateMonitoring(
        @PathParam("busCode") String busCode
    ) {
        return monitoringService.changeBusStateMonitoringState(
            busCode,
            true
        ).map(Result::success)
        .map(result -> Response.status(result.httpStatus()).entity(result).build());
    }

    @Path("/deactivate")
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed(value = {"Admin", "Transport manager", "Control center operator"})
    public Uni<Response> deactivateBusStateMonitoring(
        @PathParam("busCode") String busCode
    ) {
        return monitoringService.changeBusStateMonitoringState(
            busCode,
            false
        ).map(Result::success)
        .map(result -> Response.status(result.httpStatus()).entity(result).build());
    }

    @Path("/access")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed(value = {"Admin", "Transport manager", "Control center operator", "Bus driver"})
    public Uni<Response> registerNewListener(
        @Valid BusStateAccessRequest busStateAccessRequest
    ) {
        final String userId = UUID.randomUUID().toString();
        return busStateNotifierService.registerNewListenerToNotifyBusState(
            busStateAccessRequest.organizationCode(),
            busStateAccessRequest.routeCode(),
            busStateAccessRequest.busCode(),
            userId
        ).map(Result::success)
        .map(result -> Response.status(result.httpStatus()).entity(result).build());
    }

    @Path("/access/{userId}")
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed(value = {"Admin", "Transport manager", "Control center operator", "Bus driver"})
    public Uni<Response> removeListener(
        @PathParam("userId") String userId,
        @Valid BusStateAccessRequest busStateAccessRequest
    ) {
        return busStateNotifierService.removeRegisteredListener(
            busStateAccessRequest.organizationCode(),
            busStateAccessRequest.routeCode(),
            busStateAccessRequest.busCode(),
            userId
        ).map(Result::success)
        .map(result -> Response.status(result.httpStatus()).entity(result).build());
    }
}
