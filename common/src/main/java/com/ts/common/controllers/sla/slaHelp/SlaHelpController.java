package com.ts.common.controllers.sla.slaHelp;

import com.ts.common.application.controllers.AuthToken;
import com.ts.common.controllers.sla.BaseSlaController;
import com.ts.common.controllers.sla.SlaRequestBody;
import com.ts.common.controllers.sla.SlaResponseBody;
import com.ts.common.entitites.commonEntities.List;
import com.ts.common.entitites.commonEntities.Udfs;
import com.ts.common.entitites.tasks.Task;
import com.ts.common.enums.SlaType;
import com.ts.common.utils.JsonUtils;
import io.qameta.allure.Step;
import io.restassured.response.Response;
import org.jetbrains.annotations.NotNull;

import static com.ts.common.application.controllers.TrackStudioEndPoints.OPERATION;
import static com.ts.common.application.controllers.TrackStudioEndPoints.*;
import static com.ts.common.controllers.sla.SlaRequestBody.Fields.*;
import static com.ts.common.entitites.commonEntities.GeneralSlaId.Fields.*;
import static com.ts.common.entitites.commonEntities.Task.Constants.NOTIFICATION_SERVICE;
import static com.ts.common.entitites.commonEntities.Udfs.UdfSd.UDF_SD_MODULE;
import static com.ts.common.entitites.commonEntities.Udfs.UdfSd.UDF_SD_TASK_CODE;
import static com.ts.common.utils.DateUtils.getCurrentDate;
import static com.ts.common.utils.InitEntities.*;
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
    public Response performOperation(@NotNull Task slaTask, String requestBody) {
        return super.post(getEndpoint(OPERATION, slaTask.getNumber(), CREATE), requestBody);
    }

    public Response createTask(Task slaTask) {
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

    public Response addComment(Task slaTask) {
        slaTask.setOperation(getGeneralId(ADD_COMMENT));
        slaTask.setDescription(generateComment());
        SlaRequestBody slaRequestBody = new SlaRequestBody(slaTask);
        this.response = performOperation(slaTask, slaRequestBody
                .keepFields(SlaRequestBody.Fields.OPERATION.field, SlaRequestBody.Fields.ID.field, DESCRIPTION.field, ATTACHMENTS.field));
        return this.response;
    }

    public Response editModule(Task slaTask) {
        slaTask.setOperation(getGeneralId(CHANGE_SD_MODULE));
        Udfs udfs = slaTask.getUdfs();
        udfs.setUdfList(generateUdfList(UDF_SD_TASK_CODE, List.Constants.ABNATTR));
        udfs.setUdfTask(generateUdfTask(UDF_SD_MODULE, NOTIFICATION_SERVICE));
        Udfs module = JsonUtils.deserialize(udfs.keepTypeFieldWithName("module"), Udfs.class);
        slaTask.setUdfs(module);
        SlaRequestBody slaRequestBody = new SlaRequestBody(slaTask);
        this.response = performOperation(slaTask, slaRequestBody.keepFields(SlaRequestBody.Fields.OPERATION.field, UDFS.field, ATTACHMENTS.field));
        return this.response;
    }

    @Step("Принятие на анализ slaTask: ")
    public Response receiveAnalysis(Task slaTask) {
        Udfs udfs = slaTask.getUdfs();
        udfs.setUdfString(generateUdfString(Udfs.UdfSd.UDF_SD_PROVIDEDHELPDEADLINE, getCurrentDate()));
        slaTask.setOperation(getGeneralId(RECEIVE_ANALIZE));
        slaTask.refreshUdf(udfs);
        SlaRequestBody slaRequestBody = new SlaRequestBody(slaTask);
        this.response = performOperation(slaTask, slaRequestBody.keepFields(SlaRequestBody.Fields.OPERATION.field, DESCRIPTION.field, ATTACHMENTS.field, HANDLER_USER.field, UDFS.field));
        return this.response;
    }

    public Response requestInformation(Task slaTask) {
        slaTask.setOperation(getGeneralId(REQUEST_INFORMATION));
        SlaRequestBody slaRequestBody = new SlaRequestBody(slaTask);
        this.response = performOperation(slaTask, slaRequestBody.keepFields(OPERATION, DESCRIPTION.field, ATTACHMENTS.field));
        return this.response;
    }


    public Response provideInformation(Task slaTask) {
        slaTask.setOperation(getGeneralId(PROVIDE_INFO));
        SlaRequestBody slaRequestBody = new SlaRequestBody(slaTask);
        this.response = performOperation(slaTask, slaRequestBody.keepFields(OPERATION, DESCRIPTION.field, ATTACHMENTS.field));
        return this.response;
    }

    public Response provideConsultation(Task slaTask) {
        slaTask.setOperation(getGeneralId(PROVIDE_CONSULT));
        SlaRequestBody slaRequestBody = new SlaRequestBody(slaTask);
        this.response = performOperation(slaTask, slaRequestBody.keepFields(OPERATION, DESCRIPTION.field, ATTACHMENTS.field));
        return this.response;
    }

    @Step("Close task")
    private Response closeSLaTask(Task slaTask, String requestBody) {
        return super.post(getEndpoint(OPERATION, slaTask.getNumber(), CREATE), requestBody);
    }

    public Response closeSlaTask(Task slaTask) {
        slaTask.setOperation(getGeneralId(CLOSE_SLAHELP));
        SlaRequestBody slaRequestBody = new SlaRequestBody(slaTask);
        this.response = closeSLaTask(slaTask, slaRequestBody.keepFields(OPERATION, DESCRIPTION.field, ATTACHMENTS.field));
        return this.response;
    }
}
