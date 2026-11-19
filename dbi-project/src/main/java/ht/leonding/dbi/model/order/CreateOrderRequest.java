package ht.leonding.dbi.model.order;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.util.List;

public record CreateOrderRequest(
        @NotNull Long customerId,
        Long addressId,
        @NotBlank String fulfillmentType,
        @NotNull @FutureOrPresent LocalDate deliveryDate,
        @NotBlank String paymentMethod,
        String cardHolder,
        @Pattern(regexp = "\\d{16}") String cardNumber,
        @NotEmpty List<@Valid OrderLineRequest> lines
) {
}

