package ht.leonding.dbi.workload.product.bouquet;

import ht.leonding.dbi.workload.product.Product;
import ht.leonding.dbi.workload.product.bouquet.item.BouquetItem;
import ht.leonding.dbi.workload.product.flower.Flower;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "bouquet")
public class Bouquet extends Product {
    @Column(nullable = false)
    private boolean template;

    @OneToMany(mappedBy = "bouquet", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BouquetItem> items = new ArrayList<>();

    public boolean isTemplate() { return template; }
    public void setTemplate(boolean template) { this.template = template; }
    public List<BouquetItem> getItems() { return items; }

    public void addItem(Flower flower, int quantity) {
        items.add(new BouquetItem(this, flower, quantity));
    }

    @Override
    public String getProductType() {
        return "BOUQUET";
    }

    @Override
    public void reserveStock(int quantity) {
        for (BouquetItem item : items) {
            item.getFlower().reserveStock(item.getQuantity() * quantity);
        }
    }

    @Override
    public void releaseStock(int quantity) {
        for (BouquetItem item : items) {
            item.getFlower().releaseStock(item.getQuantity() * quantity);
        }
    }
}
