package ht.leonding.dbi.workload.product;

import ht.leonding.dbi.model.product.BestSellerDTO;
import ht.leonding.dbi.workload.order.OrderStatus;
import ht.leonding.dbi.workload.product.bouquet.Bouquet;
import ht.leonding.dbi.workload.product.flower.Flower;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@ApplicationScoped
public class ProductRepositoryImpl implements ProductRepository {
    private final EntityManager em;

    public ProductRepositoryImpl(EntityManager em) {
        this.em = em;
    }

    @Override
    public Product findById(Long id) {
        return em.find(Product.class, id);
    }

    @Override
    public void persist(Product product) {
        em.persist(product);
    }

    @Override
    public void remove(Product product) {
        em.remove(product);
    }

    @Override
    public void flush() {
        em.flush();
    }

    @Override
    public List<Product> findAll(Class<? extends Product> type, Boolean active) {
        StringBuilder jpql = new StringBuilder("select p from Product p where 1 = 1");
        if (type != null) jpql.append(" and type(p) = :type");
        if (active != null) jpql.append(" and p.active = :active");
        jpql.append(" order by p.name");
        TypedQuery<Product> q = em.createQuery(jpql.toString(), Product.class);
        if (type != null) q.setParameter("type", type);
        if (active != null) q.setParameter("active", active);
        return q.getResultList();
    }

    @Override
    public List<Flower> findFlowers(String color, boolean availableOnly) {
        StringBuilder jpql = new StringBuilder("select f from Flower f where f.active = true");
        if (color != null) jpql.append(" and lower(f.color) = lower(:color)");
        if (availableOnly) jpql.append(" and f.stock > 0");
        jpql.append(" order by f.name");
        TypedQuery<Flower> q = em.createQuery(jpql.toString(), Flower.class);
        if (color != null) q.setParameter("color", color);
        return q.getResultList();
    }

    @Override
    public List<Flower> findFlowersByIds(Collection<Long> ids) {
        return em.createQuery("select f from Flower f where f.id in :ids", Flower.class)
                .setParameter("ids", ids)
                .getResultList();
    }

    @Override
    public List<Bouquet> findAllBouquets() {
        return em.createQuery(
                "select distinct b from Bouquet b "
                        + "left join fetch b.items i left join fetch i.flower order by b.name",
                Bouquet.class).getResultList();
    }

    @Override
    public boolean isReferencedByOrderLine(Long productId) {
        Long count = em.createQuery(
                        "select count(ol) from OrderLine ol where ol.product.id = :id", Long.class)
                .setParameter("id", productId).getSingleResult();
        return count > 0;
    }

    @Override
    public boolean isFlowerUsedInBouquet(Long flowerId) {
        Long count = em.createQuery(
                        "select count(bi) from BouquetItem bi where bi.flower.id = :id", Long.class)
                .setParameter("id", flowerId).getSingleResult();
        return count > 0;
    }

    @Override
    public List<BestSellerDTO> findBestSellingProducts(LocalDateTime from, LocalDateTime toExclusive,
                                                       long minimumQuantity) {
        return em.createQuery(
                        "select new ht.leonding.dbi.model.product.BestSellerDTO(p.id, p.name, sum(ol.quantity)) "
                                + "from OrderLine ol join ol.product p join ol.order o "
                                + "where o.orderDate >= :from and o.orderDate < :to and o.status <> :cancelled "
                                + "group by p.id, p.name "
                                + "having sum(ol.quantity) >= :min "
                                + "order by sum(ol.quantity) desc, p.name",
                        BestSellerDTO.class)
                .setParameter("from", from)
                .setParameter("to", toExclusive)
                .setParameter("cancelled", OrderStatus.CANCELLED)
                .setParameter("min", minimumQuantity)
                .getResultList();
    }
}
