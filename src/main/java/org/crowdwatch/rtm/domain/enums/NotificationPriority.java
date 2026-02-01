package org.crowdwatch.rtm.domain.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum NotificationPriority {
    LOW("Low"),
    MEDIUM("Medium"),
    HIGH("High");
    private final String name;
    private NotificationPriority(String name) {
        this.name = name;
    }
    @JsonValue
    public String getName() {
        return this.name;
    }
}
