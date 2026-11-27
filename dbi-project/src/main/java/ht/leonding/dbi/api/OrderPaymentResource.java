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

@Path("/api/orders/{orderId}/pay")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class OrderPaymentResource {

    private final PaymentService service;

    public OrderPaymentResource(PaymentService service) {
        this.service = service;
    }

    @PUT
    public PaymentDTO markPaid(@PathParam("orderId") Long orderId, @Valid MarkPaidRequest request) {
        return service.markPaid(orderId, request.amountGiven());
    }
}