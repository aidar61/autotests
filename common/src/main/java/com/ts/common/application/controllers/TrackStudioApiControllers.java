package com.ts.common.application.controllers;

import com.ts.common.controllers.sla.BaseSlaController;
import com.ts.common.controllers.sla.SlaResponseBody;
import com.ts.common.controllers.sla.slaBug.SlaBugController;
import com.ts.common.controllers.sla.slaFeature.SlaFeatureController;
import com.ts.common.controllers.sla.slaHelp.SlaHelpController;
import com.ts.common.entitites.sla.SlaTask;
import com.ts.common.utils.JsonUtils;
import io.restassured.response.Response;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import static com.ts.common.config.AppConfigProvider.BASE_URL;

@Getter
@Setter
@Slf4j
public class TrackStudioApiControllers {
    private Response response;
    private AuthToken authToken;
    private SlaHelpController slaHelpController;
    private SlaBugController slaBugController;
    private SlaFeatureController slaFeatureController;
    private BaseSlaController slaController;


    public TrackStudioApiControllers(AuthToken authToken) {
        this.slaHelpController = new SlaHelpController(BASE_URL, authToken);
        this.slaBugController = new SlaBugController(BASE_URL, authToken);
        this.slaFeatureController = new SlaFeatureController(BASE_URL, authToken);
        this.slaController = new BaseSlaController(BASE_URL, authToken);
    }


    public SlaTask receiveSlaTask(String slaTaskNumber) {
        this.response = this.slaController.receiveActualTask(slaTaskNumber);
        SlaResponseBody slaResponseBody = JsonUtils.deserialize(this.response, SlaResponseBody.class);
        if (slaResponseBody != null) {
            return new SlaTask(slaResponseBody);
        }
        return null;
    }

    public void updateToken(AuthToken authToken) {
        this.slaController.setAuthToken(authToken);
        this.slaFeatureController.setAuthToken(authToken);
        this.slaBugController.setAuthToken(authToken);
        this.slaHelpController.setAuthToken(authToken);
    }

}
