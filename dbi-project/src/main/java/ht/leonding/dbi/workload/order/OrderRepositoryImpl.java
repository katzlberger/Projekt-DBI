package ht.leonding.dbi.workload.order;

import ht.leonding.dbi.workload.customer.Customer;
import ht.leonding.dbi.workload.customer.address.Address;
import ht.leonding.dbi.workload.order.OrderStatus;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.time.LocalDateTime;
import java.util.List;

@ApplicationScoped
public class OrderRepositoryImpl implements OrderRepository {

    private final EntityManager em;

    public OrderRepositoryImpl(EntityManager em) {
        this.em = em;
    }

    @Override
    public Order findById(Long id) {
        return em.find(Order.class, id);
    }

    @Override
    public void add(Order order) {
        em.persist(order);
    }

    @Override
    public Order findByIdWithDetails(Long id) {
        return em.createQuery(
                        "select distinct o from Order o "
                                + "join fetch o.customer "
                                + "left join fetch o.deliveryAddress "
                                + "left join fetch o.payment "
                                + "left join fetch o.lines l "
                                + "left join fetch l.product "
                                + "where o.id = :id",
                        Order.class)
                .setParameter("id", id)
                .getResultStream()
                .findFirst()
                .orElse(null);
    }

    @Override
    public List<Order> search(Long customerId, OrderStatus status, LocalDateTime from, LocalDateTime toExclusive) {
        StringBuilder jpql = new StringBuilder(
                "select distinct o from Order o "
                        + "join fetch o.customer "
                        + "left join fetch o.lines l "
                        + "left join fetch l.product "
                        + "where 1 = 1");
        if (customerId != null) jpql.append(" and o.customer.id = :customerId");
        if (status != null) jpql.append(" and o.status = :status");
        if (from != null) jpql.append(" and o.orderDate >= :from");
        if (toExclusive != null) jpql.append(" and o.orderDate < :to");
        jpql.append(" order by o.orderDate desc");

        TypedQuery<Order> query = em.createQuery(jpql.toString(), Order.class);
        if (customerId != null) query.setParameter("customerId", customerId);
        if (status != null) query.setParameter("status", status);
        if (from != null) query.setParameter("from", from);
        if (toExclusive != null) query.setParameter("to", toExclusive);
        return query.getResultList();
    }

    @Override
    public Customer findCustomer(Long customerId) {
        return em.find(Customer.class, customerId);
    }

    @Override
    public Address findAddress(Long addressId) {
        return em.find(Address.class, addressId);
    }
}
