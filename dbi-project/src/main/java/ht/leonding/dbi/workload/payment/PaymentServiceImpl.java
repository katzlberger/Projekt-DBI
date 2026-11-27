package ht.leonding.dbi.workload.payment;

import ht.leonding.dbi.error.DuplicateException;
import ht.leonding.dbi.error.InvalidRequestException;
import ht.leonding.dbi.error.NotFoundException;
import ht.leonding.dbi.model.payment.PaymentDTO;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.math.BigDecimal;

@ApplicationScoped
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;

    public PaymentServiceImpl(PaymentRepository paymentRepository, PaymentMapper paymentMapper) {
        this.paymentRepository = paymentRepository;
        this.paymentMapper = paymentMapper;
    }

    @Override
    @Transactional
    public PaymentDTO findById(Long id) {
        Payment payment = paymentRepository.findById(id);
        if (payment == null) {
            throw new NotFoundException("Payment " + id + " not found");
        }
        return paymentMapper.toDto(payment);
    }

    @Override
    @Transactional
    public PaymentDTO findByOrderId(Long orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId);
        if (payment == null) {
            throw new NotFoundException("No payment for order " + orderId);
        }
        return paymentMapper.toDto(payment);
    }

    @Override
    @Transactional
    public PaymentDTO createPayment(Long orderId, PaymentMethod method, BigDecimal amount, String cardHolder, String last4Digits) {
        if (orderId == null) {
            throw new InvalidRequestException("Order id must not be null");
        }
        if (method == null) {
            throw new InvalidRequestException("Payment method must not be null");
        }
        if (amount == null || amount.signum() <= 0) {
            throw new InvalidRequestException("Payment amount must be positive");
        }
        if (paymentRepository.existsByOrderId(orderId)) {
            throw new DuplicateException("Payment for order " + orderId + " already exists");
        }

        Payment payment;
        switch (method) {
            case CARD -> {
                if (last4Digits == null || !last4Digits.matches("\\d{4}")) {
                    throw new InvalidRequestException("Card payment requires exactly the last 4 digits");
                }
                CardPayment cardPayment = new CardPayment(orderId, amount, cardHolder, last4Digits);
                cardPayment.markPaid();
                payment = cardPayment;
            }
            case CASH -> payment = new CashPayment(orderId, amount);
            default -> throw new InvalidRequestException("Unsupported payment method " + method);
        }

        paymentRepository.add(payment);
        return paymentMapper.toDto(payment);
    }

    @Override
    @Transactional
    public PaymentDTO markPaid(Long id, BigDecimal amountGiven) {
        Payment payment = paymentRepository.findById(id);
        if (payment == null) {
            throw new NotFoundException("Payment " + id + " not found");
        }
        if (payment.getStatus() == PaymentStatus.PAID) {
            throw new InvalidRequestException("Payment " + id + " is already paid");
        }
        if (!(payment instanceof CashPayment cash)) {
            throw new InvalidRequestException("Only cash payments can be booked here");
        }

        cash.markPaid(amountGiven);
        return paymentMapper.toDto(payment);
    }
}
