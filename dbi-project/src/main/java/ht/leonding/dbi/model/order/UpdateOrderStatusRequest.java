package ht.leonding.dbi.model.order;

import jakarta.validation.constraints.NotBlank;

public record UpdateOrderStatusRequest(
        @NotBlank String status
) {
}