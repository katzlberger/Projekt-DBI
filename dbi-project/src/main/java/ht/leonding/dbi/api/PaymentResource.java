package ht.leonding.dbi.api;

import ht.leonding.dbi.error.NotFoundException;
import ht.leonding.dbi.model.payment.PaymentDTO;
import ht.leonding.dbi.workload.payment.PaymentService;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.Map;

@Path("/api/payments")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PaymentResource {

    private final PaymentService service;

    public PaymentResource(PaymentService service) {
        this.service = service;
    }

    @GET
    @Path("/{id}")
    public PaymentDTO findById(@PathParam("id") Long id) {
        try {
            return service.findById(id);
        } catch (NotFoundException e) {
            throw notFound(e);
        }
    }

    @GET
    public PaymentDTO findByOrderId(@QueryParam("orderId") @NotNull Long orderId) {
        try {
            return service.findByOrderId(orderId);
        } catch (NotFoundException e) {
            throw notFound(e);
        }
    }

    private static WebApplicationException notFound(NotFoundException e) {
        return new WebApplicationException(Response.status(Response.Status.NOT_FOUND)
                .type(MediaType.APPLICATION_JSON)
                .entity(Map.of("message", e.getMessage()))
                .build());
    }
}
