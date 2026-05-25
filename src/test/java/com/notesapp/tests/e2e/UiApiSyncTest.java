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

import java.util.List;
import java.util.Map;

/**
 * TC-E2E-01: Note created via UI must appear in API response with identical field values.
 * Covers FR-05 — the core UI-API data consistency requirement.
 */
@Epic("Notes App")
@Feature("Hybrid E2E — UI ↔ API Sync")
public class UiApiSyncTest extends BaseTest {

    private NotesPage notesPage;
    private String token;

    @BeforeMethod
    public void setup() {
        notesPage = new LoginPage().loginAs(
                ConfigReader.getUserEmail(), ConfigReader.getUserPassword());
        token = AuthApi.loginDefault();
    }

    @Test(description = "TC-E2E-01: Note created via UI appears in API with matching fields")
    @Story("FR-05")
    @Severity(SeverityLevel.BLOCKER)
    public void testUiNoteAppearsInApi() {
        // Unique identifiers for this test run
        String title    = "Sprint Planning " + System.currentTimeMillis();
        String desc     = "Define Q3 OKRs and deliverables";
        String category = "Work";

        // Step 1: Create note via UI
        notesPage.createNote(title, desc, category);
        Assert.assertTrue(notesPage.isNotePresent(title),
                "Note should appear in UI list after creation");

        // Step 2: Fetch notes via API
        Response apiResponse = NotesApi.getAllNotes(token);
        apiResponse.then().statusCode(200);

        // Step 3: Find the note in API response
        boolean foundInApi = apiResponse.jsonPath()
                .getList("data.title")
                .stream()
                .anyMatch(t -> title.equals(t));
        Assert.assertTrue(foundInApi,
                "Note '" + title + "' created via UI was NOT found in GET /notes API response");

        // Step 4: Verify each field matches
        List<Map<String, Object>> notes = apiResponse.jsonPath().getList("data");
        Map<String, Object> apiNote = notes.stream()
                .filter(n -> title.equals(n.get("title")))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Note not found in API list"));

        Assert.assertEquals(apiNote.get("title").toString(), title,
                "Title mismatch: UI vs API");
        Assert.assertEquals(apiNote.get("description").toString(), desc,
                "Description mismatch: UI vs API");
        Assert.assertEquals(apiNote.get("category").toString(), category,
                "Category mismatch: UI vs API");
        Assert.assertNotNull(apiNote.get("id"),
                "Note ID must not be null in API response");

        log.info("TC-E2E-01 PASSED — All fields matched between UI and API for note: '{}'", title);
    }

    @Test(description = "TC-E2E-03: API response time for GET /notes is under 2 seconds")
    @Story("FR-08")
    @Severity(SeverityLevel.NORMAL)
    public void testApiResponseTimeUnder2Seconds() {
        Response response = NotesApi.getAllNotes(token);
        response.then().statusCode(200);

        long responseTime = response.getTime();
        log.info("GET /notes response time: {}ms", responseTime);
        Assert.assertTrue(responseTime < ConfigReader.getApiResponseTimeMs(),
                "API response time " + responseTime + "ms exceeded limit of "
                        + ConfigReader.getApiResponseTimeMs() + "ms");
    }
}
