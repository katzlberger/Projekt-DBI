package ht.leonding.dbi.workload.payment;

import ht.leonding.dbi.model.payment.PaymentDTO;
import jakarta.enterprise.context.ApplicationScoped;

import java.math.BigDecimal;

@ApplicationScoped
public class PaymentMapper {

    public PaymentDTO toDto(Payment payment) {
        return new PaymentDTO(
                payment.getId(),
                payment.getMethod().name(),
                payment.getAmount(),
                payment.getStatus().name(),
                payment.getPaidAt(),
                payment.getLast4Digits()
        );
    }

    public Payment toEntity(Long orderId, PaymentMethod method, BigDecimal amount, String last4Digits) {
        return new Payment(orderId, method, amount, last4Digits);
    }
}
