package ht.leonding.dbi.model.product.accessory;

import ht.leonding.dbi.workload.product.accessory.AccessoryType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CreateAccessoryRequest(
                @NotBlank String name,
                @NotNull @DecimalMin("0.00") BigDecimal price,
                @NotNull AccessoryType type)
{ }
