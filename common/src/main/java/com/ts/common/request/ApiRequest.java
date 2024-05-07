package com.ts.common.request;


import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ts.common.application.controllers.AuthToken;
import io.qameta.allure.Step;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.internal.mapping.Jackson2Mapper;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;

/**
 * @author Aidar Askeev
 */
@Slf4j
@Data
public abstract class ApiRequest {
    public static final String SLASH = "/";
    public static final String BASIC = "Basic ";
    public static final String QUESTION_MARK = "?";
    public static final String EQUAL_MARK = "=";
    public static final String AMPERSAND_MARK = "&";
    public AuthToken authToken;
    protected Jackson2Mapper objectMapper;
    protected String url;//TODO change to URL
    protected Map<String, String> headers;
    protected RequestSpecification requestSpec;
    protected Response response;


    public ApiRequest(String url, Map<String, String> headers, AuthToken authToken) {
        this.objectMapper = initObjectMapper();
        this.headers = headers;
        this.url = url;
        this.authToken = authToken;
        this.requestSpec = new RequestSpecBuilder()
                .setBaseUri(url)
                .addHeaders(headers)
                .setRelaxedHTTPSValidation()
                .build();
        this.requestSpec.log();
    }

    private static Jackson2Mapper initObjectMapper() {
        return new Jackson2Mapper(((type, charset) -> {
            com.fasterxml.jackson.databind.ObjectMapper om = new ObjectMapper().findAndRegisterModules();
            om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            om.configure(DeserializationFeature.UNWRAP_ROOT_VALUE, true);
            return om;
        }));
    }

    public static String getEndpoint(String... args) {
        StringBuilder endpoint = new StringBuilder();
        for (String arg : args)
            endpoint.append(arg).append(SLASH);
        return endpoint.substring(0, endpoint.length() - 1);
    }

    public static String formatParameters(Map<String, String> parameters) {
        StringBuilder query = new StringBuilder("?");
        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            query.append(entry.getKey() + "=" + entry.getValue() + "&");
        }
        return query.deleteCharAt(query.length() - 1).toString();
    }

    @Step("Response is:")
    public void setResponseToAllure(String response) {
    }

    public ApiRequest logResponse() {
        log.warn("Response is:");
        log.warn(getResponse().getBody().asString());
        log.warn(String.valueOf(getResponse().getStatusCode()));
        setResponseToAllure(getResponse().getBody().asPrettyString());
        return this;
    }

    public int getStatusCode() {
        return getResponse().getStatusCode();
    }

    public Response get(String endpoint) {
        log.info("performed GET {}", endpoint);
        log.info("User is {}", authToken.getUser());
        this.response = given()
                .auth()
                .preemptive()
                .basic(authToken.getUser(), authToken.getPassword())
                .spec(requestSpec)
                .get(endpoint);
        logResponse();
        return this.response;
    }

    public Response delete(String endpoint) {
        log.info("performed DELETE {}", endpoint);
        this.response = given()
                .spec(this.requestSpec)
                .delete(endpoint);
        logResponse();
        return this.response;
    }

    public Response post(String endpoint, Object request) {
        log.info("performed POST {}", endpoint);
        log.info("Body is {}", request);
        log.info("User is {}", authToken.getUser());
        this.response = given()
                .auth()
                .preemptive()
                .basic(authToken.getUser(), authToken.getPassword())
                .spec(requestSpec)
                .body(request, objectMapper)
                .post(endpoint);
        logResponse();
        return this.response;
    }

    public Response postWithForm(String endpoint, String key, Object request) {
        log.info("performed POST {}", endpoint);
        log.info("Body is {}", request);
        log.info("User is {}", authToken.getUser());
        this.response = given()
                .auth()
                .preemptive()
                .basic(authToken.getUser(), authToken.getPassword())
                .spec(requestSpec)
                .contentType(ContentType.URLENC)
                .formParam(key, request)
                .post(endpoint);
        logResponse();
        return this.response;
    }

    public Response post(String endpoint, String body) {
        log.info("performed POST {}", endpoint);
        log.info("Body is {}", body);
        log.info("User is {}", authToken.getUser());
        this.response = given()
                .auth()
                .preemptive()
                .basic(authToken.getUser(), authToken.getPassword())
                .spec(requestSpec)
                .body(body)
                .post(endpoint);
        logResponse();
        return this.response;
    }

    public Response put(String endpoint, Object body) {
        log.info("performed PUT {}", endpoint);
        log.info("Body is {}", body);
        this.response = given()
                .spec(this.requestSpec)
                .body(body, objectMapper)
                .put(endpoint);
        logResponse();
        return this.response;
    }

    public Response put(String endpoint, String body) {
        log.info("performed PUT {}", endpoint);
        log.info("Body is {}", body);
        this.response = given()
                .spec(this.requestSpec)
                .body(body)
                .put(endpoint);
        logResponse();
        return this.response;
    }

    public Response patch(String endpoint, Object request) {
        log.info("performed PATCH {}", endpoint);
        log.info("Body is {}", request);
        this.response = given()
                .spec(this.requestSpec)
                .body(request)
                .patch(endpoint);
        return this.response;
    }

    public <T> T extractObject(Class<T> clazz) {
        try {
            return this.response
                    .then()
                    .extract()
                    .body()
                    .as(clazz, objectMapper);
        } catch (Exception e) {
            log.error("Can not parse response {}", e);
            return null;
        }
    }

}