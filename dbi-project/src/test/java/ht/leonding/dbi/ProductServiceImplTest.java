package ht.leonding.dbi;

import ht.leonding.dbi.model.product.BestSellerDTO;
import ht.leonding.dbi.model.product.UpdateProductRequest;
import ht.leonding.dbi.model.product.bouquet.BouquetDTO;
import ht.leonding.dbi.model.product.bouquet.BouquetItemRequest;
import ht.leonding.dbi.model.product.bouquet.CreateBouquetRequest;
import ht.leonding.dbi.model.product.flower.CreateFlowerRequest;
import ht.leonding.dbi.model.product.flower.FlowerDTO;
import ht.leonding.dbi.workload.product.Product;
import ht.leonding.dbi.workload.product.ProductMapper;
import ht.leonding.dbi.workload.product.ProductRepository;
import ht.leonding.dbi.workload.product.ProductServiceImpl;
import ht.leonding.dbi.workload.product.accessory.AccessoryType;
import ht.leonding.dbi.workload.product.bouquet.Bouquet;
import ht.leonding.dbi.workload.product.flower.Flower;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.WebApplicationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    ProductRepository productRepository;

    private ProductServiceImpl productService;

    @BeforeEach
    void setUp() {
        productService = new ProductServiceImpl(productRepository, new ProductMapper());
    }

    @Test
    void listProductsFiltersByTypeAndActive() {
        Flower flower = flower(1L, "Rose", "Red", 5);
        when(productRepository.findAll(Flower.class, true)).thenReturn(List.of(flower));

        List<Object> products = productService.listProducts("flower", true);

        assertEquals(1, products.size());
        assertInstanceOf(FlowerDTO.class, products.get(0));
        assertEquals("Rose", ((FlowerDTO) products.get(0)).name());
        verify(productRepository).findAll(Flower.class, true);
    }

    @Test
    void getProductUnknownIdThrowsNotFound() {
        when(productRepository.findById(999L)).thenReturn(null);

        assertThrows(NotFoundException.class, () -> productService.getProduct(999L));
    }

    @Test
    void createFlowerPersistsFlowerAndReturnsDto() {
        CreateFlowerRequest request = new CreateFlowerRequest(
                "Rose", new BigDecimal("12.50"), "Red", 8);

        FlowerDTO result = productService.createFlower(request);

        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).persist(captor.capture());
        Flower saved = assertInstanceOf(Flower.class, captor.getValue());
        assertEquals("Rose", saved.getName());
        assertEquals(new BigDecimal("12.50"), saved.getPrice());
        assertEquals("Red", saved.getColor());
        assertEquals(8, saved.getStock());
        assertEquals("Rose", result.name());
        assertEquals("FLOWER", result.type());
    }

    @Test
    void createBouquetRejectsDuplicateFlowerIdsBeforeRepositoryAccess() {
        CreateBouquetRequest request = bouquetRequest(List.of(
                new BouquetItemRequest(10L, 1),
                new BouquetItemRequest(10L, 2)));

        assertThrows(BadRequestException.class, () -> productService.createBouquet(request));

        verifyNoInteractions(productRepository);
    }

    @Test
    void createBouquetRejectsMissingFlowerWithoutPersisting() {
        CreateBouquetRequest request = bouquetRequest(List.of(new BouquetItemRequest(10L, 2)));
        when(productRepository.findFlowersByIds(Set.of(10L))).thenReturn(List.of());

        assertThrows(NotFoundException.class, () -> productService.createBouquet(request));

        verify(productRepository, never()).persist(any());
    }

    @Test
    void createBouquetRejectsInactiveFlowerWithConflict() {
        Flower inactiveFlower = flower(10L, "Rose", "Red", 5);
        inactiveFlower.setActive(false);
        when(productRepository.findFlowersByIds(Set.of(10L))).thenReturn(List.of(inactiveFlower));

        WebApplicationException exception = assertThrows(
                WebApplicationException.class,
                () -> productService.createBouquet(
                        bouquetRequest(List.of(new BouquetItemRequest(10L, 2)))));

        assertEquals(409, exception.getResponse().getStatus());
        verify(productRepository, never()).persist(any());
    }

    @Test
    void createBouquetPersistsBouquetWithRequestedItems() {
        Flower rose = flower(10L, "Rose", "Red", 5);
        when(productRepository.findFlowersByIds(Set.of(10L))).thenReturn(List.of(rose));
        CreateBouquetRequest request = new CreateBouquetRequest(
                "Red bouquet", new BigDecimal("25.00"), true,
                List.of(new BouquetItemRequest(10L, 3)));

        BouquetDTO result = productService.createBouquet(request);

        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).persist(captor.capture());
        Bouquet saved = assertInstanceOf(Bouquet.class, captor.getValue());
        assertEquals("Red bouquet", saved.getName());
        assertEquals(new BigDecimal("25.00"), saved.getPrice());
        assertEquals(1, saved.getItems().size());
        assertEquals(3, saved.getItems().get(0).getQuantity());
        assertEquals("Red bouquet", result.name());
        assertEquals(10L, result.items().get(0).flowerId());
    }

    @Test
    void updateFlowerChangesManagedEntityWithoutPersist() {
        Flower flower = flower(1L, "Rose", "Red", 5);
        when(productRepository.findById(1L)).thenReturn(flower);
        UpdateProductRequest request = new UpdateProductRequest(
                "White rose", new BigDecimal("15.00"), "White", 12, null, null);

        FlowerDTO result = assertInstanceOf(FlowerDTO.class, productService.updateProduct(1L, request));

        assertEquals("White rose", flower.getName());
        assertEquals("White", flower.getColor());
        assertEquals(12, flower.getStock());
        assertEquals("White rose", result.name());
        verify(productRepository, never()).persist(any());
    }

    @Test
    void updateFlowerRejectsFieldsForAnotherProductType() {
        Flower flower = flower(1L, "Rose", "Red", 5);
        when(productRepository.findById(1L)).thenReturn(flower);
        UpdateProductRequest request = new UpdateProductRequest(
                "Rose", new BigDecimal("12.50"), null, null, AccessoryType.VASE, null);

        assertThrows(BadRequestException.class, () -> productService.updateProduct(1L, request));

        assertEquals("Rose", flower.getName());
        verify(productRepository, never()).persist(any());
    }

    @Test
    void setProductActiveChangesManagedEntity() {
        Flower flower = flower(1L, "Rose", "Red", 5);
        when(productRepository.findById(1L)).thenReturn(flower);

        FlowerDTO result = assertInstanceOf(FlowerDTO.class, productService.setProductActive(1L, false));

        assertEquals(false, flower.isActive());
        assertEquals(false, result.active());
        verify(productRepository, never()).persist(any());
    }

    @Test
    void deleteProductRejectsOrderReferencedProduct() {
        Flower flower = flower(1L, "Rose", "Red", 5);
        when(productRepository.findById(1L)).thenReturn(flower);
        when(productRepository.isReferencedByOrderLine(1L)).thenReturn(true);

        WebApplicationException exception = assertThrows(
                WebApplicationException.class, () -> productService.deleteProduct(1L));

        assertEquals(409, exception.getResponse().getStatus());
        verify(productRepository, never()).remove(any());
    }

    @Test
    void deleteFlowerUsedInBouquetReturnsConflictWithoutRemoving() {
        Flower flower = flower(1L, "Rose", "Red", 5);
        when(productRepository.findById(1L)).thenReturn(flower);
        when(productRepository.isReferencedByOrderLine(1L)).thenReturn(false);
        when(productRepository.isFlowerUsedInBouquet(1L)).thenReturn(true);

        WebApplicationException exception = assertThrows(
                WebApplicationException.class, () -> productService.deleteProduct(1L));

        assertEquals(409, exception.getResponse().getStatus());
        verify(productRepository, never()).remove(any());
    }

    @Test
    void deleteProductRemovesUnreferencedProduct() {
        Flower flower = flower(1L, "Rose", "Red", 5);
        when(productRepository.findById(1L)).thenReturn(flower);
        when(productRepository.isReferencedByOrderLine(1L)).thenReturn(false);
        when(productRepository.isFlowerUsedInBouquet(1L)).thenReturn(false);

        productService.deleteProduct(1L);

        verify(productRepository).remove(flower);
    }

    @Test
    void findBestSellingProductsRejectsInvalidArgumentsWithoutQuerying() {
        LocalDate today = LocalDate.now();
        assertThrows(BadRequestException.class,
                () -> productService.findBestSellingProducts(null, today, 1));
        assertThrows(BadRequestException.class,
                () -> productService.findBestSellingProducts(today, today.minusDays(1), 1));
        assertThrows(BadRequestException.class,
                () -> productService.findBestSellingProducts(today, today, 0));

        verifyNoInteractions(productRepository);
    }

    @Test
    void findBestSellingProductsUsesExclusiveDayAfterEndDate() {
        LocalDate from = LocalDate.of(2025, 1, 1);
        LocalDate to = LocalDate.of(2025, 1, 31);
        List<BestSellerDTO> expected = List.of(new BestSellerDTO(1L, "Rose", 5L));
        when(productRepository.findBestSellingProducts(
                from.atStartOfDay(), to.plusDays(1).atStartOfDay(), 2)).thenReturn(expected);

        List<BestSellerDTO> result = productService.findBestSellingProducts(from, to, 2);

        assertEquals(expected, result);
        verify(productRepository).findBestSellingProducts(
                eq(from.atStartOfDay()), eq(to.plusDays(1).atStartOfDay()), eq(2L));
    }

    private static CreateBouquetRequest bouquetRequest(List<BouquetItemRequest> items) {
        return new CreateBouquetRequest("Bouquet", new BigDecimal("20.00"), false, items);
    }

    private static Flower flower(Long id, String name, String color, Integer stock) {
        Flower flower = new Flower();
        flower.setId(id);
        flower.setName(name);
        flower.setPrice(new BigDecimal("10.00"));
        flower.setColor(color);
        flower.setStock(stock);
        return flower;
    }
}
