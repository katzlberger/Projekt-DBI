package ht.leonding.dbi.api;

import ht.leonding.dbi.error.DomainException;
import ht.leonding.dbi.error.DuplicateException;
import ht.leonding.dbi.error.InvalidRequestException;
import ht.leonding.dbi.error.NotFoundException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.util.Map;

@Provider
public class DomainExceptionMapper implements ExceptionMapper<DomainException> {

    @Override
    public Response toResponse(DomainException exception) {
        Response.Status status;
        if (exception instanceof NotFoundException) {
            status = Response.Status.NOT_FOUND;
        } else if (exception instanceof InvalidRequestException) {
            status = Response.Status.BAD_REQUEST;
        } else if (exception instanceof DuplicateException) {
            status = Response.Status.CONFLICT;
        } else {
            status = Response.Status.INTERNAL_SERVER_ERROR;
        }

        return Response.status(status)
                .type(MediaType.APPLICATION_JSON)
                .entity(Map.of("message", exception.getMessage()))
                .build();
    }
}
