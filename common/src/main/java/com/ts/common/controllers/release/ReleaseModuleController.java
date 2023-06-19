package com.ts.common.controllers.release;

import com.ts.common.application.controllers.AuthToken;
import com.ts.common.controllers.BaseController;
import com.ts.common.controllers.TaskRequestBody;
import com.ts.common.controllers.TaskResponseBody;
import com.ts.common.entitites.tasks.GeneralTask;
import com.ts.common.entitites.tasks.Task;
import com.ts.common.enums.TaskType;
import com.ts.common.utils.InitEntities;
import com.ts.common.utils.JsonUtils;
import io.qameta.allure.Step;
import io.restassured.response.Response;
import org.openqa.selenium.json.Json;

import java.util.logging.StreamHandler;

import static com.ts.common.application.controllers.TrackStudioEndPoints.*;
import static com.ts.common.controllers.TaskRequestBody.Fields.PRIORITY;
import static com.ts.common.controllers.TaskRequestBody.Fields.SHORT_NAME;
import static com.ts.common.enums.TaskType.*;

public class ReleaseModuleController extends BaseController {

    private static final TaskType TASK_TYPE = RELEASE_MODULE;

    public ReleaseModuleController(String url, AuthToken authToken) {
        super(url, authToken);
        this.taskType = TASK_TYPE;
    }

    @Step("Создание Создание CAT_RELEASEMODULE: {0}")
    protected Response createTask(String requestBody) {
        return super.createTask(requestBody);
    }

    public Response createCatReleaseModule(GeneralTask releaseModule) {
        TaskRequestBody taskRequestBody = new TaskRequestBody(releaseModule);
        this.response = createTask(taskRequestBody.keepMandatoryAndCreateFieldsAnd(SHORT_NAME, PRIORITY));
        TaskResponseBody releaseModuleBody = JsonUtils.deserialize(this.response, TaskResponseBody.class);
        if (releaseModuleBody != null) {
            releaseModule.setId(releaseModuleBody.getId());
            releaseModule.setNumber(releaseModuleBody.getNumber());
            releaseModule.setFinishStatus(releaseModuleBody.getFinishStatus());
            releaseModule.setStatusName(releaseModuleBody.getStatusName());
            releaseModule.setPriorityName(releaseModule.getPriorityName());
        }
        return this.response;
    }

    public Response receiveForm(GeneralTask releaseModule) {
        this.response = super.get(getEndpoint(REST, TASK, CREATE, releaseModule.getNumber(), InitEntities.generateCategory(RELEASE).getId()));
        TaskResponseBody task = JsonUtils.deserialize(this.response, TaskResponseBody.class);
        if (task != null) {
            releaseModule.setName(task.getName());
            releaseModule.setShortName(task.getShortName());
        }
        return this.response;
    }
}
