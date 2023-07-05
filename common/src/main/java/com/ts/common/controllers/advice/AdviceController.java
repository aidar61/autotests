package com.ts.common.controllers.advice;

import com.ts.common.application.controllers.AuthToken;
import com.ts.common.controllers.BaseController;
import com.ts.common.controllers.TaskRequestBody;
import com.ts.common.controllers.TaskResponseBody;
import com.ts.common.entitites.tasks.GeneralTask;
import com.ts.common.enums.TaskType;
import com.ts.common.request.ApiRequest;
import com.ts.common.utils.JsonUtils;
import io.restassured.response.Response;

import java.util.Map;

import static com.ts.common.controllers.TaskRequestBody.Fields.HANDLER_USER;
import static com.ts.common.enums.TaskType.ADVICE;

public class AdviceController extends BaseController {
    private static final TaskType TASK_TYPE = ADVICE;

    public AdviceController(String url, AuthToken authToken) {
        super(url, authToken);
        this.taskType = TASK_TYPE;
    }

    @Override
    protected Response createTask(String requestBody) {
        return super.createTask(requestBody);
    }

    public Response createAdvice(GeneralTask adviceTask) {
        TaskRequestBody adviceTaskRequestBody = new TaskRequestBody(adviceTask);
        this.response = createTask(adviceTaskRequestBody.keepMandatoryAndCreateFieldsAnd(HANDLER_USER));
        TaskResponseBody gapResponseBody = JsonUtils.deserialize(this.response, TaskResponseBody.class);
        if (gapResponseBody != null) {
            adviceTask.setId(gapResponseBody.getId());
            adviceTask.setNumber(gapResponseBody.getNumber());
            adviceTask.setFinishStatus(gapResponseBody.getFinishStatus());
        }
        return this.response;
    }
}

