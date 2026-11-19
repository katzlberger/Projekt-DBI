package ht.leonding.dbi.model.customer;

public record CustomerDTO (
        Long id,
        String firstName,
        String LastName,
        String email,
        AddressDTO address
){
}
