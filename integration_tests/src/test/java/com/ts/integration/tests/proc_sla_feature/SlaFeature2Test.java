package com.ts.integration.tests.proc_sla_feature;

import com.ts.common.asserts.ApiAsserts;
import com.ts.common.controllers.TaskResponseBody;
import com.ts.common.controllers.sla.SlaFeatureController;
import com.ts.common.entitites.tasks.GeneralTask;
import com.ts.common.utils.InitEntities;
import com.ts.common.utils.RandomUtils;
import com.ts.integration.tests.BaseIntegrationTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static com.ts.common.application.controllers.TrackStudioHttpStatusCodes.HTTP_OK;
import static com.ts.common.entitites.commonEntities.List.Constants.*;
import static com.ts.common.entitites.commonEntities.Task.Constants.AKKREDITIVES;
import static com.ts.common.entitites.commonEntities.Task.Constants.MTBANK;
import static com.ts.common.entitites.commonEntities.Udfs.UdfSd.*;
import static com.ts.common.entitites.commonEntities.User.Constants.*;
import static com.ts.common.enums.ComSlaOperations.*;
import static com.ts.common.enums.TaskType.SLA_FEATURE;
import static com.ts.common.enums.Users.CLIENT;
import static com.ts.common.enums.Users.EMPLOYEE;
import static com.ts.common.utils.InitEntities.*;

public class SlaFeature2Test extends BaseIntegrationTest {
    private static SlaFeatureController slaFeatureController;
    private GeneralTask task;

    @BeforeClass(alwaysRun = true)
    public void beforeClass() {
        slaFeatureController = apiController.getSlaFeatureController();
    }

    @Test(groups = {"SlaFeature", "Regression"},description = "Создание задачи")
    public void msgSlaFeatureCat() {
        udf = refreshUdf();
        udf.setUdfTask(InitEntities.generateUdfTask(UDF_SD_MODULE, AKKREDITIVES));
        udf.setSecondUdfTask(generateUdfTask(UDF_BDKU_CONFIGURATION, MTBANK));
        udf.setUdfList(generateUdfList(UDF_SDFEATURE_TYPE, OWN));
        udf.setSecondUdfList(generateUdfList(UDF_SDFEATURE_PAYDCS, FREE_LAW));
        task = getSlaTask(SLA_FEATURE, CAT);
        task.setUdfs(udf);
        task.setHandlerUser(generateUser(ALTUNIN_NIKOLAY));
        apiController.updateToken(generateAuthToken(CLIENT));
        slaFeatureController.createSlaFeatureTask(task);
        ApiAsserts.assertThat(slaFeatureController.getResponse())
                .isCorrectResponseCode(HTTP_OK)
                .isParseableBody(TaskResponseBody.class);
    }

    @Test(groups = {"SlaFeature", "Regression"},description = "Начать предварительную оценку", dependsOnMethods = "msgSlaFeatureCat")
    public void msgSlaFeatureTopreCost() {
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

    @Test(groups = {"SlaFeature", "Regression"},description = "Передать на окончательную оценку аккаунт менеджеру", dependsOnMethods = "msgSlaFeatureTopreCost")
    public void msgSlaFeatureBeginCostFinal() {
        udf = refreshUdf();
        udf.setUdfDouble(generateUdfDouble(UDF_SDFEATURE_IMPLBUDGET, 4));
        udf.setUdfList(generateUdfList(UDF_SDFEATURE_DOCREVISION, NO));
        udf.setSecondUdfList(generateUdfList(UDF_SDFEATURE_TYPE, OWN));
        udf.setThirdUdfList(generateUdfList(UDF_SDFEATURE_GENUSE, GENERAL, USERDATA_ARUTYANIN.id));
        udf.setUdfUser(generateUdfUser(STDT_HANDLER, ARUTYANIN_YURIY));
        udf.setUdfString(generateUdfString(UDF_SDFEATURE_IMPLSTATEMENT, RandomUtils.generateComment()));
        udf.setSecondUdfString(generateUdfString(UDF_SDFEATURE_AGREEDDECISION, RandomUtils.generateComment()));
        task.refreshUdf(udf);
        task.setHandlerUser(generateUser(ARUTYANIN_YURIY));
        apiController.updateToken(generateAuthToken(EMPLOYEE));
        slaFeatureController.performCommonOperation(task, BEGINCOST_FINAL);
        ApiAsserts.assertThat(slaFeatureController.getResponse())
                .isCorrectResponseCode(HTTP_OK)
                .isParseableBody(TaskResponseBody.class);
    }

    @Test(groups = {"SlaFeature", "Regression"},description = "Снять запрос", dependsOnMethods = "msgSlaFeatureBeginCostFinal")
    public void msgSLaFeatureRemoveRequest() {
        udf = refreshUdf();
        udf.setUdfList(generateUdfList(UDF_SDFEATURE_CANCELREASON, CLIENTIGNORECOST));
        task.refreshUdf(udf);
        apiController.updateToken(generateAuthToken(EMPLOYEE));
        slaFeatureController.performCommonOperation(task, REMOVE_REQUEST);
        ApiAsserts.assertThat(slaFeatureController.getResponse())
                .isCorrectResponseCode(HTTP_OK)
                .isParseableBody(TaskResponseBody.class);
    }
}
