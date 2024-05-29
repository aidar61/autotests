package com.ts.integration.tests.proc_sla_feature.negative;

import com.ts.common.application.errors.ErrorResponseBody;
import com.ts.common.asserts.ApiAsserts;
import com.ts.common.controllers.TaskResponseBody;
import com.ts.common.controllers.sla.SlaFeatureController;
import com.ts.common.entitites.tasks.GeneralTask;
import com.ts.common.utils.InitEntities;
import com.ts.integration.tests.BaseIntegrationTest;
import jdk.jfr.Description;
import lombok.extern.slf4j.Slf4j;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static com.ts.common.application.controllers.TrackStudioHttpStatusCodes.HTTP_BAD_REQUEST;
import static com.ts.common.application.controllers.TrackStudioHttpStatusCodes.HTTP_OK;
import static com.ts.common.application.errors.TrackStudioErrors.OPERATION_NOT_ALLOWED_FOR_TASK;
import static com.ts.common.entitites.commonEntities.List.Constants.*;
import static com.ts.common.entitites.commonEntities.List.Constants.USERDATA_WIKI;
import static com.ts.common.entitites.commonEntities.Task.Constants.AKKREDITIVES;
import static com.ts.common.entitites.commonEntities.Task.Constants.MTBANK;
import static com.ts.common.entitites.commonEntities.Udfs.UdfSd.*;
import static com.ts.common.entitites.commonEntities.User.Constants.*;
import static com.ts.common.entitites.commonEntities.User.Constants.ARUTYANIN_YURIY;
import static com.ts.common.enums.Operations.BEGINCOST_PRE;
import static com.ts.common.enums.Operations.CAT;
import static com.ts.common.enums.TaskType.SLA_FEATURE;
import static com.ts.common.enums.Users.*;
import static com.ts.common.enums.Users.EMPLOYEE;
import static com.ts.common.utils.InitEntities.*;
import static com.ts.common.utils.InitEntities.generateAuthToken;
import static com.ts.common.utils.RandomUtils.generateComment;

@Slf4j
public class SlaFeatureNegative1Test extends BaseIntegrationTest {
    private static SlaFeatureController slaFeatureController;
    private GeneralTask task;

    @BeforeClass(alwaysRun = true)
    public void beforeClass() {
        slaFeatureController = apiController.getSlaFeatureController();
    }

    @Test(groups = {"SlaFeature", "Regression"}, description = "Создание задачи")
    public void msgSlaFeatureCat() {
        udf = refreshUdf();
        udf.setUdfTask(InitEntities.generateUdfTask(UDF_SD_MODULE, AKKREDITIVES));
        udf.setSecondUdfTask(generateUdfTask(UDF_BDKU_CONFIGURATION, MTBANK));
        udf.setUdfList(generateUdfList(UDF_SDFEATURE_TYPE, OWN));
        udf.setSecondUdfList(generateUdfList(UDF_SDFEATURE_PAYDCS, FREE_LAW));
        task = getGeneralTask(SLA_FEATURE, CAT);
        task.setUdfs(udf);
        task.setHandlerUser(generateUser(ALTUNIN_NIKOLAY));
        apiController.updateToken(generateAuthToken(CLIENT));
        slaFeatureController.createSlaFeatureTask(task);
        ApiAsserts.assertThat(slaFeatureController.getResponse())
                .isCorrectResponseCode(HTTP_OK)
                .isParseableBody(TaskResponseBody.class);
    }


    @Test(groups = {"SlaFeature", "Regression"}, description = "get запрос", dependsOnMethods = "msgSlaFeatureCat")
    @Description("Test description: Receive task")
    public void receiveTask() {
        apiController.updateToken(generateAuthToken(ROOT));
        slaFeatureController.receiveActualTask(task.getNumber());
        ApiAsserts.assertThat(slaFeatureController.getResponse())
                .isCorrectResponseCode(HTTP_OK)
                .isParseableBody(TaskResponseBody.class);
    }


    @Test(groups = {"SlaFeature", "Regression"}, description = "Начать предварительную оценку", dependsOnMethods = "receiveTask")
    @Description("Начать предварительную оценку")
    public void msgSlaFeatureTopreCost() { // начать предварительную оценку
        udf = refreshUdf();
        udf.setUdfUser(generateUdfUser(STDT_HANDLER, ABDULLAEV_BAHODIR));
        task.setUdfs(udf);
        task.setHandlerUser(generateUser(ABDULLAEV_BAHODIR));
        apiController.updateToken(generateAuthToken(EMPLOYEE));
        slaFeatureController.msgToprecost(task);
        ApiAsserts.assertThat(slaFeatureController.getResponse())
                .isCorrectResponseCode(HTTP_OK)
                .isParseableBody(TaskResponseBody.class);
    }

    @Test(groups = {"SlaFeature", "Regression"}, dependsOnMethods = "msgSlaFeatureTopreCost", description = "Запросить уточненные требования")
    @Description("Запросить уточненные требования")
    public void msgSlaFeatureRequestReqInfo() { // запросить уточненные требования
        task.refreshUdf();
        apiController.updateToken(generateAuthToken(EMPLOYEE));
        slaFeatureController.msgRequestReqInfo(task);
        ApiAsserts.assertThat(slaFeatureController.getResponse())
                .isCorrectResponseCode(HTTP_OK)
                .isParseableBody(TaskResponseBody.class);
    }

    @Test(groups = {"SlaFeature", "Regression"}, dependsOnMethods = "msgSlaFeatureRequestReqInfo", description = "Сообщить уточненные требования")
    @Description("Сообщить уточненные требования")
    public void msgSlaFeatureProvideReqInfo() { // сообщить уточненные требования
        task.refreshUdf();
        apiController.updateToken(generateAuthToken(CLIENT));
        slaFeatureController.msgProvideReqInfo(task);
        ApiAsserts.assertThat(slaFeatureController.getResponse())
                .isCorrectResponseCode(HTTP_OK)
                .isParseableBody(TaskResponseBody.class);
    }


    @Test(groups = {"SlaFeature", "Regression"}, dependsOnMethods = "msgSlaFeatureProvideReqInfo", description = "Передать на предварительную оценку аккаунт менеджеру")
    @Description("Передать на предварительную оценку аккаунт менеджеру")
    public void msgSlaFeatureBeginCostPre() {
        udf = refreshUdf();
        udf.setUdfDouble(generateUdfDouble(UDF_SDFEATURE_IMPLBUDGET, 4));
        udf.setSecondUdfDouble(generateUdfDouble(UDF_SDFEATURE_DETAILBUDGET, 4));
        udf.setUdfList(generateUdfList(UDF_SDFEATURE_TYPE, OWN));
        udf.setSecondUdfList(generateUdfList(UDF_SDFEATURE_GENUSE, GENERAL, USERDATA_WIKI.id));
        udf.setUdfString(generateUdfString(UDF_SDFEATURE_AGREEDDECISION, generateComment()));
        udf.setUdfUser(generateUdfUser(STDT_HANDLER, ARUTYANIN_YURIY));
        task.refreshUdf(udf);
        task.setHandlerUser(generateUser(ARUTYANIN_YURIY));
        apiController.updateToken(generateAuthToken(EMPLOYEE));
        slaFeatureController.saveTaskStatusFromResponse(task);
        slaFeatureController.performCommonOperation(task, BEGINCOST_PRE);
        ApiAsserts.assertThat(slaFeatureController.getResponse())
                .isCorrectResponseCode(HTTP_BAD_REQUEST)
                .isParseableBody(ErrorResponseBody.class)
                .isCorrectError(task, OPERATION_NOT_ALLOWED_FOR_TASK);
    }
}
