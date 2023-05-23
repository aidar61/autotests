package com.ts.common.controllers.gap;

import com.ts.common.application.controllers.AuthToken;
import com.ts.common.controllers.BaseController;
import com.ts.common.controllers.TaskRequestBody;
import com.ts.common.controllers.TaskResponseBody;
import com.ts.common.entitites.tasks.GeneralTask;
import com.ts.common.enums.TaskType;
import com.ts.common.utils.JsonUtils;
import io.qameta.allure.Step;
import io.restassured.response.Response;

import static com.ts.common.enums.TaskType.GAP_SOLUTION;

public class GapSolutionController extends BaseController {
    private static final TaskType TASK_TYPE = GAP_SOLUTION;

    public GapSolutionController(String url, AuthToken authToken) {
        super(url, authToken);
        this.taskType = TASK_TYPE;
    }

    @Step("Создание выбранного решения Gap: {0}")
    protected Response createTask(String requestBody) {
        return super.createTask(requestBody);
    }

    public Response createGapSolution(GeneralTask gapSolution) {
        TaskRequestBody taskRequestBody = new TaskRequestBody(gapSolution);
        this.response = createTask(taskRequestBody.keepMandatoryAndCreateFieldsAnd(TaskRequestBody.Fields.HANDLER_USER));
        TaskResponseBody gapResponseBody = JsonUtils.deserialize(this.response, TaskResponseBody.class);
        if (gapResponseBody != null) {
            gapSolution.setId(gapResponseBody.getId());
            gapSolution.setNumber(gapResponseBody.getNumber());
            gapSolution.setFinishStatus(gapResponseBody.getFinishStatus());
        }
        return this.response;
    }
}
