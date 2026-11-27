package ht.leonding.dbi.workload.product;

import ht.leonding.dbi.model.product.BestSellerDTO;
import ht.leonding.dbi.model.product.UpdateProductRequest;
import ht.leonding.dbi.model.product.accessory.AccessoryDTO;
import ht.leonding.dbi.model.product.accessory.CreateAccessoryRequest;
import ht.leonding.dbi.model.product.bouquet.BouquetDTO;
import ht.leonding.dbi.model.product.bouquet.CreateBouquetRequest;
import ht.leonding.dbi.model.product.flower.CreateFlowerRequest;
import ht.leonding.dbi.model.product.flower.FlowerDTO;

import java.time.LocalDate;
import java.util.List;

public interface ProductService {
    List<Object> listProducts(String type, Boolean active);

    Object getProduct(Long productId);

    List<FlowerDTO> listAvailableFlowers(String color, boolean availableOnly);

    List<BouquetDTO> listBouquets();

    FlowerDTO createFlower(CreateFlowerRequest request);

    AccessoryDTO createAccessory(CreateAccessoryRequest request);

    BouquetDTO createBouquet(CreateBouquetRequest request);

    Object updateProduct(Long productId, UpdateProductRequest request);

    Object setProductActive(Long productId, boolean active);

    void deleteProduct(Long productId);

    List<BestSellerDTO> findBestSellingProducts(LocalDate from, LocalDate to, long minimumQuantity);
}
