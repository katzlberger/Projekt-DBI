package ht.leonding.dbi;

import ht.leonding.dbi.workload.customer.Customer;
import ht.leonding.dbi.workload.customer.address.Address;
import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
class CustomerResourceTest {

    private static final String EMAIL = "resource-anna@test.at";

    @Inject
    EntityManager em;

    private Long customerId;
    private Long customerWithoutAddressId;
    private String emailWithoutAddress;

    @BeforeEach
    void setUp() {
        emailWithoutAddress = "resource-" + UUID.randomUUID() + "@test.at";
        QuarkusTransaction.requiringNew().run(() -> {
            Customer customer = new Customer("Anna", "Huber", EMAIL);
            customer.addAddress(new Address("Hauptstr. 1", "Hart", "4060"));
            em.persist(customer);
            customerId = customer.getId();

            Customer pickupGuest = new Customer("Bob", "Meier", emailWithoutAddress);
            em.persist(pickupGuest);
            customerWithoutAddressId = pickupGuest.getId();
        });
    }

    @AfterEach
    void tearDown() {
        QuarkusTransaction.requiringNew().run(() -> {
            Customer customer = em.find(Customer.class, customerId);
            if (customer != null) em.remove(customer);
            Customer pickupGuest = em.find(Customer.class, customerWithoutAddressId);
            if (pickupGuest != null) em.remove(pickupGuest);
        });
    }

    @Test
    void findById_existing_returns200() {
        given()
                .when().get("/api/customers/{id}", customerId)
                .then()
                .statusCode(200)
                .body("id", equalTo(customerId.intValue()))
                .body("firstName", equalTo("Anna"))
                .body("lastName", equalTo("Huber"))
                .body("email", equalTo(EMAIL))
                .body("addresses.size()", is(1))
                .body("addresses[0].street", equalTo("Hauptstr. 1"))
                .body("addresses[0].city", equalTo("Hart"))
                .body("addresses[0].zip", equalTo("4060"));
    }

    @Test
    void findById_customerWithoutAddress_returnsEmptyList() {
        given()
                .when().get("/api/customers/{id}", customerWithoutAddressId)
                .then()
                .statusCode(200)
                .body("lastName", equalTo("Meier"))
                .body("addresses", empty());
    }

    @Test
    void findById_unknown_returns404WithMessage() {
        given()
                .when().get("/api/customers/{id}", 999_999)
                .then()
                .statusCode(404)
                .contentType("application/json")
                .body("message", containsString("999999"));
    }

    @Test
    void findByEmail_existing_returns200() {
        given()
                .queryParam("email", EMAIL)
                .when().get("/api/customers")
                .then()
                .statusCode(200)
                .body("id", equalTo(customerId.intValue()))
                .body("firstName", equalTo("Anna"));
    }

    @Test
    void findByEmail_differentCase_returns200() {   // needs normalization
        given()
                .queryParam("email", "  Resource-Anna@TEST.at ")
                .when().get("/api/customers")
                .then()
                .statusCode(200)
                .body("id", equalTo(customerId.intValue()));
    }

    @Test
    void findByEmail_unknown_returns404WithMessage() {
        given()
                .queryParam("email", "nobody@test.at")
                .when().get("/api/customers")
                .then()
                .statusCode(404)
                .body("message", containsString("nobody@test.at"));
    }

    @Test
    void findByEmail_missingParameter_returns400() {
        given()
                .when().get("/api/customers")
                .then()
                .statusCode(400);
    }

    @Test
    void response_doesNotLeakInternalFields() {
        given()
                .when().get("/api/customers/{id}", customerId)
                .then()
                .statusCode(200)
                .body("$", not(hasKey("customer")))
                .body("addresses", notNullValue())
                .body("addresses[0]", not(hasKey("customer")));
    }
}