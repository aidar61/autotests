package com.ts.integration.tests.sla;

import com.ts.common.asserts.ApiAsserts;
import com.ts.common.controllers.sla.SlaHelpController;
import com.ts.common.controllers.sla.SlaResponseBody;
import com.ts.common.entitites.sla.SlaTask;
import com.ts.common.utils.RandomEntities;
import com.ts.integration.tests.BaseIntegrationTest;
import jdk.jfr.Description;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static com.ts.common.application.controllers.TrackStudioHttpStatusCodes.HTTP_OK;
import static com.ts.common.entitites.commonEntities.GeneralSlaId.Fields.CAT_SLA_HELP;

public class SlaTaskTest extends BaseIntegrationTest {
    private static SlaHelpController slaHelpController;
    private SlaTask slaTask;

    @BeforeClass(alwaysRun = true)
    public void beforeClass() {
        slaTask = RandomEntities.getSlaTask(CAT_SLA_HELP);
        slaHelpController = apiController.getSlaHelpController();

    }

    @BeforeMethod(alwaysRun = true)
    public void beforeMethod() {
    }

    @Test(priority = 0)
    @Description("Test description: Create sla consultation with MTBank module")
    public void createSlaTaskConsultation() {
        slaHelpController.createSlaTaskConsultation(slaTask);
        ApiAsserts.assertThat(slaHelpController.getResponse())
                .isCorrectResponseCode(HTTP_OK)
                .isParseableBody(SlaResponseBody.class);
        dbHelper.getGrTaskTable().receiveByTaskNumber(slaTask.getNumber());
    }

    @Test(priority = 1)
    @Description("Test description: Receive sla task consultation")
    public void receiveSlaTaskConsultation() {
        slaHelpController.receiveTaskConsultation(slaTask.getNumber());
        ApiAsserts.assertThat(slaHelpController.getResponse())
                .isCorrectResponseCode(HTTP_OK)
                .isParseableBody(SlaResponseBody.class);
    }


    @Test(priority = 2)
    @Description("Test description: Add comment to sla task consultation")
    public void addCommentSlaTaskConsultation() {
        slaHelpController.addComment(slaTask);
        ApiAsserts.assertThat(slaHelpController.getResponse())
                .isCorrectResponseCode(HTTP_OK)
                .isParseableBody(SlaResponseBody.class);
    }
}