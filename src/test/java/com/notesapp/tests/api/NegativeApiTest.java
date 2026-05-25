package com.notesapp.tests.api;

import com.notesapp.api.ApiClient;
import com.notesapp.api.AuthApi;
import com.notesapp.api.NotesApi;
import io.qameta.allure.*;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.given;

@Epic("Notes App")
@Feature("API — Negative Validation")
public class NegativeApiTest {

    @Test(description = "TC-NEG-02: GET /notes without token returns 401")
    @Story("FR-09")
    @Severity(SeverityLevel.CRITICAL)
    public void testGetNotesWithoutToken() {
        Response response = given()
                .spec(ApiClient.baseSpec())
                .get("/notes");

        response.then().statusCode(401);
        Assert.assertFalse((boolean) response.jsonPath().getBoolean("success"),
                "success should be false for unauthenticated request");
    }

    @Test(description = "TC-NEG-05: POST /notes with invalid token returns 401")
    @Story("FR-09")
    @Severity(SeverityLevel.NORMAL)
    public void testCreateNoteWithBadToken() {
        Response response = NotesApi.createNote(
                "fake-token-xyz", "Should fail", "This should not be created", "Home");
        response.then().statusCode(401);
    }

    @Test(description = "TC-NEG-06: DELETE /notes with non-existent note ID returns 404")
    @Story("FR-09")
    @Severity(SeverityLevel.NORMAL)
    public void testDeleteNonExistentNote() {
        String token = AuthApi.loginDefault();
        Response response = NotesApi.deleteNote(token, "000000000000000000000000");
        response.then().statusCode(404);
    }
}
