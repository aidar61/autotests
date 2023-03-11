package com.ts.common.application.controllers;

import com.ts.common.controllers.sla.slaBug.SlaBugController;
import com.ts.common.controllers.sla.slaFeature.SlaFeatureController;
import com.ts.common.controllers.sla.slaHelp.SlaHelpController;
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

    public TrackStudioApiControllers() {
        this.authToken = new AuthToken();
        this.slaHelpController = new SlaHelpController(BASE_URL, authToken);
        this.slaBugController = new SlaBugController(BASE_URL, authToken);
        this.slaFeatureController = new SlaFeatureController(BASE_URL, authToken);
    }
}
