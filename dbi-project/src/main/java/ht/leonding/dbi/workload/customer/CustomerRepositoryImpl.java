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
    public List<Customer> findAll() {
        return em.createQuery("select c from Customer c", Customer.class)
                .getResultList();
    }

    @Override
    public Customer findById(Long id) {
        return em.find(Customer.class, id);
    }

    @Override
    public Customer findByEmail(String email) {
        return em.createQuery("select c from Customer c where c.email == :email", Customer.class)
                .setParameter(email, email)
                .getSingleResult();
    }

    @Override
    public boolean existsByEmail(String email) {
        return em.createQuery("select COUNT(c) from Customer where c.email == :email", Long.class)
                .setParameter("email", email)
                .getSingleResult() > 0;
    }

    @Override
    public void add(Customer customer) {
        em.persist(customer);
    }

    @Override
    public void delete(Customer customer) {
        em.remove(customer);
    }
}
