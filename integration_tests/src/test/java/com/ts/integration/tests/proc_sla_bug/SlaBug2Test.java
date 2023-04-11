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

    @Test(priority = 0, description = "создание задачи")
    public void slaBugCat() {
        udf = refreshUdf();
        udf.setUdfSdModule(InitEntities.getUdfsModuleThrowsJson());
        udf.setUdfList(InitEntities.generateUdfList(UDF_SDBUG_PRIORITYBUG, CRITICAL));
        udf.setUdfsBdkuConfiguration(InitEntities.getBdkuThrowsJson());
        udf.setSecondUdfList(InitEntities.generateUdfList(UDF_SD_REMOTEACCESS, REMOTE_ACCESS));
        task = InitEntities.getSlaTask(SlaType.SLA_BUG, ComSlaOperations.CAT);
        task.refreshUdf(udf);
        apiController.updateToken(generateAuthToken(CLIENT));
        slaBugController.createSlaBugTask(task);
    }

    @Test(priority = 1, description = "принятие на анализ")
    public void slaBugMsgAnalize() {
        udf = refreshUdf();
        udf.setUdfUser(generateUdfUser(UDF_SD_TRUSTEDWATCHER, ABDULLAEV_BAHODIR));
        udf.setSecondUdfUser(generateUdfUser(UDF_WATCHER, ABDULLAEV_BAHODIR));
        udf.setThirdUdfUser(generateUdfUser(STDT_HANDLER, ALTUNIN_NIKOLAY));
        task.setHandlerUser(generateUser(ALTUNIN_NIKOLAY));
        task.refreshUdf(udf);
        apiController.updateToken(generateAuthToken(EMPLOYEE));
        slaBugController.msgAnalize(task);
    }

    @Test(priority = 2, description = "запросить информацию")
    public void slaBugMsgRequestInfo() {
        task.refreshUdf();
        slaBugController.performCommonOperation(task, REQUESTINFO);
    }

    @Test(priority = 3, description = "предоставить информацию")
    public void slaBugMsgProvideInfo() {
        task.refreshUdf();
        apiController.updateToken(generateAuthToken(CLIENT));
        slaBugController.performCommonOperation(task, PROVIDEINFO);
    }

    @Test(priority = 4, description = "запросить информацию")
    public void slaBugMsgRequestInfoRetry() {
        apiController.updateToken(generateAuthToken(EMPLOYEE));
        slaBugController.performCommonOperation(task, REQUESTINFO);
    }

    @Test(priority = 5, description = "отменить запрос информации")
    public void slaBugMsgUndoRequestInfo() {
        slaBugController.performCommonOperation(task, UNDOREQUESTINFO);
    }

    @Test(priority = 6, description = "начать работу")
    public void slaBugMsgStart() {
        slaBugController.performCommonOperation(task, START);
    }

    @Test(priority = 7, description = "вернуть на анализ")
    public void slaBugMsgUndoStart() {
        slaBugController.performCommonOperation(task, UNDOSTART);
    }

    @Test(priority = 8, description = "начать работу")
    public void slaBugMsgStartRetry() {
        slaBugController.performCommonOperation(task, START);
    }

    @Test(priority = 9, description = "предоставить решение")
    public void slaBugMsgStHotFix() {
        slaBugController.performCommonOperation(task, HOTFIX);
    }

    @Test(priority = 10, description = "вернуть в работу")
    public void slaBugMsgReturn() {
        apiController.updateToken(generateAuthToken(CLIENT));
        slaBugController.performCommonOperation(task, RETURN);
    }

    @Test(priority = 11, description = "предоставить решение")
    public void slaBugMsgProvideInfoRetry() {
        apiController.updateToken(generateAuthToken(EMPLOYEE));
        slaBugController.performCommonOperation(task, PROVIDEINFO);
    }

    @Test(priority = 12, description = "ошибка устранена")
    public void slaBugMsgAcceptSolution() {
        udf.setUdfList(generateUdfList(UDF_EVALUATING_REQUEST_EXECUTION, FIVE));
        apiController.updateToken(generateAuthToken(CLIENT));
        slaBugController.performCommonOperation(task, ACCEPTSOLUTION);
    }
}
