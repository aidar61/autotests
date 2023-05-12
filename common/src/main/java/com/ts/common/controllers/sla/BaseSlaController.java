package com.ts.common.controllers.sla;

import com.ts.common.application.controllers.AuthToken;
import com.ts.common.application.controllers.TrackStudioEndPoints;
import com.ts.common.entitites.tasks.GeneralTask;
import com.ts.common.enums.ComSlaOperations;
import com.ts.common.enums.SlaType;
import com.ts.common.request.ApiRequest;
import com.ts.common.utils.InitEntities;
import com.ts.common.utils.RandomUtils;
import io.qameta.allure.Step;
import io.restassured.response.Response;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

import static com.ts.common.application.controllers.TrackStudioEndPoints.*;
import static com.ts.common.controllers.sla.SlaRequestBody.Fields.ID;
import static com.ts.common.controllers.sla.SlaRequestBody.Fields.OPERATION;
import static com.ts.common.controllers.sla.SlaRequestBody.Fields.*;

public class BaseSlaController extends ApiRequest {
    protected SlaType slaType;
    protected SlaRequestBody.Fields[] DEFAULT_FIELDS = {ID, OPERATION, DESCRIPTION, ATTACHMENTS, UDFS};
    protected SlaRequestBody.Fields[] DEFAULT_FIELDS_WITHOUT_ID = {OPERATION, DESCRIPTION, ATTACHMENTS, UDFS};
    protected SlaRequestBody.Fields[] DEFAULT_FIELDS_CONDITION = {ID, OPERATION, DESCRIPTION, HANDLER_USER, ATTACHMENTS, UDFS};
    protected SlaRequestBody.Fields[] DEFAULT_FIELDS_USER = {OPERATION, DESCRIPTION, HANDLER_USER, ATTACHMENTS, UDFS};

    public BaseSlaController(String url, AuthToken authToken) {
        super(url, HEADERS_BASE_CONTROLLER, authToken);
    }


    protected Response createTask(String requestBody) {
        return super.post(getEndpoint(TASK, UPDATE), requestBody);
    }

    @Step("Получить SLA task, Номер задачи: {0}")
    public Response receiveActualTask(String taskNumber) {
        return super.get(getEndpoint(TASK, INFO, taskNumber));
    }

    protected Response  performOperation(@NotNull GeneralTask slaTask, String requestBody) {
        return this.response = super.post(getEndpoint(TrackStudioEndPoints.OPERATION, slaTask.getNumber(), CREATE), requestBody);
    }

    @Step("Выполнение операции: {0}")
    protected Response performOperationWithQueryParam(@NotNull GeneralTask slaTask, String requestBody) {
        HashMap<String, String> params = new HashMap<>() {{
            put(ID.field, slaTask.getId());
        }};
        return this.response = super.post(getEndpoint(TrackStudioEndPoints.OPERATION, slaTask.getNumber(), CREATE
                , formatParameters(params)), requestBody);
    }

    @Step("Выполнение общей операции: {1}")
    public Response performCommonOperation(GeneralTask slaTask, ComSlaOperations operation) {
        slaTask.setOperation(InitEntities.generateOperationID(this.slaType, operation));
        slaTask.setDescription(RandomUtils.generateDescriptionForOperation(operation));
        SlaRequestBody slaRequestBody = new SlaRequestBody(slaTask);
        if (slaTask.getHandlerUser() == null) {
            return this.response = performOperationWithQueryParam(slaTask, slaRequestBody.keepFields(DEFAULT_FIELDS_WITHOUT_ID));
        }
        return this.response = performOperationWithQueryParam(slaTask, slaRequestBody.keepFields(DEFAULT_FIELDS_USER));
    }

}
