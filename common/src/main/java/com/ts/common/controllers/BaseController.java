package com.ts.common.controllers;

import com.ts.common.application.controllers.AuthToken;
import com.ts.common.application.controllers.TrackStudioEndPoints;
import com.ts.common.entitites.commonEntities.Udfs;
import com.ts.common.entitites.tasks.GeneralTask;
import com.ts.common.enums.Operations;
import com.ts.common.enums.TaskType;
import com.ts.common.request.ApiRequest;
import com.ts.common.utils.InitEntities;
import com.ts.common.utils.RandomUtils;
import io.qameta.allure.Step;
import io.restassured.response.Response;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

import static com.ts.common.application.controllers.TrackStudioEndPoints.*;

public class BaseController extends ApiRequest {
    @Getter
    protected TaskType taskType;
    protected TaskRequestBody.Fields[] DEFAULT_FIELDS = {TaskRequestBody.Fields.ID, TaskRequestBody.Fields.OPERATION, TaskRequestBody.Fields.DESCRIPTION, TaskRequestBody.Fields.ATTACHMENTS, TaskRequestBody.Fields.UDFS};
    protected TaskRequestBody.Fields[] DEFAULT_FIELDS_WITHOUT_ID = {TaskRequestBody.Fields.OPERATION, TaskRequestBody.Fields.DESCRIPTION, TaskRequestBody.Fields.ATTACHMENTS, TaskRequestBody.Fields.UDFS};
    protected TaskRequestBody.Fields[] DEFAULT_FIELDS_CONDITION = {TaskRequestBody.Fields.ID, TaskRequestBody.Fields.OPERATION, TaskRequestBody.Fields.DESCRIPTION, TaskRequestBody.Fields.HANDLER_USER, TaskRequestBody.Fields.ATTACHMENTS, TaskRequestBody.Fields.UDFS};
    protected TaskRequestBody.Fields[] DEFAULT_FIELDS_USER = {TaskRequestBody.Fields.OPERATION, TaskRequestBody.Fields.DESCRIPTION, TaskRequestBody.Fields.HANDLER_USER, TaskRequestBody.Fields.ATTACHMENTS, TaskRequestBody.Fields.UDFS};
    public TaskRequestBody.Fields[] DEFAULT_FIELDS_WITH_STATUS = {TaskRequestBody.Fields.OPERATION, TaskRequestBody.Fields.DESCRIPTION, TaskRequestBody.Fields.ATTACHMENTS, TaskRequestBody.Fields.UDFS, TaskRequestBody.Fields.FINISH_STATUS};

    public BaseController(String url, AuthToken authToken) {
        super(url, HEADERS_BASE_CONTROLLER, authToken);
    }


    protected Response createTask(String requestBody) {
        return super.post(getEndpoint(REST, TASK, UPDATE), requestBody);
    }

    @Step("Получить task, Номер задачи: {0}")
    public Response receiveActualTask(String taskNumber) {
        return super.get(getEndpoint(REST, TASK, INFO, taskNumber));
    }

    protected Response performOperation(@NotNull GeneralTask task, String requestBody) {
        return this.response = super.post(getEndpoint(REST, TrackStudioEndPoints.OPERATION, task.getNumber(), CREATE), requestBody);
    }

    @Step("Выполнение операции: {0}")
    public Response performOperationWithQueryParam(@NotNull GeneralTask task, String requestBody) {
        HashMap<String, String> params = new HashMap<>() {{
            put(TaskRequestBody.Fields.ID.field, task.getId());
        }};
        return this.response = super.post(getEndpoint(REST, TrackStudioEndPoints.OPERATION, task.getNumber(), CREATE
                , formatParameters(params)), requestBody);
    }

    @Step("Выполнение общей операции: {1}")
    public Response performCommonOperation(GeneralTask task, Operations operation) {
        task.setOperation(InitEntities.generateOperationID(this.taskType, operation));
        task.setDescription(RandomUtils.generateDescriptionForOperation(operation));
        TaskRequestBody slaRequestBody = new TaskRequestBody(task);
        if (task.getHandlerUser() == null) {
            return this.response = performOperationWithQueryParam(task, slaRequestBody.keepFields(DEFAULT_FIELDS_WITHOUT_ID));
        }
        return this.response = performOperationWithQueryParam(task, slaRequestBody.keepFields(DEFAULT_FIELDS_USER));
    }

    public Response receiveDaughterTasksForUdfFields(Udfs.UdfSd udfSd, String taskNumber, String parentNumber) {
        HashMap<String, String> params = new HashMap<>() {{
            put(TaskRequestBody.Fields.PARENT.field, parentNumber);
        }};
        return this.response = super.get(getEndpoint(REST, UDF_VAL, udfSd.udfId, TASK, taskNumber, TASK, LIST, formatParameters(params)));
    }

}
