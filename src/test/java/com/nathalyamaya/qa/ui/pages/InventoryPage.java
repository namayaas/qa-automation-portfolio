package com.nathalyamaya.qa.ui.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

import java.math.BigDecimal;
import java.util.List;

/** The product list shown after a successful login. */
public class InventoryPage extends BasePage {

    private static final By TITLE = dataTest("title");
    private static final By ITEM = dataTest("inventory-item");
    private static final By ITEM_NAME = dataTest("inventory-item-name");
    private static final By ITEM_PRICE = dataTest("inventory-item-price");
    private static final By SORT = dataTest("product-sort-container");
    private static final By CART_BADGE = dataTest("shopping-cart-badge");
    private static final By CART_LINK = dataTest("shopping-cart-link");

    /** Values of the sort dropdown's options. */
    public enum Sort {
        NAME_A_TO_Z("az"), NAME_Z_TO_A("za"), PRICE_LOW_TO_HIGH("lohi"), PRICE_HIGH_TO_LOW("hilo");

        private final String value;

        Sort(String value) {
            this.value = value;
        }
    }

    public InventoryPage(WebDriver driver) {
        super(driver);
    }

    public boolean isLoaded() {
        return "Products".equals(visible(TITLE).getText());
    }

    public List<String> itemNames() {
        visible(ITEM_NAME);
        return driver.findElements(ITEM_NAME).stream().map(WebElement::getText).toList();
    }

    public List<BigDecimal> itemPrices() {
        visible(ITEM_PRICE);
        return driver.findElements(ITEM_PRICE).stream()
                .map(e -> new BigDecimal(e.getText().replace("$", "").trim()))
                .toList();
    }

    public InventoryPage sortBy(Sort sort) {
        new Select(visible(SORT)).selectByValue(sort.value);
        return this;
    }

    public InventoryPage addToCart(String productName) {
        itemCard(productName).findElement(By.cssSelector("button[data-test^='add-to-cart']")).click();
        return this;
    }

    public InventoryPage removeFromCart(String productName) {
        itemCard(productName).findElement(By.cssSelector("button[data-test^='remove']")).click();
        return this;
    }

    /** Number on the cart icon; 0 when the badge is hidden (empty cart). */
    public int cartCount() {
        if (!isPresent(CART_BADGE)) {
            return 0;
        }
        return Integer.parseInt(driver.findElement(CART_BADGE).getText());
    }

    public CartPage openCart() {
        click(CART_LINK);
        return new CartPage(driver);
    }

    private WebElement itemCard(String productName) {
        visible(ITEM);
        return driver.findElements(ITEM).stream()
                .filter(card -> card.findElement(ITEM_NAME).getText().equals(productName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No product named: " + productName));
    }
}
