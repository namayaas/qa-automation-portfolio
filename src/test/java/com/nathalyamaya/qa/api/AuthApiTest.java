package com.nathalyamaya.qa.api;

import org.testng.annotations.Test;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.matchesPattern;

/** Health check and authentication. Test case IDs map to TEST_PLAN.md section 6. */
public class AuthApiTest extends BaseApiTest {

    @Test(groups = {"api", "smoke"}, description = "TC-API-01: health check responds")
    public void healthCheckResponds() {
        given().spec(spec)
                .when().get("/ping")
                .then().statusCode(201);
    }

    @Test(groups = {"api", "smoke"}, description = "TC-API-02: valid credentials return a token")
    public void validCredentialsReturnToken() {
        given().spec(spec)
                .body(Map.of("username", "admin", "password", "password123"))
                .when().post("/auth")
                .then().statusCode(200)
                .body("token", matchesPattern("[a-f0-9]{10,}"));
    }

    @Test(groups = {"api", "regression"}, description = "TC-API-03: bad credentials are refused with no token")
    public void badCredentialsAreRefused() {
        given().spec(spec)
                .body(Map.of("username", "admin", "password", "wrong"))
                .when().post("/auth")
                .then()
                .body("reason", equalTo("Bad credentials"))
                .body("token", is((Object) null));
    }

    /**
     * Expected to FAIL against the practice site: it returns 200 for bad credentials.
     * Kept as a documented defect (BUG-001 in TEST_PLAN.md) and excluded from CI.
     */
    @Test(groups = {"api", "knownDefect"},
            description = "TC-API-04: bad credentials should return 401/403 (BUG-001)")
    public void badCredentialsReturnAuthError() {
        given().spec(spec)
                .body(Map.of("username", "admin", "password", "wrong"))
                .when().post("/auth")
                .then().statusCode(anyOf(is(401), is(403)));
    }
}
