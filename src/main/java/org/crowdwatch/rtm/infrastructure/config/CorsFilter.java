package org.crowdwatch.rtm.infrastructure.config;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;

@Provider
public class CorsFilter implements ContainerResponseFilter {
    @Override
    public void filter(
        ContainerRequestContext requestContext,
        ContainerResponseContext responseContext
    ) {
        
        String webhookRequestOrigin = requestContext.getHeaderString("WebHook-Request-Origin");
        if (webhookRequestOrigin != null && !webhookRequestOrigin.isEmpty()) {
            responseContext.getHeaders().add("WebHook-Allowed-Origin", webhookRequestOrigin);
        }
        responseContext.getHeaders().add("Access-Control-Allow-Origin", "*");
        responseContext.getHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, PATCH, DELETE, OPTIONS");
        responseContext.getHeaders().add("Access-Control-Allow-Headers", "Authorization, Content-Type");
        responseContext.getHeaders().add("Access-Control-Allow-Credentials", "true");

        // Esto asegura que las solicitudes OPTIONS (preflight) respondan correctamente
        if ("OPTIONS".equalsIgnoreCase(requestContext.getMethod())) {
            responseContext.setStatus(200);
        }
    }
}