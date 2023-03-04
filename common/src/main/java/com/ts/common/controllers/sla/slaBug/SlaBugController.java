package com.ts.common.controllers.sla.slaBug;

import com.ts.common.controllers.sla.BaseSlaController;
import com.ts.common.controllers.sla.SlaRequestBody;
import com.ts.common.controllers.sla.SlaResponseBody;
import com.ts.common.entitites.sla.SlaTask;
import com.ts.common.utils.JsonUtils;
import io.qameta.allure.Step;
import io.restassured.response.Response;

import java.util.Map;

import static com.ts.common.controllers.sla.SlaRequestBody.Fields.*;
import static com.ts.common.enums.ComSlaOperations.CHANGE_AUTHOR;
import static com.ts.common.enums.SlaType.SLA_BUG;
import static com.ts.common.utils.InitEntities.generateOperationID;

public class SlaBugController extends BaseSlaController {
    public SlaBugController(String url, Map<String, String> headersBaseController) {
        super(url, headersBaseController);
    }

    @Step("Создание извещения об ошибке: {0}")
    @Override
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
            slaTask.setStatus(slaResponseBody.getStatus());
        }
        return this.response;
    }


    @Override
    protected Response changeAuthor(SlaTask slaTask) {
        slaTask.setOperation(generateOperationID(SLA_BUG, CHANGE_AUTHOR));
        SlaRequestBody slaRequestBody = new SlaRequestBody(slaTask);
        return super.performOperation(slaTask, slaRequestBody.keepFields(ID, OPERATION, DESCRIPTION));
    }
}
