package com.ts.common.controllers.gap.potentialGap;

import com.ts.common.application.controllers.AuthToken;
import com.ts.common.controllers.BaseController;
import com.ts.common.controllers.TaskRequestBody;
import com.ts.common.controllers.TaskResponseBody;
import com.ts.common.entitites.tasks.GeneralTask;
import com.ts.common.enums.SlaType;
import com.ts.common.utils.JsonUtils;
import io.qameta.allure.Step;
import io.restassured.response.Response;

import static com.ts.common.controllers.TaskRequestBody.Fields.*;
import static com.ts.common.enums.SlaType.GAP;
public class PotentialGapController extends BaseController {
    private static final SlaType SLA_TYPE = GAP;

    public PotentialGapController(String url, AuthToken authToken) {
        super(url, authToken);
        this.slaType = SLA_TYPE;
    }

    @Step("Создание потенциального Gap: {0}")
    protected Response createTask(String requestBody) {
        return super.createTask(requestBody);
    }

    public Response createPotentialGap(GeneralTask potentialGap) {
        TaskRequestBody slaRequestBody = new TaskRequestBody(potentialGap);
        this.response = createTask(slaRequestBody.keepMandatoryAndCreateFieldsAnd(HANDLER_USER));
        TaskResponseBody gapResponseBody = JsonUtils.deserialize(this.response, TaskResponseBody.class);
        if (gapResponseBody != null) {
            potentialGap.setId(gapResponseBody.getId());
            potentialGap.setNumber(gapResponseBody.getNumber());
            potentialGap.setFinishStatus(gapResponseBody.getFinishStatus());
        }
        return this.response;
    }
}
