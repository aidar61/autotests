package com.ts.common.controllers.sla;

import com.ts.common.application.controllers.AuthToken;
import com.ts.common.entitites.commonEntities.Udfs;
import com.ts.common.entitites.commonEntities.udfs.UdfSdModule;
import com.ts.common.entitites.commonEntities.udfs.UdfSdTaskCode;
import com.ts.common.entitites.sla.SlaTask;
import com.ts.common.request.ApiRequest;
import com.ts.common.utils.JsonUtils;
import com.ts.common.utils.RandomEntities;
import io.qameta.allure.Step;
import io.restassured.response.Response;

import static com.ts.common.application.controllers.TrackStudioEndPoints.*;
import static com.ts.common.controllers.sla.SlaRequestBody.Fields.CATEGORY;
import static com.ts.common.entitites.commonEntities.GeneralSlaId.Fields.ADD_COMMENT;
import static com.ts.common.utils.RandomEntities.getGeneralId;
import static com.ts.common.utils.RandomUtils.generateComment;

public class SlaHelpController extends ApiRequest {

    public SlaHelpController(String url, AuthToken authToken) {
        super(url, HEADERS_BASE_CONTROLLER);
        this.authToken = authToken;
    }

    public Response createSlaTaskConsultation(String requestBody) {
        return super.post(getEndpoint(TASK, UPDATE), requestBody);
    }

    public Response receiveTaskConsultation(String taskNumber) {
        return super.get(getEndpoint(TASK, INFO, taskNumber));
    }

    @Step("Create following sla task consultation: {0}")
    public Response createSlaTaskConsultation(SlaTask slaTask) {
        SlaRequestBody slaRequestBody = new SlaRequestBody(slaTask);
        this.response = createSlaTaskConsultation(slaRequestBody.removeTypeFieldWithName(OPERATION));
        SlaResponseBody slaResponseBody = JsonUtils.deserialize(this.response, SlaResponseBody.class);
        if (slaResponseBody != null) {
            slaTask.setId(slaResponseBody.getId());
            slaTask.setNumber(slaResponseBody.getNumber());
            slaTask.setStatus(slaResponseBody.getStatus());
        }
        return this.response;
    }

    @Step("Adding comment to slaTask: {0}, with following request body {0}")
    private Response addComment(SlaTask slaTask, String requestBody) {
        return super.post(getEndpoint(OPERATION, slaTask.getNumber(), CREATE), requestBody);
    }

    public Response addComment(SlaTask slaTask) {
        slaTask.setOperation(getGeneralId(ADD_COMMENT));
        slaTask.setDescription(generateComment());
        SlaRequestBody slaRequestBody = new SlaRequestBody(slaTask);
        this.response = addComment(slaTask, slaRequestBody.removeOptionalAndTypeFieldWithName(CATEGORY.field));
        return this.response;
    }

    private Response editModule(SlaTask slaTask, String requestBody) {
        return super.post(getEndpoint(OPERATION, slaTask.getNumber(), CREATE), requestBody);
    }

    public Response editModule(SlaTask slaTask) {
        Udfs udfs = slaTask.getUdfs();
        UdfSdTaskCode udfSdTaskCode = RandomEntities.getUdfTaskCode(UdfSdTaskCode.Constants.ABNATTR.taskCodesId);
        UdfSdModule udfSdModule = RandomEntities.getUdfSdModule(UdfSdModule.Constants.NOTIFICATION_SERVICE.moduleIds);
        udfs.setUdfSdTaskCode(udfSdTaskCode);
        udfs.setUdfSdModule(udfSdModule);
//        udfs.removeAllFieldsExcept();
        slaTask.setUdfs(udfs);
        SlaRequestBody slaRequestBody = new SlaRequestBody(slaTask);
        editModule(slaTask, slaRequestBody.removeFields());
        return this.response;
    }
}
