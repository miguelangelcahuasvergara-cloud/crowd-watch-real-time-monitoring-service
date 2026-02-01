package org.crowdwatch.rtm.infrastructure.messaging;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.crowdwatch.rtm.domain.models.OccupancyUpdate;
import org.crowdwatch.rtm.infrastructure.utils.JsonService;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.EventHubClientBuilder;
import com.azure.messaging.eventhubs.EventHubConsumerAsyncClient;
import com.azure.messaging.eventhubs.EventProcessorClient;
import com.azure.messaging.eventhubs.EventProcessorClientBuilder;
import com.azure.messaging.eventhubs.checkpointstore.blob.BlobCheckpointStore;
import com.azure.messaging.eventhubs.models.EventPosition;
import com.azure.storage.blob.BlobContainerAsyncClient;
import com.azure.storage.blob.BlobContainerClientBuilder;

import io.quarkus.runtime.Startup;
import io.vertx.mutiny.core.eventbus.EventBus;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

@Startup
@ApplicationScoped
@Slf4j
public class IotPassengerDetectionReceiverService {
    @ConfigProperty(name = "azure.event-hub.available")
    Boolean isAvailable;

    @ConfigProperty(name = "azure.event-hub.connection-string")
    String eventHubConnectionString;

    @ConfigProperty(name = "azure.event-hub.name")
    String eventHubName;

    @ConfigProperty(name = "azure.event-hub.checkpointing.storage.connection-string")
    String blobStorageConnectionString;

    @ConfigProperty(name = "azure.event-hub.checkpointing.storage.container")
    String blobStorageContainerName;

    private EventProcessorClient eventProcessorClient;
    private final JsonService jsonService;
    private final EventBus eventBus;

    @Inject
    public IotPassengerDetectionReceiverService(
        JsonService jsonService,
        EventBus eventBus
    ) {
        this.jsonService = jsonService;
        this.eventBus = eventBus;
    }


    @PostConstruct
    void initializeEventHubCommunication() {
        if(!isAvailable){
            return;
        }
        BlobContainerAsyncClient blobContainerAsyncClient = new BlobContainerClientBuilder()
            .connectionString(blobStorageConnectionString)
            .containerName(blobStorageContainerName)
            .buildAsyncClient();
        EventHubConsumerAsyncClient consumerClient = new EventHubClientBuilder()
            .connectionString(eventHubConnectionString, eventHubName)
            .consumerGroup(EventHubClientBuilder.DEFAULT_CONSUMER_GROUP_NAME)
            .buildAsyncConsumerClient();
        
        List<String> partitionIds = consumerClient.getPartitionIds()
            .collectList()
            .block();
        
        Map<String, EventPosition> initialPositions = partitionIds.stream()
            .collect(Collectors.toMap(
                Function.identity(),
                id -> EventPosition.latest()
            ));

        EventProcessorClientBuilder eventProcessorClientBuilder = new EventProcessorClientBuilder()
            .connectionString(eventHubConnectionString, eventHubName)
            .consumerGroup(EventHubClientBuilder.DEFAULT_CONSUMER_GROUP_NAME)
            .initialPartitionEventPosition(initialPositions)
            .processEvent(eventContext -> {
                EventData eventData = eventContext.getEventData();
                String messageBodyInJson = eventData.getBodyAsString();
                eventContext.updateCheckpoint();
                log.info("Occupancy update message received: {}", messageBodyInJson);
                OccupancyUpdate occupancyUpdate = jsonService.deserializeFromJson(
                    messageBodyInJson, 
                    OccupancyUpdate.class
                );
                eventBus.send("process.occupancy-detection", occupancyUpdate);
            })
            .processError(error -> {
                log.error("Ocurrió un error al procesar un mensaje", error.getThrowable());
            })
            .checkpointStore(new BlobCheckpointStore(blobContainerAsyncClient));
        
        eventProcessorClient = eventProcessorClientBuilder.buildEventProcessorClient();
        eventProcessorClient.start();
    }

    @PreDestroy
    void endEventHubCommunication() {
        eventProcessorClient.stop();
    }
}
