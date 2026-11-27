package ht.leonding.dbi;

import ht.leonding.dbi.model.order.CreateOrderRequest;
import ht.leonding.dbi.model.order.OrderLineRequest;
import ht.leonding.dbi.workload.customer.Customer;
import ht.leonding.dbi.workload.order.Order;
import ht.leonding.dbi.workload.order.OrderRepository;
import ht.leonding.dbi.workload.product.flower.Flower;
import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.Response;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.is;

@QuarkusTest
class OrderResourceTest {

    private static final BigDecimal AMOUNT = new BigDecimal("12.50");

    @Inject
    EntityManager em;

    @Inject
    OrderRepository orderRepository;

    private Long customerId;
    private Long flowerId;
    private Long orderId;

    @BeforeEach
    void setUp() {
        QuarkusTransaction.requiringNew().run(() -> {
            Customer customer = new Customer("Anna", "Huber", "order-" + UUID.randomUUID() + "@test.at");
            Flower flower = new Flower();
            flower.setName("Rose " + UUID.randomUUID());
            flower.setColor("Red");
            flower.setPrice(AMOUNT);
            flower.setStock(10);
            em.persist(customer);
            em.persist(flower);
            customerId = customer.getId();
            flowerId = flower.getId();
        });
    }

    @AfterEach
    void tearDown() {
        QuarkusTransaction.requiringNew().run(() -> {
            Order order = orderId == null ? null : orderRepository.findById(orderId);
            if (order != null) {
                em.remove(order);
            }
            Flower flower = em.find(Flower.class, flowerId);
            if (flower != null) {
                em.remove(flower);
            }
            Customer customer = em.find(Customer.class, customerId);
            if (customer != null) {
                em.remove(customer);
            }
        });
    }

    private Long createOrderViaApi() {
        CreateOrderRequest request = new CreateOrderRequest(
                customerId,
                null,
                "PICKUP",
                LocalDate.now().plusDays(1),
                "CASH",
                null,
                null,
                List.of(new OrderLineRequest(flowerId, 2)));

        Number id = given()
                .contentType("application/json")
                .body(request)
                .when().post("/api/orders")
                .then()
                        .statusCode(201)
                        .extract().path("id");
        Long createdId = id.longValue();
        orderId = createdId;
        return createdId;
    }

    @Test
    void createOrder_returns201WithLocationAndOrderDetails() {
        Long createdId = createOrderViaApi();

        given()
                .when().get("/api/orders/{id}", createdId)
                .then()
                        .statusCode(200)
                        .body("id", equalTo(createdId.intValue()))
                        .body("status", is("NEW"))
                        .body("customerId", equalTo(customerId.intValue()))
                        .body("lines.size()", is(1))
                        .body("lines[0].productId", equalTo(flowerId.intValue()))
                        .body("lines[0].quantity", is(2))
                        .body("totalPrice", equalTo(25.0F));
    }

    @Test
    void createOrder_returnsCreatedWithLocationHeader() {
        Response response = given()
                .contentType("application/json")
                .body(new CreateOrderRequest(
                        customerId,
                        null,
                        "PICKUP",
                        LocalDate.now().plusDays(1),
                        "CASH",
                        null,
                        null,
                        List.of(new OrderLineRequest(flowerId, 2))))
                .when().post("/api/orders")
                .then()
                        .statusCode(201)
                        .extract().response();

        Long createdId = ((Number) response.path("id")).longValue();
        orderId = createdId;

        assertThat(response.header("Location"), endsWith("/api/orders/" + createdId));
    }

    @Test
    void getOrder_unknownReturns404() {
        given()
                .when().get("/api/orders/{id}", 999_999)
                .then()
                        .statusCode(404);
    }

    @Test
    void searchOrders_byCustomerId_returnsCreatedOrders() {
        Long createdId = createOrderViaApi();

        given()
                .queryParam("customerId", customerId)
                .when().get("/api/orders")
                .then()
                        .statusCode(200)
                        .body("[0].id", equalTo(createdId.intValue()));
    }
}
