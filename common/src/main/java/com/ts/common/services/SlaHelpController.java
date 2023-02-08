package com.ts.common.services;

import com.ts.common.application.AuthToken;
import com.ts.common.entitites.sla.CreateSlaRequestBody;
import com.ts.common.entitites.sla.CreateSlaResponseBody;
import com.ts.common.entitites.sla.SlaTask;
import com.ts.common.request.ApiRequest;
import com.ts.common.utils.JsonUtils;
import io.restassured.response.Response;

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

    public Response createSlaTaskConsultation(SlaTask slaTask) {
        CreateSlaRequestBody slaRequestBody = new CreateSlaRequestBody(slaTask);
        this.response = createSlaTaskConsultation(slaRequestBody.convertToString());
        CreateSlaResponseBody slaResponseBody = JsonUtils.deserialize(this.response, CreateSlaResponseBody.class);
        if (slaResponseBody != null) {
            slaTask.setId(slaResponseBody.getId());
            slaTask.setNumber(slaResponseBody.getNumber());
        }
        return this.response;
    }

}
