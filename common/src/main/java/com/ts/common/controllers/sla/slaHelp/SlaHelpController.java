package com.ts.common.controllers.sla.slaHelp;

import com.ts.common.application.controllers.AuthToken;
import com.ts.common.controllers.sla.BaseSlaController;
import com.ts.common.controllers.sla.SlaRequestBody;
import com.ts.common.controllers.sla.SlaResponseBody;
import com.ts.common.entitites.commonEntities.Udfs;
import com.ts.common.entitites.commonEntities.udfs.UdfSdModule;
import com.ts.common.entitites.commonEntities.udfs.UdfSdProvidedHelpDeadline;
import com.ts.common.entitites.commonEntities.udfs.UdfSdTaskCode;
import com.ts.common.entitites.sla.SlaTask;
import com.ts.common.enums.SlaType;
import com.ts.common.utils.InitEntities;
import com.ts.common.utils.JsonUtils;
import io.qameta.allure.Step;
import io.restassured.response.Response;
import org.jetbrains.annotations.NotNull;

import static com.ts.common.application.controllers.TrackStudioEndPoints.OPERATION;
import static com.ts.common.application.controllers.TrackStudioEndPoints.*;
import static com.ts.common.controllers.sla.SlaRequestBody.Fields.*;
import static com.ts.common.entitites.commonEntities.GeneralSlaId.Fields.*;
import static com.ts.common.entitites.commonEntities.Udfs.Fields.UDF_SD_PROVIDEDHELPDEADLINE;
import static com.ts.common.utils.DateUtils.getCurrentDate;
import static com.ts.common.utils.InitEntities.getGeneralId;
import static com.ts.common.utils.RandomUtils.generateComment;

public class SlaHelpController extends BaseSlaController {

    public final SlaType SLA_TYPE = SlaType.SLA_HElP;

    public SlaHelpController(String url, AuthToken authToken) {
        super(url, authToken);
        super.slaType = SLA_TYPE;
    }

    @Step("Создание консультации: ")
    public Response createTask(String requestBody) {
        return super.post(getEndpoint(TASK, UPDATE), requestBody);
    }

    @Step("Выполнение операциии SlaHelp Task: ")
    public Response performOperation(@NotNull SlaTask slaTask, String requestBody) {
        return super.post(getEndpoint(OPERATION, slaTask.getNumber(), CREATE), requestBody);
    }

    public Response createTask(SlaTask slaTask) {
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

    public Response addComment(SlaTask slaTask) {
        slaTask.setOperation(getGeneralId(ADD_COMMENT));
        slaTask.setDescription(generateComment());
        SlaRequestBody slaRequestBody = new SlaRequestBody(slaTask);
        this.response = performOperation(slaTask, slaRequestBody
                .keepFields(SlaRequestBody.Fields.OPERATION.field, SlaRequestBody.Fields.ID.field, DESCRIPTION.field, ATTACHMENTS.field));
        return this.response;
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
        this.response = performOperation(slaTask, slaRequestBody.keepFields(SlaRequestBody.Fields.OPERATION.field, UDFS.field, ATTACHMENTS.field));
        return this.response;
    }

    @Step("Принятие на анализ slaTask: ")
    public Response receiveAnalysis(SlaTask slaTask) {
        Udfs udfs = slaTask.getUdfs();
        UdfSdProvidedHelpDeadline deadline = InitEntities.getUdfSdProvidedHelpDeadline();
        deadline.setDateValue(getCurrentDate());
        udfs.setUdfSdProvidedHelpDeadline(deadline);
        Udfs module = JsonUtils.deserialize(udfs.keepFields(UDF_SD_PROVIDEDHELPDEADLINE.field), Udfs.class);
        slaTask.setOperation(getGeneralId(RECEIVE_ANALIZE));
        slaTask.setUdfs(module);
        SlaRequestBody slaRequestBody = new SlaRequestBody(slaTask);
        this.response = performOperation(slaTask, slaRequestBody.keepFields(SlaRequestBody.Fields.OPERATION.field, DESCRIPTION.field, ATTACHMENTS.field, HANDLER_USER.field, UDFS.field));
        return this.response;
    }

    public Response requestInformation(SlaTask slaTask) {
        slaTask.setOperation(getGeneralId(REQUEST_INFORMATION));
        SlaRequestBody slaRequestBody = new SlaRequestBody(slaTask);
        this.response = performOperation(slaTask, slaRequestBody.keepFields(OPERATION, DESCRIPTION.field, ATTACHMENTS.field));
        return this.response;
    }


    public Response provideInformation(SlaTask slaTask) {
        slaTask.setOperation(getGeneralId(PROVIDE_INFO));
        SlaRequestBody slaRequestBody = new SlaRequestBody(slaTask);
        this.response = performOperation(slaTask, slaRequestBody.keepFields(OPERATION, DESCRIPTION.field, ATTACHMENTS.field));
        return this.response;
    }

    public Response provideConsultation(SlaTask slaTask) {
        slaTask.setOperation(getGeneralId(PROVIDE_CONSULT));
        SlaRequestBody slaRequestBody = new SlaRequestBody(slaTask);
        this.response = performOperation(slaTask, slaRequestBody.keepFields(OPERATION, DESCRIPTION.field, ATTACHMENTS.field));
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
