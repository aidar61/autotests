package com.ts.common.application.controllers;

import com.ts.common.controllers.BaseController;
import com.ts.common.controllers.TaskResponseBody;
import com.ts.common.controllers.UserController;
import com.ts.common.controllers.advice.AdviceController;
import com.ts.common.controllers.advice.ConfirmationController;
import com.ts.common.controllers.advice.SanctionController;
import com.ts.common.controllers.dev.DevTaskController;
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

import static com.ts.common.config.AppConfigProvider.STAND;
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
    private AdviceController adviceController;
    private ConfirmationController confirmationController;
    private SanctionController sanctionController;
    private DevTaskController devTaskController;
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
        this.adviceController = new AdviceController(STAND_URL, authToken);
        this.confirmationController = new ConfirmationController(STAND_URL, authToken);
        this.sanctionController = new SanctionController(STAND_URL, authToken);
        this.devTaskController = new DevTaskController(STAND_URL, authToken);
    }


    public GeneralTask receiveGeneralTask(String slaTaskNumber) {
        this.response = this.baseController.receiveActualTask(slaTaskNumber);
        TaskResponseBody taskResponseBody = JsonUtils.deserialize(this.response, TaskResponseBody.class);
        if (taskResponseBody != null)
        {
            return new GeneralTask(taskResponseBody);
        }
        return null;
    }

    public Response receiveTask(String slaTaskNumber) {
        return this.baseController.receiveActualTask(slaTaskNumber);
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
        this.adviceController.setAuthToken(authToken);
        this.confirmationController.setAuthToken(authToken);
        this.sanctionController.setAuthToken(authToken);
        this.devTaskController.setAuthToken(authToken);
    }

}
