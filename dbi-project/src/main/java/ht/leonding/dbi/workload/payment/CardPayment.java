package ht.leonding.dbi.workload.payment;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

import java.math.BigDecimal;

@Entity
@DiscriminatorValue("CARD")
public class CardPayment extends Payment {

    @Column(length=100)
    private String cardHolder;

    @Column(name="last4digits", length=4)
    private String last4Digits;

    public CardPayment() {
    }

    public CardPayment(Long orderId, BigDecimal amount, String cardHolder, String last4Digits) {
        super(orderId, amount);
        this.cardHolder = cardHolder;
        this.last4Digits = last4Digits;
    }

    @Override
    public PaymentMethod getMethod() {
        return PaymentMethod.CARD;
    }

    public String getCardHolder() {
        return cardHolder;
    }

    public String getLast4Digits() {
        return last4Digits;
    }
}
