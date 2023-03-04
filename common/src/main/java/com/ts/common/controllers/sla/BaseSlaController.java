package com.ts.common.controllers.sla;

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

    protected Response receiveSlaTask(String taskNumber) {
        return super.get(getEndpoint(TASK, INFO, taskNumber));
    }

    protected Response performOperation(SlaTask slaTask, String requestBody) {
        return super.post(getEndpoint(OPERATION, slaTask.getNumber(), CREATE), requestBody);
    }

    @Step("Изменение автора: {0}")
    protected abstract Response changeAuthor(SlaTask slaTask);

    protected abstract Response changeAttributes(SlaTask slaTask);

    protected abstract Response changeResPerson(SlaTask slaTask);

    protected abstract Response changeCurrentRole(SlaTask slaTask);

    protected abstract Response changeLinkedTasks(SlaTask slaTask);

    protected abstract Response addTrustedWatchers(SlaTask slaTask);

    protected abstract Response addClientWatchers(SlaTask slaTask);

    protected abstract Response addWatchers(SlaTask slaTask);

    protected abstract Response comment(SlaTask slaTask);

    protected abstract Response privateComment(SlaTask slaTask);

    protected abstract Response removeRequest(SlaTask slaTask);
}
