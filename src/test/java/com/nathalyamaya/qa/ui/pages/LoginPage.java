package com.nathalyamaya.qa.ui.pages;

import com.nathalyamaya.qa.common.Config;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/** The Sauce Demo login screen. */
public class LoginPage extends BasePage {

    private static final By USERNAME = dataTest("username");
    private static final By PASSWORD = dataTest("password");
    private static final By LOGIN_BUTTON = dataTest("login-button");
    private static final By ERROR = dataTest("error");

    public LoginPage(WebDriver driver) {
        super(driver);
    }

    public LoginPage open() {
        driver.get(Config.uiBaseUrl());
        visible(USERNAME);
        return this;
    }

    /** Submits credentials. Returns the inventory page; callers on the error path use {@link #errorMessage()}. */
    public InventoryPage loginAs(String username, String password) {
        type(USERNAME, username);
        type(PASSWORD, password);
        click(LOGIN_BUTTON);
        return new InventoryPage(driver);
    }

    public String errorMessage() {
        return visible(ERROR).getText();
    }
}
