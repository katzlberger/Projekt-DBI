package ht.leonding.dbi.workload.customer;

import ht.leonding.dbi.error.NotFoundException;
import ht.leonding.dbi.model.customer.CustomerDTO;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.util.Locale;

@ApplicationScoped
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;

    public CustomerServiceImpl(CustomerRepository customerRepository, CustomerMapper customerMapper) {
        this.customerRepository = customerRepository;
        this.customerMapper = customerMapper;
    }

    @Override
    @Transactional
    public CustomerDTO findById(Long id) {
        Customer customer = customerRepository.findById(id);
        if (customer == null) {
            throw new NotFoundException("Customer " + id + " not found");
        }
        return customerMapper.toDto(customer);
    }

    @Override
    @Transactional
    public CustomerDTO findByEmail(String email) {
        String normalized = email.trim().toLowerCase(Locale.ROOT);
        Customer customer = customerRepository.findByEmail(normalized);
        if (customer == null) {
            throw new NotFoundException("No customer with email " + normalized);
        }
        return customerMapper.toDto(customer);
    }
}