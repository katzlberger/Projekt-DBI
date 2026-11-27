package ht.leonding.dbi.workload.customer;

import ht.leonding.dbi.model.customer.AddressDTO;
import ht.leonding.dbi.model.customer.CustomerDTO;
import ht.leonding.dbi.workload.customer.address.Address;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class CustomerMapper {

    public CustomerDTO toDto(Customer customer) {
        return new CustomerDTO(
                customer.getId(),
                customer.getFirstName(),
                customer.getLastName(),
                customer.getEmail(),
                customer.getAddresses().stream()
                        .map(this::toDto)
                        .toList()
        );
    }

    public AddressDTO toDto(Address address) {
        return new AddressDTO(
                address.getId(),
                address.getStreet(),
                address.getCity(),
                address.getZip()
        );
    }

}