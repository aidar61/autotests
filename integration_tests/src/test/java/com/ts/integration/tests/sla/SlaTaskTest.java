package com.ts.integration.tests.sla;

import com.ts.common.entitites.sla.SlaTask;
import com.ts.common.services.SlaHelpController;
import com.ts.common.utils.RandomEntities;
import com.ts.integration.tests.BaseIntegrationTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class SlaTaskTest extends BaseIntegrationTest {
    private static SlaHelpController slaHelpController;
    private SlaTask slaTask;

    @BeforeClass(alwaysRun = true)
    public void beforeClass() {
        slaHelpController = apiController.getSlaHelpController();
        System.out.println(slaHelpController);
    }

    @BeforeMethod(alwaysRun = true)
    public void beforeMethod() {
        slaTask = RandomEntities.getSlaTask();
    }

    @Test
    public void createSlaTaskHelp() {
        slaHelpController.createSlaTaskConsultation(slaTask);
        slaHelpController.receiveTaskConsultation(slaTask.getNumber());
    }
}
