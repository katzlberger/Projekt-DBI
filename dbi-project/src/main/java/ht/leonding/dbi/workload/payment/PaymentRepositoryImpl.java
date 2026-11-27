package ht.leonding.dbi.workload.payment;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;

@ApplicationScoped
public class PaymentRepositoryImpl implements PaymentRepository {

    private final EntityManager em;

    public PaymentRepositoryImpl(EntityManager em) {
        this.em = em;
    }

    @Override
    public Payment findById(Long id) {
        return em.find(Payment.class, id);
    }

    @Override
    public Payment findByOrderId(Long orderId) {
        return em.createQuery("select o.payment from Order o where o.id = :orderId", Payment.class)
                .setParameter("orderId", orderId)
                .getResultStream()
                .findFirst()
                .orElse(null);
    }

    @Override
    public void add(Payment payment) {
        em.persist(payment);
    }
}
