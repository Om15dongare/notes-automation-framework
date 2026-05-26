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
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;

import java.time.Duration;

/**
 * BaseTest — parent for all TestNG test classes.
 * Handles driver lifecycle, Allure attachment on failure, and cleanup.
 */
public abstract class BaseTest {

    protected final Logger log = LogManager.getLogger(getClass());

    @BeforeSuite(alwaysRun = true)
    public void cleanUpExistingNotes() {
        try {
            log.info("Suite setup: Cleaning up all existing notes for the test user...");
            String token = com.notesapp.api.AuthApi.loginDefault();
            com.notesapp.api.NotesApi.deleteAllNotes(token);
            log.info("Suite setup: Note cleanup finished successfully.");
        } catch (Exception e) {
            log.error("Suite setup: Note cleanup failed: {}", e.getMessage(), e);
        }
    }

    @BeforeMethod(alwaysRun = true)
    @Parameters({"browser", "headless"})
    public void setUp(@Optional("chrome") String browser, @Optional("false") String headless) {
        // Priority order: CLI parameters (like -Dheadless=true from Jenkins) take absolute precedence
        // over the XML parameters.
        String targetBrowser = System.getProperty("browser", browser);
        String headlessProp = System.getProperty("headless");
        boolean targetHeadless = (headlessProp != null)
                ? Boolean.parseBoolean(headlessProp)
                : Boolean.parseBoolean(headless);

        log.info("====== Setting up driver: browser={}, headless={} ======", targetBrowser, targetHeadless);
        DriverManager.setDriver(targetBrowser, targetHeadless);
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
