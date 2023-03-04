package com.ts.common.controllers.sla.slaFeature;

import com.ts.common.application.controllers.AuthToken;
import com.ts.common.controllers.sla.BaseSlaController;
import com.ts.common.controllers.sla.SlaRequestBody;
import com.ts.common.controllers.sla.SlaResponseBody;
import com.ts.common.entitites.sla.SlaTask;
import com.ts.common.utils.InitEntities;
import com.ts.common.utils.JsonUtils;
import io.qameta.allure.Step;
import io.restassured.response.Response;

import static com.ts.common.application.controllers.TrackStudioEndPoints.HEADERS_BASE_CONTROLLER;
import static com.ts.common.enums.ComSlaOperations.CAT;
import static com.ts.common.enums.SlaType.SLA_FEATURE;

public class SlaFeatureController extends BaseSlaController {
    public SlaFeatureController(String url, AuthToken authToken) {
        super(url, HEADERS_BASE_CONTROLLER);
        this.authToken = authToken;
    }

    @Step("Создание запроса на доработку ЛПО (new) : {0}")
    @Override
    protected Response createTask(String requestBody) {
        return super.createTask(requestBody);
    }

    @Override
    protected Response changeAuthor(SlaTask slaTask) {
        return null;
    }

    @Override
    protected Response changeAttributes(SlaTask slaTask) {
        return null;
    }

    @Override
    protected Response changeResPerson(SlaTask slaTask) {
        return null;
    }

    @Override
    protected Response changeCurrentRole(SlaTask slaTask) {
        return null;
    }

    @Override
    protected Response changeLinkedTasks(SlaTask slaTask) {
        return null;
    }

    @Override
    protected Response addTrustedWatchers(SlaTask slaTask) {
        return null;
    }

    @Override
    protected Response addClientWatchers(SlaTask slaTask) {
        return null;
    }

    @Override
    protected Response addWatchers(SlaTask slaTask) {
        return null;
    }

    @Override
    protected Response comment(SlaTask slaTask) {
        return null;
    }

    @Override
    protected Response privateComment(SlaTask slaTask) {
        return null;
    }

    @Override
    protected Response removeRequest(SlaTask slaTask) {
        return null;
    }

    public Response createSlaFeatureTask(SlaTask slaTask) {
        slaTask.setCategory(InitEntities.generateOperationID(SLA_FEATURE, CAT));
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

}
