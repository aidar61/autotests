//package com.ts.integration.tests.proc_sla_feature;
//
//import com.ts.common.asserts.ApiAsserts;
//import com.ts.common.controllers.sla.SlaResponseBody;
//import com.ts.common.controllers.sla.slaFeature.SlaFeatureController;
//import com.ts.common.entitites.tasks.Task;
//import com.ts.common.utils.InitEntities;
//import com.ts.integration.tests.BaseIntegrationTest;
//import org.testng.annotations.AfterMethod;
//import org.testng.annotations.BeforeClass;
//import org.testng.annotations.Test;
//
//import static com.ts.common.application.controllers.TrackStudioHttpStatusCodes.HTTP_OK;
//import static com.ts.common.entitites.commonEntities.List.Constants.*;
//import static com.ts.common.entitites.commonEntities.Task.Constants.AKKREDITIVES;
//import static com.ts.common.entitites.commonEntities.Task.Constants.MTBANK;
//import static com.ts.common.entitites.commonEntities.Udfs.UdfSd.*;
//import static com.ts.common.entitites.commonEntities.User.Constants.ABDULLAEV_BAHODIR;
//import static com.ts.common.entitites.commonEntities.User.Constants.ALTUNIN_NIKOLAY;
//import static com.ts.common.entitites.commonEntities.udf.UdfString.Constants.COST;
//import static com.ts.common.enums.ComSlaOperations.*;
//import static com.ts.common.enums.SlaType.SLA_FEATURE;
//import static com.ts.common.enums.Users.CLIENT;
//import static com.ts.common.enums.Users.EMPLOYEE;
//import static com.ts.common.utils.InitEntities.*;
//import static com.ts.common.utils.RandomUtils.generateComment;
//
//public class SlaFeature3Test extends BaseIntegrationTest {
//    private static SlaFeatureController slaFeatureController;
//    private Task task;
//    private Task actualSlaTask;
//
//    @BeforeClass(alwaysRun = true)
//    public void beforeClass() {
//        slaFeatureController = apiController.getSlaFeatureController();
//
////        TaskAsserts.assertThat(actualSlaTask).isEquals(slaTask);
//    }
//
//    @AfterMethod(alwaysRun = true)
//    public void afterMethod() {
//        ApiAsserts.assertThat(slaFeatureController.getResponse())
//                .isCorrectResponseCode(HTTP_OK)
//                .isParseableBody(SlaResponseBody.class);
//        actualSlaTask = apiController.receiveSlaTask(task.getNumber());
//    }
//
//    @Test(groups = {"SlaFeature", "Regression"},description = "Создание задачи")
//    public void msgSlaFeatureCat() {
//        udf = refreshUdf();
//        udf.setUdfTask(InitEntities.generateUdfTask(UDF_SD_MODULE, AKKREDITIVES));
//        udf.setSecondUdfTask(generateUdfTask(UDF_BDKU_CONFIGURATION, MTBANK));
//        udf.setUdfList(generateUdfList(UDF_SDFEATURE_TYPE, OWN));
//        udf.setSecondUdfList(generateUdfList(UDF_SDFEATURE_PAYDCS, FREE_LAW));
//        task = getSlaTask(SLA_FEATURE, CAT);
//        task.setUdfs(udf);
//        task.setHandlerUser(generateUser(ALTUNIN_NIKOLAY));
//        apiController.updateToken(generateAuthToken(CLIENT));
//        slaFeatureController.createSlaFeatureTask(task);
//    }
//
//    @Test(groups = {"SlaFeature", "Regression"},description = "Начать предварительную оценку", dependsOnMethods = "msgSlaFeatureCat")
//    public void msgSlaFeatureTopreCost() {
//        udf = refreshUdf();
//        udf.setUdfUser(generateUdfUser(STDT_HANDLER, ABDULLAEV_BAHODIR));
//        task.setUdfs(udf);
//        task.setHandlerUser(generateUser(ABDULLAEV_BAHODIR));
//        apiController.updateToken(generateAuthToken(EMPLOYEE));
//        slaFeatureController.msgToprecost(task);
////        TaskAsserts.assertThat(actualSlaTask.getFinishStatus())
////                .isCorrectStatus();
//    }
//
//    @Test(groups = {"SlaFeature", "Regression"},description = "изменить ответственную роль", dependsOnMethods = "msgSlaFeatureTopreCost")
//    public void msgSlaFeatureChangeCurrentRole() {
//        udf = refreshUdf();
//        udf.setUdfList(generateUdfList(UDF_ROLE_CURRENT, ANALYST));
//        udf.setUdfUser(generateUdfUser(UDF_ROLE_WORKER, ABDULLAEV_BAHODIR));
//        udf.setSecondUdfList(generateUdfList(UDF_ROLE_RESET, YES));
//        task.refreshUdf(udf);
//        apiController.updateToken(generateAuthToken(EMPLOYEE));
//        slaFeatureController.performCommonOperation(task, CHANGE_CURRENT_ROLE);
//        actualSlaTask = apiController.receiveSlaTask(task.getNumber());
////        TaskAsserts.assertThat(slaTask).isEquals(actualSlaTask);
//    }
//
//    @Test(groups = {"SlaFeature", "Regression"},description = "Сообщить предварительные условия реализации", dependsOnMethods = "msgSlaFeatureChangeCurrentRole")
//    public void msgSlaFeatureSendCostPre() {
//        udf = refreshUdf();
//        udf.setUdfList(generateUdfList(UDF_SDFEATURE_PAYDCS, FREE_LAW));
//        udf.setSecondUdfList(generateUdfList(UDF_SLA_CLIENTGENUSE, NOTCUSTOM));
//        udf.setUdfString(generateUdfString(UDF_SDFEATURE_AGREEDDECISION, generateComment()));
//        udf.setSecondUdfString(generateUdfString(UDF_SLA_AWAITCOST, COST.value));
//        udf.setThirdUdfString(generateUdfString(UDF_SLA_IMPLPLANTD_PRE, "12"));
//        udf.setUdfDate(generateUdfDate(UDF_SLA_FINALESTIMATIONDATE));
//        task.refreshUdf(udf);
//        apiController.updateToken(generateAuthToken(EMPLOYEE));
//        slaFeatureController.performCommonOperation(task, SENDCOST_PRE);
//    }
//
//    @Test(groups = {"SlaFeature", "Regression"},description = "Принять предварительные условия реализации", dependsOnMethods = "msgSlaFeatureSendCostPre")
//    public void msgSlaFeatureAcceptPreCost() {
//        task.refreshUdf();
//        apiController.updateToken(generateAuthToken(CLIENT));
//        slaFeatureController.performCommonOperation(task, ACCEPTPRECOST);
//    }
//
//    @Test(groups = {"SlaFeature", "Regression"},description = "Сообщить окончательные условия реализации", dependsOnMethods = "msgSlaFeatureAcceptPreCost")
//    public void msgSlaFeatureSendCostFinal() {
//        udf = refreshUdf();
//        udf.setUdfDate(generateUdfDate(UDF_SDFEATUREPLANTD));
//        udf.setUdfList(generateUdfList(UDF_SDFEATURE_PAYDCS, FREE_LAW));
//        udf.setSecondUdfList(generateUdfList(UDF_SLA_CLIENTGENUSE, NOTCUSTOM));
//        udf.setUdfString(generateUdfString(UDF_SLA_IMPLDEADLINE, "12"));
//        udf.setSecondUdfString(generateUdfString(UDF_SLA_RESULTCOST, COST.value));
//        task.refreshUdf(udf);
//        apiController.updateToken(generateAuthToken(EMPLOYEE));
//        slaFeatureController.performCommonOperation(task, SENDCOST_FINAL);
//    }
//
//    @Test(groups = {"SlaFeature", "Regression"},description = "Принять окончательные условия реализации", dependsOnMethods = "msgSlaFeatureSendCostFinal")
//    public void msgSlaFeatureAcceptConditions() {
//        task.refreshUdf();
//        apiController.updateToken(generateAuthToken(CLIENT));
//        slaFeatureController.performCommonOperation(task, ACCEPTCONDITIONS);
//    }
//
//    @Test(groups = {"SlaFeature", "Regression"},description = "Изменить решение и постановку на реализацию", dependsOnMethods = "msgSlaFeatureAcceptConditions")
//    public void msgSlaFeatureChangeDesicion() {
//        udf = refreshUdf();
//        udf.setUdfString(generateUdfString(UDF_SDFEATURE_IMPLSTATEMENT, ""));
//        task.refreshUdf();
//        apiController.updateToken(generateAuthToken(EMPLOYEE));
//        slaFeatureController.performCommonOperation(task, CHANGE_DECISION);
//    }
//}