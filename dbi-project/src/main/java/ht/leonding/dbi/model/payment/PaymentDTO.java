package ht.leonding.dbi.model.payment;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentDTO(
        Long id,
        String method,
        BigDecimal amount,
        String status,
        LocalDateTime paidAt,
        String Last4Digits
) {
}
