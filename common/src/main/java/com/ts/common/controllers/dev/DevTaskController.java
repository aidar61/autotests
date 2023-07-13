package com.ts.common.controllers.dev;

import com.ts.common.application.controllers.AuthToken;
import com.ts.common.controllers.BaseController;
import com.ts.common.controllers.TaskRequestBody;
import com.ts.common.controllers.TaskResponseBody;
import com.ts.common.entitites.tasks.GeneralTask;
import com.ts.common.enums.TaskType;
import com.ts.common.utils.JsonUtils;
import io.restassured.response.Response;

import static com.ts.common.controllers.TaskRequestBody.Fields.HANDLER_USER;
import static com.ts.common.enums.TaskType.DEV_TASK;

public class DevTaskController extends BaseController {

    private static final TaskType TASK_TYPE = DEV_TASK;
    public DevTaskController(String url, AuthToken authToken) {
        super(url, authToken);
        this.taskType = TASK_TYPE;
    }

    @Override
    protected Response createTask(String requestBody) {
        return super.createTask(requestBody);
    }

    public Response createDevTask(GeneralTask devTask) {
        TaskRequestBody devTaskRequestBody = new TaskRequestBody(devTask);
        this.response = createTask(devTaskRequestBody.keepMandatoryAndCreateFieldsAnd(HANDLER_USER));
        TaskResponseBody gapResponseBody = JsonUtils.deserialize(this.response, TaskResponseBody.class);
        if (gapResponseBody != null) {
            devTask.setId(gapResponseBody.getId());
            devTask.setNumber(gapResponseBody.getNumber());
            devTask.setFinishStatus(gapResponseBody.getFinishStatus());
        }
        return this.response;
    }
}
