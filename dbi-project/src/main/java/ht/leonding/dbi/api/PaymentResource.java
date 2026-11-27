package ht.leonding.dbi.api;

import ht.leonding.dbi.model.payment.CreatePaymentRequest;
import ht.leonding.dbi.model.payment.PaymentDTO;
import ht.leonding.dbi.workload.payment.PaymentMethod;
import ht.leonding.dbi.workload.payment.PaymentService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

@Path("/api/payments")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PaymentResource {

    @Inject
    PaymentService service;

    @Context
    UriInfo uriInfo;

    @GET
    @Path("/{id}")
    public Response findById(@PathParam("id") Long id) {
        return Response.ok(service.findById(id)).build();
    }

    @GET
    public Response findByOrderId(@QueryParam("orderId") Long orderId) {
        if (orderId == null) {
            throw new BadRequestException("orderId is required");
        }
        return Response.ok(service.findByOrderId(orderId)).build();
    }

    @POST
    public Response create(@Valid CreatePaymentRequest request) {
        PaymentDTO dto = service.createPayment(
                request.orderId(),
                parseMethod(request.method()),
                request.cardHolder(),
                request.cardNumber()
        );
        return Response.created(uriInfo.getBaseUriBuilder().path("api/payments/{id}").build(dto.id()))
                .entity(dto)
                .build();
    }

    private PaymentMethod parseMethod(String method) {
        try {
            return PaymentMethod.valueOf(method.trim().toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new BadRequestException("Unknown payment method: " + method);
        }
    }
}
