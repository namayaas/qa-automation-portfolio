package com.nathalyamaya.qa.ui.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/** Checkout: customer info step, overview step and the confirmation screen. */
public class CheckoutPage extends BasePage {

    private static final By FIRST_NAME = dataTest("firstName");
    private static final By LAST_NAME = dataTest("lastName");
    private static final By POSTAL_CODE = dataTest("postalCode");
    private static final By CONTINUE = dataTest("continue");
    private static final By FINISH = dataTest("finish");
    private static final By ERROR = dataTest("error");
    private static final By COMPLETE_HEADER = dataTest("complete-header");

    public CheckoutPage(WebDriver driver) {
        super(driver);
    }

    public CheckoutPage enterCustomerInfo(String firstName, String lastName, String postalCode) {
        type(FIRST_NAME, firstName);
        type(LAST_NAME, lastName);
        type(POSTAL_CODE, postalCode);
        click(CONTINUE);
        return this;
    }

    public CheckoutPage finish() {
        click(FINISH);
        return this;
    }

    public String confirmationHeader() {
        return visible(COMPLETE_HEADER).getText();
    }

    public String errorMessage() {
        return visible(ERROR).getText();
    }
}
