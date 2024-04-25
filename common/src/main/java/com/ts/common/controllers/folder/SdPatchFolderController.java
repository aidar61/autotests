package com.ts.common.controllers.folder;

import com.ts.common.application.controllers.AuthToken;
import com.ts.common.controllers.BaseController;
import com.ts.common.controllers.TaskRequestBody;
import com.ts.common.controllers.TaskResponseBody;
import com.ts.common.entitites.tasks.GeneralTask;
import com.ts.common.enums.TaskType;
import com.ts.common.utils.JsonUtils;
import io.restassured.response.Response;

import static com.ts.common.enums.TaskType.SDPATCHFOLDER;

public class SdPatchFolderController extends BaseController {
    private static final TaskType TYPE = SDPATCHFOLDER;

    public SdPatchFolderController(String url, AuthToken authToken) {
        super(url, authToken);
        this.taskType = TYPE;
    }

    public Response createSdPatchFolder(GeneralTask sdPatchFolder) {
        TaskRequestBody slaRequestBody = new TaskRequestBody(sdPatchFolder);
        this.response = createTask(slaRequestBody.keepMandatoryAndCreateFields());
        TaskResponseBody installationResponseBody = JsonUtils.deserialize(this.response, TaskResponseBody.class);
        if (installationResponseBody != null) {
            sdPatchFolder.setId(installationResponseBody.getId());
            sdPatchFolder.setNumber(installationResponseBody.getNumber());
            sdPatchFolder.setFinishStatus(installationResponseBody.getFinishStatus());
        }
        return this.response;
    }

}
