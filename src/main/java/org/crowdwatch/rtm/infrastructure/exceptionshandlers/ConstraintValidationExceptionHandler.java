package org.crowdwatch.rtm.infrastructure.exceptionshandlers;

import org.crowdwatch.rtm.interfaces.Result;

import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import lombok.extern.slf4j.Slf4j;

@Provider
@Slf4j
public class ConstraintValidationExceptionHandler implements ExceptionMapper<ConstraintViolationException> {

    @Override
    public Response toResponse(ConstraintViolationException exception) {
        log.error("An constraint validation exception occured", exception);
        Result<?> result = Result.failureFromValidationException(exception);
        return Response.status(result.httpStatus()).entity(result).build();
    }
    
}
