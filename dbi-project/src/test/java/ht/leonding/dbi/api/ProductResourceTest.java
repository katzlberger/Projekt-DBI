package ht.leonding.dbi.api;

import ht.leonding.dbi.model.product.bouquet.BouquetItemRequest;
import ht.leonding.dbi.model.product.bouquet.CreateBouquetRequest;
import ht.leonding.dbi.model.product.flower.CreateFlowerRequest;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;

@QuarkusTest
class ProductResourceTest {

    @Test
    void createFlowerReturnsCreatedAndLocation() {
        String name = unique("Rose");
        Response response = given()
                .contentType("application/json")
                .body(new CreateFlowerRequest(name, new BigDecimal("12.50"), "Red", 8))
                .when().post("/api/products/flowers")
                .then()
                .statusCode(201)
                .body("id", greaterThan(0))
                .body("name", is(name))
                .body("type", is("FLOWER"))
                .extract().response();

        long id = ((Number) response.path("id")).longValue();
        assertThat(response.header("Location"), endsWith("/api/products/" + id));
    }

    @Test
    void createFlowerWithInvalidBodyReturns400() {
        given()
                .contentType("application/json")
                .body(Map.of("name", "", "price", new BigDecimal("12.50"), "color", "Red", "stock", 8))
                .when().post("/api/products/flowers")
                .then().statusCode(400);
    }

    @Test
    void getCreatedFlowerReturns200() {
        String name = unique("Sunflower");
        long id = createFlower(name);

        given()
                .when().get("/api/products/{id}", id)
                .then()
                .statusCode(200)
                .body("id", equalTo((int) id))
                .body("name", is(name))
                .body("type", is("FLOWER"));
    }

    @Test
    void getUnknownProductReturns404() {
        given()
                .when().get("/api/products/{id}", -1L)
                .then().statusCode(404);
    }

    @Test
    void updateFlowerReturnsUpdatedDto() {
        String name = unique("Rose");
        long id = createFlower(name);
        String updatedName = name + " updated";

        given()
                .contentType("application/json")
                .body(Map.of(
                        "name", updatedName,
                        "price", new BigDecimal("18.75"),
                        "color", "White",
                        "stock", 12))
                .when().put("/api/products/{id}", id)
                .then()
                .statusCode(200)
                .body("id", equalTo((int) id))
                .body("name", is(updatedName))
                .body("color", is("White"))
                .body("stock", is(12));
    }

    @Test
    void setFlowerInactiveReturnsUpdatedDto() {
        long id = createFlower(unique("Tulip"));

        given()
                .contentType("application/json")
                .body(Map.of("active", false))
                .when().patch("/api/products/{id}/active", id)
                .then()
                .statusCode(200)
                .body("id", equalTo((int) id))
                .body("active", is(false));
    }

    @Test
    void createBouquetReturnsItsFlowerItems() {
        long flowerId = createFlower(unique("Daisy"));
        String bouquetName = unique("Bouquet");
        CreateBouquetRequest request = new CreateBouquetRequest(
                bouquetName,
                new BigDecimal("24.00"),
                true,
                List.of(new BouquetItemRequest(flowerId, 2)));

        Response response = given()
                .contentType("application/json")
                .body(request)
                .when().post("/api/products/bouquets")
                .then()
                .statusCode(201)
                .body("name", is(bouquetName))
                .body("template", is(true))
                .body("items.size()", is(1))
                .body("items[0].flowerId", equalTo((int) flowerId))
                .body("items[0].quantity", is(2))
                .extract().response();

        long bouquetId = ((Number) response.path("id")).longValue();
        assertThat(response.header("Location"), endsWith("/api/products/" + bouquetId));
    }

    @Test
    void createBouquetWithDuplicateFlowerReturns400() {
        long flowerId = createFlower(unique("Orchid"));
        CreateBouquetRequest request = new CreateBouquetRequest(
                unique("Bouquet"),
                new BigDecimal("30.00"),
                false,
                List.of(new BouquetItemRequest(flowerId, 1), new BouquetItemRequest(flowerId, 2)));

        given()
                .contentType("application/json")
                .body(request)
                .when().post("/api/products/bouquets")
                .then().statusCode(400);
    }

    @Test
    void createBouquetWithUnknownFlowerReturns404() {
        CreateBouquetRequest request = new CreateBouquetRequest(
                unique("Bouquet"),
                new BigDecimal("30.00"),
                false,
                List.of(new BouquetItemRequest(Long.MAX_VALUE, 1)));

        given()
                .contentType("application/json")
                .body(request)
                .when().post("/api/products/bouquets")
                .then().statusCode(404);
    }

    @Test
    void createBouquetWithInactiveFlowerReturns409() {
        long flowerId = createFlower(unique("Lily"));
        given()
                .contentType("application/json")
                .body(Map.of("active", false))
                .when().patch("/api/products/{id}/active", flowerId)
                .then().statusCode(200);

        CreateBouquetRequest request = new CreateBouquetRequest(
                unique("Bouquet"),
                new BigDecimal("30.00"),
                false,
                List.of(new BouquetItemRequest(flowerId, 1)));

        given()
                .contentType("application/json")
                .body(request)
                .when().post("/api/products/bouquets")
                .then().statusCode(409);
    }

    private static long createFlower(String name) {
        Response response = given()
                .contentType("application/json")
                .body(new CreateFlowerRequest(name, new BigDecimal("10.00"), "Red", 5))
                .when().post("/api/products/flowers")
                .then().statusCode(201)
                .extract().response();
        return ((Number) response.path("id")).longValue();
    }

    private static String unique(String prefix) {
        return prefix + " " + UUID.randomUUID();
    }
}
