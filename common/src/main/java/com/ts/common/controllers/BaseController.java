package com.ts.common.controllers;

import com.ts.common.application.controllers.AuthToken;
import com.ts.common.application.controllers.TrackStudioEndPoints;
import com.ts.common.entitites.commonEntities.Udfs;
import com.ts.common.entitites.tasks.GeneralTask;
import com.ts.common.enums.Operations;
import com.ts.common.enums.TaskType;
import com.ts.common.request.ApiRequest;
import com.ts.common.utils.InitEntities;
import com.ts.common.utils.JsonUtils;
import com.ts.common.utils.RandomUtils;
import io.qameta.allure.Step;
import io.restassured.response.Response;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

import static com.ts.common.application.controllers.TrackStudioEndPoints.*;
import static com.ts.common.controllers.TaskRequestBody.Fields.ID;
import static com.ts.common.controllers.TaskRequestBody.Fields.OPERATION;
import static com.ts.common.controllers.TaskRequestBody.Fields.*;

public class BaseController extends ApiRequest {
    public TaskRequestBody.Fields[] DEFAULT_FIELDS_WITH_STATUS = {OPERATION, DESCRIPTION, ATTACHMENTS, UDFS, FINISH_STATUS};
    public TaskRequestBody.Fields[] DEFAULT_FIELDS_WITH_STATUS_AND_RESOLUTION = {OPERATION, DESCRIPTION, ATTACHMENTS, UDFS, FINISH_STATUS, RESOLUTION, CONFIRMED, HANDLER_USER};
    @Getter
    protected TaskType taskType;
    protected TaskRequestBody.Fields[] DEFAULT_FIELDS = {ID, OPERATION, DESCRIPTION, ATTACHMENTS, UDFS};
    protected TaskRequestBody.Fields[] DEFAULT_FIELDS_WITHOUT_ID = {OPERATION, DESCRIPTION, ATTACHMENTS, UDFS};
    protected TaskRequestBody.Fields[] DEFAULT_FIELDS_WITH_CONFIRM = {OPERATION, DESCRIPTION, ATTACHMENTS, UDFS, CONFIRMED};
    protected TaskRequestBody.Fields[] DEFAULT_FIELDS_WITH_RESOLUTION = {OPERATION, DESCRIPTION, ATTACHMENTS, UDFS, RESOLUTION};
    protected TaskRequestBody.Fields[] DEFAULT_FIELDS_CONDITION = {ID, OPERATION, DESCRIPTION, HANDLER_USER, ATTACHMENTS, UDFS};
    protected TaskRequestBody.Fields[] DEFAULT_FIELDS_USER = {OPERATION, DESCRIPTION, HANDLER_USER, ATTACHMENTS, UDFS};

    public BaseController(String url, AuthToken authToken) {
        super(url, HEADERS_BASE_CONTROLLER, authToken);
    }

    public void changeTaskType(TaskType taskType) {
        this.taskType = taskType;
    }

    protected Response createTask(String requestBody) {
        return super.post(getEndpoint(REST, TASK, UPDATE), requestBody);
    }

    public Response receiveContextByOperation(String operations, String taskNumber) {
        return super.get(getEndpoint(REST, TrackStudioEndPoints.OPERATION, operations, taskNumber, "context"));
    }

    protected Response createTask(GeneralTask generalTask) {
        TaskRequestBody requestBody = new TaskRequestBody(generalTask);
        this.response = createTask(requestBody.keepMandatoryAndCreateFieldsAnd(HANDLER_USER));
        TaskResponseBody gapResponseBody = JsonUtils.deserialize(this.response, TaskResponseBody.class);
        if (gapResponseBody != null) {
            generalTask.setId(gapResponseBody.getId());
            generalTask.setNumber(gapResponseBody.getNumber());
            generalTask.setFinishStatus(gapResponseBody.getFinishStatus());
        }
        return this.response;
    }

    @Step("Получить task, Номер задачи: {0}")
    public Response receiveActualTask(String taskNumber) {
        return this.response = super.get(getEndpoint(REST, TASK, INFO, taskNumber));
    }

    @Step("Получить parent task payload, Номер задачи: {0}")
    public Response receiveParentTaskPayload(String parentTaskNumber, String category) {
        return this.response = super.get(getEndpoint(REST, TASK, CREATE, parentTaskNumber, category));
    }

    @Step("Проверить открытие формы  MSG_WORKTASK_FINISHDEV, для задачи: {0}")
    public Response receiveFinishDevForm(String taskNumber) {
        return this.response = super.get(getEndpoint(REST, TrackStudioEndPoints.OPERATION, "MSG_WORKTASK_FINISHDEV", taskNumber, "context"));
    }

    @Step("Получить все подзадачи, Номер задачи: {0}")
    public Response receiveAllSubTask(String taskNumber) {
        return super.get(getEndpoint(REST, TASK, INFO, taskNumber, "filter/8a8181df6e1089ea016e120b41da29d8/1/100"));
    }

    @Step("Получить активные подзадачи, Номер задачи: {0}")
    public Response receiveActiveSubTask(String taskNumber) {
        return super.get(getEndpoint(REST, TASK, INFO, taskNumber, "filter/1/1/50"));
    }

    protected Response performOperation(@NotNull GeneralTask task, String requestBody) {
        return this.response = super.post(getEndpoint(REST, TrackStudioEndPoints.OPERATION, task.getNumber(), CREATE), requestBody);
    }

    @Step("Выполнение операции: {0}")
    public Response performOperationWithQueryParam(@NotNull GeneralTask task, String requestBody) {
        HashMap<String, String> params = new HashMap<>() {{
            put(ID.field, task.getId());
        }};
        return this.response = super.post(getEndpoint(REST, TrackStudioEndPoints.OPERATION, task.getNumber(), CREATE
                , formatParameters(params)), requestBody);
    }

    @Step("Выполнение общей операции: {1}")
    public Response performCommonOperation(@NotNull GeneralTask task, Operations operation) {
        task.setOperation(InitEntities.generateOperationID(this.taskType, operation));
        if (task.getDescription() == null) task.setDescription(RandomUtils.generateDescriptionForOperation(operation));
        TaskRequestBody taskRequestBody = new TaskRequestBody(task);
        if (task.getResolution() != null) {
            if (task.getFinishStatus() != null) {
                return this.response = performOperationWithQueryParam(task, taskRequestBody.keepFields(DEFAULT_FIELDS_WITH_STATUS_AND_RESOLUTION));
            }
            return this.response = performOperationWithQueryParam(task, taskRequestBody.keepFields(DEFAULT_FIELDS_WITH_RESOLUTION));
        }
        if (task.getConfirmed() != null) {
            return this.response = performOperationWithQueryParam(task, taskRequestBody.keepFields(DEFAULT_FIELDS_WITH_CONFIRM));
        }
        if (task.getHandlerUser() != null) {
            return this.response = performOperationWithQueryParam(task, taskRequestBody.keepFields(DEFAULT_FIELDS_USER));
        }

        return this.response = performOperationWithQueryParam(task, taskRequestBody.keepFields(DEFAULT_FIELDS_WITHOUT_ID));
    }

    public Response receiveDaughterTasksForUdfFields(Udfs.UdfSd udfSd, String taskNumber, String parentNumber) {
        HashMap<String, String> params = new HashMap<>() {{
            put(TaskRequestBody.Fields.PARENT.field, parentNumber);
        }};
        return this.response = super.get(getEndpoint(REST, UDF_VAL, udfSd.udfId, TASK, taskNumber, TASK, LIST, formatParameters(params)));
    }

    public Response getBackLinks(String taskNumber) {
        return super.get(getEndpoint(REST, TASK, INFO, taskNumber, "back-links"));
    }

    public void saveTaskStatusFromResponse(GeneralTask task) {
        TaskResponseBody taskResponseBody = JsonUtils.deserialize(this.response, TaskResponseBody.class);
        assert taskResponseBody != null;
        task.setFinishStatus(taskResponseBody.receiveStatus());
    }
}
