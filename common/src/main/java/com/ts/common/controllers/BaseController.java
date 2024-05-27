package com.ts.common.controllers;

import com.ts.common.application.controllers.AuthToken;
import com.ts.common.application.controllers.TrackStudioEndPoints;
import com.ts.common.application.controllers.TrackStudioHttpStatusCodes;
import com.ts.common.entitites.commonEntities.Task;
import com.ts.common.entitites.commonEntities.Udfs;
import com.ts.common.entitites.commonEntities.User;
import com.ts.common.entitites.commonEntities.udf.UdfList;
import com.ts.common.entitites.commonEntities.udf.UdfTask;
import com.ts.common.entitites.tasks.GeneralTask;
import com.ts.common.enums.Operations;
import com.ts.common.enums.TaskType;
import com.ts.common.request.ApiRequest;
import com.ts.common.utils.JsonUtils;
import com.ts.common.utils.RandomUtils;
import io.qameta.allure.Step;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.config.YamlProcessor;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import static com.ts.common.application.controllers.TrackStudioEndPoints.*;
import static com.ts.common.controllers.TaskRequestBody.Fields.ID;
import static com.ts.common.controllers.TaskRequestBody.Fields.OPERATION;
import static com.ts.common.controllers.TaskRequestBody.Fields.*;
import static com.ts.common.utils.InitEntities.generateOperationID;

@Slf4j
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

    @Step("Получить автора, Номер задачи: {0}")
    public List<User> receiveAuthor(String taskNumber) {
        try {
            var response = super.get(getEndpoint(REST, TrackStudioEndPoints.OPERATION, "MSG_SDBUG_CHANGEAUTHOR", taskNumber, "context"));
            if (response.getStatusCode() == TrackStudioHttpStatusCodes.HTTP_OK.getValue()) {
                var user = new JsonPath(response.asString()).getList("udfs.UDF_SD_AUTHORCLIENT_MSG.userValueSelector", User.class);
                if (user == null) {
                    user = new JsonPath(response.asString()).getList("udfs.UDF_SD_AUTHORCLIENT_MSG.userValue", User.class);
                }
                return user;
            }
            return null;
        } catch (Exception e) {
            log.error("Не удалось получить автора для задачи {}: {}", taskNumber, e.getMessage());
        }
        return null;
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
        return this.response = super.post(getEndpoint(REST, TrackStudioEndPoints.OPERATION, task.getNumber(), CREATE, formatParameters(params)), requestBody);
    }

    @Step("Выполнение общей операции: {1}")
    public Response performCommonOperation(@NotNull GeneralTask task, Operations operation) {
        task.setOperation(
                this.taskType != null ?
                        generateOperationID(this.taskType, operation) :
                        generateOperationID(task.getTaskType(), operation)
        );
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

    public Response receiveTaskMessages(String taskNumber) {
        super.setAuthToken(new AuthToken("root", "password"));
        return this.response = super.get(getEndpoint(REST, TASK, INFO, taskNumber, "messages"));
    }

    public Response getBackLinks(String taskNumber) {
        super.setAuthToken(new AuthToken("root", "password"));
        return super.get(getEndpoint(REST, TASK, INFO, taskNumber, "back-links"));
    }

    public void saveTaskStatusFromResponse(GeneralTask task) {
        TaskResponseBody taskResponseBody = JsonUtils.deserialize(this.response, TaskResponseBody.class);
        assert taskResponseBody != null;
        task.setFinishStatus(taskResponseBody.receiveStatus());
    }

    public Response getParentPayload(String parentNumber, String category) {
        try {
            super.setAuthToken(new AuthToken("root", "password"));
            return super.get(getEndpoint(REST, TASK, CREATE, parentNumber, category));
        } catch (Exception e) {
            log.error("Не удалось получить данные родительской задачи {}: {}", parentNumber, e.getMessage());
        }
        return null;
    }

    public List<String> getParent_UDF_MIS_SERVICE(String parentDetailInString) {
        try {
            var misServiceListValue = new JsonPath(parentDetailInString).getList("udfs.UDF_MIS_SERVICE.listValue.id", String.class);
            if (!misServiceListValue.isEmpty()) {
                return misServiceListValue;
            }
            misServiceListValue = new JsonPath(parentDetailInString).getList("udfs.UDF_MIS_SERVICE.listValueSelector.id", String.class);
            if (!misServiceListValue.isEmpty()) {
                return misServiceListValue;
            }
        } catch (Exception e) {
            log.error("Не удалось получить Услугу для задачи: {}", e.getMessage());
        }

        return null;
    }

    public List<String> getParent_UDF_CDP_BL(String parentDetailInString) {
        try {
            var UDF_CDP_BL = new JsonPath(parentDetailInString).getObject("udfs.UDF_CDP_BL", UdfList.class);
            if (UDF_CDP_BL != null) {
                if (UDF_CDP_BL.getListValue() != null && UDF_CDP_BL.getListValue().length > 0) {
                    return Arrays.stream(UDF_CDP_BL.getListValue()).map(com.ts.common.entitites.commonEntities.List::getId).collect(Collectors.toList());
                }
                if (UDF_CDP_BL.getListValueSelector() != null && UDF_CDP_BL.getListValueSelector().length > 0) {
                    return Arrays.stream(UDF_CDP_BL.getListValueSelector()).map(com.ts.common.entitites.commonEntities.List::getId).collect(Collectors.toList());
                }
            }
        } catch (Exception e) {
            log.error("Не удалось получить Направление деятельности для задачи: {}", e.getMessage());
        }

        return null;
    }

    public List<String> getParent_WORKTASK_QUALIFICATION(String parentDetailInString) {
        try {
            return new JsonPath(parentDetailInString).getList("udfs.UDF_WORKTASK_QUALIFICATION.listValueSelector.id", String.class);
        } catch (Exception e) {
            log.error("Не удалось получить Квалификацию для задачи: {}", e.getMessage());
        }
        return null;
    }

    public Task[] getParent_UDF_PRODUCT(String parentDetailInString) {
        try {
            var product = new JsonPath(parentDetailInString).getObject("udfs.UDF_PRODUCT", UdfTask.class);
            if (product.getTaskValue() != null && product.getTaskValue().length > 1) {
                return product.getTaskValue();
            }
            if (product.getTaskValueSelector() != null) {
                return product.getTaskValueSelector();
            }
        } catch (Exception e) {
            log.error("Не удалось получить Проект БДКУ для задачи: {}", e.getMessage());
        }
        return null;
    }

    public Response getTaskMessages(String taskNumber) {

        try {
            return
                    super.get(getEndpoint(REST, TASK, INFO, taskNumber, "messages?order=desc"));
        } catch (Exception e) {
            log.error("Не удалось получить операции для задачи: {}", e.getMessage());
        }
        return null;
    }

    public User[] getAuthors(TaskType taskType, Operations operations, String taskNumber) {

        try {
            super.setAuthToken(new AuthToken("root", "password"));
            var operationId = generateOperationID(taskType, operations);
            var response =
                    super.get(getEndpoint(REST, TrackStudioEndPoints.OPERATION, operationId.getId(), taskNumber, "context"));
            var authors = new JsonPath(response.asString()).getObject("udfs.UDF_SD_AUTHORCLIENT_MSG.userValueSelector", User[].class);
            if (authors != null && authors.length > 0) {
                return authors;
            }
        } catch (Exception e) {
            log.error("Не удалось получить авторов для задачи: {}", e.getMessage());
        }
        return null;
    }

    public Task[] getParent_UDF_BDKU_CONFIGURATION(String parentDetailInString) {
        try {
            var configuration = new JsonPath(parentDetailInString).getObject("udfs.UDF_BDKU_CONFIGURATION", UdfTask.class);
            if (configuration.getTaskValue() != null && configuration.getTaskValue().length > 1) {
                return configuration.getTaskValue();
            }
            if (configuration.getTaskValueSelector() != null) {
                return configuration.getTaskValueSelector();
            }
        } catch (Exception e) {
            log.error("Не удалось получить Конфигурацию для задачи: {}", e.getMessage());
        }
        return null;
    }

    public List<String> getUDF_SLA_CONSULTPROVIDEDATE(TaskType taskType, Operations operations, String taskNumber) {
        try {
            super.setAuthToken(new AuthToken("root", "password"));
            var operationId = generateOperationID(taskType, operations);
            var response =
                    super.get(getEndpoint(REST, TrackStudioEndPoints.OPERATION, operationId.getId(), taskNumber, "context"));
            return new JsonPath(response.asString()).getList("udfs.UDF_SLA_CONSULTPROVIDEDATE.stringValueSelector", String.class);

        } catch (Exception e) {
            log.error("Не удалось получить Дата предоставления консультации: {}", e.getMessage());
        }
        return null;
    }

    public List<String> getBranches(String parentDetailString) {
        return new JsonPath(parentDetailString).getList("udfs.UDF_WORKTASK_BRANCH.stringValueSelector", String.class);
    }

    public com.ts.common.entitites.commonEntities.List[] getParent_UDF_SD_RELATED_TASK_CODES(String parentDetailInString) {
        try {
            var product = new JsonPath(parentDetailInString).getObject("udfs.UDF_SD_RELATED_TASK_CODES", UdfList.class);
            if (product.getListValue() != null && product.getListValue().length > 1) {
                return product.getListValue();
            }
            if (product.getListValueSelector() != null) {
                return product.getListValueSelector();
            }
        } catch (Exception e) {
            log.error("Не удалось получить Проект БДКУ для задачи: {}", e.getMessage());
        }
        return null;
    }

    public Response getOpretionPermission(String operationName){
        return super.get(getEndpoint(REST, WORKFLOW, MSTATUS, operationName, PERMISSIONS));

    }
}
