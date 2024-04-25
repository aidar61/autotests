package com.ts.common.controllers.installation;

import com.ts.common.application.controllers.AuthToken;
import com.ts.common.controllers.BaseController;
import com.ts.common.controllers.TaskRequestBody;
import com.ts.common.controllers.TaskResponseBody;
import com.ts.common.entitites.tasks.GeneralTask;
import com.ts.common.enums.TaskType;
import com.ts.common.utils.JsonUtils;
import io.qameta.allure.Step;
import io.restassured.response.Response;

import static com.ts.common.controllers.TaskRequestBody.Fields.HANDLER_USER;
import static com.ts.common.enums.TaskType.BDKU_INSTALLATION;


public class InstallationController extends BaseController {
    private static final TaskType TYPE = BDKU_INSTALLATION;

    public InstallationController(String url, AuthToken authToken) {
        super(url, authToken);
        this.taskType = TYPE;
    }

    @Step("Создание Инсталляции: {0}")
    protected Response createTask(String requestBody) {
        return super.createTask(requestBody);
    }

    public Response createInstallation(GeneralTask installation) {
        TaskRequestBody slaRequestBody = new TaskRequestBody(installation);
        this.response = createTask(slaRequestBody.keepMandatoryAndCreateFields());
        TaskResponseBody installationResponseBody = JsonUtils.deserialize(this.response, TaskResponseBody.class);
        if (installationResponseBody != null) {
            installation.setId(installationResponseBody.getId());
            installation.setNumber(installationResponseBody.getNumber());
            installation.setFinishStatus(installationResponseBody.getFinishStatus());
        }
        return this.response;
    }
}
