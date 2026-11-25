package ht.leonding.dbi.model.product.flower;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record CreateFlowerRequest (
        @NotBlank String name,
        @NotNull @DecimalMin("0.00") BigDecimal price,
        @NotBlank String color,
        @NotNull @PositiveOrZero Integer stock) { }
