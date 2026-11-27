package ht.leonding.dbi.workload.order;

import ht.leonding.dbi.workload.order.line.OrderLine;
import ht.leonding.dbi.workload.payment.Payment;
import ht.leonding.dbi.workload.payment.PaymentStatus;
import jakarta.persistence.*;
import jakarta.ws.rs.BadRequestException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable=false)
    private LocalDateTime orderDate = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(nullable=false)
    private OrderStatus status = OrderStatus.NEW;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false)
    private FulfillmentType fulfillmentType;

    private LocalDate deliveryDate;

    @OneToOne(cascade=CascadeType.ALL, orphanRemoval=true)
    @JoinColumn(name="payment_id")
    private Payment payment;

    @OneToMany(mappedBy="order", cascade=CascadeType.ALL, orphanRemoval=true)
    private List<OrderLine> lines = new ArrayList<>();

    public Order() {
    }

    public Order(FulfillmentType fulfillmentType, LocalDate deliveryDate) {
        this.fulfillmentType = fulfillmentType;
        this.deliveryDate = deliveryDate;
    }

    public Long getId() {
        return id;
    }

    public LocalDateTime getOrderDate() {
        return orderDate;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public FulfillmentType getFulfillmentType() {
        return fulfillmentType;
    }

    public LocalDate getDeliveryDate() {
        return deliveryDate;
    }

    public Payment getPayment() {
        return payment;
    }

    public void setPayment(Payment payment) {
        this.payment = payment;
    }

    public List<OrderLine> getLines() {
        return lines;
    }

    public void addLine(OrderLine line) {
        lines.add(line);
        line.setOrder(this);
    }

    public BigDecimal getTotalPrice() {
        return lines.stream()
                .map(OrderLine::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void cancel() {
        if (status != OrderStatus.NEW) {
            throw new BadRequestException("Only new orders can be cancelled");
        }
        if (payment != null && payment.getStatus() != PaymentStatus.PENDING) {
            throw new BadRequestException("An order with a paid payment cannot be cancelled");
        }
        this.status = OrderStatus.CANCELLED;
    }
}
