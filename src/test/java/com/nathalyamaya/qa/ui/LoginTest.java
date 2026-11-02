package com.nathalyamaya.qa.ui;

import com.nathalyamaya.qa.common.TestData;
import com.nathalyamaya.qa.ui.pages.InventoryPage;
import com.nathalyamaya.qa.ui.pages.LoginPage;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** Login feature. Test case IDs map to TEST_PLAN.md section 6. */
public class LoginTest extends BaseUiTest {

    private static final String PASSWORD = "secret_sauce";

    @Test(groups = {"ui", "smoke"}, description = "TC-UI-01: valid user lands on the product list")
    public void validUserCanLogIn() {
        InventoryPage inventory = new LoginPage(driver).open().loginAs("standard_user", PASSWORD);

        assertThat(inventory.isLoaded()).as("Products page shown").isTrue();
        assertThat(driver.getCurrentUrl()).endsWith("/inventory.html");
    }

    @DataProvider(name = "invalidLogins")
    public Object[][] invalidLogins() {
        return TestData.table("testdata/login-negative.psv");
    }

    @Test(groups = {"ui", "regression"}, dataProvider = "invalidLogins",
            description = "TC-UI-02..06: invalid, blank and locked-out logins show the correct error")
    public void invalidLoginShowsError(String caseId, String username, String password, String expectedError) {
        LoginPage login = new LoginPage(driver).open();
        login.loginAs(username, password);

        assertThat(login.errorMessage()).as(caseId).isEqualTo(expectedError);
        assertThat(driver.getCurrentUrl()).as(caseId + " stays on login page").doesNotContain("inventory");
    }
}
