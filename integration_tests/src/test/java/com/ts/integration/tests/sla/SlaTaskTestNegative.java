package com.ts.integration.tests.sla;

import com.ts.common.application.database.dbEntities.GrTaskDbEntity;
import com.ts.common.application.errors.ErrorResponseBody;
import com.ts.common.application.errors.TrackStudioErrors;
import com.ts.common.asserts.ApiAsserts;
import com.ts.common.asserts.TaskAsserts;
import com.ts.common.controllers.sla.TaskResponseBody;
import com.ts.common.controllers.sla.slaHelp.SlaHelpController;
import com.ts.common.entitites.commonEntities.List;
import com.ts.common.entitites.tasks.GeneralTask;
import com.ts.common.enums.ComSlaOperations;
import com.ts.common.enums.SlaType;
import com.ts.common.listeners.TestListener;
import com.ts.common.utils.InitEntities;
import com.ts.integration.tests.BaseIntegrationTest;
import jdk.jfr.Description;
import org.assertj.core.api.Assertions;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import static com.ts.common.application.controllers.TrackStudioHttpStatusCodes.HTTP_BAD_REQUEST;
import static com.ts.common.application.controllers.TrackStudioHttpStatusCodes.HTTP_OK;
import static com.ts.common.enums.TaskStatuses.STATUS_SLAHELP_CLOSED;

@Listeners({TestListener.class})
public class SlaTaskTestNegative extends BaseIntegrationTest {
    private static SlaHelpController slaHelpController;
    private GeneralTask slaTask;
    private GrTaskDbEntity actualTask;
    private List slaTasks;

    @BeforeClass(alwaysRun = true)
    public void beforeClass() {
        slaHelpController = apiController.getSlaHelpController();
        slaTask = InitEntities.getSlaTask(SlaType.SLA_HElP, ComSlaOperations.CAT);
        slaHelpController.createTask(slaTask);
        ApiAsserts.assertThat(slaHelpController.getResponse())
                .isCorrectResponseCode(HTTP_OK)
                .isParseableBody(TaskResponseBody.class);
        actualTask = (GrTaskDbEntity) dbHelper.getGrTaskTable().receiveByTaskNumber(slaTask.getNumber());
        TaskAsserts.assertThat(actualTask).isExist();
    }

    @AfterClass(alwaysRun = true)
    public void afterClass() {
        slaHelpController.closeSlaTask(slaTask);
        ApiAsserts.assertThat(slaHelpController.getResponse())
                .isCorrectResponseCode(HTTP_OK)
                .isParseableBody(TaskResponseBody.class);
        actualTask = (GrTaskDbEntity) dbHelper.getGrTaskTable().receiveByTaskNumber(slaTask.getNumber());
        Assertions.assertThat(actualTask.getTask_status()).isEqualTo(STATUS_SLAHELP_CLOSED.name());
    }

    @Test(description = "Try to receive analyze sla task without request information")
    @Description("Test description: ")
    public void tryToRequestInfoWithoutReceiveAnalyze() {
        slaHelpController.requestInformation(slaTask);
        ApiAsserts.assertThat(slaHelpController.getResponse())
                .isCorrectResponseCode(HTTP_BAD_REQUEST)
                .isParseableBody(ErrorResponseBody.class)
                .isCorrectError(String.format(TrackStudioErrors.REQUEST_INFO.getValue(), slaTask.getNumber()));
    }
}
