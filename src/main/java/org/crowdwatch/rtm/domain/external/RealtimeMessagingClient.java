package org.crowdwatch.rtm.domain.external;

import io.smallrye.mutiny.Uni;

public interface RealtimeMessagingClient {
    Uni<String> removeExistingListener(String groupName, String userId);
    Uni<Void> notifyBusState(String groupName, String message);
    Uni<String> generateNewListenerAccessUrl(String groupName, String userId);
}
