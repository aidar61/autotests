package com.ts.common.controllers.sla.slaBug;

import com.ts.common.application.controllers.AuthToken;
import com.ts.common.controllers.sla.BaseSlaController;
import com.ts.common.controllers.sla.SlaRequestBody;
import com.ts.common.controllers.sla.SlaResponseBody;
import com.ts.common.entitites.sla.SlaTask;
import com.ts.common.enums.SlaType;
import com.ts.common.utils.JsonUtils;
import io.qameta.allure.Step;
import io.restassured.response.Response;

import static com.ts.common.application.controllers.TrackStudioEndPoints.HEADERS_BASE_CONTROLLER;
import static com.ts.common.enums.ComSlaOperations.*;
import static com.ts.common.enums.SlaType.SLA_BUG;
import static com.ts.common.utils.InitEntities.generateOperationID;

public class SlaBugController extends BaseSlaController {
    private static final SlaType SLA_TYPE = SLA_BUG;

    public SlaBugController(String url, AuthToken authToken) {
        super(url, HEADERS_BASE_CONTROLLER, SLA_TYPE, authToken);
    }

    @Step("Создание извещения об ошибке: {0}")
    
    protected Response createTask(String requestBody) {
        return super.createTask(requestBody);
    }

    public Response createSlaBugTask(SlaTask slaTask) {
        SlaRequestBody slaRequestBody = new SlaRequestBody(slaTask);
        this.response = createTask(slaRequestBody.keepMandatoryAndCreateFields());
        SlaResponseBody slaResponseBody = JsonUtils.deserialize(this.response, SlaResponseBody.class);
        if (slaResponseBody != null) {
            slaTask.setId(slaResponseBody.getId());
            slaTask.setNumber(slaResponseBody.getNumber());
            slaTask.setFinishStatus(slaResponseBody.getFinishStatus());
        }
        return this.response;
    }


    
    protected Response changeAuthor(SlaTask slaTask) {
        slaTask.setOperation(generateOperationID(SLA_TYPE, CHANGE_AUTHOR));
        SlaRequestBody slaRequestBody = new SlaRequestBody(slaTask);
        return super.performOperation(slaTask, slaRequestBody.keepFields(DEFAULT_FIELDS));
    }

    
    protected Response changeAttributes(SlaTask slaTask) {
        slaTask.setOperation(generateOperationID(SLA_TYPE, CHANGE_ATTR));
        SlaRequestBody slaRequestBody = new SlaRequestBody(slaTask);
        return super.performOperation(slaTask, slaRequestBody.keepFields(DEFAULT_FIELDS));
    }

    
    protected Response changeResPerson(SlaTask slaTask) {
        slaTask.setOperation(generateOperationID(SLA_TYPE, CHANGE_RES_PERSON));
        SlaRequestBody slaRequestBody = new SlaRequestBody(slaTask);
        return super.performOperation(slaTask, slaRequestBody.keepFields(DEFAULT_FIELDS));
    }
    

}
