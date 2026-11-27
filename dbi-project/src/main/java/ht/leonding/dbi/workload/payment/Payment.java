package ht.leonding.dbi.workload.payment;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "dtype")
public abstract class Payment {

    @Id
    @GeneratedValue
    private Long id;

    // Until the Order entity exists this keeps the link to the order.
    @Column(nullable=false, unique=true)
    private Long orderId;

    @Column(nullable=false, precision=10, scale=2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false)
    private PaymentStatus status;

    private LocalDateTime paidAt;

    protected Payment() {
    }

    protected Payment(Long orderId, BigDecimal amount) {
        this.orderId = orderId;
        this.amount = amount;
        this.status = PaymentStatus.PENDING;
    }

    public Long getId() {
        return id;
    }

    public Long getOrderId() {
        return orderId;
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

    public abstract PaymentMethod getMethod();

    public void markPaid() {
        this.status = PaymentStatus.PAID;
        this.paidAt = LocalDateTime.now();
    }
}
