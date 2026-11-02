package com.nathalyamaya.qa.ui;

import com.nathalyamaya.qa.ui.pages.InventoryPage;
import com.nathalyamaya.qa.ui.pages.InventoryPage.Sort;
import com.nathalyamaya.qa.ui.pages.LoginPage;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** Product list sorting and cart badge. Test case IDs map to TEST_PLAN.md section 6. */
public class InventoryTest extends BaseUiTest {

    private InventoryPage inventory;

    @BeforeMethod(alwaysRun = true, dependsOnMethods = "startBrowser")
    public void logIn() {
        inventory = new LoginPage(driver).open().loginAs("standard_user", "secret_sauce");
        assertThat(inventory.isLoaded()).isTrue();
    }

    @Test(groups = {"ui", "regression"}, description = "TC-UI-07: sort by price low to high")
    public void sortByPriceLowToHigh() {
        List<BigDecimal> prices = inventory.sortBy(Sort.PRICE_LOW_TO_HIGH).itemPrices();

        assertThat(prices).isNotEmpty().isSortedAccordingTo(Comparator.naturalOrder());
    }

    @Test(groups = {"ui", "regression"}, description = "TC-UI-08: sort by price high to low")
    public void sortByPriceHighToLow() {
        List<BigDecimal> prices = inventory.sortBy(Sort.PRICE_HIGH_TO_LOW).itemPrices();

        assertThat(prices).isNotEmpty().isSortedAccordingTo(Comparator.reverseOrder());
    }

    @Test(groups = {"ui", "regression"}, description = "TC-UI-09: sort by name Z to A")
    public void sortByNameZToA() {
        List<String> names = inventory.sortBy(Sort.NAME_Z_TO_A).itemNames();

        assertThat(names).isNotEmpty().isSortedAccordingTo(Comparator.reverseOrder());
    }

    @Test(groups = {"ui", "smoke"}, description = "TC-UI-10: cart badge tracks add and remove")
    public void cartBadgeTracksAddAndRemove() {
        assertThat(inventory.cartCount()).as("empty cart at start").isZero();

        inventory.addToCart("Sauce Labs Backpack").addToCart("Sauce Labs Bike Light");
        assertThat(inventory.cartCount()).as("after adding two").isEqualTo(2);

        inventory.removeFromCart("Sauce Labs Backpack");
        assertThat(inventory.cartCount()).as("after removing one").isEqualTo(1);
    }
}
