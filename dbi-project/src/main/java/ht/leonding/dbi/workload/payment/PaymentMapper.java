package ht.leonding.dbi.workload.payment;

import ht.leonding.dbi.model.payment.PaymentDTO;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class PaymentMapper {

    public PaymentDTO toDto(Payment payment) {
        String last4Digits = payment instanceof CardPayment card ? card.getLast4Digits() : null;

        return new PaymentDTO(
                payment.getId(),
                payment.getMethod().name(),
                payment.getAmount(),
                payment.getStatus().name(),
                payment.getPaidAt(),
                last4Digits
        );
    }
}
