package ht.leonding.dbi.workload.payment;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.ws.rs.BadRequestException;

import java.math.BigDecimal;

@Entity
@DiscriminatorValue("CASH")
public class CashPayment extends Payment {

    @Column(precision=10, scale=2)
    private BigDecimal amountGiven;

    @Column(precision=10, scale=2)
    private BigDecimal changeReturned;

    public CashPayment() {
    }

    public CashPayment(BigDecimal amount) {
        super(amount);
    }

    @Override
    public PaymentMethod getMethod() {
        return PaymentMethod.CASH;
    }

    public BigDecimal getAmountGiven() {
        return amountGiven;
    }

    public BigDecimal getChangeReturned() {
        return changeReturned;
    }

    public void markPaid(BigDecimal amountGiven) {
        if (amountGiven == null || amountGiven.compareTo(getAmount()) < 0) {
            throw new BadRequestException("Insufficient cash amount for payment " + getId());
        }
        this.amountGiven = amountGiven;
        this.changeReturned = amountGiven.subtract(getAmount());
        markPaid();
    }
}
