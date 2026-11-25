package ht.leonding.dbi.workload.customer;

import java.util.List;

public interface CustomerRepository {
    List<Customer> findAll();
    Customer findById(Long id);
    Customer findByEmail(String email);
    boolean existsByEmail(String email);
    void add (Customer customer);
    void delete (Customer customer);
}
