package ht.leonding.dbi.workload.product.bouquet.item;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class BouquetItemId implements Serializable {
    @Column(name = "bouquet_id")
    private Long bouquetId;

    @Column(name = "flower_id")
    private Long flowerId;

    public BouquetItemId() { }

    public BouquetItemId(Long bouquetId, Long flowerId) {
        this.bouquetId = bouquetId;
        this.flowerId = flowerId;
    }

    public Long getBouquetId() { return bouquetId; }
    public Long getFlowerId() { return flowerId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BouquetItemId that)) return false;
        return Objects.equals(bouquetId, that.bouquetId) && Objects.equals(flowerId, that.flowerId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bouquetId, flowerId);
    }
}
