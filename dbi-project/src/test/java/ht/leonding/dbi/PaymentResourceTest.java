package ht.leonding.dbi;

import ht.leonding.dbi.workload.customer.Customer;
import ht.leonding.dbi.workload.order.FulfillmentType;
import ht.leonding.dbi.workload.order.Order;
import ht.leonding.dbi.workload.order.line.OrderLine;
import ht.leonding.dbi.workload.payment.CardPayment;
import ht.leonding.dbi.workload.payment.CashPayment;
import ht.leonding.dbi.workload.product.flower.Flower;
import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@QuarkusTest
class PaymentResourceTest {

    private static final BigDecimal AMOUNT = new BigDecimal("12.50");
    private static final String CARD_NUMBER = "1234567812344242";

    @Inject
    EntityManager em;

    private Long customerId;
    private Long flowerId;
    private Long cashOrderId;
    private Long cashPaymentId;
    private Long cardOrderId;
    private Long cardPaymentId;

    @BeforeEach
    void setUp() {
        QuarkusTransaction.requiringNew().run(() -> {
            Customer customer = new Customer("Anna", "Huber", "resource-payment-" + UUID.randomUUID() + "@test.at");
            Flower flower = new Flower();
            flower.setName("Rose " + UUID.randomUUID());
            flower.setColor("Red");
            flower.setPrice(AMOUNT);
            flower.setStock(10);
            em.persist(customer);
            em.persist(flower);
            customerId = customer.getId();
            flowerId = flower.getId();

            Order cashOrder = new Order(customer, FulfillmentType.PICKUP, LocalDate.now().plusDays(1), null);
            cashOrder.addLine(new OrderLine(flower, 1));
            cashOrder.setPayment(new CashPayment(cashOrder.getTotalPrice()));
            em.persist(cashOrder);
            cashOrderId = cashOrder.getId();
            cashPaymentId = cashOrder.getPayment().getId();

            Order cardOrder = new Order(customer, FulfillmentType.PICKUP, LocalDate.now().plusDays(2), null);
            cardOrder.addLine(new OrderLine(flower, 1));
            cardOrder.setPayment(new CardPayment(cardOrder.getTotalPrice(), "Anna", CARD_NUMBER.substring(12)));
            cardOrder.markPaid();
            em.persist(cardOrder);
            cardOrderId = cardOrder.getId();
            cardPaymentId = cardOrder.getPayment().getId();
        });
    }

    @AfterEach
    void tearDown() {
        QuarkusTransaction.requiringNew().run(() -> {
            Order cashOrder = em.find(Order.class, cashOrderId);
            if (cashOrder != null) em.remove(cashOrder);
            Order cardOrder = em.find(Order.class, cardOrderId);
            if (cardOrder != null) em.remove(cardOrder);
            Flower flower = em.find(Flower.class, flowerId);
            if (flower != null) em.remove(flower);
            Customer customer = em.find(Customer.class, customerId);
            if (customer != null) em.remove(customer);
        });
    }

    @Test
    void findById_existing_returns200() {
        given()
                .when().get("/api/payments/{id}", cashPaymentId)
                .then()
                .statusCode(200)
                .body("id", equalTo(cashPaymentId.intValue()))
                .body("method", is("CASH"))
                .body("amount", equalTo(12.5f))
                .body("status", is("PENDING"))
                .body("paidAt", nullValue())
                .body("last4Digits", nullValue())
                .body("changeReturned", nullValue());
    }

    @Test
    void findById_card_returnsOnlyLastFourDigits() {
        String body = given()
                .when().get("/api/payments/{id}", cardPaymentId)
                .then()
                .statusCode(200)
                .body("method", is("CARD"))
                .body("amount", equalTo(12.5f))
                .body("status", is("PAID"))
                .body("paidAt", notNullValue())
                .body("last4Digits", is("4242"))
                .body("changeReturned", nullValue())
                .body("$", not(hasKey("cardNumber")))
                .body("$", not(hasKey("amountGiven")))
                .extract().body().asString();

        assertThat(body, not(containsString(CARD_NUMBER)));
    }

    @Test
    void findByOrderId_existing_returns200() {
        given()
                .queryParam("orderId", cashOrderId)
                .when().get("/api/payments")
                .then()
                .statusCode(200)
                .body("id", equalTo(cashPaymentId.intValue()))
                .body("method", is("CASH"));
    }

    @Test
    void findByOrderId_missingParameter_returns400() {
        given()
                .when().get("/api/payments")
                .then()
                .statusCode(400);
    }

    @Test
    void markPaid_cash_returnsPaidWithChange() {
        given()
                .contentType("application/json")
                .body(Map.of("amountGiven", new BigDecimal("20.00")))
                .when().put("/api/orders/{orderId}/pay", cashOrderId)
                .then()
                .statusCode(200)
                .body("status", is("PAID"))
                .body("paidAt", notNullValue())
                .body("changeReturned", equalTo(7.5f))
                .body("last4Digits", nullValue());
    }

    @Test
    void markPaid_insufficientCash_isRejected() {
        given()
                .contentType("application/json")
                .body(Map.of("amountGiven", new BigDecimal("10.00")))
                .when().put("/api/orders/{orderId}/pay", cashOrderId)
                .then()
                .statusCode(400)
                .body("message", containsString("Insufficient cash"));
    }

    @Test
    void createPaymentEndpoint_isGone() {
        given()
                .contentType("application/json")
                .body(Map.of())
                .when().post("/api/payments")
                .then()
                .statusCode(405);
    }
    
    @Test
    void findById_unknown_returns404() {
        given().when().get("/api/payments/{id}", 999_999)
                .then().statusCode(404)
                .body("message", containsString("999999"));
    }

    @Test
    void findByOrderId_unknownOrder_returns404() {
        given().queryParam("orderId", 999_999)
                .when().get("/api/payments")
                .then().statusCode(404);
    }

    @Test
    void markPaid_cardPayment_isRejected() {
        given().contentType("application/json")
                .body(Map.of("amountGiven", new BigDecimal("20.00")))
                .when().put("/api/orders/{orderId}/pay", cardOrderId)
                .then().statusCode(400);
    }

    @Test
    void markPaid_insufficientCash_keepsPaymentPending() {
        given().contentType("application/json")
                .body(Map.of("amountGiven", new BigDecimal("10.00")))
                .when().put("/api/orders/{orderId}/pay", cashOrderId)
                .then().statusCode(400);

        given().when().get("/api/payments/{id}", cashPaymentId)
                .then().body("status", is("PENDING"));   // nothing was changed
    }
}
