package com.ts.common.controllers.bug;

import com.ts.common.application.controllers.AuthToken;
import com.ts.common.controllers.BaseController;
import com.ts.common.controllers.TaskRequestBody;
import com.ts.common.controllers.TaskResponseBody;
import com.ts.common.entitites.tasks.GeneralTask;
import com.ts.common.entitites.tasks.Task;
import com.ts.common.enums.TaskType;
import com.ts.common.utils.JsonUtils;
import io.restassured.response.Response;

import java.util.List;

import static com.ts.common.controllers.TaskRequestBody.Fields.HANDLER_USER;
import static com.ts.common.enums.TaskType.WORK_TASK;

public class BugTaskController extends BaseController {

    public static TaskType TASK_TYPE = WORK_TASK;
    private static String parentDetailInString;

    public BugTaskController(String url, AuthToken authToken) {
        super(url, authToken);
        this.taskType = TASK_TYPE;
    }

    public static Task getLastTask(List<Task> subTasksAcceptWork) {
        return subTasksAcceptWork.stream().max((obj1, obj2) -> {
            var id1 = Integer.parseInt(obj1.getNumber());
            var id2 = Integer.parseInt(obj2.getNumber());
            return Integer.compare(id1, id2);
        }).orElse(null);
    }

    public static Task getFirstTask(List<Task> subTasksAcceptWork) {
        return subTasksAcceptWork.stream().min((obj1, obj2) -> {
            var id1 = Integer.parseInt(obj1.getNumber());
            var id2 = Integer.parseInt(obj2.getNumber());
            return Integer.compare(id1, id2);
        }).orElse(null);
    }

    @Override
    protected Response createTask(String requestBody) {
        return super.createTask(requestBody);
    }

    public Response createBagTask(GeneralTask devTask) {
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

    public Response updateBugTask(GeneralTask devTask) {
        TaskRequestBody devTaskRequestBody = new TaskRequestBody(devTask);
        this.response = createTask(devTaskRequestBody.keepMandatoryAndCreateFieldsAnd(TaskRequestBody.Fields.NUMBER));
        TaskResponseBody gapResponseBody = JsonUtils.deserialize(this.response, TaskResponseBody.class);
        if (gapResponseBody != null) {
            devTask.setId(gapResponseBody.getId());
            devTask.setNumber(gapResponseBody.getNumber());
            devTask.setFinishStatus(gapResponseBody.getFinishStatus());
        }
        return this.response;
    }
}
