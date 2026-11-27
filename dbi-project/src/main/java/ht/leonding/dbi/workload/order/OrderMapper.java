package ht.leonding.dbi.workload.order;

import ht.leonding.dbi.model.order.OrderDTO;
import ht.leonding.dbi.model.order.OrderLineDTO;
import ht.leonding.dbi.workload.payment.PaymentMapper;
import ht.leonding.dbi.workload.customer.CustomerMapper;
import ht.leonding.dbi.workload.order.line.OrderLine;
import jakarta.enterprise.context.ApplicationScoped;

import java.math.BigDecimal;
import java.util.List;

@ApplicationScoped
public class OrderMapper {
    private final PaymentMapper paymentMapper;
    private final CustomerMapper customerMapper;

    public OrderMapper(PaymentMapper paymentMapper, CustomerMapper customerMapper) {
        this.paymentMapper = paymentMapper;
        this.customerMapper = customerMapper;
    }
    public OrderDTO toDto(Order order) {
        List<OrderLineDTO> lines = order.getLines().stream().map(this::toLineDTO).toList();
        return new OrderDTO(
                order.getId(),
                order.getOrderDate(),
                order.getStatus() == null ? null : order.getStatus().name(),
                order.getFulfillmentType() == null ? null : order.getFulfillmentType().name(),
                order.getDeliveryDate(),
                order.getCustomer() == null ? null : order.getCustomer().getId(),
                order.getDeliveryAddress() == null ? null : customerMapper.toDto(order.getDeliveryAddress()),
                lines,
                paymentMapper.toDto(order.getPayment()),
                order.getTotalPrice());
    }

    public OrderLineDTO toLineDTO(OrderLine line) {
        return new OrderLineDTO(
                line.getId(),
                line.getProduct() == null ? null : line.getProduct().getId(),
                line.getProduct() == null ? null : line.getProduct().getName(),
                line.getProduct() == null ? null : line.getProduct().getProductType(),
                line.getQuantity(),
                line.getUnitPrice(),
                line.getUnitPrice().multiply(BigDecimal.valueOf(line.getQuantity())));
    }
}
