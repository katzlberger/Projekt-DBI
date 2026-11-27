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
        Response.Status status = switch (exception) {
            case NotFoundException ignored -> Response.Status.NOT_FOUND;
            case InvalidRequestException ignored -> Response.Status.BAD_REQUEST;
            case DuplicateException ignored -> Response.Status.CONFLICT;
        };

        return Response.status(status)
                .type(MediaType.APPLICATION_JSON)
                .entity(Map.of("message", exception.getMessage()))
                .build();
    }
}
