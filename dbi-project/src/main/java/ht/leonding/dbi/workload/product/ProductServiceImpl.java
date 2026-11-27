package ht.leonding.dbi.workload.product;

import ht.leonding.dbi.model.product.BestSellerDTO;
import ht.leonding.dbi.model.product.UpdateProductRequest;
import ht.leonding.dbi.model.product.accessory.AccessoryDTO;
import ht.leonding.dbi.model.product.accessory.CreateAccessoryRequest;
import ht.leonding.dbi.model.product.bouquet.BouquetDTO;
import ht.leonding.dbi.model.product.bouquet.BouquetItemRequest;
import ht.leonding.dbi.model.product.bouquet.CreateBouquetRequest;
import ht.leonding.dbi.model.product.flower.CreateFlowerRequest;
import ht.leonding.dbi.model.product.flower.FlowerDTO;
import ht.leonding.dbi.workload.product.accessory.Accessory;
import ht.leonding.dbi.workload.product.bouquet.Bouquet;
import ht.leonding.dbi.workload.product.flower.Flower;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@ApplicationScoped
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    public ProductServiceImpl(ProductRepository repo, ProductMapper mapper) {
        this.productRepository = repo;
        this.productMapper = mapper;
    }

    @Override
    public List<Object> listProducts(String type, Boolean active) {
        return productRepository.findAll(resolveType(type), active).stream().map(productMapper::toDTO).toList();
    }

    @Override
    public Object getProduct(Long productId) {
        return productMapper.toDTO(find(productId));
    }

    @Override
    public List<FlowerDTO> listAvailableFlowers(String color, boolean availableOnly) {
        return productRepository.findFlowers(color, availableOnly).stream().map(productMapper::toFlowerDTO).toList();
    }

    @Override
    @Transactional
    public List<BouquetDTO> listBouquets() {
        return productRepository.findAllBouquets().stream().map(productMapper::toBouquetDTO).toList();
    }

    @Override
    @Transactional
    public FlowerDTO createFlower(CreateFlowerRequest request) {
        Flower f = new Flower();
        f.setName(request.name());
        f.setPrice(request.price());
        f.setColor(request.color());
        f.setStock(request.stock());
        productRepository.persist(f);
        return productMapper.toFlowerDTO(f);
    }

    @Override
    @Transactional
    public AccessoryDTO createAccessory(CreateAccessoryRequest request) {
        Accessory a = new Accessory();
        a.setName(request.name());
        a.setPrice(request.price());
        a.setType(request.type());
        productRepository.persist(a);
        return productMapper.toAccessoryDTO(a);
    }

    @Override
    @Transactional
    public BouquetDTO createBouquet(CreateBouquetRequest request) {
        Set<Long> ids = new HashSet<>();
        for (BouquetItemRequest item : request.items()) {
            if (!ids.add(item.flowerId())) {
                throw new BadRequestException("Blume " + item.flowerId() + " kommt mehrfach vor");
            }
        }
        Map<Long, Flower> flowers = productRepository.findFlowersByIds(ids).stream()
                .collect(Collectors.toMap(Flower::getId, Function.identity()));
        for (Long id : ids) {
            Flower f = flowers.get(id);
            if (f == null) throw new NotFoundException("Blume " + id + " nicht gefunden");
            if (!f.isActive()) throw conflict("Blume " + id + " ist deaktiviert");
        }
        Bouquet b = new Bouquet();
        b.setName(request.name());
        b.setPrice(request.price());
        b.setTemplate(request.template());
        for (BouquetItemRequest item : request.items()) {
            b.addItem(flowers.get(item.flowerId()), item.quantity());
        }
        productRepository.persist(b);
        return productMapper.toBouquetDTO(b);
    }

    @Override
    @Transactional
    public Object updateProduct(Long productId, UpdateProductRequest request) {
        Product p = find(productId);
        boolean flowerFields = request.color() != null || request.stock() != null;
        boolean accessoryFields = request.accessoryType() != null;
        boolean bouquetFields = request.template() != null;

        if (p instanceof Flower f) {
            if (accessoryFields || bouquetFields) throw badFields(p);
            if (request.color() != null) f.setColor(request.color());
            if (request.stock() != null) f.setStock(request.stock());
        } else if (p instanceof Accessory a) {
            if (flowerFields || bouquetFields) throw badFields(p);
            if (request.accessoryType() != null) a.setType(request.accessoryType());
        } else if (p instanceof Bouquet b) {
            if (flowerFields || accessoryFields) throw badFields(p);
            if (request.template() != null) b.setTemplate(request.template());
        }
        p.setName(request.name());
        p.setPrice(request.price());
        return productMapper.toDTO(p);
    }

    @Override
    @Transactional
    public Object setProductActive(Long productId, boolean active) {
        Product p = find(productId);
        p.setActive(active);
        return productMapper.toDTO(p);
    }

    @Override
    @Transactional
    public void deleteProduct(Long productId) {
        Product p = find(productId);
        if (productRepository.isReferencedByOrderLine(productId)) {
            throw conflict("Produkt " + productId + " ist in Bestellungen enthalten, bitte deaktivieren");
        }
        if (p instanceof Flower && productRepository.isFlowerUsedInBouquet(productId)) {
            throw conflict("Blume " + productId + " wird in Bouquets verwendet");
        }
        productRepository.remove(p);
    }

    @Override
    public List<BestSellerDTO> findBestSellingProducts(LocalDate from, LocalDate to, long minimumQuantity) {
        if (from == null || to == null || from.isAfter(to)) {
            throw new BadRequestException("Ungültiger Zeitraum");
        }
        if (minimumQuantity < 1) {
            throw new BadRequestException("minimumQuantity muss mindestens 1 sein");
        }
        return productRepository.findBestSellingProducts(from.atStartOfDay(), to.plusDays(1).atStartOfDay(), minimumQuantity);
    }

    private BadRequestException badFields(Product product) {
        return new BadRequestException("Update-Felder passen nicht zum Produkttyp "
                + product.getClass().getSimpleName());
    }

    private WebApplicationException conflict(String message) {
        return new WebApplicationException(message, Response.Status.CONFLICT);
    }

    private Product find(Long id) {
        Product p = productRepository.findById(id);
        if (p == null) throw new NotFoundException("Produkt " + id + " nicht gefunden");
        return p;
    }

    private Class<? extends Product> resolveType(String type) {
        if (type == null || type.isBlank()) return null;
        return switch (type.toUpperCase()) {
            case "FLOWER" -> Flower.class;
            case "ACCESSORY" -> Accessory.class;
            case "BOUQUET" -> Bouquet.class;
            default -> throw new BadRequestException("Unbekannter Produkttyp: " + type);
        };
    }
}
