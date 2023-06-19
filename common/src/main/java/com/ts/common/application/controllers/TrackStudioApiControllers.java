package com.ts.common.application.controllers;

import com.ts.common.controllers.BaseController;
import com.ts.common.controllers.TaskResponseBody;
import com.ts.common.controllers.UserController;
import com.ts.common.controllers.gap.GapSolutionController;
import com.ts.common.controllers.gap.PotentialGapController;
import com.ts.common.controllers.release.ReleaseModuleController;
import com.ts.common.controllers.sla.SlaBugController;
import com.ts.common.controllers.sla.SlaFeatureController;
import com.ts.common.controllers.sla.SlaHelpController;
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
    private UserController userController;
    private SlaHelpController slaHelpController;
    private SlaBugController slaBugController;
    private SlaFeatureController slaFeatureController;
    private PotentialGapController potentialGapController;
    private GapSolutionController gapSolutionController;
    private ReleaseModuleController releaseModuleController;
    private BaseController baseController;


    public TrackStudioApiControllers(AuthToken authToken) {
        this.userController = new UserController(STAND_URL, authToken);
        this.slaHelpController = new SlaHelpController(STAND_URL, authToken);
        this.slaBugController = new SlaBugController(STAND_URL, authToken);
        this.slaFeatureController = new SlaFeatureController(STAND_URL, authToken);
        this.potentialGapController = new PotentialGapController(STAND_URL, authToken);
        this.baseController = new BaseController(STAND_URL, authToken);
        this.gapSolutionController = new GapSolutionController(STAND_URL, authToken);
        this.releaseModuleController = new ReleaseModuleController(STAND_URL, authToken);
    }


    public GeneralTask receiveGeneralTask(String slaTaskNumber) {
        this.response = this.baseController.receiveActualTask(slaTaskNumber);
        TaskResponseBody slaResponseBody = JsonUtils.deserialize(this.response, TaskResponseBody.class);
        if (slaResponseBody != null) {
            return new GeneralTask(slaResponseBody);
        }
        return null;
    }


    @Step("Пользователь: {0}")
    public void updateToken(AuthToken authToken) {
        this.baseController.setAuthToken(authToken);
        this.slaFeatureController.setAuthToken(authToken);
        this.slaBugController.setAuthToken(authToken);
        this.slaHelpController.setAuthToken(authToken);
        this.potentialGapController.setAuthToken(authToken);
        this.gapSolutionController.setAuthToken(authToken);
        this.releaseModuleController.setAuthToken(authToken);
    }

}
