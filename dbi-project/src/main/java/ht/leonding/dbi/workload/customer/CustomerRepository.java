package ht.leonding.dbi.workload.customer;

public interface CustomerRepository {
    Customer findById(Long id);
    Customer findByEmail(String email);
    void add(Customer customer);
}