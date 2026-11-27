package ht.leonding.dbi;

import ht.leonding.dbi.error.DuplicateException;
import ht.leonding.dbi.error.InvalidRequestException;
import ht.leonding.dbi.model.payment.PaymentDTO;
import ht.leonding.dbi.workload.payment.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class  PaymentServiceTest {

    private static final BigDecimal AMOUNT = new BigDecimal("12.50");

    private PaymentRepository paymentRepository;
    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        paymentRepository = mock(PaymentRepository.class);
        paymentService = new PaymentServiceImpl(paymentRepository, new PaymentMapper());
    }

    @Test
    void cardPaymentIsPaidAtCheckout() {
        when(paymentRepository.existsByOrderId(1L)).thenReturn(false);

        PaymentDTO dto = paymentService.createPayment(1L, PaymentMethod.CARD, AMOUNT, "Anna", "4242");

        assertEquals("CARD", dto.method());
        assertEquals("PAID", dto.status());
        assertNotNull(dto.paidAt());
        assertEquals("4242", dto.Last4Digits());

        ArgumentCaptor<Payment> captor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository).add(captor.capture());
        assertInstanceOf(CardPayment.class, captor.getValue());
    }

    @Test
    void cashPaymentStartsPending() {
        when(paymentRepository.existsByOrderId(2L)).thenReturn(false);

        PaymentDTO dto = paymentService.createPayment(2L, PaymentMethod.CASH, AMOUNT, null, null);

        assertEquals("CASH", dto.method());
        assertEquals("PENDING", dto.status());
        assertNull(dto.paidAt());
        assertNull(dto.Last4Digits());
    }

    @Test
    void bookingCashPaymentSetsChange() {
        CashPayment cash = new CashPayment(3L, AMOUNT);
        when(paymentRepository.findById(7L)).thenReturn(cash);

        PaymentDTO dto = paymentService.markPaid(7L, new BigDecimal("20.00"));

        assertEquals("PAID", dto.status());
        assertNotNull(dto.paidAt());
        assertEquals(new BigDecimal("20.00"), cash.getAmountGiven());
        assertEquals(new BigDecimal("7.50"), cash.getChangeReturned());
    }

    @Test
    void insufficientCashIsRejectedWithoutBooking() {
        CashPayment cash = new CashPayment(3L, AMOUNT);
        when(paymentRepository.findById(7L)).thenReturn(cash);

        assertThrows(InvalidRequestException.class,
                () -> paymentService.markPaid(7L, new BigDecimal("10.00")));
        assertEquals(PaymentStatus.PENDING, cash.getStatus());
        verify(paymentRepository, never()).add(any());
    }

    @Test
    void duplicatePaymentForOrderIsRejected() {
        when(paymentRepository.existsByOrderId(4L)).thenReturn(true);

        assertThrows(DuplicateException.class,
                () -> paymentService.createPayment(4L, PaymentMethod.CARD, AMOUNT, "Anna", "4242"));
        verify(paymentRepository, never()).add(any());
    }

    @Test
    void invalidCardDigitsAreRejected() {
        when(paymentRepository.existsByOrderId(5L)).thenReturn(false);

        assertThrows(InvalidRequestException.class,
                () -> paymentService.createPayment(5L, PaymentMethod.CARD, AMOUNT, "Anna", "42"));
        verify(paymentRepository, never()).add(any());
    }

    @Test
    void cardPaymentCannotBeBookedAgain() {
        CardPayment card = new CardPayment(6L, AMOUNT, "Anna", "4242");
        card.markPaid();
        when(paymentRepository.findById(8L)).thenReturn(card);

        assertThrows(InvalidRequestException.class,
                () -> paymentService.markPaid(8L, AMOUNT));
    }
}
