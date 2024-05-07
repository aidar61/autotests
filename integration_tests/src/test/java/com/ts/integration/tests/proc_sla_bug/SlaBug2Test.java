package com.ts.integration.tests.proc_sla_bug;

import com.ts.common.application.controllers.TrackStudioHttpStatusCodes;
import com.ts.common.asserts.ApiAsserts;
import com.ts.common.controllers.TaskResponseBody;
import com.ts.common.controllers.sla.SlaBugController;
import com.ts.common.entitites.tasks.GeneralTask;
import com.ts.common.enums.Operations;
import com.ts.common.enums.TaskType;
import com.ts.common.utils.InitEntities;
import com.ts.integration.tests.BaseIntegrationTest;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static com.ts.common.entitites.commonEntities.List.Constants.*;
import static com.ts.common.entitites.commonEntities.Task.Constants.AKKREDITIVES;
import static com.ts.common.entitites.commonEntities.Task.Constants.MTBANK;
import static com.ts.common.entitites.commonEntities.Udfs.UdfSd.*;
import static com.ts.common.entitites.commonEntities.User.Constants.ABDULLAEV_BAHODIR;
import static com.ts.common.entitites.commonEntities.User.Constants.ALTUNIN_NIKOLAY;
import static com.ts.common.enums.Operations.*;
import static com.ts.common.enums.Users.CLIENT;
import static com.ts.common.enums.Users.EMPLOYEE;
import static com.ts.common.utils.InitEntities.*;

public class SlaBug2Test extends BaseIntegrationTest {
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

    @Test(groups = {"SlaBug", "Regression"}, description = "создание задачи")
    public void slaBugCat() {
        udf = refreshUdf();
        udf.setUdfTask(InitEntities.generateUdfTask(UDF_SD_MODULE, AKKREDITIVES));
        udf.setSecondUdfTask(generateUdfTask(UDF_BDKU_CONFIGURATION, MTBANK));
        udf.setUdfList(InitEntities.generateUdfList(UDF_SDBUG_PRIORITYBUG, CRITICAL));
        udf.setSecondUdfList(InitEntities.generateUdfList(UDF_SD_REMOTEACCESS, NO_REMOTE_ACCESS));
        task = InitEntities.getGeneralTask(TaskType.SLA_BUG, Operations.CAT);
        task.refreshUdf(udf);
        task.setHandlerUser(generateUser(ALTUNIN_NIKOLAY));
        apiController.updateToken(generateAuthToken(CLIENT));
        slaBugController.createSlaBugTask(task);
    }

    @Test(groups = {"SlaBug", "Regression"}, description = "принятие на анализ", dependsOnMethods = "slaBugCat")
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

    @Test(groups = {"SlaBug", "Regression"}, description = "запросить информацию", dependsOnMethods = "slaBugMsgAnalize")
    public void slaBugMsgRequestInfo() {
        task.refreshUdf();
        apiController.updateToken(generateAuthToken(EMPLOYEE));
        slaBugController.performCommonOperation(task, REQUESTINFO);
    }

    @Test(groups = {"SlaBug", "Regression"}, description = "предоставить информацию", dependsOnMethods = "slaBugMsgRequestInfo")
    public void slaBugMsgProvideInfo() {
        task.refreshUdf();
        apiController.updateToken(generateAuthToken(CLIENT));
        slaBugController.performCommonOperation(task, PROVIDEINFO);
    }

    @Test(groups = {"SlaBug", "Regression"}, description = "запросить информацию", dependsOnMethods = "slaBugMsgProvideInfo")
    public void slaBugMsgRequestInfoRetry() {
        apiController.updateToken(generateAuthToken(EMPLOYEE));
        slaBugController.performCommonOperation(task, REQUESTINFO);
    }

    @Test(groups = {"SlaBug", "Regression"}, description = "отменить запрос информации", dependsOnMethods = "slaBugMsgRequestInfoRetry")
    public void slaBugMsgUndoRequestInfo() {
        apiController.updateToken(generateAuthToken(EMPLOYEE));
        slaBugController.performCommonOperation(task, UNDOREQUESTINFO);
    }

    @Test(groups = {"SlaBug", "Regression"}, description = "начать работу", dependsOnMethods = "slaBugMsgUndoRequestInfo")
    public void slaBugMsgStart() {
        apiController.updateToken(generateAuthToken(EMPLOYEE));
        slaBugController.performCommonOperation(task, START);
    }

    @Test(groups = {"SlaBug", "Regression"}, description = "вернуть на анализ", dependsOnMethods = "slaBugMsgStart")
    public void slaBugMsgUndoStart() {
        apiController.updateToken(generateAuthToken(EMPLOYEE));
        slaBugController.performCommonOperation(task, UNDOSTART);
    }

    @Test(groups = {"SlaBug", "Regression"}, description = "начать работу", dependsOnMethods = "slaBugMsgUndoStart")
    public void slaBugMsgStartRetry() {
        apiController.updateToken(generateAuthToken(EMPLOYEE));
        slaBugController.performCommonOperation(task, START);
    }

    @Test(groups = {"SlaBug", "Regression"}, description = "предоставить решение", dependsOnMethods = "slaBugMsgStartRetry")
    public void slaBugMsgStHotFix() {
        apiController.updateToken(generateAuthToken(EMPLOYEE));
        slaBugController.performCommonOperation(task, HOTFIX);
    }

    @Test(groups = {"SlaBug", "Regression"}, description = "вернуть в работу", dependsOnMethods = "slaBugMsgStHotFix")
    public void slaBugMsgReturn() {
        apiController.updateToken(generateAuthToken(CLIENT));
        slaBugController.performCommonOperation(task, RETURN);
    }

    @Test(groups = {"SlaBug", "Regression"}, description = "предоставить решение", dependsOnMethods = "slaBugMsgReturn")
    public void slaBugMsgProvideInfoRetry() {
        apiController.updateToken(generateAuthToken(EMPLOYEE));
        slaBugController.performCommonOperation(task, HOTFIX);
    }

    @Test(groups = {"SlaBug", "Regression"}, description = "ошибка устранена", dependsOnMethods = "slaBugMsgProvideInfoRetry")
    public void slaBugMsgAcceptSolution() {
        udf.setUdfList(generateUdfList(UDF_EVALUATING_REQUEST_EXECUTION, FIVE));
        apiController.updateToken(generateAuthToken(CLIENT));
        slaBugController.performCommonOperation(task, ACCEPTSOLUTION);
    }
}
