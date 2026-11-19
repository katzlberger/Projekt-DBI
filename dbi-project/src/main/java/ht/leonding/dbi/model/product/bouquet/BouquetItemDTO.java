package ht.leonding.dbi.model.product.bouquet;

import java.math.BigDecimal;

public record BouquetItemDTO(
        Long flowerId,
        String flowerName,
        BigDecimal flowerPrice,
        int quantity
) {
}
