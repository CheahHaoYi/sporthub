package com.sporthub.qa.api;

import com.sporthub.qa.base.TestConfig;
import io.restassured.RestAssured;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.hamcrest.Matchers.equalTo;

/** API smoke/sanity checks against endpoints that are implemented today. */
public class PlatformApiTest {
    @BeforeClass
    public void configure() {
        RestAssured.baseURI = TestConfig.systemBaseUrl();
    }

    @Test(groups = {"api", "smoke"})
    public void healthEndpointAndDatabaseAreAvailable() {
        RestAssured.given()
                .when().get("/actuator/health")
                .then()
                .statusCode(200)
                .body("status", equalTo("UP"));
    }

    @Test(groups = {"api", "sanity"})
    public void springdocMetadataIsProtectedByDefaultSecurity() {
        RestAssured.given()
                .when().get("/v3/api-docs")
                .then()
                .statusCode(401);
    }
}
