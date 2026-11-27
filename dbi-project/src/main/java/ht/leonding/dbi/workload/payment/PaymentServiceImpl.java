package ht.leonding.dbi.workload.payment;

import ht.leonding.dbi.error.DuplicateException;
import ht.leonding.dbi.error.NotFoundException;
import ht.leonding.dbi.model.payment.PaymentDTO;
import ht.leonding.dbi.workload.order.FulfillmentType;
import ht.leonding.dbi.workload.order.Order;
import ht.leonding.dbi.workload.order.OrderRepository;
import ht.leonding.dbi.workload.order.OrderStatus;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;

import java.math.BigDecimal;

@ApplicationScoped
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;
    private final OrderRepository orderRepository;

    public PaymentServiceImpl(PaymentRepository paymentRepository, PaymentMapper paymentMapper,
                              OrderRepository orderRepository) {
        this.paymentRepository = paymentRepository;
        this.paymentMapper = paymentMapper;
        this.orderRepository = orderRepository;
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
    public PaymentDTO createPayment(Long orderId, PaymentMethod method, String cardHolder, String cardNumber) {
        if (orderId == null) {
            throw new BadRequestException("Order id must not be null");
        }
        if (method == null) {
            throw new BadRequestException("Payment method must not be null");
        }

        Order order = orderRepository.findById(orderId);
        if (order == null) {
            throw new NotFoundException("Order " + orderId + " not found");
        }
        if (order.getPayment() != null) {
            throw new DuplicateException("Order " + orderId + " already has a payment");
        }

        BigDecimal amount = order.getTotalPrice();
        Payment payment;
        switch (method) {
            case CARD -> {
                if (cardNumber == null || !cardNumber.matches("\\d{16}")) {
                    throw new BadRequestException("Card number must have exactly 16 digits");
                }
                payment = new CardPayment(amount, cardHolder, cardNumber.substring(12));
                order.setStatus(OrderStatus.PAID);
            }
            case CASH -> {
                if (order.getFulfillmentType() != FulfillmentType.PICKUP) {
                    throw new BadRequestException("Cash payment is only allowed for pickup orders");
                }
                payment = new CashPayment(amount);
            }
            default -> throw new BadRequestException("Unsupported payment method " + method);
        }

        order.setPayment(payment);
        paymentRepository.add(payment);
        return paymentMapper.toDto(payment);
    }

    @Override
    @Transactional
    public PaymentDTO markPaid(Long orderId, BigDecimal amountGiven) {
        Order order = orderRepository.findById(orderId);
        if (order == null || order.getPayment() == null) {
            throw new NotFoundException("No payment for order " + orderId);
        }

        Payment payment = order.getPayment();
        if (payment.getStatus() == PaymentStatus.PAID) {
            throw new BadRequestException("Payment for order " + orderId + " is already paid");
        }
        if (!(payment instanceof CashPayment cash)) {
            throw new BadRequestException("Only cash payments can be booked here");
        }

        cash.markPaid(amountGiven);
        order.setStatus(OrderStatus.PAID);
        return paymentMapper.toDto(payment);
    }
}
