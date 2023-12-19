package com.ts.common.controllers;

import com.ts.common.application.controllers.AuthToken;
import com.ts.common.entitites.BaseEntity;
import com.ts.common.entitites.commonEntities.Task;
import com.ts.common.entitites.commonEntities.Udfs;
import com.ts.common.entitites.commonEntities.udf.UdfTask;
import com.ts.common.entitites.tasks.GeneralTask;
import com.ts.common.enums.TaskType;
import com.ts.common.services.models.ListValueSelector;
import com.ts.common.utils.JsonUtils;
import io.qameta.allure.Step;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;

import static com.ts.common.application.controllers.TrackStudioEndPoints.*;
import static com.ts.common.entitites.commonEntities.Udfs.UdfSd.UDF_SD_MODULE;

public class CreateFromExcelTaskController extends BaseController {
    private static final TaskType TASK_TYPE = TaskType.CAT_CFGTASK;
    private String parentTaskPayload = "";

    public CreateFromExcelTaskController(String url, AuthToken authToken) {
        super(url, authToken);
        super.taskType = TASK_TYPE;
    }

    public Task getModuleByName(String term, String parentNumber) {
        var response = super.get(getEndpoint(REST, UDF_VAL, "UDF_SD_MODULE", TASK, parentNumber, TASK, "search?term=" + term.substring(0, 10)));
        if (response != null)
            return new JsonPath(response.asString()).getObject("[0]", Task.class);
        return null;
    }

    public void getParentPayload(String parentNumber) {
        var response = super.get(getEndpoint(REST, TASK, CREATE, parentNumber, "CAT_CFGTASK"));
        if (response != null)
            parentTaskPayload = response.asString().replace("\\&", "\\\\&");
    }

    public String getParentCDP_BL(String searchTerm) {
        var valueSelector = new JsonPath(parentTaskPayload).getList("udfs.UDF_CDP_BL.listValueSelector", String.class);
        var result = valueSelector.stream().filter(s -> s.contains(searchTerm)).findAny().get();
        return result;
    }

    public UdfTask getUdfProduct(){
        return new JsonPath(parentTaskPayload).getObject("udfs.UDF_PRODUCT", UdfTask.class);
    }

    public ListValueSelector getParentUdfListValueSelector(String searchTerm, Udfs.UdfSd udfType) {
        var result = new JsonPath(parentTaskPayload).getList("udfs." + udfType.udfId + ".listValueSelector", ListValueSelector.class);
        return result.stream().filter(s -> s.getName().contains(searchTerm)).findFirst().get();
    }
    public String getParentUdfList(String searchTerm, Udfs.UdfSd udfType) {
        var result = new JsonPath(parentTaskPayload).getList("udfs." + udfType.udfId + ".listValue", ListValueSelector.class);
        return result.stream().filter(s -> s.getName().contains(searchTerm)).findFirst().get().getId();
    }

    public ListValueSelector getParentFieldId(String searchTerm, String field) {
        var result = new JsonPath(parentTaskPayload).getList(field, ListValueSelector.class);
        return result.stream().filter(s -> s.getName().equals(searchTerm)).findFirst().get();
    }

    public UdfTask getParentUdfTask(String type) {
        return new JsonPath(parentTaskPayload).getObject("udfs." + type, UdfTask.class);
    }


    @Override
    @Step("Создание CAT_CFGTASK")
    protected Response createTask(String requestBody) {
        return super.createTask(requestBody);
    }

    public void createExcelTask(GeneralTask sdQuestionTask) {
        TaskRequestBody sdQuestionRequestBody = new TaskRequestBody(sdQuestionTask);
        this.response = createTask(sdQuestionRequestBody.keepMandatoryAndCreateFields());
        TaskResponseBody taskResponseBody = JsonUtils.deserialize(this.response, TaskResponseBody.class);
        if (taskResponseBody != null) {
            sdQuestionTask.setId(taskResponseBody.getId());
            sdQuestionTask.setNumber(taskResponseBody.getNumber());
            sdQuestionTask.setFinishStatus(taskResponseBody.getFinishStatus());
        }
    }
}
