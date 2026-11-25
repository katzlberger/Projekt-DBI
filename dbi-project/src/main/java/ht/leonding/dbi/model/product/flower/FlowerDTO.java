package ht.leonding.dbi.model.product.flower;

import java.math.BigDecimal;

public record FlowerDTO(
        Long id,
        String name,
        BigDecimal price,
        boolean active,
        String color,
        int stock,
        String type
) {
}
