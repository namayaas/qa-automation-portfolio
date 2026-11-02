package com.nathalyamaya.qa.ui;

import com.nathalyamaya.qa.common.Config;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Gives every UI test a fresh, isolated Chrome session and cleans it up afterwards.
 * Selenium Manager (built into Selenium 4) downloads the matching chromedriver, so
 * no driver binary has to be installed by hand.
 */
public abstract class BaseUiTest {

    private static final Logger LOG = Logger.getLogger(BaseUiTest.class.getName());
    private static final Path SCREENSHOT_DIR = Path.of("target", "screenshots");

    protected WebDriver driver;

    @BeforeMethod(alwaysRun = true)
    public void startBrowser() {
        ChromeOptions options = new ChromeOptions();
        if (Config.headless()) {
            options.addArguments("--headless=new");
        }
        options.addArguments("--window-size=1366,900", "--disable-notifications");
        // Chrome's "change your password" leak-check dialog blocks the page on practice sites.
        options.setExperimentalOption("prefs", Map.of(
                "credentials_enable_service", false,
                "profile.password_manager_enabled", false,
                "profile.password_manager_leak_detection", false));
        driver = new ChromeDriver(options);
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(30));
    }

    @AfterMethod(alwaysRun = true)
    public void stopBrowser(ITestResult result) {
        if (driver == null) {
            return;
        }
        try {
            if (result.getStatus() == ITestResult.FAILURE) {
                saveScreenshot(result);
            }
        } finally {
            driver.quit();
            driver = null;
        }
    }

    private void saveScreenshot(ITestResult result) {
        try {
            Files.createDirectories(SCREENSHOT_DIR);
            byte[] png = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
            Path file = SCREENSHOT_DIR.resolve(result.getMethod().getMethodName()
                    + "-" + System.currentTimeMillis() + ".png");
            Files.write(file, png);
            LOG.info(() -> "Saved failure screenshot: " + file.toAbsolutePath());
        } catch (IOException | RuntimeException e) {
            LOG.warning(() -> "Could not save screenshot: " + e);
        }
    }
}
