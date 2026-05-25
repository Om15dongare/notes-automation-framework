package com.notesapp.pages;

import com.notesapp.base.BasePage;
import io.qameta.allure.Step;
import org.openqa.selenium.By;

/**
 * ProfilePage — account settings, profile update, and account deletion.
 */
public class ProfilePage extends BasePage {

    private final By profileMenu      = By.cssSelector("[data-testid='profile-menu'], a[href*='profile']");
    private final By nameField        = By.cssSelector("input[data-testid='profile-name']");
    private final By phoneField       = By.cssSelector("input[data-testid='profile-phone']");
    private final By updateBtn        = By.cssSelector("button[data-testid='profile-update']");
    private final By successAlert     = By.cssSelector(".alert-success");
    private final By deleteAccountBtn = By.cssSelector("button[data-testid='delete-account']");
    private final By confirmDeleteBtn = By.cssSelector("button[data-testid='delete-account-confirm']");
    private final By logoutBtn        = By.cssSelector("a[data-testid='logout'], button[data-testid='logout']");

    @Step("Open profile page")
    public ProfilePage open() {
        click(profileMenu);
        waitForPageLoad();
        return this;
    }

    @Step("Update profile name to: {name}")
    public ProfilePage updateName(String name) {
        clearAndType(nameField, name);
        click(updateBtn);
        return this;
    }

    public boolean isSuccessAlertDisplayed() {
        return isDisplayed(successAlert);
    }

    @Step("Logout")
    public LoginPage logout() {
        click(logoutBtn);
        waitForPageLoad();
        return new LoginPage();
    }

    @Step("Delete account from UI")
    public LoginPage deleteAccount() {
        click(deleteAccountBtn);
        click(confirmDeleteBtn);
        waitForPageLoad();
        log.info("Account deleted via UI");
        return new LoginPage();
    }
}
