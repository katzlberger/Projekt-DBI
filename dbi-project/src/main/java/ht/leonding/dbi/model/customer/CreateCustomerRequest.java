package ht.leonding.dbi.model.customer;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateCustomerRequest(
        @NotBlank @Size(max = 100) String lastName,
        @Size(max = 100) String firstName,
        @NotBlank @Email @Size(max = 120) String email,
        @NotBlank @Size(max = 120) String street,
        @NotBlank @Size(max = 80) String city,
        @NotBlank @Size(max = 10) String zip
) {
}
