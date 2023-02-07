package com.ts.common.asserts;

import com.ts.common.application.TrackStudioHttpStatusCodes;
import com.ts.common.request.ResponseBody;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;

import static org.testng.Assert.assertTrue;

public class ApiAsserts {
    private Response response;
    private ResponseBody responseBody;
    public ApiAsserts(Response response) {
        this.response = response;
    }

    public static ApiAsserts assertThat(Response response) {
        return new ApiAsserts(response);
    }

    public ApiAsserts isCorrectResponseCode(TrackStudioHttpStatusCodes code) {
        if (this.response == null)
            assertTrue(false);
        Assertions.assertThat(this.response.getStatusCode())
                .isEqualTo(code.getValue())
                .withFailMessage("Response code is incorrect. Expected: %s , Actual: %s", code.getValue(), this.response.getStatusCode());
        return this;
    }
}
