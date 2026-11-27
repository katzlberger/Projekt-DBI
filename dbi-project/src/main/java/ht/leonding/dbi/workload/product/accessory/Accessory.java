package ht.leonding.dbi.workload.product.accessory;

import ht.leonding.dbi.workload.product.Product;
import jakarta.persistence.*;

@Entity
@Table(name = "accessory")
public class Accessory extends Product {
    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private AccessoryType type;

    public AccessoryType getType() {
        return type;
    }

    public void setType(AccessoryType type) {
        this.type = type;
    }

    @Override
    public String getProductType() {
        return "ACCESSORY";
    }

    @Override
    public void reserveStock(int quantity) {
        // Accessories have no stock.
    }

    @Override
    public void releaseStock(int quantity) {
        // Accessories have no stock.
    }
}
