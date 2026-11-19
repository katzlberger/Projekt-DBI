package ht.leonding.dbi.model.product.bouquet;

public record BouquetItemRequest(
        Long flowerId,
        int quantity
) {
}
