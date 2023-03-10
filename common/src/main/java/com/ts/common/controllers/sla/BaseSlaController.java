package com.ts.common.controllers.sla;

import com.ts.common.application.controllers.TrackStudioEndPoints;
import com.ts.common.entitites.sla.SlaTask;
import com.ts.common.enums.ComSlaOperations;
import com.ts.common.enums.SlaType;
import com.ts.common.request.ApiRequest;
import com.ts.common.utils.InitEntities;
import io.qameta.allure.Step;
import io.restassured.response.Response;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

import static com.ts.common.application.controllers.TrackStudioEndPoints.*;
import static com.ts.common.controllers.sla.SlaRequestBody.Fields.ID;
import static com.ts.common.controllers.sla.SlaRequestBody.Fields.OPERATION;
import static com.ts.common.controllers.sla.SlaRequestBody.Fields.*;

public abstract class BaseSlaController extends ApiRequest {
    protected SlaType slaType;
    protected SlaRequestBody.Fields[] DEFAULT_FIELDS = {ID, OPERATION, DESCRIPTION, ATTACHMENTS, UDFS};

    public BaseSlaController(String url, Map<String, String> headersBaseController) {
        super(url, headersBaseController);
    }

    public BaseSlaController(String url, Map<String, String> headersBaseController, SlaType slaType) {
        super(url, headersBaseController);
        this.slaType = slaType;
    }

    protected Response createTask(String requestBody) {
        return super.post(getEndpoint(TASK, UPDATE), requestBody);
    }

    protected Response receiveSlaTask(String taskNumber) {
        return super.get(getEndpoint(TASK, INFO, taskNumber));
    }

    protected Response performOperation(@NotNull SlaTask slaTask, String requestBody) {
        return super.post(getEndpoint(TrackStudioEndPoints.OPERATION, slaTask.getNumber(), CREATE), requestBody);
    }

    public Response performCommonOperation(SlaTask slaTask, ComSlaOperations operation) {
        slaTask.setOperation(InitEntities.generateOperationID(this.slaType, operation));
        SlaRequestBody slaRequestBody = new SlaRequestBody(slaTask);
        return performOperation(slaTask, slaRequestBody.keepFields(DEFAULT_FIELDS));
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
