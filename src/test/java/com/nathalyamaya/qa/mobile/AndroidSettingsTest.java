package com.nathalyamaya.qa.mobile;

import com.nathalyamaya.qa.common.Config;
import io.appium.java_client.AppiumBy;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.options.UiAutomator2Options;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.SkipException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Mobile UI tests on an Android emulator or a USB device (Fire OS devices are Android too).
 * Uses the built-in Settings app so no APK is needed.
 *
 * Needs: an emulator or device visible to `adb devices`, and an Appium 2 server with the
 * UiAutomator2 driver running at appium.url (default http://127.0.0.1:4723).
 * When the server isn't reachable the tests are SKIPPED, not failed, so CI stays green.
 */
public class AndroidSettingsTest {

    private static final String SETTINGS_PACKAGE = "com.android.settings";
    private AndroidDriver driver;

    @BeforeMethod(alwaysRun = true)
    public void startSession() throws IOException {
        URL server = URI.create(Config.appiumUrl()).toURL();
        if (!appiumIsUp(server)) {
            throw new SkipException("Appium server not reachable at " + server + "; see README 'Mobile tests'");
        }
        UiAutomator2Options options = new UiAutomator2Options()
                .setAppPackage(SETTINGS_PACKAGE)
                .setAppActivity(".Settings")
                .setNoReset(true)
                .setNewCommandTimeout(Duration.ofSeconds(60));
        String udid = System.getProperty("android.udid");
        if (udid != null && !udid.isBlank()) {
            options.setUdid(udid);
        }
        driver = new AndroidDriver(server, options);
    }

    @AfterMethod(alwaysRun = true)
    public void endSession() {
        if (driver != null) {
            driver.quit();
            driver = null;
        }
    }

    @Test(groups = {"mobile"}, description = "TC-MOB-01: Settings app launches in the foreground")
    public void settingsAppLaunches() {
        assertThat(driver.getCurrentPackage()).isEqualTo(SETTINGS_PACKAGE);
    }

    @Test(groups = {"mobile"}, description = "TC-MOB-02: user can scroll to and open the About screen")
    public void canOpenAboutScreen() {
        WebElement about = driver.findElement(AppiumBy.androidUIAutomator(
                "new UiScrollable(new UiSelector().scrollable(true))"
                        + ".scrollIntoView(new UiSelector().textContains(\"About\"))"));
        about.click();

        WebElement heading = new WebDriverWait(driver, Duration.ofSeconds(Config.waitSeconds()))
                .until(ExpectedConditions.presenceOfElementLocated(
                        AppiumBy.androidUIAutomator("new UiSelector().textContains(\"Android version\")")));
        assertThat(heading.isDisplayed()).isTrue();
    }

    private static boolean appiumIsUp(URL server) {
        try {
            HttpURLConnection conn = (HttpURLConnection) URI.create(server + "/status").toURL().openConnection();
            conn.setConnectTimeout(2000);
            conn.setReadTimeout(2000);
            return conn.getResponseCode() == 200;
        } catch (IOException e) {
            return false;
        }
    }
}
