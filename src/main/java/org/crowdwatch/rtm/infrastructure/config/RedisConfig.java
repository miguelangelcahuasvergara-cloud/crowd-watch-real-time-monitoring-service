package org.crowdwatch.rtm.infrastructure.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import io.vertx.core.net.JksOptions;
import io.vertx.core.net.NetClientOptions;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.redis.client.Redis;
import io.vertx.redis.client.RedisOptions;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import jakarta.inject.Qualifier;
import lombok.extern.slf4j.Slf4j;

@ApplicationScoped
@Slf4j
public class RedisConfig {
    @Inject
    Vertx vertx;

    @ConfigProperty(name = "quarkus.redis.hosts")
    String redisHosts;

    @ConfigProperty(name = "quarkus.redis.password")
    String redisPassword;

    @ConfigProperty(name = "quarkus.redis.tls.trust-store-jks.path", defaultValue = "META-INF/resources/truststore.jks")
    String truststorePath;

    @ConfigProperty(name = "quarkus.redis.tls.trust-store-jks.password")
    String truststorePassword;

    @ConfigProperty(name = "quarkus.redis.tls.key-store-file", defaultValue = "META-INF/resources/keystore.jks")
    String keystorePath;

    @ConfigProperty(name = "quarkus.redis.tls.key-store-password")
    String keystorePassword;

    @Produces
    @CustomRedisQualifier
    public Redis redisClient() {
        log.info("Initializing Redis connection");
        NetClientOptions netClientOptions = new NetClientOptions()
            .setSsl(true)
            .setTrustOptions(new JksOptions()
                .setPath(truststorePath)
                .setPassword(truststorePassword)
            )
            .setKeyCertOptions(new JksOptions()
                .setPath(keystorePath)
                .setPassword(keystorePassword)
            )
            .setHostnameVerificationAlgorithm("");
        RedisOptions options = new RedisOptions()
                .setConnectionString(redisHosts)
                .setPassword(redisPassword)
                .setNetClientOptions(netClientOptions);
        return Redis.createClient(vertx, options);
    }

    @Qualifier
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
    public static @interface CustomRedisQualifier {}
}
