package com.ts.common.controllers.sla.slaFeature;

import com.ts.common.application.controllers.AuthToken;
import com.ts.common.application.controllers.TrackStudioEndPoints;
import com.ts.common.controllers.sla.BaseController;
import com.ts.common.controllers.sla.TaskRequestBody;
import com.ts.common.controllers.sla.TaskResponseBody;
import com.ts.common.entitites.commonEntities.User;
import com.ts.common.entitites.tasks.GeneralTask;
import com.ts.common.enums.SlaType;
import com.ts.common.utils.JsonUtils;
import io.qameta.allure.Step;
import io.restassured.response.Response;

import java.util.HashMap;

import static com.ts.common.application.controllers.TrackStudioEndPoints.CREATE;
import static com.ts.common.controllers.sla.TaskRequestBody.Fields.*;
import static com.ts.common.enums.ComSlaOperations.*;
import static com.ts.common.enums.SlaType.SLA_FEATURE;
import static com.ts.common.utils.InitEntities.generateOperationID;
import static com.ts.common.utils.InitEntities.generateUser;
import static com.ts.common.utils.RandomUtils.generateDescriptionForOperation;

public class SlaFeatureController extends BaseController {
    private static final SlaType SLA_TYPE = SLA_FEATURE;

    public SlaFeatureController(String url, AuthToken authToken) {
        super(url, authToken);
        this.slaType = SLA_TYPE;
    }

    @Step("Создание запроса на доработку ЛПО (new) ")
    @Override
    protected Response createTask(String requestBody) {
        return super.createTask(requestBody);
    }

    protected Response changeAuthor(GeneralTask slaTask) {
        slaTask.setOperation(generateOperationID(SLA_FEATURE, CHANGE_AUTHOR));
        TaskRequestBody slaRequestBody = new TaskRequestBody(slaTask);
        return super.performOperation(slaTask, slaRequestBody.keepFields(ID, OPERATION, DESCRIPTION, ATTACHMENTS, UDFS));
    }

    public Response createSlaFeatureTask(GeneralTask slaTask) {
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

    public Response msgToprecost(GeneralTask slaTask) {
        slaTask.setOperation(generateOperationID(this.slaType, TOPRECOST));
        slaTask.setDescription(generateDescriptionForOperation(TOPRECOST));
        TaskRequestBody slaRequestBody = new TaskRequestBody(slaTask);
        return this.response = super.performOperation(slaTask, slaRequestBody.keepFields(DEFAULT_FIELDS_CONDITION));
    }

    @Step("Выполнение операции: REQUESTREQINFO ")
    public Response msgRequestReqInfo(GeneralTask slaTask) {
        HashMap<String, String> queryParams = new HashMap<>() {{
            put(ID.field, slaTask.getId());
        }};
        slaTask.setOperation(generateOperationID(this.slaType, REQUESTREQINFO));
        slaTask.setDescription(generateDescriptionForOperation(REQUESTREQINFO));
        TaskRequestBody slaRequestBody = new TaskRequestBody(slaTask);
        this.response = super.post(getEndpoint(TrackStudioEndPoints.OPERATION, slaTask.getNumber(), CREATE
                , formatParameters(queryParams)), slaRequestBody.keepFields(DEFAULT_FIELDS_WITHOUT_ID));
        return this.response;
    }

    @Step("Выполнение операции PROVIDEREQINFO: ")
    public Response msgProvideReqInfo(GeneralTask slaTask) {
        slaTask.refreshUdf();
        slaTask.setOperation(generateOperationID(this.slaType, PROVIDEREQINFO));
        slaTask.setDescription(generateDescriptionForOperation(PROVIDEREQINFO));
        TaskRequestBody slaRequestBody = new TaskRequestBody(slaTask);
        this.response = super.post(getEndpoint(TrackStudioEndPoints.OPERATION, slaTask.getNumber(), CREATE)
                , slaRequestBody.keepFields(DEFAULT_FIELDS_WITHOUT_ID));
        return this.response;
    }

    public Response msgBeginCostPre(GeneralTask slaTask) {
        slaTask.setOperation(generateOperationID(this.slaType, BEGINCOST_PRE));
        slaTask.setDescription(generateDescriptionForOperation(BEGINCOST_PRE));
        TaskRequestBody slaRequestBody = new TaskRequestBody(slaTask);
        return this.response = super.performOperationWithQueryParam(slaTask, slaRequestBody.keepFields( DEFAULT_FIELDS_USER));
    }

    public Response msgStart(GeneralTask slaTask) {
        HashMap<String, String> params = new HashMap<>() {{
            put(ID.field, slaTask.getId());
        }};
        slaTask.setOperation(generateOperationID(this.slaType, START));
        slaTask.setDescription(generateDescriptionForOperation(START));
        slaTask.setHandlerUser(generateUser(User.Constants.BABUSHKIN_IVAN));
        TaskRequestBody slaRequestBody = new TaskRequestBody(slaTask);
        return this.response = super.post(getEndpoint(TrackStudioEndPoints.OPERATION, slaTask.getNumber(), CREATE
                        , formatParameters(params))
                , slaRequestBody.keepFields(DEFAULT_FIELDS_USER));
    }

}
