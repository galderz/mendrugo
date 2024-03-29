package org.acme.greeting.extension.it;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class GreetingExtensionResourceTest
{

    @Test
    public void testHelloEndpoint() {
        given()
                .when().get("/fibula-extension")
                .then()
                .statusCode(200)
                .body(is("Hello fibula-extension"));
    }
}
