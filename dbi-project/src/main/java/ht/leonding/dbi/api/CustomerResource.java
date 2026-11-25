package ht.leonding.dbi.api;

import ht.leonding.dbi.model.customer.CustomerDTO;
import ht.leonding.dbi.workload.customer.CustomerService;
import jakarta.validation.constraints.NotBlank;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

@Path("/api/customers")
@Produces(MediaType.APPLICATION_JSON)
public class CustomerResource {

    private final CustomerService customerService;

    public CustomerResource(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GET
    @Path("/{id}")
    public CustomerDTO findById(@PathParam("id") Long id) {
        return customerService.findById(id);
    }

    @GET
    public CustomerDTO findByEmail(@QueryParam("email") @NotBlank String email) {
        return customerService.findByEmail(email);
    }
}