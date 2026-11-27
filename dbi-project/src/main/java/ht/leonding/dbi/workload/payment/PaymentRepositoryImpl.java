package ht.leonding.dbi.workload.payment;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;

import java.util.List;

@ApplicationScoped
public class PaymentRepositoryImpl implements PaymentRepository {

    private final EntityManager em;

    public PaymentRepositoryImpl(EntityManager em) {
        this.em = em;
    }

    @Override
    public List<Payment> findAll() {
        return em.createQuery("select p from Payment p", Payment.class)
                .getResultList();
    }

    @Override
    public Payment findById(Long id) {
        return em.find(Payment.class, id);
    }

    @Override
    public Payment findByOrderId(Long orderId) {
        return em.createQuery("select p from Payment p where p.orderId = :orderId", Payment.class)
                .setParameter("orderId", orderId)
                .getResultStream()
                .findFirst()
                .orElse(null);
    }

    @Override
    public boolean existsByOrderId(Long orderId) {
        return em.createQuery("select COUNT(p) from Payment p where p.orderId = :orderId", Long.class)
                .setParameter("orderId", orderId)
                .getSingleResult() > 0;
    }

    @Override
    public void add(Payment payment) {
        em.persist(payment);
    }

    @Override
    public void delete(Payment payment) {
        em.remove(payment);
    }
}
