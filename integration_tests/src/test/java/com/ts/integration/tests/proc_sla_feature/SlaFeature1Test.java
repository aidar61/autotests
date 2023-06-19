package com.ts.integration.tests.proc_sla_feature;

import com.ts.common.asserts.ApiAsserts;
import com.ts.common.controllers.TaskResponseBody;
import com.ts.common.controllers.sla.SlaFeatureController;
import com.ts.common.entitites.tasks.GeneralTask;
import com.ts.common.listeners.TestListener;
import com.ts.common.utils.InitEntities;
import com.ts.integration.tests.BaseIntegrationTest;
import jdk.jfr.Description;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import static com.ts.common.application.controllers.TrackStudioHttpStatusCodes.HTTP_OK;
import static com.ts.common.entitites.commonEntities.List.Constants.*;
import static com.ts.common.entitites.commonEntities.Task.Constants.AKKREDITIVES;
import static com.ts.common.entitites.commonEntities.Task.Constants.MTBANK;
import static com.ts.common.entitites.commonEntities.Udfs.UdfSd.*;
import static com.ts.common.entitites.commonEntities.User.Constants.*;
import static com.ts.common.entitites.commonEntities.udf.UdfString.Constants.COST;
import static com.ts.common.enums.Operations.*;
import static com.ts.common.enums.TaskType.SLA_FEATURE;
import static com.ts.common.enums.Users.*;
import static com.ts.common.utils.InitEntities.*;
import static com.ts.common.utils.RandomUtils.generateComment;

@Listeners({TestListener.class})
public class SlaFeature1Test extends BaseIntegrationTest {
    private static SlaFeatureController slaFeatureController;
    private GeneralTask task;

    //TODO нужно добавить в каждом тесте в контроллер пользователя, который выполняет операции (КЛИЕНТ, СОТРУДНИК)
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
        task = getGeneralTask(SLA_FEATURE, CAT);
        task.setUdfs(udf);
        task.setHandlerUser(generateUser(ALTUNIN_NIKOLAY));
        apiController.updateToken(generateAuthToken(CLIENT));
        slaFeatureController.createSlaFeatureTask(task);
        ApiAsserts.assertThat(slaFeatureController.getResponse())
                .isCorrectResponseCode(HTTP_OK)
                .isParseableBody(TaskResponseBody.class);
    }

    @AfterClass(alwaysRun = false)
    public void afterClass() {

//        udf = refreshUdf();
//        udf.setUdfList(generateUdfList(UDF_SDFEATURE_CANCELREASON, CLIENTIGNOREANL));
//        slaTask.refreshUdf(udf);
//        slaFeatureController.performCommonOperation(slaTask, REMOVE_REQUEST);
//        ApiAsserts.assertThat(slaFeatureController.getResponse())
//                .isCorrectResponseCode(HTTP_OK)
//                .isParseableBody(SlaResponseBody.class);
    }

    @Test(groups = {"SlaFeature", "Regression"},description = "get запрос", dependsOnMethods = "msgSlaFeatureCat")
    @Description("Test description: Receive task")
    public void receiveTask() {
        apiController.updateToken(generateAuthToken(ROOT));
        slaFeatureController.receiveActualTask(task.getNumber());
        ApiAsserts.assertThat(slaFeatureController.getResponse())
                .isCorrectResponseCode(HTTP_OK)
                .isParseableBody(TaskResponseBody.class);
    }

    //    @Test(groups = {"SlaFeature", "Regression"},priority = 1)
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

    @Test(groups = {"SlaFeature", "Regression"},description = "Начать предварительную оценку", dependsOnMethods = "receiveTask")
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

    @Test(groups = {"SlaFeature", "Regression"},dependsOnMethods = "msgSlaFeatureTopreCost", description = "Запросить уточненные требования")
    @Description("Запросить уточненные требования")
    public void msgSlaFeatureRequestReqInfo() { // запросить уточненные требования
        task.refreshUdf();
        apiController.updateToken(generateAuthToken(EMPLOYEE));
        slaFeatureController.msgRequestReqInfo(task);
        ApiAsserts.assertThat(slaFeatureController.getResponse())
                .isCorrectResponseCode(HTTP_OK)
                .isParseableBody(TaskResponseBody.class);
    }

    @Test(groups = {"SlaFeature", "Regression"},dependsOnMethods = "msgSlaFeatureRequestReqInfo", description = "Сообщить уточненные требования")
    @Description("Сообщить уточненные требования")
    public void msgSlaFeatureProvideReqInfo() { // сообщить уточненные требования
        task.refreshUdf();
        apiController.updateToken(generateAuthToken(CLIENT));
        slaFeatureController.msgProvideReqInfo(task);
        ApiAsserts.assertThat(slaFeatureController.getResponse())
                .isCorrectResponseCode(HTTP_OK)
                .isParseableBody(TaskResponseBody.class);
    }


    @Test(groups = {"SlaFeature", "Regression"},dependsOnMethods = "msgSlaFeatureProvideReqInfo", description = "Передать на предварительную оценку аккаунт менеджеру")
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
        slaFeatureController.msgBeginCostPre(task);
        ApiAsserts.assertThat(slaFeatureController.getResponse())
                .isCorrectResponseCode(HTTP_OK)
                .isParseableBody(TaskResponseBody.class);
    }

    @Test(groups = {"SlaFeature", "Regression"},dependsOnMethods = "msgSlaFeatureBeginCostPre", description = "Сообщить предварительные условия реализации")
    @Description("Сообщить предварительные условия реализации")
    public void msgSlaFeatureSendCostPre() {
        udf = refreshUdf();
        udf.setUdfList(generateUdfList(UDF_SDFEATURE_PAYDCS, FREE_LAW));
        udf.setSecondUdfList(generateUdfList(UDF_SLA_CLIENTGENUSE, NOTCUSTOM));
        udf.setUdfString(generateUdfString(UDF_SDFEATURE_AGREEDDECISION, generateComment()));
        udf.setSecondUdfString(generateUdfString(UDF_SLA_AWAITCOST, COST.value));
        udf.setThirdUdfString(generateUdfString(UDF_SLA_IMPLPLANTD_PRE, "4"));
        udf.setUdfDate(generateUdfDate(UDF_SLA_FINALESTIMATIONDATE));
        task.refreshUdf(udf);
        apiController.updateToken(generateAuthToken(EMPLOYEE));
        slaFeatureController.performCommonOperation(task, SENDCOST_PRE);
        ApiAsserts.assertThat(slaFeatureController.getResponse())
                .isCorrectResponseCode(HTTP_OK)
                .isParseableBody(TaskResponseBody.class);
    }

    @Test(groups = {"SlaFeature", "Regression"},dependsOnMethods = "msgSlaFeatureSendCostPre", description = "Задать вопрос или предложить альтернативные вопросы реализации")
    @Description("Задать вопрос или предложить альтернативные вопросы реализации")
    public void msgSlaFeatureAlternateCost() {
        task.refreshUdf();
        apiController.updateToken(generateAuthToken(CLIENT));
        slaFeatureController.performCommonOperation(task, ALTERNATECOST);
        ApiAsserts.assertThat(slaFeatureController.getResponse())
                .isCorrectResponseCode(HTTP_OK)
                .isParseableBody(TaskResponseBody.class);
    }

    @Test(groups = {"SlaFeature", "Regression"},dependsOnMethods = "msgSlaFeatureAlternateCost", description = "Сообщить повторно предварительные условия реализации")
    @Description("Сообщить повторно предварительные условия реализации")
    public void msgSlaFeatureSendCostPreRetry() {
        udf = refreshUdf();
        udf.setUdfList(generateUdfList(UDF_SDFEATURE_PAYDCS, FREE_LAW));
        udf.setSecondUdfList(generateUdfList(UDF_SLA_CLIENTGENUSE, NOTCUSTOM));
        udf.setUdfString(generateUdfString(UDF_SDFEATURE_AGREEDDECISION, generateComment()));
        udf.setSecondUdfString(generateUdfString(UDF_SLA_AWAITCOST, COST.value));
        udf.setThirdUdfString(generateUdfString(UDF_SLA_IMPLPLANTD_PRE, "4"));
        udf.setUdfDate(generateUdfDate(UDF_SLA_FINALESTIMATIONDATE));
        task.refreshUdf(udf);
        apiController.updateToken(generateAuthToken(EMPLOYEE));
        slaFeatureController.performCommonOperation(task, SENDCOST_PRE);
        ApiAsserts.assertThat(slaFeatureController.getResponse())
                .isCorrectResponseCode(HTTP_OK)
                .isParseableBody(TaskResponseBody.class);
    }

    @Test(groups = {"SlaFeature", "Regression"},dependsOnMethods = "msgSlaFeatureSendCostPreRetry", description = "Принять предварительные условия реализации")
    @Description("Принять предварительные условия реализации")
    public void msgSlaFeatureAcceptPreCost() {
        task.refreshUdf();
        apiController.updateToken(generateAuthToken(CLIENT));
        slaFeatureController.performCommonOperation(task, ACCEPTPRECOST);
        ApiAsserts.assertThat(slaFeatureController.getResponse())
                .isCorrectResponseCode(HTTP_OK)
                .isParseableBody(TaskResponseBody.class);
    }

    @Test(groups = {"SlaFeature", "Regression"},dependsOnMethods = "msgSlaFeatureAcceptPreCost", description = "Запросить уточнение требований")
    @Description("Запросить уточнение требований")
    public void msgSlaFeatureRequestReqInfoRetry() {
        task.refreshUdf();
        apiController.updateToken(generateAuthToken(EMPLOYEE));
        slaFeatureController.msgRequestReqInfo(task);
        ApiAsserts.assertThat(slaFeatureController.getResponse())
                .isCorrectResponseCode(HTTP_OK)
                .isParseableBody(TaskResponseBody.class);
    }

    @Test(groups = {"SlaFeature", "Regression"},dependsOnMethods = "msgSlaFeatureRequestReqInfoRetry", description = "Сообщить уточненные требования")
    @Description("Сообщить уточненные требования")
    public void msgSlaFeatureProvideReqInfoRetry() {
        task.refreshUdf();
        apiController.updateToken(generateAuthToken(CLIENT));
        slaFeatureController.msgProvideReqInfo(task);
        ApiAsserts.assertThat(slaFeatureController.getResponse())
                .isCorrectResponseCode(HTTP_OK)
                .isParseableBody(TaskResponseBody.class);
    }

    @Test(groups = {"SlaFeature", "Regression"},dependsOnMethods = "msgSlaFeatureProvideReqInfoRetry", description = "Сообщить окончательные условия реализации")
    @Description("Сообщить окончательные условия реализации")
    public void msgSlaFeatureSenCostFinal() {
        udf = refreshUdf();
        udf.setUdfDate(generateUdfDate(UDF_SDFEATUREPLANTD));
        udf.setUdfList(generateUdfList(UDF_SDFEATURE_PAYDCS, FREE_LAW));
        udf.setSecondUdfList(generateUdfList(UDF_SLA_CLIENTGENUSE, NOTCUSTOM));
        udf.setUdfString(generateUdfString(UDF_SLA_IMPLDEADLINE, "12"));
        udf.setSecondUdfString(generateUdfString(UDF_SLA_RESULTCOST, COST.value));
        task.refreshUdf(udf);
        apiController.updateToken(generateAuthToken(EMPLOYEE));
        slaFeatureController.performCommonOperation(task, SENDCOST_FINAL);
        ApiAsserts.assertThat(slaFeatureController.getResponse())
                .isCorrectResponseCode(HTTP_OK)
                .isParseableBody(TaskResponseBody.class);
    }

    @Test(groups = {"SlaFeature", "Regression"},dependsOnMethods = "msgSlaFeatureSenCostFinal", description = "Принять окончательные условия реализации")
    @Description("Принять окончательные условия реализации")
    public void msgSlaFeatureAcceptConditions() {
        task.refreshUdf();
        apiController.updateToken(generateAuthToken(CLIENT));
        slaFeatureController.performCommonOperation(task, ACCEPTCONDITIONS);
        ApiAsserts.assertThat(slaFeatureController.getResponse())
                .isCorrectResponseCode(HTTP_OK)
                .isParseableBody(TaskResponseBody.class);
    }

    @Test(groups = {"SlaFeature", "Regression"},dependsOnMethods = "msgSlaFeatureAcceptConditions", description = "Передать в разработку")
    @Description("Передать в разработку")
    public void msgSlaFeatureStart() {
        udf = refreshUdf();
        udf.setUdfDate(generateUdfDate(UDF_SDFEATUREPLANTD));
        udf.setUdfUser(generateUdfUser(STDT_HANDLER, BABUSHKIN_IVAN));
        task.refreshUdf(udf);
        apiController.updateToken(generateAuthToken(EMPLOYEE));
        slaFeatureController.msgStart(task);
        ApiAsserts.assertThat(slaFeatureController.getResponse())
                .isCorrectResponseCode(HTTP_OK)
                .isParseableBody(TaskResponseBody.class);
    }

    @Test(groups = {"SlaFeature", "Regression"},dependsOnMethods = "msgSlaFeatureStart", description = "Запросить информацию")
    @Description("Запросить информацию")
    public void msgSlaFeatureRequestInfo() {
        task.refreshUdf();
        apiController.updateToken(generateAuthToken(EMPLOYEE));
        slaFeatureController.performCommonOperation(task, REQUESTINFO);
        ApiAsserts.assertThat(slaFeatureController.getResponse())
                .isCorrectResponseCode(HTTP_OK)
                .isParseableBody(TaskResponseBody.class);
    }

    @Test(groups = {"SlaFeature", "Regression"},dependsOnMethods = "msgSlaFeatureRequestInfo", description = "Предоставить информацию")
    @Description("Предоставить информацию")
    public void msgSlaFeatureProvideInfo() {
        task.refreshUdf();
        apiController.updateToken(generateAuthToken(CLIENT));
        slaFeatureController.performCommonOperation(task, PROVIDEINFO);
        ApiAsserts.assertThat(slaFeatureController.getResponse())
                .isCorrectResponseCode(HTTP_OK)
                .isParseableBody(TaskResponseBody.class);
    }

    @Test(groups = {"SlaFeature", "Regression"},dependsOnMethods = "msgSlaFeatureProvideInfo", description = "Запросить информацию")
    @Description("Запросить информацию")
    public void msgSlaFeatureRequestInfoRetry() {
        task.refreshUdf();
        apiController.updateToken(generateAuthToken(EMPLOYEE));
        slaFeatureController.performCommonOperation(task, REQUESTINFO);
        ApiAsserts.assertThat(slaFeatureController.getResponse())
                .isCorrectResponseCode(HTTP_OK)
                .isParseableBody(TaskResponseBody.class);
    }

    @Test(groups = {"SlaFeature", "Regression"},dependsOnMethods = "msgSlaFeatureRequestInfoRetry", description = "Отменить запрос информации")
    @Description("Отменить запрос информации")
    public void msgSlaFeatureUndoRequestInfo() {
        task.refreshUdf();
        apiController.updateToken(generateAuthToken(EMPLOYEE));
        slaFeatureController.performCommonOperation(task, UNDOREQUESTINFO);
        ApiAsserts.assertThat(slaFeatureController.getResponse())
                .isCorrectResponseCode(HTTP_OK)
                .isParseableBody(TaskResponseBody.class);
    }

    @Test(groups = {"SlaFeature", "Regression"},dependsOnMethods = "msgSlaFeatureUndoRequestInfo", description = "Завершить выполнение работы")
    @Description("Завершить выполнение работы")
    public void msgSlaFeatureFinish() {
        task.refreshUdf();
        apiController.updateToken(generateAuthToken(EMPLOYEE));
        slaFeatureController.performCommonOperation(task, FINISH);
        ApiAsserts.assertThat(slaFeatureController.getResponse())
                .isCorrectResponseCode(HTTP_OK)
                .isParseableBody(TaskResponseBody.class);
    }

    @Test(groups = {"SlaFeature", "Regression"},dependsOnMethods = "msgSlaFeatureFinish", description = "Передать на проверку клиенту")
    @Description("Передать на проверку клиенту")
    public void msgSlaFeatureToClientTest() {
        task.refreshUdf();
        apiController.updateToken(generateAuthToken(EMPLOYEE));
        slaFeatureController.performCommonOperation(task, TOCLIENTTEST);
        ApiAsserts.assertThat(slaFeatureController.getResponse())
                .isCorrectResponseCode(HTTP_OK)
                .isParseableBody(TaskResponseBody.class);
    }

    @Test(groups = {"SlaFeature", "Regression"},dependsOnMethods = "msgSlaFeatureToClientTest", description = "Сообщить о замечании")
    @Description("Сообщить о замечании")
    public void msgSlaFeatureBuGonAccept() {
        task.refreshUdf();
        apiController.updateToken(generateAuthToken(CLIENT));
        slaFeatureController.performCommonOperation(task, BUGONACCEPT);
        ApiAsserts.assertThat(slaFeatureController.getResponse())
                .isCorrectResponseCode(HTTP_OK)
                .isParseableBody(TaskResponseBody.class);
    }

    @Test(groups = {"SlaFeature", "Regression"},dependsOnMethods = "msgSlaFeatureBuGonAccept", description = "Передать на проверку клиента")
    @Description("Передать на проверку клиента")
    public void msgSlaFeatureToClientTestRetry() {
        task.refreshUdf();
        apiController.updateToken(generateAuthToken(EMPLOYEE));
        slaFeatureController.performCommonOperation(task, TOCLIENTTEST);
        ApiAsserts.assertThat(slaFeatureController.getResponse())
                .isCorrectResponseCode(HTTP_OK)
                .isParseableBody(TaskResponseBody.class);
    }

    @Test(groups = {"SlaFeature", "Regression"},dependsOnMethods = "msgSlaFeatureToClientTestRetry", description = "Утвердить доработку")
    @Description("Утвердить доработку")
    public void msgSlaFeatureAcceptFeature() {
        apiController.updateToken(generateAuthToken(CLIENT));
        slaFeatureController.performCommonOperation(task, ACCEPTFEATURE);
        ApiAsserts.assertThat(slaFeatureController.getResponse())
                .isCorrectResponseCode(HTTP_OK)
                .isParseableBody(TaskResponseBody.class);
    }

    @Test(groups = {"SlaFeature", "Regression"},dependsOnMethods = "msgSlaFeatureAcceptFeature", description = "Отправить патч")
    @Description("Отправить патч")
    public void msgSlaFeatureSend() {
        apiController.updateToken(generateAuthToken(EMPLOYEE));
        slaFeatureController.performCommonOperation(task, SEND);
        ApiAsserts.assertThat(slaFeatureController.getResponse())
                .isCorrectResponseCode(HTTP_OK)
                .isParseableBody(TaskResponseBody.class);
    }

    @Test(groups = {"SlaFeature", "Regression"},dependsOnMethods = "msgSlaFeatureSend", description = "Сообщить о замечании")
    @Description("Сообщить о замечании")
    public void msgSlaFeatureBuGonAcceptRetry() {
        task.refreshUdf();
        apiController.updateToken(generateAuthToken(CLIENT));
        slaFeatureController.performCommonOperation(task, BUGONACCEPT);
        ApiAsserts.assertThat(slaFeatureController.getResponse())
                .isCorrectResponseCode(HTTP_OK)
                .isParseableBody(TaskResponseBody.class);
    }

    @Test(groups = {"SlaFeature", "Regression"},dependsOnMethods = "msgSlaFeatureBuGonAcceptRetry", description = "Передать на включение в патч")
    @Description("Передать на включение в патч")
    public void msgSlaFeatureReadyPatch() {
        apiController.updateToken(generateAuthToken(EMPLOYEE));
        slaFeatureController.performCommonOperation(task, READYTOPATCH);
        ApiAsserts.assertThat(slaFeatureController.getResponse())
                .isCorrectResponseCode(HTTP_OK)
                .isParseableBody(TaskResponseBody.class);
    }

    @Test(groups = {"SlaFeature", "Regression"},dependsOnMethods = "msgSlaFeatureReadyPatch", description = "Отправить в патч")
    @Description("Отправить в патч")
    public void msgSlaFeatureSendRetry() {
        task.refreshUdf();
        apiController.updateToken(generateAuthToken(EMPLOYEE));
        slaFeatureController.performCommonOperation(task, SEND);
        ApiAsserts.assertThat(slaFeatureController.getResponse())
                .isCorrectResponseCode(HTTP_OK)
                .isParseableBody(TaskResponseBody.class);
    }

    @Test(groups = {"SlaFeature", "Regression"},dependsOnMethods = "msgSlaFeatureSendRetry", description = "Установить в производственную среду")
    @Description("Установить в производственную среду")
    public void msgSlaFeatureInstall() {
        apiController.updateToken(generateAuthToken(CLIENT));
        slaFeatureController.performCommonOperation(task, INSTALL);
        ApiAsserts.assertThat(slaFeatureController.getResponse())
                .isCorrectResponseCode(HTTP_OK)
                .isParseableBody(TaskResponseBody.class);
    }

    @Test(groups = {"SlaFeature", "Regression"},dependsOnMethods = "msgSlaFeatureInstall", description = "Закрыть(поставщик)")
    @Description("Закрыть(поставщик)")
    public void msgSlaFeatureClose() {
        apiController.updateToken(generateAuthToken(EMPLOYEE));
        slaFeatureController.performCommonOperation(task, CLOSE);
        ApiAsserts.assertThat(slaFeatureController.getResponse())
                .isCorrectResponseCode(HTTP_OK)
                .isParseableBody(TaskResponseBody.class);
    }
}
