package com.ts.common.application.controllers;

import com.ts.common.controllers.sla.SlaHelpController;
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

    public TrackStudioApiControllers() {
        this.authToken = new AuthToken();
        this.slaHelpController = new SlaHelpController(BASE_URL, authToken);
    }
}
