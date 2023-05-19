package com.ts.integration.tests.proc_sla_bug;

import com.ts.common.application.controllers.TrackStudioHttpStatusCodes;
import com.ts.common.asserts.ApiAsserts;
import com.ts.common.controllers.TaskResponseBody;
import com.ts.common.controllers.sla.slaBug.SlaBugController;
import com.ts.common.entitites.tasks.GeneralTask;
import com.ts.common.enums.ComSlaOperations;
import com.ts.common.enums.SlaType;
import com.ts.common.utils.InitEntities;
import com.ts.integration.tests.BaseIntegrationTest;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static com.ts.common.entitites.commonEntities.List.Constants.*;
import static com.ts.common.entitites.commonEntities.Task.Constants.AKKREDITIVES;
import static com.ts.common.entitites.commonEntities.Task.Constants.MTBANK;
import static com.ts.common.entitites.commonEntities.Udfs.UdfSd.*;
import static com.ts.common.entitites.commonEntities.User.Constants.ALTUNIN_NIKOLAY;
import static com.ts.common.enums.Users.CLIENT;
import static com.ts.common.utils.InitEntities.*;

public class SlaBug6Test extends BaseIntegrationTest {
    private SlaBugController slaBugController;
    private GeneralTask task;

    @BeforeClass(alwaysRun = true)
    public void beforeClass() {
        slaBugController = apiController.getSlaBugController();
    }

    @AfterMethod(alwaysRun = true)
    public void afterMethod() {
        ApiAsserts.assertThat(slaBugController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class);
    }

    @Test(groups = {"SlaBug", "Regression"},description = "создание задачи")
    public void slaBugCat() {
        udf = refreshUdf();
        udf.setUdfTask(InitEntities.generateUdfTask(UDF_SD_MODULE, AKKREDITIVES));
        udf.setSecondUdfTask(generateUdfTask(UDF_BDKU_CONFIGURATION, MTBANK));
        udf.setUdfList(InitEntities.generateUdfList(UDF_SDBUG_PRIORITYBUG, CRITICAL));
        udf.setSecondUdfList(InitEntities.generateUdfList(UDF_SD_REMOTEACCESS, REMOTE_ACCESS));
        task = InitEntities.getSlaTask(SlaType.SLA_BUG, ComSlaOperations.CAT);
        task.refreshUdf(udf);
        task.setHandlerUser(generateUser(ALTUNIN_NIKOLAY));
        apiController.updateToken(generateAuthToken(CLIENT));
        slaBugController.createSlaBugTask(task);
    }

//    @Test(groups = {"SlaBug", "Regression"},description = "закрыть задачу", dependsOnMethods = "slaBugCat")
//    public void slaBugMsgClose() {
//        udf = refreshUdf();
//        udf.setUdfList(generateUdfList(UDF_EVALUATING_REQUEST_EXECUTION, FIVE));
//        udf.setSecondUdfList(generateUdfList(UDF_SD_CLOSEREASON, SOLVED));
//        task.refreshUdf(udf);
//        apiController.updateToken(generateAuthToken(CLIENT));
//        slaBugController.performCommonOperation(task, ComSlaOperations.CLOSE);
//    }
}
