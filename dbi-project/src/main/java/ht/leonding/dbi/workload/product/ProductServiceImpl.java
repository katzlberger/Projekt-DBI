package ht.leonding.dbi.workload.product;

import ht.leonding.dbi.model.product.BestSellerDTO;
import ht.leonding.dbi.model.product.UpdateProductRequest;
import ht.leonding.dbi.model.product.accessory.AccessoryDTO;
import ht.leonding.dbi.model.product.accessory.CreateAccessoryRequest;
import ht.leonding.dbi.model.product.bouquet.BouquetDTO;
import ht.leonding.dbi.model.product.bouquet.CreateBouquetRequest;
import ht.leonding.dbi.model.product.flower.CreateFlowerRequest;
import ht.leonding.dbi.model.product.flower.FlowerDTO;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.LocalDate;
import java.util.List;

@ApplicationScoped
public class ProductServiceImpl implements ProductService {
    @Override
    public List<Object> listProducts(String type, Boolean active) {
        return List.of();
    }

    @Override
    public Object getProduct(Long productId) {
        return null;
    }

    @Override
    public List<FlowerDTO> listAvailableFlowers(String color, boolean availableOnly) {
        return List.of();
    }

    @Override
    public List<BouquetDTO> listBouquets() {
        return List.of();
    }

    @Override
    public FlowerDTO createFlower(CreateFlowerRequest request) {
        return null;
    }

    @Override
    public AccessoryDTO createAccessory(CreateAccessoryRequest request) {
        return null;
    }

    @Override
    public BouquetDTO createBouquet(CreateBouquetRequest request) {
        return null;
    }

    @Override
    public Object updateProduct(Long productId, UpdateProductRequest request) {
        return null;
    }

    @Override
    public Object setProductActive(Long productId, boolean active) {
        return null;
    }

    @Override
    public void deleteProduct(Long productId) {

    }

    @Override
    public List<BestSellerDTO> findBestSellingProducts(LocalDate from, LocalDate to, long minimumQuantity) {
        return List.of();
    }
}
