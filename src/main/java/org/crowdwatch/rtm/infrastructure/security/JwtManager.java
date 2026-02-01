package org.crowdwatch.rtm.infrastructure.security;

import java.util.concurrent.atomic.AtomicReference;

import org.crowdwatch.rtm.infrastructure.clients.IaamClient;
import org.crowdwatch.rtm.infrastructure.clients.resources.request.LoginRequest;
import org.crowdwatch.rtm.interfaces.Result;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.GenericType;

@ApplicationScoped
public class JwtManager {
    private static final String DEFAULT_JWT_VALUE = "";
    private final String email;
    private final String password;
    private final JwtVerifier jwtVerifier;
    private AtomicReference<String> jwtReference;

    @Inject
    @RestClient
    IaamClient iaamClient;

    @Inject
    public JwtManager(
        @ConfigProperty(name = "external.auth.jwt.email") String email,
        @ConfigProperty(name = "external.auth.jwt.password") String password,
        JwtVerifier jwtVerifier
    ) {
        this.email = email;
        this.password = password;
        this.jwtReference = new AtomicReference<String>(DEFAULT_JWT_VALUE);
        this.jwtVerifier = jwtVerifier;
    }

    public Uni<String> getJwt() {
        final String jwt = jwtReference.get();
        final Uni<String> generateNewJwt = iaamClient.login(LoginRequest.builder().emailAddress(email).password(password).build())
            .map(response -> response.readEntity(new GenericType<Result<String>>(){}))
            .map(result -> "Bearer " + result.data())
            .invoke(newJwt -> jwtReference.set(newJwt))
            .log();
        if(DEFAULT_JWT_VALUE.equals(jwt)) {
            return generateNewJwt;
        }
        return jwtVerifier.verifyExpiration(jwt)
        .chain(isExpired -> isExpired ? generateNewJwt : Uni.createFrom().item(jwt))
        .log();
    }
}
