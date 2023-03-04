package com.ts.common.controllers.sla.slaFeature;

import com.ts.common.application.controllers.AuthToken;
import com.ts.common.controllers.sla.BaseSlaController;
import com.ts.common.controllers.sla.slaHelp.SlaRequestBody;
import com.ts.common.controllers.sla.slaHelp.SlaResponseBody;
import com.ts.common.entitites.commonEntities.GeneralSlaId;
import com.ts.common.entitites.sla.SlaTask;
import com.ts.common.utils.JsonUtils;
import io.qameta.allure.Step;
import io.restassured.response.Response;

import static com.ts.common.application.controllers.TrackStudioEndPoints.HEADERS_BASE_CONTROLLER;

public class SlaFeatureController extends BaseSlaController {
    public SlaFeatureController(String url, AuthToken authToken) {
        super(url, HEADERS_BASE_CONTROLLER);
        this.authToken = authToken;
    }

    @Override
    @Step("Create following sla feature task: {0}")
    protected Response createTask(String requestBody) {
        return super.createTask(requestBody);
    }

    public Response createSlaFeatureTask(SlaTask slaTask) {
        SlaRequestBody slaRequestBody = new SlaRequestBody(slaTask);
        this.response = createTask(slaRequestBody.keepMandatoryAndCreateFields());
        SlaResponseBody slaResponseBody = JsonUtils.deserialize(this.response, SlaResponseBody.class);
        if (slaResponseBody != null) {
            slaTask.setId(slaResponseBody.getId());
            slaTask.setNumber(slaResponseBody.getNumber());
            slaTask.setStatus(slaResponseBody.getStatus());
        }
        return this.response;
    }

    @Override
    protected Response performOperation(SlaTask slaTask, String requestBody, GeneralSlaId.Fields operation) {
        SlaRequestBody slaRequestBody = new SlaRequestBody();
        return super.performOperation(slaTask, requestBody, operation);
    }
}
