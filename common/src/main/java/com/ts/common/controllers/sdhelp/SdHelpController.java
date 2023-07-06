package com.ts.common.controllers.sdhelp;

import com.ts.common.application.controllers.AuthToken;
import com.ts.common.controllers.BaseController;
import com.ts.common.controllers.TaskRequestBody;
import com.ts.common.controllers.TaskResponseBody;
import com.ts.common.entitites.tasks.GeneralTask;
import com.ts.common.enums.TaskType;
import com.ts.common.utils.JsonUtils;
import io.qameta.allure.Step;
import io.restassured.response.Response;
import jdk.jfr.Registered;

public class SdHelpController extends BaseController {
    private static final TaskType TASK_TYPE = TaskType.SD_HELP;

    public SdHelpController(String url, AuthToken authToken) {
        super(url, authToken);
        super.taskType = TASK_TYPE;
    }

    @Override
    @Step("Создание SD Help")
    protected Response createTask(String requestBody) {
        return super.createTask(requestBody);
    }

    public void createSdHelp(GeneralTask sdHelpTask) {
        TaskRequestBody sdHelpRequestBody = new TaskRequestBody(sdHelpTask);
        this.response = createTask(sdHelpRequestBody.keepMandatoryAndCreateFields());
        TaskResponseBody taskResponseBody = JsonUtils.deserialize(this.response, TaskResponseBody.class);
        if (taskResponseBody != null) {
            sdHelpTask.setId(taskResponseBody.getId());
            sdHelpTask.setNumber(taskResponseBody.getNumber());
            sdHelpTask.setFinishStatus(taskResponseBody.getFinishStatus());
        }
    }

}
