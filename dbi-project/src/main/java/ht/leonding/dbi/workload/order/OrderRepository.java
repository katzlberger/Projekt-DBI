package ht.leonding.dbi.workload.order;

public interface OrderRepository {
    Order findById(Long id);
    void add(Order order);
}
