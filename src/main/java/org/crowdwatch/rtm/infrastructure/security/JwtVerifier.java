package org.crowdwatch.rtm.infrastructure.security;

import java.time.Instant;
import java.util.Set;

import org.crowdwatch.rtm.domain.models.UserRole;
import org.eclipse.microprofile.jwt.JsonWebToken;

import io.smallrye.jwt.auth.principal.JWTParser;
import io.smallrye.jwt.auth.principal.ParseException;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class JwtVerifier {
    @Inject
    JWTParser jwtParser;

    public boolean verify(
        String jwtToken,
        UserRole... allowedRoles
    ) throws ParseException {
        JsonWebToken jsonWebToken = jwtParser.parse(jwtToken);
        Set<UserRole> allowedRolesSet = Set.of(allowedRoles);
        Long jwtRolesSize = (long) jsonWebToken.getGroups().size();
        Long verifiedjwtRoles = jsonWebToken.getGroups()
            .stream()
            .map(jwtRole -> UserRole.createFrom(jwtRole))
            .filter(userRole -> allowedRolesSet.contains(userRole))
            .count();
        return jwtRolesSize.equals(verifiedjwtRoles);
    }
    
    public Uni<Boolean> verifyExpiration(String jwt) {
        JsonWebToken jsonWebToken;
        try {
            jsonWebToken = jwtParser.parse(jwt);
        } catch(ParseException parseException) {
            return Uni.createFrom().failure(parseException);
        }
        Long exp = jsonWebToken.getClaim("exp");
        Instant expirationTime = Instant.ofEpochSecond(exp);
        Instant now = Instant.now();
        return Uni.createFrom().item(now.isBefore(expirationTime));
    }
}
