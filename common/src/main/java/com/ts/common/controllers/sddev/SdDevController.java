package com.ts.common.controllers.sddev;

import com.ts.common.application.controllers.AuthToken;
import com.ts.common.controllers.BaseController;
import com.ts.common.controllers.TaskRequestBody;
import com.ts.common.controllers.TaskResponseBody;
import com.ts.common.entitites.tasks.GeneralTask;
import com.ts.common.enums.TaskType;
import com.ts.common.utils.JsonUtils;
import io.restassured.response.Response;

import static com.ts.common.controllers.TaskRequestBody.Fields.HANDLER_USER;
import static com.ts.common.enums.TaskType.SD_DEV;

public class SdDevController extends BaseController {

    private static final TaskType TASK_TYPE = SD_DEV;

    public SdDevController(String url, AuthToken authToken) {
        super(url, authToken);
        this.taskType = TASK_TYPE;
    }

    @Override
    protected Response createTask(String requestBody) {
        return super.createTask(requestBody);
    }

    public Response create(GeneralTask task) {
        TaskRequestBody devTaskRequestBody = new TaskRequestBody(task);
        this.response = createTask(devTaskRequestBody.keepMandatoryAndCreateFieldsAnd(HANDLER_USER));
        TaskResponseBody gapResponseBody = JsonUtils.deserialize(this.response, TaskResponseBody.class);
        if (gapResponseBody != null) {
            task.setId(gapResponseBody.getId());
            task.setNumber(gapResponseBody.getNumber());
            task.setFinishStatus(gapResponseBody.getFinishStatus());
        }
        return this.response;
    }
}
