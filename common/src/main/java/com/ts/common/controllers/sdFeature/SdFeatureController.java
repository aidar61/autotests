package com.ts.common.controllers.sdFeature;

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
import static com.ts.common.enums.TaskType.SD_FEATURE;

public class SdFeatureController extends BaseController {
    private static final TaskType TASK_TYPE = SD_FEATURE;

    public SdFeatureController(String url, AuthToken authToken) {
        super(url, authToken);
        this.taskType = SD_FEATURE;
    }

    @Override
    protected Response createTask(String requestBody) {
        return super.createTask(requestBody);
    }

    public Response createTask(GeneralTask task) {
        TaskRequestBody sdFeatureRequestBody = new TaskRequestBody(task);
        this.response = createTask(sdFeatureRequestBody.keepMandatoryAndCreateFields());
        TaskResponseBody sdFeatureResponseBody = JsonUtils.deserialize(this.response, TaskResponseBody.class);
        if (sdFeatureResponseBody != null) {
            task.setId(sdFeatureResponseBody.getId());
            task.setNumber(sdFeatureResponseBody.getNumber());
            task.setFinishStatus(sdFeatureResponseBody.getFinishStatus());
        }
        return this.response;
    }
}
