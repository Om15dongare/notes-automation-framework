package com.notesapp.pages;

import com.notesapp.base.BasePage;
import com.notesapp.config.ConfigReader;
import io.qameta.allure.Step;
import org.openqa.selenium.By;

/**
 * RegisterPage — handles new user registration at /notes/app/register
 */
public class RegisterPage extends BasePage {

    private final By nameField       = By.cssSelector("input[data-testid='register-name']");
    private final By emailField      = By.cssSelector("input[data-testid='register-email']");
    private final By passwordField   = By.cssSelector("input[data-testid='register-password']");
    private final By confirmPassword = By.cssSelector("input[data-testid='register-confirmPassword']");
    private final By registerButton  = By.cssSelector("button[data-testid='register-submit']");
    private final By successMessage  = By.cssSelector("div.alert-success, .success-message");
    private final By errorMessage    = By.cssSelector("div.alert-danger, .error-message");

    @Step("Navigate to Register page")
    public RegisterPage open() {
        navigateTo(ConfigReader.getBaseUrl() + "/register");
        return this;
    }

    @Step("Register user: {name} / {email}")
    public LoginPage register(String name, String email, String password) {
        type(nameField, name);
        type(emailField, email);
        type(passwordField, password);
        type(confirmPassword, password);
        click(registerButton);
        waitForPageLoad();
        return new LoginPage();
    }

    public boolean isSuccessMessageDisplayed() {
        return isDisplayed(successMessage);
    }

    public boolean isErrorDisplayed() {
        return isDisplayed(errorMessage);
    }

    public String getErrorMessage() {
        return getText(errorMessage);
    }
}
