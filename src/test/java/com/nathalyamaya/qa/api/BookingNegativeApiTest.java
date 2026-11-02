package com.nathalyamaya.qa.api;

import com.nathalyamaya.qa.common.TestData;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.lessThan;

/** Authorization, not-found, data-driven boundary values and invalid payloads. */
public class BookingNegativeApiTest extends BaseApiTest {

    private int existingId;
    private final List<Integer> createdIds = new ArrayList<>();

    @BeforeClass(alwaysRun = true, dependsOnMethods = "configureRestAssured")
    public void createFixtureBooking() {
        existingId = createBooking(sampleBooking("Nathaly", "NegativeTest"));
        createdIds.add(existingId);
    }

    @Test(groups = {"api", "regression"}, description = "TC-API-11: unknown id returns 404")
    public void unknownIdReturns404() {
        given().spec(spec)
                .when().get("/booking/999999999")
                .then().statusCode(404);
    }

    @Test(groups = {"api", "regression"}, description = "TC-API-12: update without a token is forbidden")
    public void updateWithoutTokenIsForbidden() {
        given().spec(spec)
                .body(sampleBooking("Hacker", "NoToken"))
                .when().put("/booking/" + existingId)
                .then().statusCode(403);
    }

    @Test(groups = {"api", "regression"}, description = "TC-API-13: delete with an invalid token is forbidden")
    public void deleteWithInvalidTokenIsForbidden() {
        given().spec(spec)
                .cookie("token", "not-a-real-token")
                .when().delete("/booking/" + existingId)
                .then().statusCode(403);
    }

    @DataProvider(name = "validBookings")
    public Object[][] validBookings() {
        return TestData.jsonList("testdata/bookings-valid.json");
    }

    /** Each row in bookings-valid.json is one test condition (boundary values, special characters). */
    @Test(groups = {"api", "regression"}, dataProvider = "validBookings",
            description = "TC-API-14: boundary and special-character bookings are stored exactly")
    @SuppressWarnings("unchecked")
    public void validBookingVariantsAreStored(Map<String, Object> row) {
        Map<String, Object> booking = (Map<String, Object>) row.get("booking");

        int id = given().spec(spec)
                .body(booking)
                .when().post("/booking")
                .then().statusCode(200)
                .body("booking.firstname", equalTo(booking.get("firstname")))
                .body("booking.totalprice", equalTo(booking.get("totalprice")))
                .extract().path("bookingid");
        createdIds.add(id);
    }

    /**
     * Expected to FAIL against the practice site: a booking missing required fields
     * returns 500 (server error) instead of 400 (client error). Documented as BUG-002.
     */
    @Test(groups = {"api", "knownDefect"},
            description = "TC-API-15: missing required fields should return 4xx, not 500 (BUG-002)")
    public void missingRequiredFieldsReturnClientError() {
        given().spec(spec)
                .body(Map.of("firstname", "OnlyAFirstName"))
                .when().post("/booking")
                .then().statusCode(allOf(greaterThanOrEqualTo(400), lessThan(500)));
    }

    @AfterClass(alwaysRun = true)
    public void cleanUp() {
        createdIds.forEach(this::deleteBooking);
    }
}
