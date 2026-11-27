package ht.leonding.dbi.api;

import ht.leonding.dbi.error.DomainException;
import ht.leonding.dbi.error.DuplicateException;
import ht.leonding.dbi.error.InvalidRequestException;
import ht.leonding.dbi.error.NotFoundException;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DomainExceptionMapperTest {

    private final DomainExceptionMapper mapper = new DomainExceptionMapper();

    @Test
    void notFoundMapsTo404() {
        assertStatus(new NotFoundException("missing"), 404);
    }

    @Test
    void invalidRequestMapsTo400() {
        assertStatus(new InvalidRequestException("invalid"), 400);
    }

    @Test
    void duplicateMapsTo409() {
        assertStatus(new DuplicateException("duplicate"), 409);
    }

    private void assertStatus(DomainException exception, int expectedStatus) {
        try (Response response = mapper.toResponse(exception)) {
            assertEquals(expectedStatus, response.getStatus());
            assertEquals(Map.of("message", exception.getMessage()), response.getEntity());
        }
    }
}
