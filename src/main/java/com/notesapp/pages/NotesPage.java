package com.notesapp.pages;

import com.notesapp.base.BasePage;
import io.qameta.allure.Step;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.List;
import java.util.stream.Collectors;

/**
 * NotesPage — the main dashboard after login.
 * Selectors verified against the live app DOM via browser inspection.
 *
 * Key findings from live app:
 * - Note title is inside div.card-header, NOT h5.card-title
 * - Category filter buttons have text "Home •", "Work •", "Personal •"
 * - "Add Note" opens a MODAL (URL stays the same)
 * - Edit modal Save button text = "Save"
 * - card-header class contains bg-danger/bg-info/bg-success for Home/Work/Personal
 */
public class NotesPage extends BasePage {

    // ===== Core locators =====
    private final By addNoteButton      = By.xpath("//button[contains(., 'Add Note')]");
    private final By noteTitleInput     = By.id("title");
    private final By noteDescInput      = By.id("description");
    private final By noteCategorySelect = By.id("category");
    // Create modal has "Create", Edit modal has "Save"
    private final By createNoteButton   = By.xpath("//button[normalize-space(text())='Create']");
    private final By saveNoteButton     = By.xpath("//button[normalize-space(text())='Save']");

    // Note cards — title lives in div.card-header
    private final By noteCards          = By.cssSelector("div.card");
    private final By noteCardHeaders    = By.cssSelector("div.card div.card-header");

    // Profile link = username indicator in header
    private final By profileLink        = By.cssSelector("a[href*='profile']");

    // Title required validation (HTML5 required attribute triggers browser popup,
    // but the app may also show a div.invalid-feedback or toast)
    private final By titleValidationMsg = By.cssSelector(".invalid-feedback, .alert.alert-danger");

    // Confirm delete in modal
    private final By confirmDeleteBtn   = By.xpath("//button[normalize-space(text())='Delete']");

    // ===== Dynamic locators =====

    /** Card header that contains the note title */
    private By cardHeaderByTitle(String title) {
        return By.xpath("//div[contains(@class,'card')]" +
                "/div[contains(@class,'card-header') and contains(normalize-space(.),'" + title + "')]");
    }

    /** Edit button inside the card with the given title — works for both <a> and <button> */
    private By editBtnForNote(String title) {
        return By.xpath("//div[contains(@class,'card')]"
                + "[.//div[contains(@class,'card-header') and contains(normalize-space(.),'"
                + title + "')]]"
                + "//*[self::a or self::button][contains(normalize-space(text()),'Edit')]");
    }

    /** Delete button inside the card with the given title */
    private By deleteBtnForNote(String title) {
        return By.xpath("//div[contains(@class,'card')]" +
                "[.//div[contains(@class,'card-header') and contains(normalize-space(.),'" + title + "')]]" +
                "//button[contains(text(),'Delete')]");
    }

    /** Category filter button — live app uses "Home •", "Work •", "Personal •"
     * Also handles plain text buttons for headless rendering */
    private By categoryFilterBtn(String category) {
        // Match button containing the category name as text, regardless of bullet character
        return By.xpath("//button[contains(., '" + category + "')]");
    }

    // ===== State checks =====

    public boolean isOnDashboard() {
        String url = getCurrentUrl();
        // Fast check: URL alone — no element wait that could cause 15s timeout
        return url.contains("/notes/app") && !url.contains("/login") && !url.contains("/register");
    }

    public boolean isUsernameVisible() {
        return isDisplayed(profileLink);
    }

    public boolean isNotePresent(String title) {
        return isDisplayed(cardHeaderByTitle(title));
    }

    public boolean isNoteAbsent(String title) {
        return isAbsent(cardHeaderByTitle(title));
    }

    public int getNoteCount() {
        return getDriver().findElements(noteCards).size();
    }

    public boolean isTitleErrorDisplayed() {
        return isDisplayed(titleValidationMsg);
    }

    public List<String> getNoteCategoriesDisplayed() {
        // Live app card-header CSS class → category mapping (verified via DOM inspection):
        // bg-warning → Home, bg-primary → Work, bg-info → Personal
        List<WebElement> cards = getDriver().findElements(By.cssSelector("div.card"));
        return cards.stream()
                .map(card -> {
                    try {
                        WebElement header = card.findElement(By.cssSelector(".card-header"));
                        String cls = header.getAttribute("class");
                        if (cls.contains("bg-warning")) return "Home";
                        if (cls.contains("bg-primary")) return "Work";
                        if (cls.contains("bg-info"))    return "Personal";
                        // Fallback: bg-danger=Home, bg-success=Personal (legacy)
                        if (cls.contains("bg-danger"))  return "Home";
                        if (cls.contains("bg-success")) return "Personal";
                    } catch (Exception ignored) {}
                    return "";
                })
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    // ===== Actions =====

    @Step("Click Add Note button")
    public NotesPage clickAddNote() {
        click(addNoteButton);
        // Wait for modal to open
        find(noteTitleInput);
        return this;
    }

    @Step("Enter note title: {title}")
    public NotesPage enterTitle(String title) {
        type(noteTitleInput, title);
        return this;
    }

    @Step("Enter note description")
    public NotesPage enterDescription(String description) {
        type(noteDescInput, description);
        return this;
    }

    @Step("Select category: {category}")
    public NotesPage selectCategory(String category) {
        selectByVisibleText(noteCategorySelect, category);
        return this;
    }

    /**
     * Waits until the dashboard "Add Note" button is visible.
     * Used as a reliable signal that the SPA has fully rendered the note list
     * (including a fresh fetch from the server after navigation).
     */
    public NotesPage waitForAddNoteButtonVisible() {
        find(addNoteButton); // WaitUtils.waitForVisible with 25s timeout
        sleepMs(500);        // brief extra pause for the card grid to finish rendering
        log.info("Dashboard ready — 'Add Note' button visible");
        return this;
    }

    @Step("Click Create (save new note)")
    public NotesPage saveNote() {
        click(createNoteButton);
        isAbsent(noteTitleInput); // Active wait: wait for create modal to close
        sleepMs(500);             // brief extra pause for card list to finish rendering
        return this;
    }

    @Step("Create note: title={title}, category={category}")
    public NotesPage createNote(String title, String description, String category) {
        clickAddNote();
        enterTitle(title);
        enterDescription(description);
        selectCategory(category);
        saveNote();
        log.info("Created note: '{}' in category '{}'", title, category);
        return this;
    }

    @Step("Edit note title from {currentTitle} to {newTitle}")
    public NotesPage editNoteTitle(String currentTitle, String newTitle) {
        click(editBtnForNote(currentTitle));
        // Wait for edit modal/page to load — title input must be visible
        find(noteTitleInput);
        clearAndType(noteTitleInput, newTitle);
        // Edit modal has "Save" button
        click(saveNoteButton);
        isAbsent(noteTitleInput); // Active wait: wait for edit modal to close
        // Always navigate back to dashboard — edit may open a detail page or an inline modal;
        // either way we need to be on /notes/app to verify the updated card
        try {
            getDriver().navigate().to("https://practice.expandtesting.com/notes/app");
        } catch (Exception e) {
            log.warn("Navigate back to dashboard failed: {}", e.getMessage());
        }
        waitForAddNoteButtonVisible(); // Active wait: wait for dashboard to render completely
        log.info("Edited note '{}' → '{}'", currentTitle, newTitle);
        return this;
    }

    @Step("Delete note: {title}")
    public NotesPage deleteNote(String title) {
        jsScrollIntoView(deleteBtnForNote(title));
        click(deleteBtnForNote(title));
        // Confirm in modal
        click(confirmDeleteBtn);
        sleepMs(800);
        log.info("Deleted note: '{}'", title);
        return this;
    }

    @Step("Filter notes by category: {category}")
    public NotesPage filterByCategory(String category) {
        // Filter buttons have bullet: "Home •", "Work •", "Personal •"
        click(categoryFilterBtn(category));
        sleepMs(500);
        log.info("Applied category filter: {}", category);
        return this;
    }

    @Step("Open note: {title}")
    public NotesPage openNote(String title) {
        click(editBtnForNote(title));
        find(noteTitleInput);
        return this;
    }
}
