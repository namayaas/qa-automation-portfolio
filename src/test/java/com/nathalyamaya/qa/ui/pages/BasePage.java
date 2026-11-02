package com.nathalyamaya.qa.ui.pages;

import com.nathalyamaya.qa.common.Config;
import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * Shared helpers for page objects. Every interaction waits explicitly for the
 * element's state instead of using Thread.sleep, which keeps tests fast and stable.
 */
public abstract class BasePage {

    protected final WebDriver driver;
    protected final WebDriverWait wait;

    protected BasePage(WebDriver driver) {
        this.driver = driver;
        // React re-renders can swap an element out mid-check; re-poll instead of failing.
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(Config.waitSeconds()));
        this.wait.ignoring(StaleElementReferenceException.class);
    }

    protected WebElement visible(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    protected void click(By locator) {
        wait.until(ExpectedConditions.elementToBeClickable(locator)).click();
    }

    protected void type(By locator, String text) {
        WebElement field = visible(locator);
        field.clear();
        field.sendKeys(text);
    }

    protected boolean isPresent(By locator) {
        return !driver.findElements(locator).isEmpty();
    }

    /** Builds a locator from the site's data-test attributes, which are stable test hooks. */
    protected static By dataTest(String value) {
        return By.cssSelector("[data-test='" + value + "']");
    }
}
