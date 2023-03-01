package com.ts.common.controllers.sla;

import com.ts.common.request.ApiRequest;
import io.restassured.response.Response;

import java.util.Map;

import static com.ts.common.application.controllers.TrackStudioEndPoints.TASK;
import static com.ts.common.application.controllers.TrackStudioEndPoints.UPDATE;

public class BaseSlaController extends ApiRequest {
    public BaseSlaController(String url, Map<String, String> headersBaseController) {
        super(url, headersBaseController);
    }

    protected Response createTask(String requestBody) {
        return super.post(getEndpoint(TASK, UPDATE), requestBody);
    }
}
