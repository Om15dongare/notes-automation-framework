package com.notesapp.base;

import com.notesapp.drivers.DriverManager;
import com.notesapp.utils.WaitUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.Select;

/**
 * BasePage — all page objects extend this.
 * Provides thread-safe driver access and reusable interaction methods.
 * No PageFactory used — fully custom locator methods for reliability.
 */
public abstract class BasePage {

    protected final Logger log = LogManager.getLogger(getClass());

    protected WebDriver getDriver() {
        return DriverManager.getDriver();
    }

    // ===== Core element actions =====

    protected WebElement find(By locator) {
        return WaitUtils.waitForVisible(locator);
    }

    protected void click(By locator) {
        log.debug("Clicking: {}", locator);
        try {
            WaitUtils.waitForClickable(locator).click();
        } catch (ElementClickInterceptedException e) {
            log.warn("Standard click intercepted — retrying with JS: {}", locator);
            jsClick(locator);
        } catch (StaleElementReferenceException e) {
            log.warn("Stale element on click — retrying: {}", locator);
            sleepMs(500);
            WaitUtils.waitForClickable(locator).click();
        }
    }

    protected void type(By locator, String text) {
        log.debug("Typing '{}' into: {}", text, locator);
        WebElement el = find(locator);
        el.clear();
        el.sendKeys(text);
    }

    public void clearAndType(By locator, String text) {
        WebElement el = find(locator);
        el.sendKeys(Keys.chord(Keys.CONTROL, "a"));
        el.sendKeys(Keys.DELETE);
        el.sendKeys(text);
    }

    protected String getText(By locator) {
        return find(locator).getText().trim();
    }

    protected boolean isDisplayed(By locator) {
        try {
            return WaitUtils.waitForVisible(locator).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    protected boolean isAbsent(By locator) {
        return WaitUtils.waitForInvisibility(locator);
    }

    protected void selectByVisibleText(By locator, String text) {
        new Select(find(locator)).selectByVisibleText(text);
    }

    // ===== JavaScript helpers =====

    protected void jsClick(By locator) {
        JavascriptExecutor js = (JavascriptExecutor) getDriver();
        js.executeScript("arguments[0].click();", find(locator));
    }

    protected void jsScrollIntoView(By locator) {
        JavascriptExecutor js = (JavascriptExecutor) getDriver();
        js.executeScript("arguments[0].scrollIntoView({block:'center'});", find(locator));
    }

    protected void waitForPageLoad() {
        WaitUtils.waitForPageLoad();
    }

    // ===== Navigation =====

    protected void navigateTo(String url) {
        log.info("Navigating to: {}", url);
        getDriver().get(url);
        waitForPageLoad();
    }

    public String getCurrentUrl() {
        return getDriver().getCurrentUrl();
    }

    protected String getTitle() {
        return getDriver().getTitle();
    }

    // ===== Actions helper =====

    protected void hoverAndClick(By locator) {
        Actions actions = new Actions(getDriver());
        actions.moveToElement(find(locator)).click().perform();
    }

    public void sleepMs(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) { Thread.currentThread().interrupt(); }
    }
}
