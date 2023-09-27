package org.acme.command.extension.it;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class CommandExtensionResourceTest {

    @Test
    public void testHelloEndpoint() {
        given()
                .when().get("/command-extension")
                .then()
                .statusCode(200)
                .body(is("Hello command-extension"));
    }
}
