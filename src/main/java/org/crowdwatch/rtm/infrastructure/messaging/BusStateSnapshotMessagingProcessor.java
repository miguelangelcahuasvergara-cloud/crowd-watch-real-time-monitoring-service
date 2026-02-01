package org.crowdwatch.rtm.infrastructure.messaging;

import java.util.concurrent.CompletableFuture;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusSenderAsyncClient;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class BusStateSnapshotMessagingProcessor {
    private final ServiceBusSenderAsyncClient senderAsyncClient;

    public BusStateSnapshotMessagingProcessor(
        @ConfigProperty(name = "azure.servicebus.connection-string") String connectionString,
        @ConfigProperty(name = "azure.servicebus.bus-state-snapshot.queue-name") String queueName
    ) {
        this.senderAsyncClient = new ServiceBusClientBuilder()
            .connectionString(connectionString)
            .sender()
            .queueName(queueName)
            .buildAsyncClient();
    }

    public CompletableFuture<Void> sendMessage(String content) {
        ServiceBusMessage message = new ServiceBusMessage(content);
        return senderAsyncClient.sendMessage(message).toFuture();
    }
    
}
