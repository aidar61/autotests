package com.ts.common.controllers.sla.slaHelp;

import com.ts.common.application.controllers.AuthToken;
import com.ts.common.entitites.commonEntities.Udfs;
import com.ts.common.entitites.commonEntities.udfs.UdfSdModule;
import com.ts.common.entitites.commonEntities.udfs.UdfSdProvidedHelpDeadline;
import com.ts.common.entitites.commonEntities.udfs.UdfSdTaskCode;
import com.ts.common.entitites.sla.SlaTask;
import com.ts.common.request.ApiRequest;
import com.ts.common.utils.JsonUtils;
import com.ts.common.utils.InitEntities;
import io.qameta.allure.Step;
import io.restassured.response.Response;

import static com.ts.common.application.controllers.TrackStudioEndPoints.OPERATION;
import static com.ts.common.application.controllers.TrackStudioEndPoints.*;
import static com.ts.common.controllers.sla.slaHelp.SlaRequestBody.Fields.*;
import static com.ts.common.entitites.commonEntities.GeneralSlaId.Fields.*;
import static com.ts.common.utils.InitEntities.getGeneralId;
import static com.ts.common.utils.RandomUtils.generateComment;

public class SlaHelpController extends ApiRequest {

    public SlaHelpController(String url, AuthToken authToken) {
        super(url, HEADERS_BASE_CONTROLLER);
        this.authToken = authToken;
    }

    @Step("Create following sla task consultation: {0}")
    public Response createSlaTaskConsultation(String requestBody) {
        return super.post(getEndpoint(TASK, UPDATE), requestBody);
    }

    public Response receiveTaskConsultation(String taskNumber) {
        return super.get(getEndpoint(TASK, INFO, taskNumber));
    }

    public Response createSlaTaskConsultation(SlaTask slaTask) {
        SlaRequestBody slaRequestBody = new SlaRequestBody(slaTask);
        this.response = createSlaTaskConsultation(slaRequestBody.keepMandatoryAndCreateFields());
        SlaResponseBody slaResponseBody = JsonUtils.deserialize(this.response, SlaResponseBody.class);
        if (slaResponseBody != null) {
            slaTask.setId(slaResponseBody.getId());
            slaTask.setNumber(slaResponseBody.getNumber());
            slaTask.setStatus(slaResponseBody.getStatus());
        }
        return this.response;
    }

    @Step("Adding comment to slaTask with following request body {1}")
    private Response addComment(SlaTask slaTask, String requestBody) {
        return super.post(getEndpoint(OPERATION, slaTask.getNumber(), CREATE), requestBody);
    }

    public Response addComment(SlaTask slaTask) {
        slaTask.setOperation(getGeneralId(ADD_COMMENT));
        slaTask.setDescription(generateComment());
        SlaRequestBody slaRequestBody = new SlaRequestBody(slaTask);
        this.response = operation(slaTask, slaRequestBody
                .keepFields(SlaRequestBody.Fields.OPERATION.field, SlaRequestBody.Fields.ID.field, DESCRIPTION.field, ATTACHMENTS.field));
        return this.response;
    }

    private Response editModule(SlaTask slaTask, String requestBody) {
        return super.post(getEndpoint(OPERATION, slaTask.getNumber(), CREATE), requestBody);
    }

    public Response editModule(SlaTask slaTask) {
        slaTask.setOperation(getGeneralId(CHANGE_SD_MODULE));
        Udfs udfs = slaTask.getUdfs();
        UdfSdTaskCode udfSdTaskCode = InitEntities.getUdfTaskCode(UdfSdTaskCode.Constants.ABNATTR.taskCodesId);
        UdfSdModule udfSdModule = InitEntities.getUdfSdModule(UdfSdModule.Constants.NOTIFICATION_SERVICE.moduleIds);
        udfs.setUdfSdTaskCode(udfSdTaskCode);
        udfs.setUdfSdModule(udfSdModule);
        Udfs module = JsonUtils.deserialize(udfs.keepTypeFieldWithName("module"), Udfs.class);
        slaTask.setUdfs(module);
        SlaRequestBody slaRequestBody = new SlaRequestBody(slaTask);
        this.response = operation(slaTask, slaRequestBody.keepFields(SlaRequestBody.Fields.OPERATION.field, UDFS.field, ATTACHMENTS.field));
        return this.response;
    }

    @Step("Receive to analysis sla task: {1}")
    private Response receiveAnalysis(SlaTask slaTask, String requestBody) {
        return super.post(getEndpoint(OPERATION, slaTask.getNumber(), CREATE), requestBody);
    }

    public Response receiveAnalysis(SlaTask slaTask) {
        Udfs udfs = slaTask.getUdfs();
        UdfSdProvidedHelpDeadline deadline = InitEntities.getUdfSdProvidedHelpDeadline();
        deadline.setDateValue("2023-02-15");
        udfs.setUdfSdProvidedHelpDeadline(deadline);
        Udfs module = JsonUtils.deserialize(udfs.keepTypeFieldWithName("date"), Udfs.class);
        slaTask.setOperation(getGeneralId(RECEIVE_ANALIZE));
        slaTask.setUdfs(module);
        SlaRequestBody slaRequestBody = new SlaRequestBody(slaTask);
        this.response = operation(slaTask, slaRequestBody.keepFields(SlaRequestBody.Fields.OPERATION.field, ATTACHMENTS.field, HANDLER_USER.field, UDFS.field));
        return this.response;
    }

    @Step("Request information")
    private Response requestInformation(SlaTask slaTask, String requestBody) {
        return super.post(getEndpoint(OPERATION, slaTask.getNumber(), CREATE), requestBody);
    }

    public Response requestInformation(SlaTask slaTask) {
        slaTask.setOperation(getGeneralId(REQUEST_INFORMATION));
        SlaRequestBody slaRequestBody = new SlaRequestBody(slaTask);
        this.response = operation(slaTask, slaRequestBody.keepFields(OPERATION, DESCRIPTION.field, ATTACHMENTS.field));
        return this.response;
    }

    @Step("Выполнение операциии")
    private Response operation(SlaTask slaTask, String requestBody) {
        return super.post(getEndpoint(OPERATION, slaTask.getNumber(), CREATE), requestBody);
    }

    public Response provideInformation(SlaTask slaTask) {
        slaTask.setOperation(getGeneralId(PROVIDE_INFO));
        SlaRequestBody slaRequestBody = new SlaRequestBody(slaTask);
        this.response = operation(slaTask, slaRequestBody.keepFields(OPERATION, DESCRIPTION.field, ATTACHMENTS.field));
        return this.response;
    }

    public Response provideConsultation(SlaTask slaTask) {
        slaTask.setOperation(getGeneralId(PROVIDE_CONSULT));
        SlaRequestBody slaRequestBody = new SlaRequestBody(slaTask);
        this.response = operation(slaTask, slaRequestBody.keepFields(OPERATION, DESCRIPTION.field, ATTACHMENTS.field));
        return this.response;
    }

    @Step("Close task")
    private Response closeSLaTask(SlaTask slaTask, String requestBody) {
        return super.post(getEndpoint(OPERATION, slaTask.getNumber(), CREATE), requestBody);
    }

    public Response closeSlaTask(SlaTask slaTask) {
        slaTask.setOperation(getGeneralId(CLOSE_SLAHELP));
        SlaRequestBody slaRequestBody = new SlaRequestBody(slaTask);
        this.response = closeSLaTask(slaTask, slaRequestBody.keepFields(OPERATION, DESCRIPTION.field, ATTACHMENTS.field));
        return this.response;
    }
}
