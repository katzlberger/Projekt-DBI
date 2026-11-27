package ht.leonding.dbi.workload.order;

import ht.leonding.dbi.error.InvalidRequestException;
import ht.leonding.dbi.workload.customer.Customer;
import ht.leonding.dbi.workload.customer.address.Address;
import ht.leonding.dbi.workload.order.line.OrderLine;
import ht.leonding.dbi.workload.payment.Payment;
import ht.leonding.dbi.workload.payment.PaymentStatus;
import jakarta.persistence.*;

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

    @Column(nullable=false)
    private LocalDate deliveryDate;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    private Address deliveryAddress;

    @OneToOne(cascade=CascadeType.ALL, orphanRemoval=true)
    @JoinColumn(name="payment_id")
    private Payment payment;

    @OneToMany(mappedBy="order", cascade=CascadeType.ALL, orphanRemoval=true)
    private List<OrderLine> lines = new ArrayList<>();

    public Order() {
    }

    public Order(Customer customer, FulfillmentType fulfillmentType, LocalDate deliveryDate, Address deliveryAddress) {
        this.customer = customer;
        this.fulfillmentType = fulfillmentType;
        this.deliveryDate = deliveryDate;
        this.deliveryAddress = deliveryAddress;
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

    public FulfillmentType getFulfillmentType() {
        return fulfillmentType;
    }

    public LocalDate getDeliveryDate() {
        return deliveryDate;
    }

    public Customer getCustomer() {
        return customer;
    }

    public Address getDeliveryAddress() {
        return deliveryAddress;
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

    public void markPaid() {
        requireStatus(OrderStatus.NEW);
        this.status = OrderStatus.PAID;
    }

    public void markReady() {
        requireStatus(OrderStatus.PAID);
        this.status = OrderStatus.READY;
    }

    public void complete() {
        requireStatus(OrderStatus.READY);
        this.status = OrderStatus.COMPLETED;
    }

    public void cancel() {
        if (status != OrderStatus.NEW) {
            throw new InvalidRequestException("Only new orders can be cancelled");
        }
        if (payment != null && payment.getStatus() != PaymentStatus.PENDING) {
            throw new InvalidRequestException("An order with a paid payment cannot be cancelled");
        }
        for (OrderLine line : lines) {
            line.getProduct().releaseStock(line.getQuantity());
        }
        this.status = OrderStatus.CANCELLED;
    }

    private void requireStatus(OrderStatus expected) {
        if (status != expected) {
            throw new InvalidRequestException("Order " + id + " is " + status + ", expected " + expected);
        }
    }
}
