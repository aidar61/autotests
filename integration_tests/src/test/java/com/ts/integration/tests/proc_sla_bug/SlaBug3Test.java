package com.ts.integration.tests.proc_sla_bug;

import com.ts.common.application.controllers.TrackStudioHttpStatusCodes;
import com.ts.common.asserts.ApiAsserts;
import com.ts.common.controllers.sla.SlaResponseBody;
import com.ts.common.controllers.sla.slaBug.SlaBugController;
import com.ts.common.entitites.tasks.Task;
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

public class SlaBug3Test extends BaseIntegrationTest {
    private SlaBugController slaBugController;
    private Task task;
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

    @Test(priority = 2, description = "начать работу")
    public void slaBugMsgStart() {
        task.refreshUdf();
        slaBugController.performCommonOperation(task, START);
    }

    @Test(priority = 3, description = "запросить информацию")
    public void slaBugMsgRequestInfo() {
        slaBugController.performCommonOperation(task, REQUESTINFO);
    }

    @Test(priority = 4, description = "предоставить решение")
    public void slaBugMsgHotFix() {
        slaBugController.performCommonOperation(task, HOTFIX);
    }

    @Test(priority = 5, description = "закрыть")
    public void slaBugMsgClose() {
        udf = refreshUdf();
        udf.setUdfList(generateUdfList(UDF_EVALUATING_REQUEST_EXECUTION, FIVE));
        udf.setSecondUdfList(generateUdfList(UDF_SD_CLOSEREASON, SOLVED));
        task.refreshUdf(udf);
        slaBugController.performCommonOperation(task, CLOSE);
    }
}
