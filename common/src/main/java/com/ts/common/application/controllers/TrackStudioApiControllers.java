package com.ts.common.application.controllers;

import com.ts.common.controllers.BaseController;
import com.ts.common.controllers.TaskResponseBody;
import com.ts.common.controllers.gap.potentialGap.PotentialGapController;
import com.ts.common.controllers.sla.slaBug.SlaBugController;
import com.ts.common.controllers.sla.slaFeature.SlaFeatureController;
import com.ts.common.controllers.sla.slaHelp.SlaHelpController;
import com.ts.common.entitites.tasks.GeneralTask;
import com.ts.common.utils.JsonUtils;
import io.qameta.allure.Step;
import io.restassured.response.Response;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import static com.ts.common.config.AppConfigProvider.STAND_URL;

@Getter
@Setter
@Slf4j
public class TrackStudioApiControllers {
    private Response response;
    private AuthToken authToken;
    private SlaHelpController slaHelpController;
    private SlaBugController slaBugController;
    private SlaFeatureController slaFeatureController;
    private PotentialGapController potentialGapController;
    private BaseController slaController;


    public TrackStudioApiControllers(AuthToken authToken) {
        this.slaHelpController = new SlaHelpController(STAND_URL, authToken);
        this.slaBugController = new SlaBugController(STAND_URL, authToken);
        this.slaFeatureController = new SlaFeatureController(STAND_URL, authToken);
        this.potentialGapController = new PotentialGapController(STAND_URL, authToken);
        this.slaController = new BaseController(STAND_URL, authToken);
    }


    public GeneralTask receiveSlaTask(String slaTaskNumber) {
        this.response = this.slaController.receiveActualTask(slaTaskNumber);
        TaskResponseBody slaResponseBody = JsonUtils.deserialize(this.response, TaskResponseBody.class);
        if (slaResponseBody != null) {
            return new GeneralTask(slaResponseBody);
        }
        return null;
    }

    @Step("Пользователь: {0}")
    public void updateToken(AuthToken authToken) {
        this.slaController.setAuthToken(authToken);
        this.slaFeatureController.setAuthToken(authToken);
        this.slaBugController.setAuthToken(authToken);
        this.slaHelpController.setAuthToken(authToken);
        this.potentialGapController.setAuthToken(authToken);
    }

}
