package com.ts.common.controllers;

import com.ts.common.application.controllers.AuthToken;
import com.ts.common.entitites.tasks.GeneralTask;
import com.ts.common.utils.JsonUtils;
import io.restassured.response.Response;

import static com.ts.common.controllers.TaskRequestBody.Fields.PRIORITY;

public class ProjectController extends BaseController {
    public ProjectController(String url, AuthToken authToken) {
        super(url, authToken);
    }

    @Override
    protected Response createTask(String requestBody) {
        return super.createTask(requestBody);
    }

    public GeneralTask createProject(GeneralTask project) {
        TaskRequestBody requestBody = new TaskRequestBody(project);
        if (project.getPriority() != null) {
            this.response = createTask(requestBody.keepMandatoryAndCreateFieldsAnd(PRIORITY));
        } else {
            this.response = createTask(requestBody.keepMandatoryAndCreateFields());
        }
        TaskResponseBody projectResponseBody = JsonUtils.deserialize(this.response, TaskResponseBody.class);
        if (projectResponseBody != null) {
            project.setId(projectResponseBody.getId());
            project.setNumber(projectResponseBody.getNumber());
            project.setFinishStatus(projectResponseBody.getFinishStatus());
        }
        return project;
    }
}
