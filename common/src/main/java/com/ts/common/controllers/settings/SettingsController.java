package com.ts.common.controllers.settings;

import com.ts.common.application.controllers.AuthToken;
import com.ts.common.application.errors.ErrorResponseBody;
import com.ts.common.controllers.BaseController;
import com.ts.common.entitites.commonEntities.GeneralSlaId;
import com.ts.common.entitites.commonEntities.Status;
import com.ts.common.entitites.commonEntities.Udfs;
import com.ts.common.entitites.helpers.OperationsAccess;
import com.ts.common.entitites.helpers.Permissions;
import com.ts.common.entitites.helpers.Transition;
import com.ts.common.enums.Operations;
import com.ts.common.enums.TaskType;
import com.ts.common.utils.JsonUtils;

import static com.ts.common.application.controllers.TrackStudioEndPoints.*;
import static com.ts.common.utils.InitEntities.generateAuthToken;
import static com.ts.common.utils.InitEntities.generateOperationID;
import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;

public class SettingsController extends BaseController {

    public SettingsController(String url, AuthToken authToken) {
        super(url, authToken);
    }

    private String processFormat(TaskType taskType) {
        String format = "PROC_%S";
        return String.format(format, taskType.name());
    }

    public Status[] getStatusesFor(TaskType taskType) {
        this.response = super.get(getEndpoint(REST, WORKFLOW, processFormat(taskType), STATUS));
        return JsonUtils.deserialize(this.response, Status[].class);
    }

    public GeneralSlaId[] getOperationsFor(TaskType taskType) {
        this.response = super.get(getEndpoint(REST, WORKFLOW, processFormat(taskType), M_STATUSES));
        return JsonUtils.deserialize(this.response, GeneralSlaId[].class);
    }

    public Transition[] getTransitionsFor(TaskType taskType, Operations operation) {
        GeneralSlaId operationId = generateOperationID(taskType, operation);
        this.response = super.get(getEndpoint(REST, WORKFLOW, M_STATUS, operationId.getId(), TRANSITION));
        return JsonUtils.deserialize(this.response, Transition[].class);
    }

    public Permissions[] getPermissionsFor(TaskType taskType, Operations operation) {
        GeneralSlaId operationId = generateOperationID(taskType, operation);
        this.response = super.get(getEndpoint(REST, WORKFLOW, M_STATUS, operationId.getId(), PERMISSIONS));
        return JsonUtils.deserialize(this.response, Permissions[].class);
    }

    public boolean isAccessUserForUdfField(String user, Udfs.UdfSd udf) {
        setAuthToken(generateAuthToken(user));
        this.response = super.get(getEndpoint(REST, UDF, udf.udfId));
        if (this.response.getStatusCode() == HTTP_BAD_REQUEST) {
            ErrorResponseBody errorResponseBody = JsonUtils.deserialize(this.response, ErrorResponseBody.class);
            if (errorResponseBody != null) {
                return !errorResponseBody.getMessage().contains("Not enough rights for viewing udf");
            }
        }
        return true;
    }

    public OperationsAccess[] getOperationsAccessUserFor(String user, Udfs.UdfSd udf) {
        setAuthToken(generateAuthToken(user));
        this.response = super.get(getEndpoint(REST, UDF, udf.udfId, OPERATIONS));
        if (this.response.getStatusCode() == HTTP_BAD_REQUEST) {
            ErrorResponseBody errorResponseBody = JsonUtils.deserialize(this.response, ErrorResponseBody.class);
            if (errorResponseBody != null) {
                if (!errorResponseBody.getMessage().contains("Not enough rights for viewing udf")) {
                    return new OperationsAccess[]{
                            new OperationsAccess(false)
                    };
                }
            }
        }
        return JsonUtils.deserialize(this.response, OperationsAccess[].class);
    }
}
