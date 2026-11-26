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

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
class CustomerResourceTest {

    private static final String EMAIL = "resource-anna@test.at";

    @Inject
    EntityManager em;

    private Long customerId;

    @BeforeEach
    void setUp() {
        customerId = QuarkusTransaction.requiringNew().call(() -> {
            Customer customer = new Customer("Anna", "Huber", EMAIL);
            customer.addAddress(new Address("Hauptstr. 1", "Hart", "4060"));
            em.persist(customer);
            return customer.getId();
        });
    }

    @AfterEach
    void tearDown() {
        QuarkusTransaction.requiringNew().run(() -> {
            Customer customer = em.find(Customer.class, customerId);
            if (customer != null) {
                em.remove(customer);
            }
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
                .body("address.size()", is(1))
                .body("address[0].street", equalTo("Hauptstr. 1"))
                .body("address[0].city", equalTo("Hart"))
                .body("address[0].zip", equalTo("4060"));
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
                .body("addresses[0]", not(hasKey("customer")));
    }
}