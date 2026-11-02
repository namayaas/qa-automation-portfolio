package com.nathalyamaya.qa.ui.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.List;

/** The shopping cart. */
public class CartPage extends BasePage {

    private static final By TITLE = dataTest("title");
    private static final By ITEM_NAME = dataTest("inventory-item-name");
    private static final By CHECKOUT = dataTest("checkout");

    public CartPage(WebDriver driver) {
        super(driver);
        wait.until(ExpectedConditions.textToBe(TITLE, "Your Cart"));
    }

    public List<String> itemNames() {
        return driver.findElements(ITEM_NAME).stream().map(WebElement::getText).toList();
    }

    public CheckoutPage checkout() {
        click(CHECKOUT);
        return new CheckoutPage(driver);
    }
}
