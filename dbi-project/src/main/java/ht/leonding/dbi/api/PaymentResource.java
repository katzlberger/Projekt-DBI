package ht.leonding.dbi.api;

import ht.leonding.dbi.model.payment.PaymentDTO;
import ht.leonding.dbi.workload.payment.PaymentService;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

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
        return service.findById(id);
    }

    @GET
    public PaymentDTO findByOrderId(@QueryParam("orderId") @NotNull Long orderId) {
        return service.findByOrderId(orderId);
    }
}
