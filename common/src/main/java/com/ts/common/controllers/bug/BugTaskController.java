package com.ts.common.controllers.bug;

import com.ts.common.application.controllers.AuthToken;
import com.ts.common.controllers.BaseController;
import com.ts.common.controllers.TaskRequestBody;
import com.ts.common.controllers.TaskResponseBody;
import com.ts.common.entitites.commonEntities.udf.UdfList;
import com.ts.common.entitites.commonEntities.udf.UdfTask;
import com.ts.common.entitites.tasks.GeneralTask;
import com.ts.common.enums.TaskType;
import com.ts.common.utils.JsonUtils;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

import java.util.HashMap;
import java.util.Map;

import static com.ts.common.application.controllers.TrackStudioEndPoints.*;
import static com.ts.common.controllers.TaskRequestBody.Fields.HANDLER_USER;
import static com.ts.common.enums.TaskType.BUG_TASK;

public class BugTaskController extends BaseController {

    public static TaskType TASK_TYPE = BUG_TASK;
    private static String parentDetailInString;

    public BugTaskController(String url, AuthToken authToken) {
        super(url, authToken);
        this.taskType = TASK_TYPE;
    }

    public void changeTaskType(TaskType taskType) {
        TASK_TYPE = taskType;
        this.taskType = TASK_TYPE;
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

    public Map<String, UdfTask> getTaskForSDRequest(String parentNumber) {
        this.response = super.get(getEndpoint(REST, TASK, CREATE, parentNumber, "CAT_BUGTASK"));
        parentDetailInString = this.response.asString().replace("\\&", "\\\\&");
        var udfProductTask = new JsonPath(parentDetailInString).getObject("udfs.UDF_PRODUCT", UdfTask.class);
        var udfBDKUTask = new JsonPath(parentDetailInString).getObject("udfs.UDF_BDKU_CONFIGURATION", UdfTask.class);
        var returnTasks = new HashMap<String, UdfTask>();
        returnTasks.put("UDF_PRODUCT", udfProductTask);
        returnTasks.put("UDF_BDKU_CONFIGURATION", udfBDKUTask);
        return returnTasks;
    }

    public String getMisService() {
        var misService = new JsonPath(parentDetailInString).getObject("udfs.UDF_MIS_SERVICE", UdfList.class);
        if (misService != null) {
            if (misService.getListValue() != null && misService.getListValue().length > 0) {
                return misService.getListValue()[0].getId();
            }
            if (misService.getListValueSelector() != null && misService.getListValueSelector().length > 0) {
                return misService.getListValueSelector()[0].getId();
            }
        }

        return null;
    }

    public String getCdpBl() {
        var misService = new JsonPath(parentDetailInString).getObject("udfs.UDF_CDP_BL", UdfList.class);
        if (misService != null) {
            if (misService.getListValue() != null && misService.getListValue().length > 0) {
                return misService.getListValue()[0].getId();
            }
            if (misService.getListValueSelector() != null && misService.getListValueSelector().length > 0) {
                return misService.getListValueSelector()[0].getId();
            }
        }

        return null;
    }
}
