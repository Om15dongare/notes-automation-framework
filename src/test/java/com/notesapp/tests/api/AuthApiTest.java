package com.notesapp.tests.api;

import com.notesapp.api.AuthApi;
import com.notesapp.api.ApiClient;
import com.notesapp.config.ConfigReader;
import io.qameta.allure.*;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.Test;

@Epic("Notes App")
@Feature("API — Authentication")
public class AuthApiTest {

    @Test(description = "TC-API-AUTH-01: Login API returns 200 and valid token")
    @Story("FR-01")
    @Severity(SeverityLevel.BLOCKER)
    public void testLoginReturnsToken() {
        Response response = AuthApi.login(
                ConfigReader.getUserEmail(),
                ConfigReader.getUserPassword());

        response.then().spec(ApiClient.okSpec());
        Assert.assertEquals((boolean) response.jsonPath().getBoolean("success"), true);
        String token = response.jsonPath().getString("data.token");
        Assert.assertNotNull(token, "Token must not be null");
        Assert.assertFalse(token.isEmpty(), "Token must not be empty");
    }

    @Test(description = "TC-API-AUTH-02: Login with wrong password returns 401")
    @Story("FR-09")
    @Severity(SeverityLevel.CRITICAL)
    public void testLoginWrongPassword() {
        Response response = AuthApi.login(ConfigReader.getUserEmail(), "badpassword");
        response.then().statusCode(401);
        Assert.assertFalse((boolean) response.jsonPath().getBoolean("success"),
                "success should be false on failed login");
    }

    @Test(description = "TC-API-AUTH-03: Login with unregistered email returns 401")
    @Story("FR-09")
    @Severity(SeverityLevel.NORMAL)
    public void testLoginUnknownEmail() {
        Response response = AuthApi.login("nobody@unknown.com", "Test@1234");
        response.then().statusCode(401);
    }

    @Test(description = "TC-API-AUTH-04: GET /profile with valid token returns user data")
    @Story("FR-01")
    @Severity(SeverityLevel.NORMAL)
    public void testGetProfile() {
        String token = AuthApi.loginDefault();
        Response response = AuthApi.getProfile(token);
        response.then().spec(ApiClient.okSpec());
        Assert.assertEquals(response.jsonPath().getString("data.email"),
                ConfigReader.getUserEmail());
    }
}
