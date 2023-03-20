package com.ts.integration.tests.sla;

import com.ts.common.asserts.ApiAsserts;
import com.ts.common.controllers.sla.SlaResponseBody;
import com.ts.common.controllers.sla.slaFeature.SlaFeatureController;
import com.ts.common.entitites.commonEntities.Udfs;
import com.ts.common.entitites.sla.SlaTask;
import com.ts.common.listeners.LogCatchListener;
import com.ts.common.utils.RandomUtils;
import com.ts.integration.tests.BaseIntegrationTest;
import jdk.jfr.Description;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import static com.ts.common.application.controllers.TrackStudioHttpStatusCodes.HTTP_OK;
import static com.ts.common.entitites.commonEntities.List.Constants.FREE_LAW;
import static com.ts.common.entitites.commonEntities.List.Constants.OWN;
import static com.ts.common.entitites.commonEntities.Udfs.UdfSd.*;
import static com.ts.common.entitites.commonEntities.User.Constants.ABDULLAEV_BAHODIR;
import static com.ts.common.enums.ComSlaOperations.*;
import static com.ts.common.enums.SlaType.SLA_FEATURE;
import static com.ts.common.utils.InitEntities.*;

@Listeners({LogCatchListener.class})
public class SlaFeatureTest extends BaseIntegrationTest {
    private static SlaFeatureController slaFeatureController;
    private SlaTask slaTask;
    private Udfs udf;

    @BeforeClass(alwaysRun = true)
    public void beforeClass() {
        udf = refreshUdf();
        udf.setUdfSdModule(getUdfsModuleThrowsJson());
        udf.setUdfsBdkuConfiguration(getBdkuThrowsJson());
        udf.setUdfList(generateUdfList(UDF_SDFEATURE_TYPE, OWN));
        udf.setSecondUdfList(generateUdfList(UDF_SDFEATURE_PAYDCS, FREE_LAW));
        slaTask = getSlaTask(SLA_FEATURE, CAT);
        slaTask.setUdfs(udf);
        slaFeatureController = apiController.getSlaFeatureController();
        slaFeatureController.createSlaFeatureTask(slaTask);
        ApiAsserts.assertThat(slaFeatureController.getResponse())
                .isCorrectResponseCode(HTTP_OK)
                .isParseableBody(SlaResponseBody.class);
    }

    @AfterClass(alwaysRun = true)
    public void afterClass() {

    }

    @Test(priority = 0)
    @Description("Test description: Receive task")
    public void receiveTask() {
        slaFeatureController.receiveSlaTask(slaTask.getNumber());
        ApiAsserts.assertThat(slaFeatureController.getResponse())
                .isCorrectResponseCode(HTTP_OK)
                .isParseableBody(SlaResponseBody.class);
    }

//    @Test(priority = 1)
//    @Description("Test description: Perform operation to change author")
//    public void commonOperation() {
//        udf = refreshUdf();
//        udf.setUdfUser(generateUdfUser(UDF_SD_AUTHORCLIENT_MSG));
//        slaTask.setUdfs(udf);
//        slaFeatureController.performCommonOperation(slaTask, CHANGE_AUTHOR);
//        ApiAsserts.assertThat(slaFeatureController.getResponse())
//                .isCorrectResponseCode(HTTP_OK)
//                .isParseableBody(SlaResponseBody.class);
//    }

    @Test(priority = 1)
    public void msgSlaFeatureTopreCost() { // начать предварительную оценку
        udf = refreshUdf();
        udf.setUdfUser(generateUdfUser(STDT_HANDLER, ABDULLAEV_BAHODIR));
        slaTask.setUdfs(udf);
        slaTask.setHandlerUser(generateUser(ABDULLAEV_BAHODIR));
        slaFeatureController.msgToprecost(slaTask);
        ApiAsserts.assertThat(slaFeatureController.getResponse())
                .isCorrectResponseCode(HTTP_OK)
                .isParseableBody(SlaResponseBody.class);
    }

    @Test(priority = 2, dependsOnMethods = "msgSlaFeatureTopreCost")
    public void msgSlaFeatureRequestReqInfo() throws InterruptedException {
        Thread.sleep(5000);
        slaTask.refreshUdf();
        slaFeatureController.performCommonOperation(slaTask, REQUESTREQINFO);
        ApiAsserts.assertThat(slaFeatureController.getResponse())
                .isCorrectResponseCode(HTTP_OK)
                .isParseableBody(SlaResponseBody.class);
    }

    @Test(priority = 3)
    public void msgSlaFeatureProvideReqInfo() {
        slaTask.refreshUdf();
        slaFeatureController.performCommonOperation(slaTask, PROVIDEREQINFO);
        ApiAsserts.assertThat(slaFeatureController.getResponse())
                .isCorrectResponseCode(HTTP_OK)
                .isParseableBody(SlaResponseBody.class);
    }
}
