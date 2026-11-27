package ht.leonding.dbi.api;

import ht.leonding.dbi.model.order.MarkPaidRequest;
import ht.leonding.dbi.model.payment.PaymentDTO;
import ht.leonding.dbi.workload.payment.PaymentService;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

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
        PaymentDTO dto = service.markPaid(orderId, request.amountGiven());
        return Response.ok(dto).build();
    }
}
