package org.crowdwatch.rtm.infrastructure.clients;

import org.crowdwatch.rtm.infrastructure.clients.resources.request.LoginRequest;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/crowd-watch/identity-and-access-management-service")
@RegisterRestClient
public interface IaamClient {
    @POST
    @Path("/auth/login")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Response> login(LoginRequest request);
}
