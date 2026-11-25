package ht.leonding.dbi.workload.customer;

import ht.leonding.dbi.model.customer.CustomerDTO;

public interface CustomerService {
    CustomerDTO findById(Long id);
    CustomerDTO findByEmail(String email);
}