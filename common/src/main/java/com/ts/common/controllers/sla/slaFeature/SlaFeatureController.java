package com.ts.common.controllers.sla.slaFeature;

import com.ts.common.application.controllers.AuthToken;
import com.ts.common.application.controllers.TrackStudioEndPoints;
import com.ts.common.controllers.sla.BaseSlaController;
import com.ts.common.controllers.sla.SlaRequestBody;
import com.ts.common.controllers.sla.SlaResponseBody;
import com.ts.common.entitites.sla.SlaTask;
import com.ts.common.enums.SlaType;
import com.ts.common.utils.JsonUtils;
import io.qameta.allure.Step;
import io.restassured.response.Response;

import java.util.HashMap;

import static com.ts.common.application.controllers.TrackStudioEndPoints.CREATE;
import static com.ts.common.application.controllers.TrackStudioEndPoints.HEADERS_BASE_CONTROLLER;
import static com.ts.common.controllers.sla.SlaRequestBody.Fields.*;
import static com.ts.common.enums.ComSlaOperations.*;
import static com.ts.common.enums.SlaType.SLA_FEATURE;
import static com.ts.common.utils.InitEntities.generateOperationID;
import static com.ts.common.utils.RandomUtils.generateDescriptionForOperation;

public class SlaFeatureController extends BaseSlaController {
    private static final SlaType SLA_TYPE = SLA_FEATURE;

    public SlaFeatureController(String url, AuthToken authToken) {
        super(url, HEADERS_BASE_CONTROLLER, SLA_TYPE, authToken);
        this.authToken = authToken;
    }

    @Step("Создание запроса на доработку ЛПО (new) : {0}")
    @Override
    protected Response createTask(String requestBody) {
        return super.createTask(requestBody);
    }

    @Override
    protected Response changeAuthor(SlaTask slaTask) {
        slaTask.setOperation(generateOperationID(SLA_FEATURE, CHANGE_AUTHOR));
        SlaRequestBody slaRequestBody = new SlaRequestBody(slaTask);
        return super.performOperation(slaTask, slaRequestBody.keepFields(ID, OPERATION, DESCRIPTION, ATTACHMENTS, UDFS));
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

    public Response msgToprecost(SlaTask slaTask) {
        slaTask.setOperation(generateOperationID(this.slaType, TOPRECOST));
        slaTask.setDescription(generateDescriptionForOperation(TOPRECOST));
        SlaRequestBody slaRequestBody = new SlaRequestBody(slaTask);
        return this.response = super.performOperation(slaTask, slaRequestBody.keepFields(DEFAULT_FIELDS_CONDITION));
    }

    public Response msgRequestReqInfo(SlaTask slaTask) {
        HashMap<String, String> queryParams = new HashMap<>() {{
            put(ID.field, slaTask.getId());
        }};
        slaTask.setOperation(generateOperationID(this.slaType, REQUESTREQINFO));
        slaTask.setDescription(generateDescriptionForOperation(REQUESTREQINFO));
        SlaRequestBody slaRequestBody = new SlaRequestBody(slaTask);
        this.response = super.post(getEndpoint(TrackStudioEndPoints.OPERATION, slaTask.getNumber(), CREATE
                , formatParameters(queryParams)), slaRequestBody.keepFields(DEFAULT_FIELDS_WITHOUT_ID));
        return this.response;
    }

    public Response msgProvideReqInfo(SlaTask slaTask) {
        slaTask.refreshUdf();
        slaTask.setOperation(generateOperationID(this.slaType, PROVIDEREQINFO));
        slaTask.setDescription(generateDescriptionForOperation(PROVIDEREQINFO));
        SlaRequestBody slaRequestBody = new SlaRequestBody(slaTask);
        this.response = super.post(getEndpoint(TrackStudioEndPoints.OPERATION, slaTask.getNumber(), CREATE)
                , slaRequestBody.keepFields(DEFAULT_FIELDS_WITHOUT_ID));
        return this.response;
    }

    public Response msgBeginCostPre(SlaTask slaTask) {
        slaTask.setOperation(generateOperationID(this.slaType, BEGINCOST_PRE));
        slaTask.setDescription(generateDescriptionForOperation(BEGINCOST_PRE));
        SlaRequestBody slaRequestBody = new SlaRequestBody(slaTask);
        return this.response = super.performOperation(slaTask, slaRequestBody.keepFields(DEFAULT_FIELDS_CONDITION));
    }

}
