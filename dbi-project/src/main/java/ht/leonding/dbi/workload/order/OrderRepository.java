package ht.leonding.dbi.workload.order;

import ht.leonding.dbi.workload.customer.Customer;
import ht.leonding.dbi.workload.customer.address.Address;
import ht.leonding.dbi.workload.order.OrderStatus;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderRepository {

    Order findById(Long id);

    void add(Order order);

    Order findByIdWithDetails(Long id);

    List<Order> search(Long customerId, OrderStatus status, LocalDateTime from, LocalDateTime toExclusive);

    Customer findCustomer(Long customerId);

    Address findAddress(Long addressId);
}
