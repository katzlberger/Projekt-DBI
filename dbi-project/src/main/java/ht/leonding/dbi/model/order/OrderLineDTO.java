package ht.leonding.dbi.model.order;

import java.math.BigDecimal;

public record OrderLineDTO(
        Long id,
        Long productId,
        String productName,
        String productType,
        int quantity,
        BigDecimal unitPrice,
        BigDecimal lineTotal
) {
}
