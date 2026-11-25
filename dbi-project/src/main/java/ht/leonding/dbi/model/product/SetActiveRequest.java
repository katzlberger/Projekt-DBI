package ht.leonding.dbi.model.product;

import jakarta.validation.constraints.NotNull;

public record SetActiveRequest(@NotNull Boolean active) { }
