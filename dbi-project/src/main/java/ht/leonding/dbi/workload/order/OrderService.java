package ht.leonding.dbi.workload.order;

import ht.leonding.dbi.model.order.CreateOrderRequest;
import ht.leonding.dbi.model.order.OrderDTO;
import ht.leonding.dbi.workload.order.OrderStatus;

import java.time.LocalDate;
import java.util.List;

public interface OrderService {

    OrderDTO createOrder(CreateOrderRequest request);

    OrderDTO getOrder(Long orderId);

    List<OrderDTO> searchOrders(Long customerId, OrderStatus status, LocalDate from, LocalDate to);
}
