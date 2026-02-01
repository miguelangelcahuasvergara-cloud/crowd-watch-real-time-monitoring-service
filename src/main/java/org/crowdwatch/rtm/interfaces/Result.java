package org.crowdwatch.rtm.interfaces;

import static java.util.Objects.isNull;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Response.Status;

@JsonInclude(Include.NON_EMPTY)
public record Result<T>(
    T data,
    String message,
    List<String> errors,
    Status httpStatus
) {
    private static final String DEFAULT_SUCCESS_MESSAGE = "La operacion se realizó correctamente";
    public boolean hasError() {
        return isNull(data) && httpStatus.getStatusCode() >= 400;
    }
    public <R> Result<R> map(Function<T, R> mapper) {
        R newData = null;
        if(!isNull(data)) {
            newData = mapper.apply(data);
        }
        return new Result<R>(newData, message, errors, httpStatus);
    }
    public static <T> Result<T> fromException(Throwable exception) {
        return new Result<T>(null, exception.getMessage(), Collections.emptyList(), Status.INTERNAL_SERVER_ERROR);
    }
    public static <T extends ConstraintViolationException> Result<?> failureFromValidationException(T exception) {
        var constraintViolations = exception.getConstraintViolations();
        List<String> errorMessages = constraintViolations.stream().map(constrainViolation -> constrainViolation.getMessage()).toList();
        return new Result<T>(null, exception.getMessage(), errorMessages, Status.BAD_REQUEST);
    }
    public static <T> Result<T> success(T data, String message, Status httpStatus) {
        boolean isASuccessStatus = httpStatus.equals(Status.OK) 
        || httpStatus.equals(Status.CREATED)
        || httpStatus.equals(Status.ACCEPTED);
        if(!isASuccessStatus) {
            throw new IllegalArgumentException("httpStatus %s is not valid for successful result".formatted(httpStatus.name()));
        }
        return new Result<T>(data, message, Collections.emptyList(), httpStatus);
    }
    public static <T> Result<T> success(T data, String message) {
        return new Result<T>(data, message, Collections.emptyList(), Status.OK);
    }
    public static <T> Result<T> success(T data) {
        return new Result<T>(data, DEFAULT_SUCCESS_MESSAGE, Collections.emptyList(), Status.OK);
    }
    public static <T> Result<T> success(T data, Status httpStatus) {
        return new Result<T>(data, DEFAULT_SUCCESS_MESSAGE, Collections.emptyList(), httpStatus);
    }
}
