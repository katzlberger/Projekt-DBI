package ht.leonding.dbi.model.product.accessory;

import java.math.BigDecimal;

public record AccessoryDTO(
        Long id,
        String name,
        BigDecimal price,
        boolean active,
        String type
) {
}
