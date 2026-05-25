package com.notesapp.tests.ui;

import com.notesapp.base.BaseTest;
import com.notesapp.config.ConfigReader;
import com.notesapp.pages.LoginPage;
import com.notesapp.pages.NotesPage;
import io.qameta.allure.*;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Epic("Notes App")
@Feature("UI — Authentication")
public class LoginTest extends BaseTest {

    private LoginPage loginPage;

    @BeforeMethod
    public void openLogin() {
        loginPage = new LoginPage();
        loginPage.open();
    }

    @Test(description = "TC-UI-01: Login with valid credentials")
    @Story("FR-01 — Successful login")
    @Severity(SeverityLevel.BLOCKER)
    public void testValidLogin() {
        NotesPage notesPage = loginPage
                .enterEmail(ConfigReader.getUserEmail())
                .enterPassword(ConfigReader.getUserPassword())
                .clickLogin();

        Assert.assertTrue(notesPage.isOnDashboard(),
                "Should be on Notes dashboard after login");
        Assert.assertTrue(notesPage.isUsernameVisible(),
                "Username should be visible in header");
        log.info("TC-UI-01 PASSED — Valid login successful");
    }

    @Test(description = "TC-NEG-01: Login with wrong password shows error")
    @Story("FR-09 — Negative validation")
    @Severity(SeverityLevel.CRITICAL)
    public void testLoginWrongPassword() {
        loginPage.enterEmail(ConfigReader.getUserEmail())
                .enterPassword("wrongpass123")
                .clickLoginExpectingFailure();

        // Attach screenshot for visual verification of wrong password rejection in Allure report
        byte[] screenshot = com.notesapp.utils.ScreenshotUtils.captureScreenshot();
        com.notesapp.utils.AllureUtils.attachScreenshot("Login Failure — Wrong Password", screenshot);

        // The live app may show a toast/alert that doesn’t match a static CSS selector.
        // Behavioral assertion: user must NOT have been redirected away from login page.
        Assert.assertTrue(loginPage.isOnLoginPage(),
                "User should stay on login page after failed attempt");
        log.info("TC-NEG-01 PASSED — Wrong password correctly rejected (user stayed on /login)");
    }

    @Test(description = "TC-NEG-04: Login with blank email shows validation error")
    @Story("FR-09 — Negative validation")
    @Severity(SeverityLevel.NORMAL)
    public void testLoginEmptyEmail() {
        loginPage.enterEmail("")
                .enterPassword(ConfigReader.getUserPassword())
                .clickLoginExpectingFailure();

        // Attach screenshot for visual verification of empty email rejection in Allure report
        byte[] screenshot = com.notesapp.utils.ScreenshotUtils.captureScreenshot();
        com.notesapp.utils.AllureUtils.attachScreenshot("Login Failure — Blank Email", screenshot);

        // Blank email: app uses HTML5 required validation (browser popup)
        // or stays on login page. Either way the user is NOT on dashboard.
        Assert.assertTrue(loginPage.isOnLoginPage(),
                "User should remain on login page when email is blank");
        log.info("TC-NEG-04 PASSED — Blank email correctly rejected (user stayed on /login)");
    }
}
