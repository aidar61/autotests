package com.ts.integration.tests.sla;

import com.ts.common.asserts.ApiAsserts;
import com.ts.common.entitites.sla.SlaTask;
import com.ts.common.services.SlaHelpController;
import com.ts.common.utils.RandomEntities;
import com.ts.integration.tests.BaseIntegrationTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static com.ts.common.application.TrackStudioHttpStatusCodes.HTTP_OK;

public class SlaTaskTest extends BaseIntegrationTest {
    private static SlaHelpController slaHelpController;
    private SlaTask slaTask;

    @BeforeClass(alwaysRun = true)
    public void beforeClass() {
        slaHelpController = apiController.getSlaHelpController();
    }

    @BeforeMethod(alwaysRun = true)
    public void beforeMethod() {
        slaTask = RandomEntities.getSlaTask();
    }

    @Test
    public void receiveTaskConsulTation() {
        slaHelpController.receiveTaskConsultation("1405500");
        ApiAsserts.assertThat(slaHelpController.getResponse())
                .isCorrectResponseCode(HTTP_OK);
    }
}
