package ht.leonding.dbi.api;

import ht.leonding.dbi.error.InvalidRequestException;
import ht.leonding.dbi.error.NotFoundException;
import ht.leonding.dbi.model.order.MarkPaidRequest;
import ht.leonding.dbi.workload.payment.PaymentService;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.Map;

@Path("/api/orders/{orderId}/pay")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class OrderPaymentResource {

    private final PaymentService service;

    public OrderPaymentResource(PaymentService service) {
        this.service = service;
    }

    @PUT
    public Response markPaid(@PathParam("orderId") Long orderId, @Valid MarkPaidRequest request) {
        try {
            return Response.ok(service.markPaid(orderId, request.amountGiven())).build();
        } catch (InvalidRequestException e) {
            return error(Response.Status.BAD_REQUEST, e);
        } catch (NotFoundException e) {
            return error(Response.Status.NOT_FOUND, e);
        }
    }

    private static Response error(Response.Status status, RuntimeException e) {
        return Response.status(status)
                .type(MediaType.APPLICATION_JSON)
                .entity(Map.of("message", e.getMessage()))
                .build();
    }
}
