package com.notesapp.api;

import com.notesapp.config.ConfigReader;
import io.qameta.allure.Step;
import io.restassured.response.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;

/**
 * AuthApi — handles user auth endpoints.
 * POST /users/register
 * POST /users/login
 * GET  /users/profile
 * DELETE /users/delete-account
 */
public class AuthApi {

    private static final Logger log = LogManager.getLogger(AuthApi.class);

    @Step("API: POST /users/login — {email}")
    public static Response login(String email, String password) {
        Map<String, String> body = new HashMap<>();
        body.put("email", email);
        body.put("password", password);

        log.info("API Login: {}", email);
        return given()
                .spec(ApiClient.baseSpec())
                .body(body)
                .post("/users/login");
    }

    /**
     * Convenience method — logs in and returns the auth token directly.
     * Asserts HTTP 200 internally.
     */
    @Step("API: Login and extract token — {email}")
    public static String loginAndGetToken(String email, String password) {
        Response response = login(email, password);
        response.then().statusCode(200);
        String token = response.jsonPath().getString("data.token");
        if (token == null || token.isEmpty()) {
            throw new RuntimeException("Auth token is null after login — check credentials or API");
        }
        log.info("Token obtained: {}...{}", token.substring(0, 8), token.substring(token.length() - 4));
        return token;
    }

    /**
     * Login with default credentials from config.properties
     */
    public static String loginDefault() {
        return loginAndGetToken(ConfigReader.getUserEmail(), ConfigReader.getUserPassword());
    }

    @Step("API: POST /users/register")
    public static Response register(String name, String email, String password) {
        Map<String, String> body = new HashMap<>();
        body.put("name", name);
        body.put("email", email);
        body.put("password", password);

        return given()
                .spec(ApiClient.baseSpec())
                .body(body)
                .post("/users/register");
    }

    @Step("API: GET /users/profile")
    public static Response getProfile(String token) {
        return given()
                .spec(ApiClient.authSpec(token))
                .get("/users/profile");
    }

    @Step("API: DELETE /users/delete-account")
    public static Response deleteAccount(String token) {
        log.warn("Deleting user account via API — ensure this is a disposable test account!");
        return given()
                .spec(ApiClient.authSpec(token))
                .delete("/users/delete-account");
    }
}
