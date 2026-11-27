package ht.leonding.dbi.workload.payment;

public interface PaymentRepository {
    Payment findById(Long id);
    Payment findByOrderId(Long orderId);
    void add(Payment payment);
}
