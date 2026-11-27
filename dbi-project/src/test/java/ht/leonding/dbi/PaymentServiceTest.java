package ht.leonding.dbi;

import ht.leonding.dbi.error.DuplicateException;
import ht.leonding.dbi.model.payment.PaymentDTO;
import ht.leonding.dbi.workload.order.FulfillmentType;
import ht.leonding.dbi.workload.order.Order;
import ht.leonding.dbi.workload.order.OrderRepository;
import ht.leonding.dbi.workload.order.OrderStatus;
import ht.leonding.dbi.workload.order.line.OrderLine;
import ht.leonding.dbi.workload.payment.*;
import ht.leonding.dbi.workload.product.flower.Flower;
import jakarta.ws.rs.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PaymentServiceTest {

    private static final BigDecimal AMOUNT = new BigDecimal("12.50");
    private static final String CARD_NUMBER = "1234567812344242";

    private PaymentRepository paymentRepository;
    private OrderRepository orderRepository;
    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        paymentRepository = mock(PaymentRepository.class);
        orderRepository = mock(OrderRepository.class);
        paymentService = new PaymentServiceImpl(paymentRepository, new PaymentMapper(), orderRepository);
    }

    @Test
    void cardPaymentIsPaidAtCheckout() {
        Order order = pickupOrder();
        when(orderRepository.findById(1L)).thenReturn(order);

        PaymentDTO dto = paymentService.createPayment(1L, PaymentMethod.CARD, "Anna", CARD_NUMBER);

        assertEquals("CARD", dto.method());
        assertEquals("PAID", dto.status());
        assertNotNull(dto.paidAt());
        assertEquals("4242", dto.Last4Digits());
        assertEquals(OrderStatus.PAID, order.getStatus());

        ArgumentCaptor<Payment> captor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository).add(captor.capture());
        CardPayment card = assertInstanceOf(CardPayment.class, captor.getValue());
        assertEquals("Anna", card.getCardHolder());
        assertEquals("4242", card.getLast4Digits());
    }

    @Test
    void cashPaymentStartsPending() {
        Order order = pickupOrder();
        when(orderRepository.findById(2L)).thenReturn(order);

        PaymentDTO dto = paymentService.createPayment(2L, PaymentMethod.CASH, null, null);

        assertEquals("CASH", dto.method());
        assertEquals("PENDING", dto.status());
        assertNull(dto.paidAt());
        assertNull(dto.Last4Digits());
    }

    @Test
    void cashIsNotAllowedForDelivery() {
        Order order = order(FulfillmentType.DELIVERY);
        when(orderRepository.findById(3L)).thenReturn(order);

        assertThrows(BadRequestException.class,
                () -> paymentService.createPayment(3L, PaymentMethod.CASH, null, null));
        verify(paymentRepository, never()).add(any());
    }

    @Test
    void bookingCashPaymentSetsChange() {
        Order order = pickupOrder();
        CashPayment cash = new CashPayment(AMOUNT);
        order.setPayment(cash);
        when(orderRepository.findById(7L)).thenReturn(order);

        PaymentDTO dto = paymentService.markPaid(7L, new BigDecimal("20.00"));

        assertEquals("PAID", dto.status());
        assertNotNull(dto.paidAt());
        assertEquals(new BigDecimal("20.00"), cash.getAmountGiven());
        assertEquals(new BigDecimal("7.50"), cash.getChangeReturned());
        assertEquals(OrderStatus.PAID, order.getStatus());
    }

    @Test
    void insufficientCashIsRejectedWithBadRequest() {
        Order order = pickupOrder();
        CashPayment cash = new CashPayment(AMOUNT);
        order.setPayment(cash);
        when(orderRepository.findById(7L)).thenReturn(order);

        assertThrows(BadRequestException.class,
                () -> paymentService.markPaid(7L, new BigDecimal("10.00")));
        assertEquals(PaymentStatus.PENDING, cash.getStatus());
        verify(paymentRepository, never()).add(any());
    }

    @Test
    void duplicatePaymentForOrderIsRejected() {
        Order order = pickupOrder();
        order.setPayment(new CashPayment(AMOUNT));
        when(orderRepository.findById(4L)).thenReturn(order);

        assertThrows(DuplicateException.class,
                () -> paymentService.createPayment(4L, PaymentMethod.CARD, "Anna", CARD_NUMBER));
        verify(paymentRepository, never()).add(any());
    }

    @Test
    void invalidCardNumberIsRejected() {
        Order order = pickupOrder();
        when(orderRepository.findById(5L)).thenReturn(order);

        assertThrows(BadRequestException.class,
                () -> paymentService.createPayment(5L, PaymentMethod.CARD, "Anna", "42"));
        verify(paymentRepository, never()).add(any());
    }

    @Test
    void cardPaymentCannotBeBookedAgain() {
        Order order = pickupOrder();
        order.setPayment(new CardPayment(AMOUNT, "Anna", "4242"));
        when(orderRepository.findById(8L)).thenReturn(order);

        assertThrows(BadRequestException.class,
                () -> paymentService.markPaid(8L, AMOUNT));
    }

    @Test
    void cancelIsAllowedWhileOrderIsNewAndPaymentIsPending() {
        Order order = pickupOrder();
        order.setPayment(new CashPayment(AMOUNT));

        order.cancel();

        assertEquals(OrderStatus.CANCELLED, order.getStatus());
    }

    @Test
    void cancelIsRejectedWhenPaymentIsPaid() {
        Order order = pickupOrder();
        order.setPayment(new CardPayment(AMOUNT, "Anna", "4242"));

        assertThrows(BadRequestException.class, order::cancel);
        assertEquals(OrderStatus.NEW, order.getStatus());
    }

    private static Order pickupOrder() {
        return order(FulfillmentType.PICKUP);
    }

    private static Order order(FulfillmentType fulfillmentType) {
        Flower flower = new Flower();
        flower.setPrice(AMOUNT);
        Order order = new Order(fulfillmentType, LocalDate.now());
        order.addLine(new OrderLine(flower, 1));
        return order;
    }
}
