package ht.leonding.dbi.model.payment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreatePaymentRequest(
        @NotNull Long orderId,
        @NotBlank String method,
        @Size(max = 100) String cardHolder,
        @Pattern(regexp = "\\d{16}") String cardNumber
) {
}
