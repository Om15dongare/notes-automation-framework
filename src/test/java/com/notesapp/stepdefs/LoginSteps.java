package com.notesapp.stepdefs;

import com.notesapp.config.ConfigReader;
import com.notesapp.pages.LoginPage;
import com.notesapp.pages.NotesPage;
import io.cucumber.java.en.*;
import org.testng.Assert;

public class LoginSteps {

    private LoginPage loginPage = new LoginPage();
    private NotesPage notesPage;

    @Given("the user is on the login page")
    public void theUserIsOnTheLoginPage() {
        loginPage.open();
    }

    @When("the user enters email {string} and password {string}")
    public void enterCredentials(String email, String password) {
        loginPage.enterEmail(email).enterPassword(password);
    }

    @When("the user clicks the Login button")
    public void clickLoginButton() {
        loginPage.clickLoginExpectingFailure();
    }

    @Then("the user should be redirected to the Notes dashboard")
    public void verifyDashboard() {
        notesPage = new NotesPage();
        Assert.assertTrue(notesPage.isOnDashboard(),
                "Expected to be on the Notes dashboard but current URL is: " + notesPage.getCurrentUrl());
    }

    @Then("the username should be visible in the header")
    public void verifyUsernameVisible() {
        Assert.assertTrue(new NotesPage().isUsernameVisible(),
                "Username should be visible in the header after login");
    }

    @Then("an error message {string} should be displayed")
    public void verifyErrorMessage(String expectedMsg) {
        Assert.assertTrue(loginPage.isErrorDisplayed(),
                "Expected an error message to be displayed");
        Assert.assertTrue(loginPage.getErrorMessage().contains(expectedMsg),
                "Expected error: '" + expectedMsg + "' but got: '" + loginPage.getErrorMessage() + "'");
    }

    @Then("the user should remain on the login page")
    public void verifyOnLoginPage() {
        Assert.assertTrue(loginPage.isOnLoginPage(),
                "User should have stayed on login page but was redirected");
    }

    @Then("a validation error should appear on the email field")
    public void verifyEmailFieldError() {
        Assert.assertTrue(loginPage.isEmailFieldErrorDisplayed() || loginPage.isErrorDisplayed(),
                "Email field validation error should be displayed");
    }

    @Given("the user is logged in as {string} with password {string}")
    public void loginAs(String email, String password) {
        notesPage = loginPage.loginAs(email, password);
    }

    @Given("the user is logged in via UI")
    public void loginWithDefaults() {
        notesPage = loginPage.loginAs(ConfigReader.getUserEmail(), ConfigReader.getUserPassword());
    }
}
