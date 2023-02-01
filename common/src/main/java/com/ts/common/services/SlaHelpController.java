package com.ts.common.services;

import com.ts.common.application.AuthToken;
import com.ts.common.request.ApiRequest;
import io.restassured.response.Response;

import java.util.Map;

import static com.ts.common.application.TrackStudioEndPoints.*;

public class SlaHelpController extends ApiRequest {
    public SlaHelpController(String url, AuthToken authToken) {
        super(url, HEADERS_BASE_CONTROLLER);
        this.authToken = authToken;
    }

    public Response createSlaTaskConsultation(String requestBody) {
        return super.post(getEndpoint(TASK, UPDATE), requestBody);
    }

    public Response receiveTaskConsultation(String taskNumber) {
        return super.get(getEndpoint(TASK, INFO, taskNumber));
    }
}
