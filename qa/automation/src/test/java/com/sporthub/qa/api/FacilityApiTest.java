package com.sporthub.qa.api;

import com.sporthub.qa.base.TestConfig;
import io.restassured.RestAssured;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.hamcrest.Matchers.lessThan;

public class FacilityApiTest {
    @BeforeClass
    public void configure() { RestAssured.baseURI = TestConfig.apiBaseUrl(); }

    @Test(groups = {"api", "smoke"})
    public void facilitiesEndpointRespondsWithinBaseline() {
        RestAssured.given().when().get("/facilities")
                .then().time(lessThan(5000L)).statusCode(200);
    }
}
