package ht.leonding.dbi.model.product.bouquet;

import java.util.List;

public record CreateBouquetRequest(
        String name,
        List<BouquetItemDTO> items,

) {
}
