package com.notesapp.api;

import com.notesapp.config.ConfigReader;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;

import static org.hamcrest.Matchers.lessThan;

/**
 * ApiClient — base RestAssured configuration.
 * Builds reusable request and response specifications.
 * All API classes extend or use this.
 */
public class ApiClient {

    private static final String BASE_URL = ConfigReader.getApiBaseUrl();

    /**
     * Unauthenticated base request spec — used for login and register.
     */
    public static RequestSpecification baseSpec() {
        return new RequestSpecBuilder()
                .setBaseUri(BASE_URL)
                .setContentType(ContentType.JSON)
                .setAccept(ContentType.JSON)
                .addFilter(new AllureRestAssured())
                .addFilter(new RequestLoggingFilter())
                .addFilter(new ResponseLoggingFilter())
                .build();
    }

    /**
     * Authenticated request spec — includes x-auth-token header.
     */
    public static RequestSpecification authSpec(String token) {
        return new RequestSpecBuilder()
                .addRequestSpecification(baseSpec())
                .addHeader("x-auth-token", token)
                .build();
    }

    /**
     * Standard 200 response spec with performance assertion.
     */
    public static ResponseSpecification okSpec() {
        return new ResponseSpecBuilder()
                .expectStatusCode(200)
                .expectContentType(ContentType.JSON)
                .expectResponseTime(lessThan(ConfigReader.getApiResponseTimeMs()))
                .build();
    }

    /**
     * Response spec for a specific status code with performance assertion.
     */
    public static ResponseSpecification statusSpec(int statusCode) {
        return new ResponseSpecBuilder()
                .expectStatusCode(statusCode)
                .expectContentType(ContentType.JSON)
                .expectResponseTime(lessThan(ConfigReader.getApiResponseTimeMs()))
                .build();
    }
}
