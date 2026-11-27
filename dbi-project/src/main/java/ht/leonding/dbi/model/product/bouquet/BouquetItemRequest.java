package ht.leonding.dbi.model.product.bouquet;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record BouquetItemRequest(
        @NotNull @Positive Long flowerId,
        @Positive int quantity
) {
}
