package org.crowdwatch.rtm.infrastructure.exceptions;

import lombok.Getter;

@Getter
public class MappingException extends RuntimeException {
    private static final String DEFAULT_MESSAGE = "An error occurred while mapping";
    private final Class<?> sourceType;
    private final Object source;
    private final Class<?> targetType;

    public MappingException(Object source, Class<?> target) {
        super(DEFAULT_MESSAGE);
        this.sourceType = source.getClass();
        this.targetType = target.getClass();
        this.source = source;
    }
}
