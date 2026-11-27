package ht.leonding.dbi.workload.product.flower;
import ht.leonding.dbi.workload.product.Product;
import jakarta.persistence.*;

@Entity
@Table(name = "flower")
public class Flower extends Product {
    @Column(nullable = false)
    private String color;
    @Column(nullable = false)
    private Integer stock;

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    @Override
    public String getProductType() {
        return "FLOWER";
    }

    @Override
    public void reserveStock(int quantity) {
        decreaseStock(quantity);
    }

    @Override
    public void releaseStock(int quantity) {
        increaseStock(quantity);
    }

    public void decreaseStock(int quantity) {
        this.stock -= quantity;
    }

    public void increaseStock(int quantity) {
        this.stock += quantity;
    }
}
