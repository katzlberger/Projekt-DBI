package ht.leonding.dbi.model.payment;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CreatePaymentRequest(
        @NotNull Long orderId,
        @NotBlank String method,
        @NotNull @DecimalMin(value = "0.01") BigDecimal amount,
        @Size(max = 100) String cardHolder,
        @Pattern(regexp = "\\d{4}") String last4Digits
) {
}
