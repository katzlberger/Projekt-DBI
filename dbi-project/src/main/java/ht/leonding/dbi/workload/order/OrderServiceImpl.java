package ht.leonding.dbi.workload.order;

import ht.leonding.dbi.model.order.CreateOrderRequest;
import ht.leonding.dbi.model.order.OrderDTO;
import ht.leonding.dbi.model.order.OrderLineRequest;
import ht.leonding.dbi.workload.customer.Customer;
import ht.leonding.dbi.workload.customer.address.Address;
import ht.leonding.dbi.workload.order.line.OrderLine;
import ht.leonding.dbi.workload.payment.CardPayment;
import ht.leonding.dbi.workload.payment.CashPayment;
import ht.leonding.dbi.workload.payment.Payment;
import ht.leonding.dbi.workload.payment.PaymentMethod;
import ht.leonding.dbi.workload.product.Product;
import ht.leonding.dbi.workload.product.ProductRepository;
import ht.leonding.dbi.workload.product.flower.Flower;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final OrderMapper orderMapper;

    public OrderServiceImpl(OrderRepository orderRepository, ProductRepository productRepository, OrderMapper orderMapper) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.orderMapper = orderMapper;
    }

    @Override
    @Transactional
    public OrderDTO createOrder(CreateOrderRequest request) {
        Customer customer = orderRepository.findCustomer(request.customerId());
        if (customer == null) {
            throw new NotFoundException("Kunde " + request.customerId() + " nicht gefunden");
        }

        FulfillmentType fulfillmentType = parseFulfillmentType(request.fulfillmentType());
        LocalDate deliveryDate = request.deliveryDate();

        Address address = null;
        if (fulfillmentType == FulfillmentType.DELIVERY) {
            if (request.addressId() == null) throw new BadRequestException("Für Lieferung ist eine Adresse nötig");
            if (deliveryDate == null) throw new BadRequestException("Für Lieferung ist ein Lieferdatum nötig");
            if (deliveryDate.isBefore(LocalDate.now())) throw new BadRequestException("Lieferdatum liegt in der Vergangenheit");
            address = orderRepository.findAddress(request.addressId());
            if (address == null) throw new NotFoundException("Adresse " + request.addressId() + " nicht gefunden");
            if (!address.getCustomer().getId().equals(customer.getId())) {
                throw new BadRequestException("Adresse gehört nicht zum Kunden");
            }
        }

        Map<Long, Product> products = new LinkedHashMap<>();
        for (OrderLineRequest line : request.lines()) {
            if (products.containsKey(line.productId())) {
                throw new BadRequestException("Produkt " + line.productId() + " kommt mehrfach vor");
            }
            Product product = productRepository.findById(line.productId());
            if (product == null) throw new NotFoundException("Produkt " + line.productId() + " nicht gefunden");
            if (!product.isActive()) throw conflict("Produkt " + line.productId() + " ist nicht mehr verfügbar");
            if (product instanceof Flower flower && flower.getStock() < line.quantity()) {
                throw conflict("Zu wenig Bestand für Produkt " + line.productId());
            }
            products.put(line.productId(), product);
        }

        Order order = new Order(customer, fulfillmentType, deliveryDate, address);
        for (OrderLineRequest line : request.lines()) {
            Product product = products.get(line.productId());
            order.addLine(new OrderLine(product, line.quantity()));
            product.reserveStock(line.quantity());
        }

        order.setPayment(createPayment(order, parsePaymentMethod(request.paymentMethod()), request.cardHolder(), request.cardNumber()));

        orderRepository.add(order);
        return orderMapper.toDto(order);
    }

    @Override
    @Transactional
    public OrderDTO getOrder(Long orderId) {
        Order order = orderRepository.findByIdWithDetails(orderId);
        if (order == null) throw new NotFoundException("Bestellung " + orderId + " nicht gefunden");
        return orderMapper.toDto(order);
    }

    @Override
    @Transactional
    public List<OrderDTO> searchOrders(Long customerId, OrderStatus status, LocalDate from, LocalDate to) {
        if (from != null && to != null && from.isAfter(to)) {
            throw new BadRequestException("Ungültiger Zeitraum");
        }
        LocalDateTime fromTime = from == null ? null : from.atStartOfDay();
        LocalDateTime toTime = to == null ? null : to.plusDays(1).atStartOfDay();
        return orderRepository.search(customerId, status, fromTime, toTime).stream().map(orderMapper::toDto).toList();
    }

    private FulfillmentType parseFulfillmentType(String fulfillmentType) {
        if (fulfillmentType == null || fulfillmentType.isBlank()) throw new BadRequestException("Lieferart ist erforderlich");
        try {
            return FulfillmentType.valueOf(fulfillmentType.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Unbekannte Lieferart: " + fulfillmentType);
        }
    }

    private PaymentMethod parsePaymentMethod(String paymentMethod) {
        if (paymentMethod == null || paymentMethod.isBlank()) throw new BadRequestException("Zahlungsart ist erforderlich");
        try {
            return PaymentMethod.valueOf(paymentMethod.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Unbekannte Zahlungsart: " + paymentMethod);
        }
    }

    private Payment createPayment(Order order, PaymentMethod method, String cardHolder, String cardNumber) {
        BigDecimal amount = order.getTotalPrice();
        switch (method) {
            case CARD -> {
                if (cardHolder == null || cardHolder.isBlank()) throw new BadRequestException("Karteninhaber darf nicht leer sein");
                if (cardNumber == null || !cardNumber.matches("\\d{16}")) throw new BadRequestException("Kartennummer muss 16 Stellen lang sein");
                CardPayment cardPayment = new CardPayment(amount, cardHolder, cardNumber.substring(12));
                order.markPaid();
                return cardPayment;
            }
            case CASH -> {
                if (order.getFulfillmentType() != FulfillmentType.PICKUP) throw new BadRequestException("Bargeldzahlung ist nur bei Abholung möglich");
                return new CashPayment(amount);
            }
        }
        throw new BadRequestException("Unbekannte Zahlungsart " + method);
    }

    private WebApplicationException conflict(String message) {
        return new WebApplicationException(message, Response.Status.CONFLICT);
    }
}
