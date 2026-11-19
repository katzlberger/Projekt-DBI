package ht.leonding.dbi.model.order;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record MarkPaidRequest(
        @NotNull @Positive BigDecimal amountGiven
) {
}