package com.ts.integration.tests.sla;

import com.ts.common.application.database.dbEntities.GrTaskDbEntity;
import com.ts.common.asserts.ApiAsserts;
import com.ts.common.asserts.TaskAsserts;
import com.ts.common.controllers.sla.slaHelp.SlaHelpController;
import com.ts.common.controllers.sla.slaHelp.SlaResponseBody;
import com.ts.common.entitites.commonEntities.List;
import com.ts.common.entitites.sla.SlaTask;
import com.ts.common.enums.TaskStatuses;
import com.ts.common.listeners.LogCatchListener;
import com.ts.common.utils.InitEntities;
import com.ts.integration.tests.BaseIntegrationTest;
import jdk.jfr.Description;
import org.assertj.core.api.Assertions;
import org.testng.annotations.*;

import static com.ts.common.application.controllers.TrackStudioHttpStatusCodes.HTTP_OK;
import static com.ts.common.entitites.commonEntities.GeneralSlaId.Fields.CAT;
import static com.ts.common.enums.TaskStatuses.STATUS_SLAHELP_CLOSED;
import static com.ts.common.enums.TaskStatuses.STATUS_SLAHELP_CONSULTED;
@Listeners({LogCatchListener.class})
public class SlaTaskTest extends BaseIntegrationTest {
    private static SlaHelpController slaHelpController;
    private SlaTask slaTask;
    private GrTaskDbEntity actualTask;
    private List slaTasks;

    @BeforeClass(alwaysRun = true)
    public void beforeClass() {
        slaTask = InitEntities.getSlaTask(CAT);
        slaHelpController = apiController.getSlaHelpController();
        slaHelpController.createTask(slaTask);
        ApiAsserts.assertThat(slaHelpController.getResponse())
                .isCorrectResponseCode(HTTP_OK)
                .isParseableBody(SlaResponseBody.class);
        actualTask = (GrTaskDbEntity) dbHelper.getGrTaskTable().receiveByTaskNumber(slaTask.getNumber());
        TaskAsserts.assertThat(actualTask).isExist();
    }

    @AfterClass(alwaysRun = true)
    public void afterClass() {
        slaHelpController.closeSlaTask(slaTask);
        ApiAsserts.assertThat(slaHelpController.getResponse())
                .isCorrectResponseCode(HTTP_OK)
                .isParseableBody(SlaResponseBody.class);
        actualTask = (GrTaskDbEntity) dbHelper.getGrTaskTable().receiveByTaskNumber(slaTask.getNumber());
        Assertions.assertThat(actualTask.getTask_status()).isEqualTo(STATUS_SLAHELP_CLOSED.name());
    }

    @BeforeMethod(alwaysRun = true)
    public void beforeMethod() {
    }

    @Test(priority = 0, alwaysRun = true)
    @Description("Test description: Create sla consultation with MTBank module")
    public void createSlaTaskConsultation() {

    }

    @Test(priority = 1, dependsOnMethods = "createSlaTaskConsultation")
    @Description("Test description: Receive sla task consultation")
    public void receiveSlaTaskConsultation() {
        slaHelpController.receiveTaskConsultation(slaTask.getNumber());
        ApiAsserts.assertThat(slaHelpController.getResponse())
                .isCorrectResponseCode(HTTP_OK)
                .isParseableBody(SlaResponseBody.class);
    }


    @Test(priority = 2, dependsOnMethods = "createSlaTaskConsultation")
    @Description("Test description: Add comment to sla task consultation")
    public void addCommentSlaTaskConsultation() {
        slaHelpController.addComment(slaTask);
        ApiAsserts.assertThat(slaHelpController.getResponse())
                .isCorrectResponseCode(HTTP_OK)
                .isParseableBody(SlaResponseBody.class);
    }

    @Test(priority = 3, dependsOnMethods = "createSlaTaskConsultation")
    @Description("Test description: Edit module of sla task consultation")
    public void editModule() {
        slaHelpController.editModule(slaTask);
        ApiAsserts.assertThat(slaHelpController.getResponse())
                .isCorrectResponseCode(HTTP_OK)
                .isParseableBody(SlaResponseBody.class);
    }

    @Test(priority = 4, dependsOnMethods = "createSlaTaskConsultation")
    @Description("Test description: Receive task to Analyses")
    public void receiveAnalyses() {
        slaHelpController.receiveAnalysis(slaTask);
        ApiAsserts.assertThat(slaHelpController.getResponse())
                .isCorrectResponseCode(HTTP_OK)
                .isParseableBody(SlaResponseBody.class);
        actualTask = (GrTaskDbEntity) dbHelper.getGrTaskTable().receiveByTaskNumber(slaTask.getNumber());
        Assertions.assertThat(actualTask.getTask_status()).isEqualTo(TaskStatuses.STATUS_SLAHELP_ANALIZING.name());
    }

    @Test(priority = 5, dependsOnMethods = "createSlaTaskConsultation")
    @Description("Test description: Request information about sla task")
    public void requestInformation() {
        slaHelpController.requestInformation(slaTask);
        ApiAsserts.assertThat(slaHelpController.getResponse())
                .isCorrectResponseCode(HTTP_OK)
                .isParseableBody(SlaResponseBody.class);
        actualTask = (GrTaskDbEntity) dbHelper.getGrTaskTable().receiveByTaskNumber(slaTask.getNumber());
        Assertions.assertThat(actualTask.getTask_status()).isEqualTo(TaskStatuses.STATUS_SLAHELP_WAITANALIZING.name());
    }

    @Test(priority = 6, dependsOnMethods = "createSlaTaskConsultation")
    public void provideInformation() {
        slaHelpController.provideInformation(slaTask);
        ApiAsserts.assertThat(slaHelpController.getResponse())
                .isCorrectResponseCode(HTTP_OK)
                .isParseableBody(SlaResponseBody.class);
    }

    @Test(priority = 7, dependsOnMethods = "createSlaTaskConsultation")
    public void provideConsultation() {
        slaHelpController.provideConsultation(slaTask);
        ApiAsserts.assertThat(slaHelpController.getResponse())
                .isCorrectResponseCode(HTTP_OK)
                .isParseableBody(SlaResponseBody.class);
        actualTask = (GrTaskDbEntity) dbHelper.getGrTaskTable().receiveByTaskNumber(slaTask.getNumber());
        Assertions.assertThat(actualTask.getTask_status()).isEqualTo(STATUS_SLAHELP_CONSULTED.name());
    }
}