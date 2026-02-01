package org.crowdwatch.rtm.domain.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import io.quarkus.runtime.annotations.RegisterForReflection;

import java.util.Arrays;

@RegisterForReflection
public enum BusEvent {
    PASSENGER_ENTRY("Passenger entry"),
    PASSENGER_EXIT("Passenger exit");
    private final String name;
    private BusEvent(String name) {
        this.name = name;
    }
    public String getName() {
        return this.name;
    }
    @Override
    public String toString() {
        return this.name;
    }

    @JsonValue
    public String jsonValue() {
        return name;
    }

    @JsonCreator
    public static BusEvent fromString(String value) {
        return Arrays.stream(values())
            .filter(e -> e.getName().equals(value))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException(
                "Value "+ value + " does not exists in enum BusEvent"
            ));
    }
}
