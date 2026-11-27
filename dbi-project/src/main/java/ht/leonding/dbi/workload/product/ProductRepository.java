package ht.leonding.dbi.workload.product;

import ht.leonding.dbi.model.product.BestSellerDTO;
import ht.leonding.dbi.workload.product.bouquet.Bouquet;
import ht.leonding.dbi.workload.product.flower.Flower;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface ProductRepository {
    Product findById(Long id);

    void persist(Product product);

    void remove(Product product);

    void flush();

    List<Product> findAll(Class<? extends Product> type, Boolean active);

    List<Flower> findFlowers(String color, boolean availableOnly);

    List<Flower> findFlowersByIds(Collection<Long> ids);

    List<Bouquet> findAllBouquets();

    boolean isReferencedByOrderLine(Long productId);

    boolean isFlowerUsedInBouquet(Long flowerId);

    List<BestSellerDTO> findBestSellingProducts(LocalDateTime from, LocalDateTime toExclusive,
                                                long minimumQuantity);
}
