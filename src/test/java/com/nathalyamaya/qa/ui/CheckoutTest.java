package com.nathalyamaya.qa.ui;

import com.nathalyamaya.qa.ui.pages.CartPage;
import com.nathalyamaya.qa.ui.pages.CheckoutPage;
import com.nathalyamaya.qa.ui.pages.LoginPage;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** End-to-end purchase flow. Test case IDs map to TEST_PLAN.md section 6. */
public class CheckoutTest extends BaseUiTest {

    private static final String PRODUCT = "Sauce Labs Backpack";

    private CartPage cartWithOneItem() {
        return new LoginPage(driver).open()
                .loginAs("standard_user", "secret_sauce")
                .addToCart(PRODUCT)
                .openCart();
    }

    @Test(groups = {"ui", "smoke"}, description = "TC-UI-11: user can buy an item end to end")
    public void userCanCompletePurchase() {
        CartPage cart = cartWithOneItem();
        assertThat(cart.itemNames()).containsExactly(PRODUCT);

        String header = cart.checkout()
                .enterCustomerInfo("Test", "User", "22041")
                .finish()
                .confirmationHeader();

        assertThat(header).isEqualTo("Thank you for your order!");
    }

    @Test(groups = {"ui", "regression"}, description = "TC-UI-12: checkout requires a postal code")
    public void checkoutRequiresPostalCode() {
        CheckoutPage checkout = cartWithOneItem().checkout().enterCustomerInfo("Test", "User", "");

        assertThat(checkout.errorMessage()).isEqualTo("Error: Postal Code is required");
    }
}
