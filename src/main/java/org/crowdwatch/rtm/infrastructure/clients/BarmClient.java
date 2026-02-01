package org.crowdwatch.rtm.infrastructure.clients;

import org.crowdwatch.rtm.infrastructure.clients.resources.response.RouteResponse;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/crowd-watch/barm")
@RegisterRestClient
public interface BarmClient {
    @GET
    @Path("/routes/{code}/data-only")
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<RouteResponse> getRouteByCode(
        @PathParam("code") String routeCode
    );

    @GET
    @Path("/routes/buses/{busCode}")
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Response> getBusByCode(
        @HeaderParam("Authorization") String jwt,
        @PathParam("busCode") String busCode
    );
}