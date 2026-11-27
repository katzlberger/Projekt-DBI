package ht.leonding.dbi.api;

import ht.leonding.dbi.model.order.CreateOrderRequest;
import ht.leonding.dbi.model.order.OrderDTO;
import ht.leonding.dbi.workload.order.OrderService;
import ht.leonding.dbi.workload.order.OrderStatus;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import java.time.LocalDate;

@Path("/api/orders")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class OrderResource {

    @Inject
    OrderService service;

    @Context
    UriInfo uriInfo;

    @POST
    public Response createOrder(@Valid CreateOrderRequest request) {
        OrderDTO dto = service.createOrder(request);
        return Response.created(uriInfo.getBaseUriBuilder().path("api/orders/{id}").build(dto.id()))
                .entity(dto).build();
    }

    @GET
    @Path("/{orderId}")
    public Response getOrder(@PathParam("orderId") Long orderId) {
        return Response.ok(service.getOrder(orderId)).build();
    }

    @GET
    public Response searchOrders(@QueryParam("customerId") Long customerId,
                                 @QueryParam("status") String status,
                                 @QueryParam("from") LocalDate from,
                                 @QueryParam("to") LocalDate to) {
        OrderStatus parsed = null;
        if (status != null && !status.isBlank()) {
            try {
                parsed = OrderStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new BadRequestException("Unbekannter Status: " + status);
            }
        }
        return Response.ok(service.searchOrders(customerId, parsed, from, to)).build();
    }
}
