package com.nathalyamaya.qa.api;

import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasItem;

/**
 * Full booking lifecycle: create, read, search, update, partial update, delete.
 * Steps share one booking and run in order through dependsOnMethods, so a failure
 * early in the chain skips the later steps instead of producing misleading failures.
 */
public class BookingCrudApiTest extends BaseApiTest {

    private int bookingId;
    private boolean deleted;

    @Test(groups = {"api", "smoke"}, description = "TC-API-05: create booking returns id and echoes data")
    public void createBooking() {
        Map<String, Object> booking = sampleBooking("Nathaly", "CrudTest");

        bookingId = given().spec(spec)
                .body(booking)
                .when().post("/booking")
                .then().statusCode(200)
                .body("bookingid", greaterThan(0))
                .body("booking.firstname", equalTo("Nathaly"))
                .body("booking.bookingdates.checkin", equalTo("2027-03-01"))
                .extract().path("bookingid");
    }

    @Test(groups = {"api", "smoke"}, dependsOnMethods = "createBooking",
            description = "TC-API-06: get booking matches the JSON contract")
    public void getBookingMatchesSchema() {
        given().spec(spec)
                .when().get("/booking/" + bookingId)
                .then().statusCode(200)
                .body(matchesJsonSchemaInClasspath("schemas/booking-schema.json"))
                .body("lastname", equalTo("CrudTest"));
    }

    @Test(groups = {"api", "regression"}, dependsOnMethods = "createBooking",
            description = "TC-API-07: search by name finds the new booking")
    public void searchByNameFindsBooking() {
        given().spec(spec)
                .queryParam("firstname", "Nathaly")
                .queryParam("lastname", "CrudTest")
                .when().get("/booking")
                .then().statusCode(200)
                .body("bookingid", hasItem(bookingId));
    }

    @Test(groups = {"api", "regression"}, dependsOnMethods = "getBookingMatchesSchema",
            description = "TC-API-08: full update with token replaces the booking")
    public void fullUpdateWithToken() {
        Map<String, Object> updated = new HashMap<>(sampleBooking("Nathaly", "CrudTest"));
        updated.put("totalprice", 275);
        updated.put("additionalneeds", "Late checkout");

        given().spec(spec)
                .cookie("token", authToken())
                .body(updated)
                .when().put("/booking/" + bookingId)
                .then().statusCode(200)
                .body("totalprice", equalTo(275))
                .body("additionalneeds", equalTo("Late checkout"));
    }

    @Test(groups = {"api", "regression"}, dependsOnMethods = "fullUpdateWithToken",
            description = "TC-API-09: partial update changes only the sent field")
    public void partialUpdateChangesOnlySentField() {
        given().spec(spec)
                .cookie("token", authToken())
                .body(Map.of("depositpaid", false))
                .when().patch("/booking/" + bookingId)
                .then().statusCode(200)
                .body("depositpaid", equalTo(false))
                .body("totalprice", equalTo(275));
    }

    @Test(groups = {"api", "regression"}, dependsOnMethods = "partialUpdateChangesOnlySentField",
            description = "TC-API-10: delete removes the booking")
    public void deleteRemovesBooking() {
        given().spec(spec)
                .cookie("token", authToken())
                .when().delete("/booking/" + bookingId)
                .then().statusCode(201);
        deleted = true;

        given().spec(spec)
                .when().get("/booking/" + bookingId)
                .then().statusCode(404);
    }

    @AfterClass(alwaysRun = true)
    public void cleanUp() {
        if (bookingId > 0 && !deleted) {
            deleteBooking(bookingId);
        }
    }
}
