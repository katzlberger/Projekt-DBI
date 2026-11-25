package ht.leonding.dbi.model.product;

import ht.leonding.dbi.workload.product.accessory.AccessoryType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record UpdateProductRequest
    (@NotBlank
    String name,
    @NotNull
    @DecimalMin("0.00")
    BigDecimal price,
    String color,
    @PositiveOrZero
    Integer stock,
    AccessoryType accessoryType,
    Boolean template)
{}
