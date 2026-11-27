package ht.leonding.dbi.workload.payment;

import ht.leonding.dbi.model.payment.PaymentDTO;
import jakarta.enterprise.context.ApplicationScoped;

import java.math.BigDecimal;

@ApplicationScoped
public class PaymentMapper {

    public PaymentDTO toDto(Payment payment) {
        String last4Digits = null;
        BigDecimal changeReturned = null;
        if (payment instanceof CardPayment card) {
            last4Digits = card.getLast4Digits();
        } else if (payment instanceof CashPayment cash) {
            changeReturned = cash.getChangeReturned();
        }

        return new PaymentDTO(
                payment.getId(),
                payment.getMethod().name(),
                payment.getAmount(),
                payment.getStatus().name(),
                payment.getPaidAt(),
                last4Digits,
                changeReturned
        );
    }
}
