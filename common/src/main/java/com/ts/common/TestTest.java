package com.ts.common;

import com.ts.common.application.TrackStudioApiControllers;
import com.ts.common.services.SlaHelpController;
import com.ts.common.tests.AbstractBaseTest;

public class TestTest extends AbstractBaseTest {
    private static SlaHelpController slaHelpController;

    public static void main(String[] args) {
        apiController = new TrackStudioApiControllers();
        slaHelpController = apiController.getSlaHelpController();
        slaHelpController.receiveTaskConsultation("1405500");
    }
}
