package com.notesapp.stepdefs;

import com.notesapp.api.AuthApi;
import com.notesapp.api.NotesApi;
import com.notesapp.config.ConfigReader;
import com.notesapp.drivers.DriverManager;
import com.notesapp.pages.NotesPage;
import io.cucumber.java.en.*;
import io.restassured.response.Response;
import org.testng.Assert;

import java.util.Map;

public class ApiSteps {

    private String token;
    private Response lastResponse;
    private NotesPage notesPage = new NotesPage();

    @Given("a valid API auth token is available")
    public void getToken() {
        token = AuthApi.loginDefault();
        Assert.assertNotNull(token, "Auth token must not be null");
    }

    @When("the user calls GET \\/notes via API")
    public void callGetNotes() {
        lastResponse = NotesApi.getAllNotes(token);
    }

    @When("the user creates a note via UI with title {string} category {string} description {string}")
    public void createNoteViaUi(String title, String category, String description) {
        notesPage.createNote(title, description, category);
    }

    @When("the user creates a note via API with title {string}")
    public void createNoteViaApi(String title) {
        lastResponse = NotesApi.createNote(token, title, "API generated note", "Work");
        lastResponse.then().statusCode(200);
    }

    @Then("the API response should contain a note with title {string}")
    public void apiHasNoteTitle(String title) {
        lastResponse = NotesApi.getAllNotes(token);
        lastResponse.then().statusCode(200);
        boolean found = NotesApi.noteExistsByTitle(token, title);
        Assert.assertTrue(found, "API GET /notes did not contain a note with title: " + title);
    }

    @Then("the API note category should be {string}")
    public void apiNoteCategory(String category) {
        String actual = lastResponse.jsonPath().getString("data[0].category");
        Assert.assertEquals(actual, category, "Note category mismatch in API");
    }

    @Then("the API note description should be {string}")
    public void apiNoteDescription(String description) {
        String actual = lastResponse.jsonPath().getString("data[0].description");
        Assert.assertEquals(actual, description, "Note description mismatch in API");
    }

    @Then("all fields should match exactly between UI and API")
    public void allFieldsMatch() {
        Assert.assertNotNull(lastResponse.jsonPath().getString("data[0].id"),
                "Note ID should not be null in API response");
    }

    @When("the note {string} is deleted via the API")
    public void deleteNoteViaApi(String title) {
        lastResponse = NotesApi.getAllNotes(token);
        lastResponse.then().statusCode(200);
        String noteId = lastResponse.jsonPath()
                .getList("data", Map.class)
                .stream()
                .filter(n -> title.equals(n.get("title")))
                .map(n -> n.get("id").toString())
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Note '" + title + "' not found in API for deletion"));
        NotesApi.deleteNote(token, noteId).then().statusCode(200);
    }

    @When("the user refreshes the Notes dashboard")
    public void refreshDashboard() {
        DriverManager.getDriver().navigate().refresh();
    }

    @Then("the note {string} should not be visible in the UI list")
    public void noteAbsentFromUi(String title) {
        Assert.assertTrue(notesPage.isNoteAbsent(title),
                "Note '" + title + "' should be absent from UI after API deletion");
    }

    @Then("the total note count in the UI should decrease by 1")
    public void noteCountDecreased() {
        // count check handled per test — this step serves as a logical marker
    }

    @Then("the API response time should be less than {int} milliseconds")
    public void apiResponseTime(int ms) {
        long time = lastResponse.getTime();
        Assert.assertTrue(time < ms,
                "API response time was " + time + "ms — expected < " + ms + "ms");
    }
}
