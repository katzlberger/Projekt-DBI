package ht.leonding.dbi.model.product.bouquet;

import java.math.BigDecimal;
import java.util.List;

public record BouquetDTO(
        Long id,
        String name,
        BigDecimal price,
        boolean template,
        List<BouquetItemDTO> items
) {
}
