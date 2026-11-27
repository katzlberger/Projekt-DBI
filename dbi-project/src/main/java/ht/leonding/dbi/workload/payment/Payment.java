package ht.leonding.dbi.workload.payment;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
public class Payment {

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable=false, unique=true)
    private Long orderId;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false)
    private PaymentMethod method;

    @Column(nullable=false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false)
    private PaymentStatus status;

    private LocalDateTime paidAt;

    @Column(length=4)
    private String last4Digits;

    public Payment(Long orderId, PaymentMethod method, BigDecimal amount, String last4Digits) {
        this.orderId = orderId;
        this.method = method;
        this.amount = amount;
        this.last4Digits = last4Digits;
        this.status = PaymentStatus.PENDING;
    }

    public Payment() {
    }

    public Long getId() {
        return id;
    }

    public Long getOrderId() {
        return orderId;
    }

    public PaymentMethod getMethod() {
        return method;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public LocalDateTime getPaidAt() {
        return paidAt;
    }

    public String getLast4Digits() {
        return last4Digits;
    }

    public void setMethod(PaymentMethod method) {
        this.method = method;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public void setLast4Digits(String last4Digits) {
        this.last4Digits = last4Digits;
    }

    public void markPaid() {
        this.status = PaymentStatus.PAID;
        this.paidAt = LocalDateTime.now();
    }
}
