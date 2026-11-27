package ht.leonding.dbi.workload.payment;

import java.util.List;

public interface PaymentRepository {
    List<Payment> findAll();
    Payment findById(Long id);
    Payment findByOrderId(Long orderId);
    boolean existsByOrderId(Long orderId);
    void add(Payment payment);
    void delete(Payment payment);
}
