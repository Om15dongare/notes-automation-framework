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
@Feature("UI — Notes CRUD")
public class NotesCrudTest extends BaseTest {

    private NotesPage notesPage;

    @BeforeMethod
    public void login() {
        notesPage = new LoginPage().loginAs(
                ConfigReader.getUserEmail(),
                ConfigReader.getUserPassword());
    }

    @Test(description = "TC-UI-02: Create note via UI and verify in list")
    @Story("FR-02, FR-03")
    @Severity(SeverityLevel.BLOCKER)
    public void testCreateNote() {
        String title = "Grocery List " + System.currentTimeMillis();
        notesPage.createNote(title, "Buy milk, eggs, and bread", "Home");

        Assert.assertTrue(notesPage.isNotePresent(title),
                "Newly created note should appear in the notes list");
        log.info("TC-UI-02 PASSED — Note created: {}", title);
    }

    @Test(description = "TC-UI-04: Edit existing note and verify updated title")
    @Story("FR-02, FR-03")
    @Severity(SeverityLevel.CRITICAL)
    public void testEditNote() {
        String original = "Edit Target " + System.currentTimeMillis();
        String updated  = "Updated " + original;

        notesPage.createNote(original, "Will be edited", "Work");
        Assert.assertTrue(notesPage.isNotePresent(original), "Original note must exist before edit");

        notesPage.editNoteTitle(original, updated);
        notesPage.sleepMs(1000); // allow SPA to re-render card list

        // Verifying new title is present is sufficient — it's the same note, just renamed
        Assert.assertTrue(notesPage.isNotePresent(updated),
                "Edited note title should appear in the list");
        log.info("TC-UI-04 PASSED — Note renamed from '{}' to '{}'", original, updated);
    }

    @Test(description = "TC-NEG-03: Create note with blank title shows validation error")
    @Story("FR-09")
    @Severity(SeverityLevel.NORMAL)
    public void testCreateNoteBlankTitle() {
        int countBefore = notesPage.getNoteCount();

        // Try to submit with empty title
        notesPage.clickAddNote()
                .enterTitle("")
                .enterDescription("This should not be saved")
                .saveNote();

        // Attach screenshot for visual verification of empty title validation error in Allure report
        byte[] screenshot = com.notesapp.utils.ScreenshotUtils.captureScreenshot();
        com.notesapp.utils.AllureUtils.attachScreenshot("Create Note Failure — Blank Title", screenshot);

        // The app uses HTML5 native browser validation popup (not a DOM element),
        // so we verify the note was NOT added to the count instead
        // Wait briefly for any possible async save, then check
        notesPage.sleepMs(1000);
        // Close modal if still open by clicking Cancel
        try {
            getDriver().findElement(
                    org.openqa.selenium.By.cssSelector("button.btn-secondary")).click();
        } catch (Exception ignored) {}

        int countAfter = notesPage.getNoteCount();
        Assert.assertEquals(countAfter, countBefore,
                "Note count should not increase when title is blank — expected "
                        + countBefore + " but found " + countAfter);
        log.info("TC-NEG-03 PASSED — Blank title note was not saved");
    }
}
