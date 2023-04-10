package com.ts.integration.tests.proc_sla_bug;

import com.ts.common.application.controllers.TrackStudioHttpStatusCodes;
import com.ts.common.asserts.ApiAsserts;
import com.ts.common.controllers.sla.SlaResponseBody;
import com.ts.common.controllers.sla.slaBug.SlaBugController;
import com.ts.common.enums.ComSlaOperations;
import com.ts.common.enums.SlaType;
import com.ts.common.utils.InitEntities;
import com.ts.integration.tests.BaseIntegrationTest;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static com.ts.common.entitites.commonEntities.List.Constants.*;
import static com.ts.common.entitites.commonEntities.Udfs.UdfSd.*;
import static com.ts.common.entitites.commonEntities.User.Constants.ABDULLAEV_BAHODIR;
import static com.ts.common.entitites.commonEntities.User.Constants.ALTUNIN_NIKOLAY;
import static com.ts.common.enums.ComSlaOperations.*;
import static com.ts.common.enums.Users.CLIENT;
import static com.ts.common.enums.Users.EMPLOYEE;
import static com.ts.common.utils.InitEntities.*;

public class SlaBug2Test extends BaseIntegrationTest {
    private SlaBugController slaBugController;

    @BeforeClass
    public void beforeClass() {
        slaBugController = apiController.getSlaBugController();
    }

    @AfterMethod
    public void afterMethod() {
        ApiAsserts.assertThat(slaBugController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(SlaResponseBody.class);
    }

    @Test(priority = 0)
    public void slaBugCat() {
        udf = refreshUdf();
        udf.setUdfSdModule(InitEntities.getUdfsModuleThrowsJson());
        udf.setUdfList(InitEntities.generateUdfList(UDF_SDBUG_PRIORITYBUG, CRITICAL));
        udf.setUdfsBdkuConfiguration(InitEntities.getBdkuThrowsJson());
        udf.setSecondUdfList(InitEntities.generateUdfList(UDF_SD_REMOTEACCESS, REMOTE_ACCESS));
        slaTask = InitEntities.getSlaTask(SlaType.SLA_BUG, ComSlaOperations.CAT);
        slaTask.refreshUdf(udf);
        apiController.updateToken(generateAuthToken(CLIENT));
        slaBugController.createSlaBugTask(slaTask);
    }

    @Test(priority = 1)
    public void slaBugMsgAnalize() {
        udf = refreshUdf();
        udf.setUdfUser(generateUdfUser(UDF_SD_TRUSTEDWATCHER, ABDULLAEV_BAHODIR));
        udf.setSecondUdfUser(generateUdfUser(UDF_WATCHER, ABDULLAEV_BAHODIR));
        udf.setThirdUdfUser(generateUdfUser(STDT_HANDLER, ALTUNIN_NIKOLAY));
        slaTask.setHandlerUser(generateUser(ALTUNIN_NIKOLAY));
        slaTask.refreshUdf(udf);
        apiController.updateToken(generateAuthToken(EMPLOYEE));
        slaBugController.msgAnalize(slaTask);
    }

    @Test(priority = 2)
    public void slaBugMsgRequestInfo() {
        slaTask.refreshUdf();
        slaBugController.performCommonOperation(slaTask, REQUESTINFO);
    }

    @Test(priority = 3)
    public void slaBugMsgProvideInfo() {
        slaTask.refreshUdf();
        apiController.updateToken(generateAuthToken(CLIENT));
        slaBugController.performCommonOperation(slaTask, PROVIDEINFO);
    }

    @Test(priority = 4)
    public void slaBugMsgRequestInfoRetry() {
        apiController.updateToken(generateAuthToken(EMPLOYEE));
        slaBugController.performCommonOperation(slaTask, REQUESTINFO);
    }

    @Test(priority = 5)
    public void slaBugMsgUndoRequestInfo() {
        slaBugController.performCommonOperation(slaTask, UNDOREQUESTINFO);
    }

    @Test(priority = 6)
    public void slaBugMsgStart() {
        slaBugController.performCommonOperation(slaTask, START);
    }

    @Test(priority = 7)
    public void slaBugMsgUndoStart() {
        slaBugController.performCommonOperation(slaTask, UNDOSTART);
    }

    @Test(priority = 8)
    public void slaBugMsgStartRetry() {
        slaBugController.performCommonOperation(slaTask, START);
    }

    @Test(priority = 9)
    public void slaBugMsgStHotFix() {
        slaBugController.performCommonOperation(slaTask, HOTFIX);
    }

    @Test(priority = 10)
    public void slaBugMsgReturn() {
        apiController.updateToken(generateAuthToken(CLIENT));
        slaBugController.performCommonOperation(slaTask, RETURN);
    }

    @Test(priority = 11)
    public void slaBugMsgProvideInfoRetry() {
        apiController.updateToken(generateAuthToken(EMPLOYEE));
        slaBugController.performCommonOperation(slaTask, PROVIDEINFO);
    }

    @Test(priority = 12)
    public void slaBugMsgAcceptSolution() {
        udf.setUdfList(generateUdfList(UDF_EVALUATING_REQUEST_EXECUTION, FIVE));
        apiController.updateToken(generateAuthToken(CLIENT));
        slaBugController.performCommonOperation(slaTask, ACCEPTSOLUTION);
    }
}
