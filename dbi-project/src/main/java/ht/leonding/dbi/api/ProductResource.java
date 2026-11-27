package ht.leonding.dbi.api;

import ht.leonding.dbi.model.product.SetActiveRequest;
import ht.leonding.dbi.model.product.UpdateProductRequest;
import ht.leonding.dbi.model.product.accessory.AccessoryDTO;
import ht.leonding.dbi.model.product.accessory.CreateAccessoryRequest;
import ht.leonding.dbi.model.product.bouquet.BouquetDTO;
import ht.leonding.dbi.model.product.bouquet.CreateBouquetRequest;
import ht.leonding.dbi.model.product.flower.CreateFlowerRequest;
import ht.leonding.dbi.model.product.flower.FlowerDTO;
import ht.leonding.dbi.workload.product.ProductService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import java.time.LocalDate;

@Path("/api/products")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ProductResource {

    @Inject
    ProductService service;

    @Context
    UriInfo uriInfo;

    @GET
    @Path("/")
    public Response listProducts(@QueryParam("type") String type,
                                 @QueryParam("active") Boolean active) {
        return Response.ok(service.listProducts(type, active)).build();
    }

    @GET
    @Path("/best-sellers")
    public Response bestSellers(@QueryParam("from") LocalDate from,
                                @QueryParam("to") LocalDate to,
                                @QueryParam("minQuantity") @DefaultValue("1") long minQuantity) {
        return Response.ok(service.findBestSellingProducts(from, to, minQuantity)).build();
    }

    @GET
    @Path("/{productId}")
    public Response getProduct(@PathParam("productId") Long productId) {
        return Response.ok(service.getProduct(productId)).build();
    }

    @PUT
    @Path("/{productId}")
    public Response updateProduct(@PathParam("productId") Long productId,
                                  @Valid UpdateProductRequest request) {
        return Response.ok(service.updateProduct(productId, request)).build();
    }

    @PATCH
    @Path("/{productId}/active")
    public Response setActive(@PathParam("productId") Long productId,
                              @Valid SetActiveRequest request) {
        return Response.ok(service.setProductActive(productId, request.active())).build();
    }

    @DELETE
    @Path("/{productId}")
    public Response deleteProduct(@PathParam("productId") Long productId) {
        service.deleteProduct(productId);
        return Response.noContent().build();
    }

    @GET
    @Path("/flowers")
    public Response listFlowers(@QueryParam("color") String color,
                                @QueryParam("availableOnly") @DefaultValue("false") boolean availableOnly) {
        return Response.ok(service.listAvailableFlowers(color, availableOnly)).build();
    }

    @POST
    @Path("/flowers")
    public Response createFlower(@Valid CreateFlowerRequest request) {
        FlowerDTO dto = service.createFlower(request);
        return created(dto.id(), dto);
    }

    @POST
    @Path("/accessories")
    public Response createAccessory(@Valid CreateAccessoryRequest request) {
        AccessoryDTO dto = service.createAccessory(request);
        return created(dto.id(), dto);
    }

    @GET
    @Path("/bouquets")
    public Response listBouquets() {
        return Response.ok(service.listBouquets()).build();
    }

    @POST
    @Path("/bouquets")
    public Response createBouquet(@Valid CreateBouquetRequest request) {
        BouquetDTO dto = service.createBouquet(request);
        return created(dto.id(), dto);
    }

    private Response created(Long id, Object body) {
        return Response.created(uriInfo.getBaseUriBuilder().path("api/products/{id}").build(id))
                .entity(body).build();
    }
}