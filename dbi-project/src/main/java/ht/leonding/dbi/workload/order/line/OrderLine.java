package ht.leonding.dbi.workload.order.line;

import ht.leonding.dbi.error.InvalidRequestException;
import ht.leonding.dbi.workload.order.Order;
import ht.leonding.dbi.workload.product.Product;
import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "order_line")
public class OrderLine {

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable=false)
    private int quantity;

    @Column(nullable=false, precision=10, scale=2)
    private BigDecimal unitPrice;

    @ManyToOne(optional=false, fetch=FetchType.LAZY)
    private Order order;

    @ManyToOne(optional=false, fetch=FetchType.LAZY)
    private Product product;

    public OrderLine() {
    }

    public OrderLine(Product product, int quantity) {
        if (quantity < 1) {
            throw new InvalidRequestException("Quantity must be at least 1");
        }
        this.product = product;
        this.quantity = quantity;
        this.unitPrice = product.getPrice();
    }

    public Long getId() {
        return id;
    }

    public int getQuantity() {
        return quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public Product getProduct() {
        return product;
    }

    public BigDecimal lineTotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}
