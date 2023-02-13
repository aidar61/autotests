package com.ts.common.asserts;

import com.ts.common.application.TrackStudioHttpStatusCodes;
import com.ts.common.request.ResponseBody;
import com.ts.common.utils.JsonUtils;
import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

@Slf4j
public class ApiAsserts {
    private Response response;
    private ResponseBody responseBody;

    public ApiAsserts(Response response) {
        this.response = response;
    }

    public static ApiAsserts assertThat(Response response) {
        log.info("Checking following actual response: \n{}", response);
        return new ApiAsserts(response);
    }

    public ApiAsserts isCorrectResponseCode(TrackStudioHttpStatusCodes code) {
        if (this.response == null)
            assertTrue(false);
        Assertions.assertThat(this.response.getStatusCode())
                .isEqualTo(code.getValue())
                .withFailMessage("Response code is incorrect. Expected: %s , Actual: %s", code.getValue(), this.response.getStatusCode());
        log.info("Status code is correct: Actual {}, Expected {}", this.response.getStatusCode(), code);
        return this;
    }

    public <T> ApiAsserts isParseableBody(Class<T> clazz) {
        Object obj = JsonUtils.deserialize(response, clazz);
        assertNotNull(obj, "Response body is not parseable");
        this.responseBody = (ResponseBody) obj;
        log.info("Response body is correct");
        return this;
    }
}
