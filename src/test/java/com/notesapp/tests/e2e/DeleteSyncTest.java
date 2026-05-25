package com.notesapp.tests.e2e;

import com.notesapp.api.AuthApi;
import com.notesapp.api.NotesApi;
import com.notesapp.base.BaseTest;
import com.notesapp.config.ConfigReader;
import com.notesapp.pages.LoginPage;
import com.notesapp.pages.NotesPage;
import io.qameta.allure.*;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Map;

/**
 * TC-E2E-02: Note deleted via API must vanish from the UI.
 */
@Epic("Notes App")
@Feature("Hybrid E2E — Delete Sync")
public class DeleteSyncTest extends BaseTest {

    private NotesPage notesPage;
    private String token;

    @BeforeMethod
    public void setup() {
        notesPage = new LoginPage().loginAs(
                ConfigReader.getUserEmail(), ConfigReader.getUserPassword());
        token = AuthApi.loginDefault();
    }

    @Test(description = "TC-E2E-02: API deletion immediately reflected in UI")
    @Story("FR-06, FR-07")
    @Severity(SeverityLevel.BLOCKER)
    public void testApiDeleteReflectedInUI() {
        String title = "Delete Sync Test " + System.currentTimeMillis();

        // Step 1: Create note via UI
        notesPage.createNote(title, "This will be deleted via API", "Personal");
        Assert.assertTrue(notesPage.isNotePresent(title),
                "Note should appear in UI before API deletion");

        // Step 2: Find note ID via API
        Response getResponse = NotesApi.getAllNotes(token);
        getResponse.then().statusCode(200);

        String noteId = getResponse.jsonPath()
                .getList("data", Map.class)
                .stream()
                .filter(n -> title.equals(n.get("title")))
                .map(n -> n.get("id").toString())
                .findFirst()
                .orElseThrow(() -> new AssertionError("Note not found via API after UI creation"));

        // Step 3: Delete via API
        NotesApi.deleteNote(token, noteId).then().statusCode(200);

        // Step 4: Navigate to dashboard (full navigation — more stable than refresh on Chrome 148)
        getDriver().navigate().to("https://practice.expandtesting.com/notes/app");

        // Active wait: confirm the dashboard is fully rendered by waiting for
        // the "Add Note" button (always present on dashboard) before asserting absence.
        // This prevents a false "still present" result when 85+ notes are still loading.
        notesPage.waitForAddNoteButtonVisible();

        Assert.assertTrue(notesPage.isNoteAbsent(title),
                "Note '" + title + "' should be gone from UI after API deletion");

        log.info("TC-E2E-02 PASSED — API deletion reflected in UI for: '{}'", title);
    }
}
