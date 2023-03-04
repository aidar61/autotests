package com.ts.common.controllers.sla;

import com.ts.common.entitites.commonEntities.GeneralSlaId;
import com.ts.common.entitites.sla.SlaTask;
import com.ts.common.request.ApiRequest;
import io.qameta.allure.Step;
import io.restassured.response.Response;

import java.util.Map;

import static com.ts.common.application.controllers.TrackStudioEndPoints.*;

public abstract class BaseSlaController extends ApiRequest {
    public BaseSlaController(String url, Map<String, String> headersBaseController) {
        super(url, headersBaseController);
    }

    protected Response createTask(String requestBody) {
        return super.post(getEndpoint(TASK, UPDATE), requestBody);
    }

    protected Response performOperation(SlaTask slaTask, String requestBody, GeneralSlaId.Fields operation) {
        return super.post(getEndpoint(OPERATION, slaTask.getNumber(), CREATE), requestBody);
    }

    protected Response performOperation(SlaTask slaTask, String requestBody) {
        return super.post(getEndpoint(OPERATION, slaTask.getNumber(), CREATE), requestBody);
    }

    @Step("Изменение автора: {0}")
    protected abstract Response changeAuthor(SlaTask slaTask);
}
