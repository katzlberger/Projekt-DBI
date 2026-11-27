package ht.leonding.dbi.workload.payment;

import ht.leonding.dbi.model.payment.PaymentDTO;

import java.math.BigDecimal;

public interface PaymentService {
    PaymentDTO findById(Long id);
    PaymentDTO findByOrderId(Long orderId);
    PaymentDTO createPayment(Long orderId, PaymentMethod method, String cardHolder, String cardNumber);
    PaymentDTO markPaid(Long orderId, BigDecimal amountGiven);
}
