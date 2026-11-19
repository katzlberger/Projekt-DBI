package ht.leonding.dbi.model.product;

import java.math.BigDecimal;

public record ProductDTO(
        Long id,
        String name,
        BigDecimal price,
        boolean active,
        String type
) {
}
