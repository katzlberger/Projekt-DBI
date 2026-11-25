package ht.leonding.dbi.workload.product;

import ht.leonding.dbi.model.product.BestSellerDTO;
import ht.leonding.dbi.workload.product.bouquet.Bouquet;
import ht.leonding.dbi.workload.product.flower.Flower;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;

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
        return null;
    }

    @Override
    public void persist(Product product) {

    }

    @Override
    public void remove(Product product) {

    }

    @Override
    public List<Product> findAll(Class<? extends Product> type, Boolean active) {
        return List.of();
    }

    @Override
    public List<Flower> findFlowers(String color, boolean availableOnly) {
        return List.of();
    }

    @Override
    public List<Flower> findFlowersByIds(Collection<Long> ids) {
        return List.of();
    }

    @Override
    public List<Bouquet> findAllBouquets() {
        return List.of();
    }

    @Override
    public boolean isReferencedByOrderLine(Long productId) {
        return false;
    }

    @Override
    public boolean isFlowerUsedInBouquet(Long flowerId) {
        return false;
    }

    @Override
    public List<BestSellerDTO> findBestSellingProducts(LocalDateTime from, LocalDateTime toExclusive, long minimumQuantity) {
        return List.of();
    }
}
