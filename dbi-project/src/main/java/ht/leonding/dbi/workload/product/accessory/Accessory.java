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
}
