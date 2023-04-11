package com.ts.integration.tests.sla;

import com.ts.common.asserts.ApiAsserts;
import com.ts.common.controllers.sla.SlaResponseBody;
import com.ts.common.controllers.sla.slaBug.SlaBugController;
import com.ts.common.entitites.commonEntities.Udfs;
import com.ts.common.entitites.tasks.Task;
import com.ts.common.enums.ComSlaOperations;
import com.ts.common.enums.SlaType;
import com.ts.common.listeners.TestListener;
import com.ts.common.utils.InitEntities;
import com.ts.integration.tests.BaseIntegrationTest;
import jdk.jfr.Description;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import static com.ts.common.application.controllers.TrackStudioHttpStatusCodes.HTTP_OK;
import static com.ts.common.entitites.commonEntities.List.Constants.CRITICAL;
import static com.ts.common.entitites.commonEntities.List.Constants.REMOTE_ACCESS;
import static com.ts.common.entitites.commonEntities.Udfs.UdfSd.*;
import static com.ts.common.enums.Users.*;
import static com.ts.common.utils.InitEntities.*;

@Listeners({TestListener.class})
public class SlaBugTest extends BaseIntegrationTest {
    private static SlaBugController slaBugController;
    private Task slaTask;
    private Udfs udf;

    @BeforeClass(alwaysRun = true)
    public void beforeClass() {
        udf = refreshUdf();
        udf.setUdfSdModule(InitEntities.getUdfsModuleThrowsJson());
        udf.setUdfList(InitEntities.generateUdfList(UDF_SDBUG_PRIORITYBUG, CRITICAL));
        udf.setUdfsBdkuConfiguration(InitEntities.getBdkuThrowsJson());
        udf.setSecondUdfList(InitEntities.generateUdfList(UDF_SD_REMOTEACCESS, REMOTE_ACCESS));
        slaTask = InitEntities.getSlaTask(SlaType.SLA_BUG, ComSlaOperations.CAT);
        slaTask.setUdfs(udf);
        slaBugController = apiController.getSlaBugController();
        slaBugController.createSlaBugTask(slaTask);
        ApiAsserts.assertThat(slaBugController.getResponse())
                .isCorrectResponseCode(HTTP_OK)
                .isParseableBody(SlaResponseBody.class);
    }

    @AfterClass(alwaysRun = true)
    public void afterClass() {
        slaBugController.performCommonOperation(slaTask, ComSlaOperations.REMOVE_REQUEST);
    }

    @Test(priority = 0)
    @Description("Test description: Receive task")
    public void receiveTask() {
        slaBugController.receiveActualTask(slaTask.getNumber());
        ApiAsserts.assertThat(slaBugController.getResponse())
                .isCorrectResponseCode(HTTP_OK)
                .isParseableBody(SlaResponseBody.class);
    }

    @Test(priority = 1)
    @Description("Test description: Perform operation to change author")
    public void commonOperations() {
        udf = refreshUdf();
        udf.setUdfUser(generateUdfUser(UDF_SD_AUTHORCLIENT_MSG));
        slaTask.setUdfs(udf);
        slaBugController.setAuthToken(generateAuthToken(EMPLOYEE));
        slaBugController.performCommonOperation(slaTask, ComSlaOperations.CHANGE_AUTHOR);
        ApiAsserts.assertThat(slaBugController.getResponse())
                .isCorrectResponseCode(HTTP_OK)
                .isParseableBody(SlaResponseBody.class);
    }
}
