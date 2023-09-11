package com.ts.common.asserts;

import com.ts.common.application.controllers.TrackStudioHttpStatusCodes;
import com.ts.common.application.errors.ErrorResponseBody;
import com.ts.common.controllers.TaskResponseBody;
import com.ts.common.entitites.tasks.GeneralTask;
import com.ts.common.request.ResponseBody;
import com.ts.common.utils.JsonUtils;
import io.qameta.allure.Step;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;

import static org.testng.Assert.*;

@Slf4j
public class ApiAsserts {
    private Response response;
    private ResponseBody responseBody;

    public ApiAsserts(Response response) {
        this.response = response;
    }

    @Step("[Assert] Response")
    public static ApiAsserts assertThat(Response response) {
//        logResponse(response.getBody().asPrettyString());
        return new ApiAsserts(response);
    }

    @Step("Checking expected code: {0}")
    public ApiAsserts isCorrectResponseCode(TrackStudioHttpStatusCodes code) {
        if (this.response == null)
            assertTrue(false);
        Assertions.assertThat(this.response.getStatusCode())
                .isEqualTo(code.getValue())
                .withFailMessage("Response code is incorrect. Expected: %s , Actual: %s", code.getValue(), this.response.getStatusCode());
        log.info("Status code is correct: Actual {}, Expected {}", this.response.getStatusCode(), code);
        return this;
    }

    @Step("Response messaga is: {0}")
    public ApiAsserts checkingResponseMessageField(String messageField, String expectedValue) {
        var actualValue = this.response.jsonPath().getString("message." + messageField);
        assertEquals(actualValue, expectedValue, "Error is correct");
        log.info("Message field '{}' is correct Actual: {}, Expected: {}",
                messageField, actualValue, expectedValue);
        return this;
    }

    @Step("Response body is: {0}")
    private static void logResponse(String responseBody) {
//        log.info("Response body is : " + responseBody);
    }

    public <T> ApiAsserts isParseableBody(Class<T> clazz) {
        Object obj = JsonUtils.deserialize(this.response, clazz);
        assertNotNull(obj, "Response body is not parseable");
        this.responseBody = (ResponseBody) obj;
        log.info("Response body is correct");
        return this;
    }

    @Step("Checking task is correct")
    public TaskAsserts assertTask() {
        TaskResponseBody task = JsonUtils.deserialize(this.response, TaskResponseBody.class);
        log.warn("Parsed to JAVA Object: {}", task.toString());
        return TaskAsserts.assertThat(task);
    }

    @Step("Checking expected error: {0}")
    public ApiAsserts isCorrectErrorMessage(String expectedError) {
        var actualError = new JsonPath(response.asString()).getString("message");
        assertEquals(actualError, expectedError, "Error is correct");
        return this;
    }
}
