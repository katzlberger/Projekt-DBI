package ht.leonding.dbi.workload.customer;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;

import java.util.List;

@ApplicationScoped
public class CustomerRepositoryImpl implements CustomerRepository{

    private final EntityManager em;

    public CustomerRepositoryImpl(EntityManager em) {
        this.em = em;
    }


    @Override
    public Customer findById(Long id) {
        return em.find(Customer.class, id);
    }

    @Override
    public Customer findByEmail(String email) {
        return em.createQuery("select c from Customer c where c.email = :email", Customer.class)
                .setParameter("email", email)
                .getResultStream()
                .findFirst()
                .orElse(null);
    }


    @Override
    public void add(Customer customer) {
        em.persist(customer);
    }

}
