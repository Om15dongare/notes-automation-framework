package com.notesapp.api;

import io.qameta.allure.Step;
import io.restassured.response.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;

/**
 * NotesApi — CRUD operations on /notes endpoints.
 * GET    /notes
 * POST   /notes
 * GET    /notes/{id}
 * PATCH  /notes/{id}
 * DELETE /notes/{id}
 */
public class NotesApi {

    private static final Logger log = LogManager.getLogger(NotesApi.class);

    @Step("API: GET /notes")
    public static Response getAllNotes(String token) {
        log.info("Fetching all notes via API");
        return given()
                .spec(ApiClient.authSpec(token))
                .get("/notes");
    }

    @Step("API: POST /notes — title={title}, category={category}")
    public static Response createNote(String token, String title, String description, String category) {
        Map<String, String> body = new HashMap<>();
        body.put("title", title);
        body.put("description", description);
        body.put("category", category);

        log.info("Creating note: '{}' in category '{}'", title, category);
        return given()
                .spec(ApiClient.authSpec(token))
                .body(body)
                .post("/notes");
    }

    @Step("API: GET /notes/{id}")
    public static Response getNoteById(String token, String noteId) {
        return given()
                .spec(ApiClient.authSpec(token))
                .get("/notes/" + noteId);
    }

    @Step("API: PUT /notes/{id} — full update")
    public static Response updateNote(String token, String noteId, String title,
                                      String description, String category, boolean completed) {
        Map<String, Object> body = new HashMap<>();
        body.put("title", title);
        body.put("description", description);
        body.put("category", category);
        body.put("completed", completed);

        log.info("Updating (PUT) note id: {}", noteId);
        return given()
                .spec(ApiClient.authSpec(token))
                .body(body)
                .put("/notes/" + noteId);
    }

    @Step("API: DELETE /notes/{id}")
    public static Response deleteNote(String token, String noteId) {
        log.info("Deleting note id: {}", noteId);
        return given()
                .spec(ApiClient.authSpec(token))
                .delete("/notes/" + noteId);
    }

    /**
     * Helper — creates a note and returns its ID for chained operations.
     */
    @Step("API: Create note and return ID — {title}")
    public static String createNoteAndGetId(String token, String title, String description, String category) {
        Response response = createNote(token, title, description, category);
        response.then().statusCode(200);
        String id = response.jsonPath().getString("data.id");
        if (id == null) throw new RuntimeException("Note ID null after create — check API response");
        log.info("Created note id: {}", id);
        return id;
    }

    /**
     * Checks whether a note with the given title exists in GET /notes response.
     */
    public static boolean noteExistsByTitle(String token, String title) {
        Response response = getAllNotes(token);
        response.then().statusCode(200);
        return response.jsonPath()
                .getList("data.title")
                .stream()
                .anyMatch(t -> title.equals(t));
    }
}
