package com.notesapp.cucumber;

import com.notesapp.config.ConfigReader;
import com.notesapp.drivers.DriverManager;
import com.notesapp.utils.AllureUtils;
import com.notesapp.utils.ScreenshotUtils;
import io.cucumber.java.After;
import io.cucumber.java.AfterStep;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;

import java.time.Duration;

/**
 * Cucumber Hooks — mirrors what BaseTest does for TestNG scenarios.
 *
 * Lifecycle:
 *   @Before  → initialise WebDriver (reads browser/headless from config.properties)
 *   @After   → quit driver; attach screenshot to Allure if scenario failed
 *
 * NOTE: No @Parameters here — Cucumber does not support TestNG @Parameters.
 *       Browser and headless mode are read from config.properties (or overridden
 *       by the -Dheadless=true / -Dbrowser=chrome system properties set in Jenkins).
 */
public class Hooks {

    private static final Logger log = LogManager.getLogger(Hooks.class);
    private static boolean notesCleaned = false;

    /**
     * @Before runs before every Cucumber scenario.
     * Order = 1 so it executes before any step-definition @Before hooks.
     */
    @Before(order = 1)
    public void setUp(Scenario scenario) {
        if (!notesCleaned) {
            try {
                log.info("[Cucumber] Running one-time note cleanup before first scenario...");
                String token = com.notesapp.api.AuthApi.loginDefault();
                com.notesapp.api.NotesApi.deleteAllNotes(token);
                notesCleaned = true;
                log.info("[Cucumber] One-time note cleanup completed successfully.");
            } catch (Exception e) {
                log.error("[Cucumber] One-time note cleanup failed: {}", e.getMessage(), e);
            }
        }

        // Priority order:
        //   1. -Dheadless=true / -Dbrowser=chrome passed on Maven CLI (Jenkins)
        //   2. config.properties values (headless=false → visible browser locally)
        String browser = System.getProperty("browser", ConfigReader.getBrowser());

        // Explicitly check for the system property; do NOT use String default trick
        // because Boolean.parseBoolean("false") and ConfigReader both need to be checked.
        String headlessProp = System.getProperty("headless");
        boolean headless = (headlessProp != null)
                ? Boolean.parseBoolean(headlessProp)
                : ConfigReader.isHeadless();   // config.properties → false by default

        log.info("====== [Cucumber] Starting scenario: '{}' | browser={}, headless={} ======",
                scenario.getName(), browser, headless);

        DriverManager.setDriver(browser, headless);

        WebDriver driver = DriverManager.getDriver();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(ConfigReader.getImplicitWait()));
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(ConfigReader.getPageLoadTimeout()));

        // Maximise window (Chrome headless handles this automatically)
        try {
            driver.manage().window().maximize();
        } catch (Exception e) {
            log.warn("Window maximize skipped: {}", e.getMessage());
        }
    }

    /**
     * @After runs after every Cucumber scenario.
     * Order = 1 so screenshots are attached before driver quits.
     */
    @After(order = 1)
    public void tearDown(Scenario scenario) {
        if (scenario.isFailed()) {
            log.error("[Cucumber] SCENARIO FAILED: {}", scenario.getName());

            // 1. Attach screenshot to Allure report
            if (ConfigReader.screenshotOnFailure()) {
                byte[] screenshot = ScreenshotUtils.captureScreenshot();
                AllureUtils.attachScreenshot("Failure Screenshot — " + scenario.getName(), screenshot);
            }

            // 2. Also embed screenshot directly into Cucumber HTML report
            try {
                if (DriverManager.getDriver() != null) {
                    byte[] screenshot = ScreenshotUtils.captureScreenshot();
                    if (screenshot != null && screenshot.length > 0) {
                        scenario.attach(screenshot, "image/png", "Screenshot on Failure");
                    }
                }
            } catch (Exception e) {
                log.warn("Could not embed screenshot in Cucumber report: {}", e.getMessage());
            }
        } else {
            log.info("[Cucumber] SCENARIO PASSED: {}", scenario.getName());
        }

        DriverManager.quitDriver();
        log.info("====== [Cucumber] Driver quit — scenario cleaned up ======");
    }

    /**
     * Optional: log each step result for debugging.
     * Comment this out if you want less noise in CI logs.
     */
    @AfterStep
    public void afterStep(Scenario scenario) {
        if (scenario.isFailed()) {
            log.debug("[Cucumber] Step failed in scenario: {}", scenario.getName());
        }
    }
}
