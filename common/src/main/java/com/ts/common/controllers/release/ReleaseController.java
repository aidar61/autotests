package com.ts.common.controllers.release;

import com.ts.common.application.controllers.AuthToken;
import com.ts.common.controllers.BaseController;
import com.ts.common.controllers.TaskRequestBody;
import com.ts.common.controllers.TaskResponseBody;
import com.ts.common.entitites.tasks.GeneralTask;
import com.ts.common.enums.TaskType;
import com.ts.common.utils.JsonUtils;
import io.restassured.response.Response;

import static com.ts.common.application.controllers.TrackStudioEndPoints.REST;
import static com.ts.common.controllers.TaskRequestBody.Fields.HANDLER_USER;
import static com.ts.common.enums.TaskType.RELEASE;

public class ReleaseController extends BaseController {

    private static final TaskType TASK_TYPE = RELEASE;

    public ReleaseController(String url, AuthToken authToken) {
        super(url, authToken);
        this.taskType = TASK_TYPE;
    }

    @Override
    protected Response createTask(String requestBody) {
        return super.createTask(requestBody);
    }

    public Response addTask(String[] tasks, String taskNumber) {
        return super.post(getEndpoint(REST, "portlet", "release", "addtasks", taskNumber), tasks);
    }

    public Response removeTask(String[] tasks, String taskNumber) {
        return super.post(getEndpoint(REST, "portlet", "release", "removetasks", taskNumber), tasks);
    }

    public Response addClient(String[] clients, String taskNumber) {
        return super.post(getEndpoint(REST, "portlet", "release", "addclients", taskNumber), clients);
    }

    public Response removeClient(String[] clients, String taskNumber) {
        return super.post(getEndpoint(REST, "portlet", "release", "removeclients", taskNumber), clients);
    }

    public Response getClients(String taskNumber) {
        return super.get(getEndpoint(REST, "portlet", "release", "getClients", taskNumber));
    }

    public Response getTasks(String taskNumber) {
        return super.get(getEndpoint(REST, "portlet", "release", "gettasks", taskNumber));
    }

    public Response getCompat(String taskNumber) {
        return super.get(getEndpoint(REST, "portlet", "release", "getcompat", taskNumber));
    }

    public Response getBackLinkCompat(String taskNumber) {
        return super.get(getEndpoint(REST, "portlet", "release", "getbacklinkcompat", taskNumber));
    }

    public Response generateDescription(String taskNumber, String key, String data) {
        return super.postWithForm(getEndpoint(REST, "portlet", "release", "generateDescription", taskNumber), "data", data);
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
