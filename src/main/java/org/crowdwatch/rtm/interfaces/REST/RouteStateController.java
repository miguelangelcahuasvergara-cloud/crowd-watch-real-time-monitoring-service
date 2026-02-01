package org.crowdwatch.rtm.interfaces.REST;

import java.util.UUID;

import org.crowdwatch.rtm.domain.service.RouteStateNotifierService;
import org.crowdwatch.rtm.interfaces.Result;
import org.crowdwatch.rtm.interfaces.resources.request.RouteStateAccessRequest;

import io.smallrye.mutiny.Uni;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/crowd-watch/realtime-monitoring-service/routes/{routeCode}/route-state")
public class RouteStateController {

    private final RouteStateNotifierService routeStateNotifierService;

    @Inject
    public RouteStateController(
        RouteStateNotifierService routeStateNotifierService
    ) {
        this.routeStateNotifierService = routeStateNotifierService;
    }

    @Path("/access")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed(value = {"Admin", "Transport manager", "Control center operator"})
    public Uni<Response> registerNewListener(
        RouteStateAccessRequest request
    ) {
        final String userId = UUID.randomUUID().toString();
        return routeStateNotifierService.registerNewListenerToNotifyRouteState(
            request.organizationCode(),
            request.routeCode(),
            userId
        ).map(Result::success)
        .map(result -> Response.status(result.httpStatus()).entity(result).build());
    }

    @Path("/access/{userId}")
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed(value = {"Admin", "Transport manager", "Control center operator"})
    public Uni<Response> removeListener(
        @PathParam("userId") String userId,
        RouteStateAccessRequest request
    ) {
        return routeStateNotifierService.removeRegisteredListener(
            request.organizationCode(),
            request.routeCode(),
            userId
        ).map(Result::success)
        .map(result -> Response.status(result.httpStatus()).entity(result).build());
    }
}
