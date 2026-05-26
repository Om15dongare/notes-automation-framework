package com.notesapp.pages;

import com.notesapp.base.BasePage;
import com.notesapp.config.ConfigReader;
import io.qameta.allure.Step;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * LoginPage — covers the login screen at /notes/app/login
 * Selectors verified against the live app DOM.
 *
 * This is a React SPA — login uses pushState navigation (no full page reload).
 * Waits must be condition-based, not waitForPageLoad()-based.
 */
public class LoginPage extends BasePage {

    // ===== Locators (verified against live app) =====
    private final By emailField    = By.id("email");
    private final By passwordField = By.id("password");
    private final By loginButton   = By.cssSelector("button.btn-primary");
    private final By errorMessage  = By.cssSelector("div.alert.alert-danger, [data-testid='alert-message']");

    // ===== Actions =====

    @Step("Navigate to the login page")
    public LoginPage open() {
        navigateTo(ConfigReader.getBaseUrl() + "/login");
        // Wait for the form to be ready
        find(emailField);
        log.info("Opened login page");
        return this;
    }

    @Step("Enter email: {email}")
    public LoginPage enterEmail(String email) {
        type(emailField, email);
        return this;
    }

    @Step("Enter password")
    public LoginPage enterPassword(String password) {
        type(passwordField, password);
        return this;
    }

    @Step("Click the Login button (expect success — wait for URL to change)")
    public NotesPage clickLogin() {
        click(loginButton);
        // SPA navigation: wait for URL to leave /login
        try {
            new WebDriverWait(getDriver(), Duration.ofSeconds(10))
                    .until(d -> !d.getCurrentUrl().contains("/login"));
        } catch (Exception e) {
            log.warn("URL did not leave /login within 10s — current URL: {}", getCurrentUrl());
        }
        sleepMs(500); // brief settle for dashboard elements
        return new NotesPage();
    }

    @Step("Submit login and expect failure — wait for error alert")
    public LoginPage clickLoginExpectingFailure() {
        click(loginButton);
        // For a failed login the URL stays on /login and an error alert appears
        // Wait up to 8s for the error div to appear (API round-trip)
        try {
            new WebDriverWait(getDriver(), Duration.ofSeconds(8))
                    .until(ExpectedConditions.visibilityOfElementLocated(errorMessage));
        } catch (Exception e) {
            log.warn("Error alert did not appear within 8s after failed login");
        }
        return this;
    }

    @Step("Click Login and wait for dynamic transition (either success or failure)")
    public LoginPage clickLoginDynamic() {
        click(loginButton);
        try {
            new WebDriverWait(getDriver(), Duration.ofSeconds(8))
                    .until(d -> !d.getCurrentUrl().contains("/login") || 
                               d.findElements(errorMessage).size() > 0 && d.findElement(errorMessage).isDisplayed());
        } catch (Exception e) {
            log.warn("Login transition did not complete within 8s");
        }
        return this;
    }

    @Step("Login with credentials: {email}")
    public NotesPage loginAs(String email, String password) {
        open();
        enterEmail(email);
        enterPassword(password);
        return clickLogin();
    }

    // ===== Assertion helpers =====

    public boolean isErrorDisplayed() {
        // No timeout — the clickLoginExpectingFailure already waited for the alert
        try {
            return getDriver().findElement(errorMessage).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public String getErrorMessage() {
        return getText(errorMessage);
    }

    public boolean isEmailFieldErrorDisplayed() {
        try {
            WebElement emailEl = getDriver().findElement(emailField);
            JavascriptExecutor js = (JavascriptExecutor) getDriver();
            boolean isHtml5Invalid = (Boolean) js.executeScript("return !arguments[0].validity.valid;", emailEl);
            return isHtml5Invalid || isErrorDisplayed();
        } catch (Exception e) {
            return isErrorDisplayed();
        }
    }

    public boolean isOnLoginPage() {
        return getCurrentUrl().contains("/login");
    }
}
