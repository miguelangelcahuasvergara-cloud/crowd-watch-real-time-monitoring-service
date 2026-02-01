package org.crowdwatch.rtm.infrastructure.clients;

import java.util.Collections;
import java.util.List;

import org.crowdwatch.rtm.domain.external.RealtimeMessagingClient;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import com.azure.core.http.rest.RequestOptions;
import com.azure.messaging.webpubsub.WebPubSubServiceAsyncClient;
import com.azure.messaging.webpubsub.WebPubSubServiceClientBuilder;
import com.azure.messaging.webpubsub.models.GetClientAccessTokenOptions;
import com.azure.messaging.webpubsub.models.WebPubSubClientAccessToken;
import com.azure.messaging.webpubsub.models.WebPubSubContentType;

import io.quarkus.runtime.Startup;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

@Startup
@ApplicationScoped
public class AzureRealtimeMessagingClient implements RealtimeMessagingClient {
    private final WebPubSubServiceAsyncClient client;
    public AzureRealtimeMessagingClient(
        @ConfigProperty(name = "azure.webpubsub.connection-string") String connectionString,
        @ConfigProperty(name = "azure.webpubsub.hub") String hub
    ) {
        this.client = new WebPubSubServiceClientBuilder()
            .connectionString(connectionString)
            .hub(hub)
            .buildAsyncClient();
    }

    @Override
    public Uni<String> removeExistingListener(String groupName, String userId) {
        return Uni.createFrom()
            .future(
                client.removeUserFromGroupWithResponse(groupName, userId, new RequestOptions()).toFuture()
            )
            .replaceWith("Listener has been removed");
    }

    @Override
    public Uni<Void> notifyBusState(String groupName, String message) {
        return Uni.createFrom()
            .future(
                client.sendToGroup(groupName, message, WebPubSubContentType.APPLICATION_JSON).toFuture()
            );
    }

    @Override
    public Uni<String> generateNewListenerAccessUrl(String groupName, String userId) {
        GetClientAccessTokenOptions options = new GetClientAccessTokenOptions()
            .setUserId(userId)
            .setGroups(Collections.singletonList(groupName))
            .setRoles(List.of(
                "webpubsub.sendToGroup",
                "webpubsub.joinLeaveGroup",
                "webpubsub.sendToConnection",
                "webpubsub.sendToUser",
                "webpubsub.addConnectionToGroup",
                "webpubsub.removeConnectionFromGroup",
                "webpubsub.joinLeaveUserGroup"
            ));
        return Uni.createFrom().future(client.getClientAccessToken(options).toFuture()).map(WebPubSubClientAccessToken::getUrl);
    }
}
