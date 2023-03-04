package com.ts.common.controllers.sla.slaBug;

import com.ts.common.controllers.sla.BaseSlaController;
import com.ts.common.entitites.commonEntities.GeneralSlaId;
import com.ts.common.entitites.sla.SlaTask;
import io.restassured.response.Response;

import java.util.Map;

public class SlaBugController extends BaseSlaController {
    public SlaBugController(String url, Map<String, String> headersBaseController) {
        super(url, headersBaseController);
    }

    @Override
    protected Response createTask(String requestBody) {
        return super.createTask(requestBody);
    }

    @Override
    protected Response performOperation(SlaTask slaTask, String requestBody) {
        return super.performOperation(slaTask, requestBody);
    }
}
