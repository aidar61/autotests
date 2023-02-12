package com.ts.integration.tests.sla;

import com.ts.common.asserts.ApiAsserts;
import com.ts.common.controllers.sla.SlaHelpController;
import com.ts.common.controllers.sla.SlaResponseBody;
import com.ts.common.entitites.sla.SlaTask;
import com.ts.common.utils.RandomEntities;
import com.ts.integration.tests.BaseIntegrationTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static com.ts.common.application.TrackStudioHttpStatusCodes.HTTP_OK;
import static com.ts.common.entitites.commonEntities.GeneralSlaId.Fields.CAT_SLA_HELP;

public class SlaTaskTest extends BaseIntegrationTest {
    private static SlaHelpController slaHelpController;
    private SlaTask slaTask;

    @BeforeClass(alwaysRun = true)
    public void beforeClass() {
        slaTask = RandomEntities.getSlaTask(CAT_SLA_HELP);
        slaHelpController = apiController.getSlaHelpController();
        slaHelpController.createSlaTaskConsultation(slaTask);
        ApiAsserts.assertThat(slaHelpController.getResponse())
                .isCorrectResponseCode(HTTP_OK)
                .isParseableBody(SlaResponseBody.class);
    }

    @BeforeMethod(alwaysRun = true)
    public void beforeMethod() {
    }

    @Test(priority = 0)
    public void receiveSlaTaskConsultation() {
        slaHelpController.receiveTaskConsultation(slaTask.getNumber());
        ApiAsserts.assertThat(slaHelpController.getResponse())
                .isCorrectResponseCode(HTTP_OK)
                .isParseableBody(SlaResponseBody.class);
    }

    @Test(priority = 1)
    public void addCommentSlaTaskConsultation() {
        slaHelpController.addComment(slaTask);
        ApiAsserts.assertThat(slaHelpController.getResponse())
                .isCorrectResponseCode(HTTP_OK)
                .isParseableBody(SlaResponseBody.class);
    }
}