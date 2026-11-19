package ht.leonding.dbi.model.order;

import ht.leonding.dbi.model.customer.AddressDTO;
import ht.leonding.dbi.model.payment.PaymentDTO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record OrderDTO(
        Long id,
        LocalDateTime orderDate,
        String status,
        String fulfillmentType,
        LocalDate deliveryDate,
        Long customerId,
        AddressDTO deliveryAddress,
        List<OrderLineDTO> lines,
        PaymentDTO payment,
        BigDecimal totalPrice
) {
}
