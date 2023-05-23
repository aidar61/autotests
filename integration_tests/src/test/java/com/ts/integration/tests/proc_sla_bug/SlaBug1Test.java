package com.ts.integration.tests.proc_sla_bug;

import com.ts.common.asserts.ApiAsserts;
import com.ts.common.controllers.TaskResponseBody;
import com.ts.common.controllers.sla.SlaBugController;
import com.ts.common.entitites.tasks.GeneralTask;
import com.ts.common.enums.ComSlaOperations;
import com.ts.common.enums.TaskType;
import com.ts.common.utils.InitEntities;
import com.ts.integration.tests.BaseIntegrationTest;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static com.ts.common.application.controllers.TrackStudioHttpStatusCodes.HTTP_OK;
import static com.ts.common.entitites.commonEntities.List.Constants.CRITICAL;
import static com.ts.common.entitites.commonEntities.List.Constants.REMOTE_ACCESS;
import static com.ts.common.entitites.commonEntities.Task.Constants.AKKREDITIVES;
import static com.ts.common.entitites.commonEntities.Task.Constants.MTBANK;
import static com.ts.common.entitites.commonEntities.Udfs.UdfSd.*;
import static com.ts.common.entitites.commonEntities.User.Constants.ABDULLAEV_BAHODIR;
import static com.ts.common.entitites.commonEntities.User.Constants.ALTUNIN_NIKOLAY;
import static com.ts.common.enums.ComSlaOperations.*;
import static com.ts.common.enums.Users.CLIENT;
import static com.ts.common.enums.Users.EMPLOYEE;
import static com.ts.common.utils.InitEntities.*;

public class SlaBug1Test extends BaseIntegrationTest {
    private SlaBugController slaBugController;
    private GeneralTask task;


    @BeforeClass(alwaysRun = true)
    public void beforeClass() {
        slaBugController = apiController.getSlaBugController();
    }

    @AfterMethod(alwaysRun = true)
    public void afterMethod() {
        ApiAsserts.assertThat(slaBugController.getResponse())
                .isCorrectResponseCode(HTTP_OK)
                .isParseableBody(TaskResponseBody.class);
    }

    @Test(priority = 0, groups = {"SlaBug", "Regression"}, description = "Создание извещения об ошибке")
    public void slaBugCat() {
        udf = refreshUdf();
        udf.setUdfTask(InitEntities.generateUdfTask(UDF_SD_MODULE, AKKREDITIVES));
        udf.setSecondUdfTask(generateUdfTask(UDF_BDKU_CONFIGURATION, MTBANK));
        udf.setUdfList(InitEntities.generateUdfList(UDF_SDBUG_PRIORITYBUG, CRITICAL));
        udf.setSecondUdfList(InitEntities.generateUdfList(UDF_SD_REMOTEACCESS, REMOTE_ACCESS));
        task = InitEntities.getSlaTask(TaskType.SLA_BUG, ComSlaOperations.CAT);
        task.refreshUdf(udf);
        task.setHandlerUser(generateUser(ALTUNIN_NIKOLAY));
        apiController.updateToken(generateAuthToken(CLIENT));
        slaBugController.createSlaBugTask(task);
    }

    @Test(priority = 0, groups = {"SlaBug", "Regression"}, description = "принять на анализ", dependsOnMethods = "slaBugCat")
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

    @Test(priority = 0, groups = {"SlaBug", "Regression"}, description = "отклонить", dependsOnMethods = "slaBugMsgAnalize")
    public void slaBugMsgDecline() {
        task.refreshUdf();
        apiController.updateToken(generateAuthToken(EMPLOYEE));
        slaBugController.performCommonOperation(task, DECLINE);
    }

    @Test(priority = 0, groups = {"SlaBug", "Regression"}, description = "вернуть на анализ", dependsOnMethods = "slaBugMsgDecline")
    public void slaBugMsgUndoStart() {
        udf = refreshUdf();
        udf.setUdfUser(generateUdfUser(STDT_HANDLER, ALTUNIN_NIKOLAY));
        task.setHandlerUser(generateUser(ALTUNIN_NIKOLAY));
        task.refreshUdf(udf);
        apiController.updateToken(generateAuthToken(EMPLOYEE));
        slaBugController.performCommonOperation(task, UNDOSTART);
    }

    @Test(priority = 0, groups = {"SlaBug", "Regression"}, description = "закрыть как неустраненную", dependsOnMethods = "slaBugMsgUndoStart")
    public void slaBugMsgCloseUnfixable() {
        apiController.updateToken(generateAuthToken(EMPLOYEE));
        slaBugController.performCommonOperation(task, CLOSEUNFIXABLE);
    }
}
