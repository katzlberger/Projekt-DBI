package ht.leonding.dbi.workload.product.bouquet.item;

import ht.leonding.dbi.workload.product.bouquet.Bouquet;
import ht.leonding.dbi.workload.product.flower.Flower;
import jakarta.persistence.*;

@Entity
@Table(name = "bouquet_item")
public class BouquetItem {
    @EmbeddedId
    private BouquetItemId id = new BouquetItemId();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("bouquetId")
    @JoinColumn(name = "bouquet_id")
    private Bouquet bouquet;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("flowerId")
    @JoinColumn(name = "flower_id")
    private Flower flower;

    @Column(nullable = false)
    private Integer quantity;

    protected BouquetItem() { }

    public BouquetItem(Bouquet bouquet, Flower flower, int quantity) {
        this.bouquet = bouquet;
        this.flower = flower;
        this.quantity = quantity;
    }

    public BouquetItemId getId() { return id; }
    public Bouquet getBouquet() { return bouquet; }
    public Flower getFlower() { return flower; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
}
