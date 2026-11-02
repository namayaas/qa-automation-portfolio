package com.nathalyamaya.qa.api;

import com.nathalyamaya.qa.common.Config;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.testng.annotations.BeforeClass;

import java.util.Map;

import static io.restassured.RestAssured.given;

/** Shared REST Assured setup and helpers for the Restful-Booker API. */
public abstract class BaseApiTest {

    protected RequestSpecification spec;

    @BeforeClass(alwaysRun = true)
    public void configureRestAssured() {
        // Print the full request and response only when an assertion fails: quiet logs, easy debugging.
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails(LogDetail.ALL);
        spec = new RequestSpecBuilder()
                .setBaseUri(Config.apiBaseUrl())
                .setContentType(ContentType.JSON)
                // Restful-Booker answers 418 unless Accept is exactly application/json;
                // REST Assured's ContentType.JSON would send four JSON media types.
                .addHeader("Accept", "application/json")
                .build();
    }

    /** Requests an auth token used by PUT, PATCH and DELETE. */
    protected String authToken() {
        return given().spec(spec)
                .body(Map.of("username", Config.apiAdminUser(), "password", Config.apiAdminPassword()))
                .when().post("/auth")
                .then().statusCode(200)
                .extract().path("token");
    }

    /** Creates a booking and returns its id, for tests whose subject is not creation itself. */
    protected int createBooking(Map<String, Object> booking) {
        return given().spec(spec)
                .body(booking)
                .when().post("/booking")
                .then().statusCode(200)
                .extract().path("bookingid");
    }

    /** Best-effort cleanup so tests don't leave data on the shared practice server. */
    protected void deleteBooking(int id) {
        given().spec(spec).cookie("token", authToken())
                .when().delete("/booking/" + id);
    }

    /** A valid booking payload; tests copy and override fields as needed. */
    protected static Map<String, Object> sampleBooking(String firstName, String lastName) {
        return Map.of(
                "firstname", firstName,
                "lastname", lastName,
                "totalprice", 150,
                "depositpaid", true,
                "bookingdates", Map.of("checkin", "2027-03-01", "checkout", "2027-03-05"),
                "additionalneeds", "Breakfast");
    }
}
