package com.notesapp.tests.api;

import com.notesapp.api.ApiClient;
import com.notesapp.api.AuthApi;
import com.notesapp.api.NotesApi;
import io.qameta.allure.*;
import io.restassured.module.jsv.JsonSchemaValidator;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

@Epic("Notes App")
@Feature("API — Notes CRUD")
public class NotesApiTest {

    private String token;

    @BeforeClass
    public void getToken() {
        token = AuthApi.loginDefault();
        NotesApi.deleteAllNotes(token);
    }

    @Test(description = "TC-API-01: GET /notes returns 200 with valid schema and <2s response time")
    @Story("FR-04, FR-08")
    @Severity(SeverityLevel.BLOCKER)
    public void testGetAllNotes() {
        Response response = NotesApi.getAllNotes(token);
        response.then()
                .spec(ApiClient.okSpec())
                .body(JsonSchemaValidator.matchesJsonSchemaInClasspath("schemas/notes_list_schema.json"));

        Assert.assertTrue(response.jsonPath().getBoolean("success"));
        Assert.assertNotNull(response.jsonPath().getList("data"));
    }

    @Test(description = "TC-API-02: POST /notes creates note with correct fields")
    @Story("FR-02")
    @Severity(SeverityLevel.BLOCKER)
    public void testCreateNote() {
        String title = "API Test Note " + System.currentTimeMillis();
        Response response = NotesApi.createNote(token, title, "Created via API", "Work");

        response.then()
                .spec(ApiClient.okSpec())
                .body(JsonSchemaValidator.matchesJsonSchemaInClasspath("schemas/note_create_schema.json"));

        Assert.assertEquals(response.jsonPath().getString("data.title"), title);
        Assert.assertEquals(response.jsonPath().getString("data.category"), "Work");
        Assert.assertNotNull(response.jsonPath().getString("data.id"));
    }

    @Test(description = "TC-API-02b: DELETE /notes/:id removes the note")
    @Story("FR-06")
    @Severity(SeverityLevel.BLOCKER)
    public void testDeleteNote() {
        String noteId = NotesApi.createNoteAndGetId(
                token, "Delete Target " + System.currentTimeMillis(), "to be deleted", "Personal");

        Response deleteResponse = NotesApi.deleteNote(token, noteId);
        deleteResponse.then().spec(ApiClient.okSpec());

        // Confirm note is gone
        NotesApi.getNoteById(token, noteId).then().statusCode(404);
    }

    @Test(description = "TC-API-03b: PUT /notes/:id updates note fields")
    @Story("FR-02")
    @Severity(SeverityLevel.CRITICAL)
    public void testUpdateNote() {
        String noteId = NotesApi.createNoteAndGetId(
                token, "Before Update " + System.currentTimeMillis(), "original", "Home");

        // PUT replaces the note — response contains the updated snapshot
        Response putResponse = NotesApi.updateNote(
                token, noteId, "After Update", "updated description", "Work", false);

        putResponse.then().spec(ApiClient.okSpec());
        Assert.assertEquals(putResponse.jsonPath().getString("data.title"), "After Update",
                "Title should be updated after PUT");
        Assert.assertEquals(putResponse.jsonPath().getString("data.category"), "Work",
                "Category should be updated after PUT");
        Assert.assertEquals(putResponse.jsonPath().getString("data.description"), "updated description",
                "Description should be updated after PUT");
    }
}
