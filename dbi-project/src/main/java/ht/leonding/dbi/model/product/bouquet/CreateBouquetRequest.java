package ht.leonding.dbi.model.product.bouquet;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

public record CreateBouquetRequest(
        @NotBlank String name,
        @NotNull @DecimalMin("0.00") BigDecimal price,
        boolean template,
        @NotEmpty List<@Valid BouquetItemRequest> items
) {
}
