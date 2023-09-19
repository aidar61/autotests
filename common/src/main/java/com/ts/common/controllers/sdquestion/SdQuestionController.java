package com.ts.common.controllers.sdquestion;

import com.ts.common.application.controllers.AuthToken;
import com.ts.common.controllers.BaseController;
import com.ts.common.controllers.TaskRequestBody;
import com.ts.common.controllers.TaskResponseBody;
import com.ts.common.entitites.tasks.GeneralTask;
import com.ts.common.enums.TaskType;
import com.ts.common.utils.JsonUtils;
import io.qameta.allure.Step;
import io.restassured.response.Response;

public class SdQuestionController extends BaseController {
    private static final TaskType TASK_TYPE = TaskType.SD_QUESTION;

    public SdQuestionController(String url, AuthToken authToken) {
        super(url, authToken);
        super.taskType = TASK_TYPE;
    }

    @Override
    @Step("Создание SD Question")
    protected Response createTask(String requestBody) {
        return super.createTask(requestBody);
    }

    public void createSdQuestion(GeneralTask sdQuestionTask) {
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
