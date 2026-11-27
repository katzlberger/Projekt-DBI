package ht.leonding.dbi.model.customer;

import java.util.List;

public record CustomerDTO(
        Long id,
        String firstName,
        String lastName,
        String email,
        List<AddressDTO> addresses
) {
}