package org.crowdwatch.rtm.infrastructure.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;


@ApplicationScoped
@Slf4j
public class JsonService {
    private final ObjectMapper objectMapper;

    public JsonService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public Uni<String> serializeToJsonAsync(Object value) {
        return Uni.createFrom()
            .item(() -> {
                try {
                    return objectMapper.writeValueAsString(value);
                }catch(JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            });
    }

    public <T> Uni<T> deserializeFromJsonAsync(String json, Class<T> type) {
        return Uni.createFrom()
            .item(() -> {
                try {
                    return objectMapper.readValue(json, type);
                }catch(JsonProcessingException e) {
                    log.error("An exception when deserializing occured", e);
                    throw new RuntimeException(e);
                }
            });
    }

    public <T> T deserializeFromJson(String json, Class<T> type) {
        try {
            return objectMapper.readValue(json, type);
        }catch(JsonProcessingException e) {
            log.error("An exception when deserializing occured", e);
            throw new RuntimeException(e);
        }
    }
}
