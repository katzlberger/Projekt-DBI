package ht.leonding.dbi.workload.product;

import ht.leonding.dbi.model.product.accessory.AccessoryDTO;
import ht.leonding.dbi.model.product.bouquet.BouquetDTO;
import ht.leonding.dbi.model.product.bouquet.BouquetItemDTO;
import ht.leonding.dbi.model.product.flower.FlowerDTO;
import ht.leonding.dbi.workload.product.accessory.Accessory;
import ht.leonding.dbi.workload.product.bouquet.Bouquet;
import ht.leonding.dbi.workload.product.flower.Flower;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class ProductMapper {
    public Object toDTO(Product p) {
        if (p instanceof Flower f) return toFlowerDTO(f);
        if (p instanceof Accessory a) return toAccessoryDTO(a);
        if (p instanceof Bouquet b) return toBouquetDTO(b);
        throw new IllegalArgumentException("Unbekannter Produkttyp: " + p.getClass());
    }

    public FlowerDTO toFlowerDTO(Flower f) {
        return new FlowerDTO(
                f.getId(),
                f.getName(),
                f.getPrice(),
                f.isActive(),
                f.getColor(),
                f.getStock(),
                "FLOWER");
    }

    public AccessoryDTO toAccessoryDTO(Accessory a) {
        return new AccessoryDTO(
                a.getId(),
                a.getName(),
                a.getPrice(),
                a.isActive(),
                a.getType().name());
    }

    public BouquetDTO toBouquetDTO(Bouquet b) {
        List<BouquetItemDTO> items = b.getItems().stream()
                .map(i -> new BouquetItemDTO(
                        i.getFlower().getId(),
                        i.getFlower().getName(),
                        i.getFlower().getPrice(),
                        i.getQuantity()))
                .toList();
        return new BouquetDTO(
                b.getId(),
                b.getName(),
                b.getPrice(),
                b.isTemplate(),
                items);
    }
}