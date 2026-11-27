package ht.leonding.dbi;

import ht.leonding.dbi.error.DuplicateException;
import ht.leonding.dbi.error.InvalidRequestException;
import ht.leonding.dbi.model.payment.PaymentDTO;
import ht.leonding.dbi.workload.customer.Customer;
import ht.leonding.dbi.workload.order.FulfillmentType;
import ht.leonding.dbi.workload.order.Order;
import ht.leonding.dbi.workload.order.OrderRepository;
import ht.leonding.dbi.workload.order.OrderStatus;
import ht.leonding.dbi.workload.order.line.OrderLine;
import ht.leonding.dbi.workload.payment.*;
import ht.leonding.dbi.workload.product.flower.Flower;
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
        assertEquals("4242", dto.last4Digits());
        assertNull(dto.changeReturned());
        assertEquals(OrderStatus.PAID, order.getStatus());

        ArgumentCaptor<Payment> captor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository).add(captor.capture());
        CardPayment card = assertInstanceOf(CardPayment.class, captor.getValue());
        assertEquals("Anna", card.getCardHolder());
        assertEquals("4242", card.getLast4Digits());
        assertFalse(card.getLast4Digits().length() > 4);
    }

    @Test
    void cashPaymentStartsPending() {
        Order order = pickupOrder();
        when(orderRepository.findById(2L)).thenReturn(order);

        PaymentDTO dto = paymentService.createPayment(2L, PaymentMethod.CASH, null, null);

        assertEquals("CASH", dto.method());
        assertEquals("PENDING", dto.status());
        assertNull(dto.paidAt());
        assertNull(dto.last4Digits());
        assertNull(dto.changeReturned());
        assertEquals(OrderStatus.NEW, order.getStatus());
    }

    @Test
    void cashIsNotAllowedForDelivery() {
        Order order = order(FulfillmentType.DELIVERY);
        when(orderRepository.findById(3L)).thenReturn(order);

        assertThrows(InvalidRequestException.class,
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
        assertEquals(0, new BigDecimal("7.50").compareTo(dto.changeReturned()));
        assertNull(dto.last4Digits());
        assertEquals(OrderStatus.PAID, order.getStatus());
    }

    @Test
    void insufficientCashIsRejected() {
        Order order = pickupOrder();
        CashPayment cash = new CashPayment(AMOUNT);
        order.setPayment(cash);
        when(orderRepository.findById(7L)).thenReturn(order);

        assertThrows(InvalidRequestException.class,
                () -> paymentService.markPaid(7L, new BigDecimal("10.00")));
        assertEquals(PaymentStatus.PENDING, cash.getStatus());
        assertEquals(OrderStatus.NEW, order.getStatus());
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

        InvalidRequestException ex = assertThrows(InvalidRequestException.class,
                () -> paymentService.createPayment(5L, PaymentMethod.CARD, "Anna", "42"));
        assertFalse(ex.getMessage().contains("42"));
        verify(paymentRepository, never()).add(any());
    }

    @Test
    void blankCardHolderIsRejected() {
        Order order = pickupOrder();
        when(orderRepository.findById(5L)).thenReturn(order);

        assertThrows(InvalidRequestException.class,
                () -> paymentService.createPayment(5L, PaymentMethod.CARD, "  ", CARD_NUMBER));
        verify(paymentRepository, never()).add(any());
    }

    @Test
    void paidOrderIsNotOpenForPayment() {
        Order order = pickupOrder();
        order.markPaid();
        when(orderRepository.findById(6L)).thenReturn(order);

        InvalidRequestException ex = assertThrows(InvalidRequestException.class,
                () -> paymentService.createPayment(6L, PaymentMethod.CASH, null, null));
        assertEquals("Order 6 is not open for payment", ex.getMessage());
        verify(paymentRepository, never()).add(any());
    }

    @Test
    void paidOrderCannotBeBookedAgain() {
        Order order = pickupOrder();
        order.setPayment(new CashPayment(AMOUNT));
        order.markPaid();
        when(orderRepository.findById(9L)).thenReturn(order);

        InvalidRequestException ex = assertThrows(InvalidRequestException.class,
                () -> paymentService.markPaid(9L, AMOUNT));
        assertEquals("Order 9 is not open for payment", ex.getMessage());
    }

    @Test
    void cardPaymentCannotBeBookedAgain() {
        Order order = pickupOrder();
        order.setPayment(new CardPayment(AMOUNT, "Anna", "4242"));
        when(orderRepository.findById(8L)).thenReturn(order);

        assertThrows(InvalidRequestException.class,
                () -> paymentService.markPaid(8L, AMOUNT));
    }

    @Test
    void cancelIsAllowedWhileOrderIsNewAndPaymentIsPending() {
        Order order = pickupOrder();
        order.setPayment(new CashPayment(AMOUNT));
        Flower flower = (Flower) order.getLines().get(0).getProduct();

        order.cancel();

        assertEquals(OrderStatus.CANCELLED, order.getStatus());
        assertEquals(11, flower.getStock());
    }

    @Test
    void cancelIsRejectedWhenPaymentIsPaid() {
        Order order = pickupOrder();
        order.setPayment(new CardPayment(AMOUNT, "Anna", "4242"));

        assertThrows(InvalidRequestException.class, order::cancel);
        assertEquals(OrderStatus.NEW, order.getStatus());
    }

    @Test
    void orderStatusTransitionsFollowTheExpectedSequence() {
        Order order = pickupOrder();
        assertThrows(InvalidRequestException.class, order::markReady);
        assertThrows(InvalidRequestException.class, order::complete);

        order.markPaid();
        assertEquals(OrderStatus.PAID, order.getStatus());
        assertThrows(InvalidRequestException.class, order::markPaid);
        assertThrows(InvalidRequestException.class, order::complete);

        order.markReady();
        assertEquals(OrderStatus.READY, order.getStatus());
        assertThrows(InvalidRequestException.class, order::cancel);

        order.complete();
        assertEquals(OrderStatus.COMPLETED, order.getStatus());
    }

    @Test
    void orderLineRejectsQuantityBelowOne() {
        Flower flower = new Flower();
        assertThrows(InvalidRequestException.class, () -> new OrderLine(flower, 0));
    }

    @Test
    void stockBookingIsPolymorphic() {
        Flower flower = new Flower();
        flower.setStock(5);
        assertEquals("FLOWER", flower.getProductType());
        flower.reserveStock(2);
        assertEquals(3, flower.getStock());
        flower.releaseStock(2);
        assertEquals(5, flower.getStock());
    }

    private static Order pickupOrder() {
        return order(FulfillmentType.PICKUP);
    }

    private static Order order(FulfillmentType fulfillmentType) {
        Flower flower = new Flower();
        flower.setPrice(AMOUNT);
        flower.setStock(10);
        Customer customer = new Customer("Anna", "Muster", "anna@example.com");
        Order order = new Order(customer, fulfillmentType, LocalDate.now(), null);
        order.addLine(new OrderLine(flower, 1));
        return order;
    }
}
