package com.notesapp.utils;

import com.notesapp.config.ConfigReader;
import com.notesapp.drivers.DriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * WaitUtils — centralised explicit wait strategies.
 * Uses FluentWait for polling and WebDriverWait for standard conditions.
 */
public class WaitUtils {

    private WaitUtils() {}

    private static WebDriver driver() {
        return DriverManager.getDriver();
    }

    private static WebDriverWait driverWait() {
        return new WebDriverWait(driver(), Duration.ofSeconds(ConfigReader.getExplicitWait()));
    }

    public static WebElement waitForVisible(By locator) {
        return driverWait().until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    public static WebElement waitForClickable(By locator) {
        return driverWait().until(ExpectedConditions.elementToBeClickable(locator));
    }

    public static boolean waitForInvisibility(By locator) {
        try {
            return driverWait().until(ExpectedConditions.invisibilityOfElementLocated(locator));
        } catch (Exception e) {
            return false;
        }
    }

    public static void waitForPageLoad() {
        driverWait().until(driver ->
                ((JavascriptExecutor) driver)
                        .executeScript("return document.readyState")
                        .equals("complete"));
    }

    /**
     * FluentWait with custom polling — useful for slow dynamic elements.
     */
    public static WebElement fluentWait(By locator, int timeoutSec, int pollMs) {
        return new FluentWait<>(driver())
                .withTimeout(Duration.ofSeconds(timeoutSec))
                .pollingEvery(Duration.ofMillis(pollMs))
                .ignoring(NoSuchElementException.class)
                .until(ExpectedConditions.visibilityOfElementLocated(locator));
    }
}
