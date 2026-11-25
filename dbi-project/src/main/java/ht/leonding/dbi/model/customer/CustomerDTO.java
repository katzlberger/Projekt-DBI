package ht.leonding.dbi.model.customer;

import java.util.List;

public record CustomerDTO (
        Long id,
        String firstName,
        String LastName,
        String email,
        List<AddressDTO> address
){
}
