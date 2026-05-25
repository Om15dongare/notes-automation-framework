package com.notesapp.base;

import com.notesapp.config.ConfigReader;
import com.notesapp.drivers.DriverManager;
import com.notesapp.utils.AllureUtils;
import com.notesapp.utils.ScreenshotUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;

import java.time.Duration;

/**
 * BaseTest — parent for all TestNG test classes.
 * Handles driver lifecycle, Allure attachment on failure, and cleanup.
 */
public abstract class BaseTest {

    protected final Logger log = LogManager.getLogger(getClass());

    @BeforeMethod(alwaysRun = true)
    @Parameters({"browser", "headless"})
    public void setUp(@Optional("chrome") String browser, @Optional("false") String headless) {
        log.info("====== Setting up driver: browser={}, headless={} ======", browser, headless);
        DriverManager.setDriver(browser, Boolean.parseBoolean(headless));
        WebDriver driver = DriverManager.getDriver();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(ConfigReader.getImplicitWait()));
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(ConfigReader.getPageLoadTimeout()));
        try {
            driver.manage().window().maximize();
        } catch (Exception e) {
            log.warn("Window maximize skipped (renderer not ready): {}", e.getMessage());
        }
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown(ITestResult result) {
        if (result.getStatus() == ITestResult.FAILURE) {
            log.error("TEST FAILED: {}", result.getName());
            if (ConfigReader.screenshotOnFailure()) {
                byte[] screenshot = ScreenshotUtils.captureScreenshot();
                AllureUtils.attachScreenshot("Failure Screenshot — " + result.getName(), screenshot);
            }
        } else if (result.getStatus() == ITestResult.SUCCESS) {
            log.info("TEST PASSED: {}", result.getName());
        } else {
            log.warn("TEST SKIPPED: {}", result.getName());
        }
        DriverManager.quitDriver();
        log.info("====== Driver quit — thread cleaned up ======");
    }

    protected WebDriver getDriver() {
        return DriverManager.getDriver();
    }
}
