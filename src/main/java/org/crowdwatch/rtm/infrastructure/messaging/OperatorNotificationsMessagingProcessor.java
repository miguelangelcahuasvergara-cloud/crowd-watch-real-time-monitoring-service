package org.crowdwatch.rtm.infrastructure.messaging;

import java.util.concurrent.CompletableFuture;

import org.crowdwatch.rtm.infrastructure.utils.JsonService;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusSenderAsyncClient;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class OperatorNotificationsMessagingProcessor {
    private final ServiceBusSenderAsyncClient senderAsyncClient;
    private final JsonService jsonService;
    public OperatorNotificationsMessagingProcessor(
        @ConfigProperty(name = "azure.servicebus.connection-string") String connectionString,
        @ConfigProperty(name = "azure.servicebus.operator-notifications.queue-name") String queueName,
        JsonService jsonService
    ) {
        this.senderAsyncClient = new ServiceBusClientBuilder()
            .connectionString(connectionString)
            .sender()
            .queueName(queueName)
            .buildAsyncClient();
        this.jsonService = jsonService;
    }
    public CompletableFuture<Void> sendMessage(String content) {
        ServiceBusMessage message = new ServiceBusMessage(content);
        return senderAsyncClient.sendMessage(message).toFuture();
    }
    public Uni<Void> sendMessage(Object content) {
        return jsonService.serializeToJsonAsync(content)
                .map(json -> new ServiceBusMessage(json))
                .chain(message -> Uni.createFrom().item(senderAsyncClient.sendMessage(message).toFuture()))
                .replaceWithVoid();
    }
}
