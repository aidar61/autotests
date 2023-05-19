package com.ts.common.controllers.sla;

import com.ts.common.application.controllers.AuthToken;
import com.ts.common.controllers.BaseController;
import com.ts.common.controllers.TaskRequestBody;
import com.ts.common.controllers.TaskResponseBody;
import com.ts.common.entitites.tasks.GeneralTask;
import com.ts.common.enums.SlaType;
import com.ts.common.utils.JsonUtils;
import io.qameta.allure.Step;
import io.restassured.response.Response;

import static com.ts.common.enums.ComSlaOperations.*;
import static com.ts.common.enums.SlaType.SLA_BUG;
import static com.ts.common.utils.InitEntities.generateOperationID;
import static com.ts.common.utils.RandomUtils.generateDescriptionForOperation;

public class SlaBugController extends BaseController {
    private static final SlaType SLA_TYPE = SLA_BUG;

    public SlaBugController(String url, AuthToken authToken) {
        super(url, authToken);
        this.slaType = SLA_TYPE;
    }

    @Step("Создание извещения об ошибке: {0}")
    protected Response createTask(String requestBody) {
        return super.createTask(requestBody);
    }

    public Response createSlaBugTask(GeneralTask slaTask) {
        TaskRequestBody slaRequestBody = new TaskRequestBody(slaTask);
        this.response = createTask(slaRequestBody.keepMandatoryAndCreateFields());
        TaskResponseBody slaResponseBody = JsonUtils.deserialize(this.response, TaskResponseBody.class);
        if (slaResponseBody != null) {
            slaTask.setId(slaResponseBody.getId());
            slaTask.setNumber(slaResponseBody.getNumber());
            slaTask.setFinishStatus(slaResponseBody.getFinishStatus());
        }
        return this.response;
    }

    public Response msgAnalize(GeneralTask slaTask) {
        slaTask.setOperation(generateOperationID(this.slaType, ANALIZE));
        slaTask.setDescription(generateDescriptionForOperation(ANALIZE));
        TaskRequestBody slaRequestBody = new TaskRequestBody(slaTask);
        return this.response = super.performOperationWithQueryParam(slaTask, slaRequestBody.keepFields(DEFAULT_FIELDS_USER));
    }

    protected Response changeAuthor(GeneralTask slaTask) {
        slaTask.setOperation(generateOperationID(SLA_TYPE, CHANGE_AUTHOR));
        TaskRequestBody slaRequestBody = new TaskRequestBody(slaTask);
        return super.performOperation(slaTask, slaRequestBody.keepFields(DEFAULT_FIELDS));
    }


    protected Response changeAttributes(GeneralTask slaTask) {
        slaTask.setOperation(generateOperationID(SLA_TYPE, CHANGE_ATTR));
        TaskRequestBody slaRequestBody = new TaskRequestBody(slaTask);
        return super.performOperation(slaTask, slaRequestBody.keepFields(DEFAULT_FIELDS));
    }


    protected Response changeResPerson(GeneralTask slaTask) {
        slaTask.setOperation(generateOperationID(SLA_TYPE, CHANGE_RES_PERSON));
        TaskRequestBody slaRequestBody = new TaskRequestBody(slaTask);
        return super.performOperation(slaTask, slaRequestBody.keepFields(DEFAULT_FIELDS));
    }


}
