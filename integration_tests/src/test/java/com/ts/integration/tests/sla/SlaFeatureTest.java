package com.ts.integration.tests.sla;

import com.ts.common.asserts.ApiAsserts;
import com.ts.common.controllers.sla.SlaResponseBody;
import com.ts.common.controllers.sla.slaFeature.SlaFeatureController;
import com.ts.common.entitites.commonEntities.Udfs;
import com.ts.common.entitites.sla.SlaTask;
import com.ts.common.listeners.TestListener;
import com.ts.integration.tests.BaseIntegrationTest;
import jdk.jfr.Description;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import static com.ts.common.application.controllers.TrackStudioHttpStatusCodes.HTTP_OK;
import static com.ts.common.entitites.commonEntities.List.Constants.*;
import static com.ts.common.entitites.commonEntities.Udfs.UdfSd.*;
import static com.ts.common.entitites.commonEntities.User.Constants.*;
import static com.ts.common.entitites.commonEntities.udf.UdfString.Constants.COST;
import static com.ts.common.enums.ComSlaOperations.*;
import static com.ts.common.enums.SlaType.SLA_FEATURE;
import static com.ts.common.utils.InitEntities.*;
import static com.ts.common.utils.RandomUtils.generateComment;

@Listeners({TestListener.class})
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

    @Test(priority = 1, description = "Начать предварительную оценку")
    @Description("Начать предварительную оценку")
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

    @Test(priority = 2, dependsOnMethods = "msgSlaFeatureTopreCost", description = "Запросить уточненные требования")
    @Description("Запросить уточненные требования")
    public void msgSlaFeatureRequestReqInfo() { // запросить уточненные требования
        slaTask.refreshUdf();
        slaFeatureController.msgRequestReqInfo(slaTask);
        ApiAsserts.assertThat(slaFeatureController.getResponse())
                .isCorrectResponseCode(HTTP_OK)
                .isParseableBody(SlaResponseBody.class);
    }

    @Test(priority = 3, dependsOnMethods = "msgSlaFeatureRequestReqInfo", description = "Сообщить уточненные требования")
    @Description("Сообщить уточненные требования")
    public void msgSlaFeatureProvideReqInfo() { // сообщить уточненные требования
        slaTask.refreshUdf();
        slaFeatureController.msgProvideReqInfo(slaTask);
        ApiAsserts.assertThat(slaFeatureController.getResponse())
                .isCorrectResponseCode(HTTP_OK)
                .isParseableBody(SlaResponseBody.class);
    }


    @Test(priority = 4, dependsOnMethods = "msgSlaFeatureProvideReqInfo", description = "Передать на предварительную оценку аккаунт менеджеру")
    @Description("Передать на предварительную оценку аккаунт менеджеру")
    public void msgSlaFeatureBeginCostPre() {
        udf = refreshUdf();
        udf.setUdfDouble(generateUdfDouble(UDF_SDFEATURE_IMPLBUDGET, 4));
        udf.setSecondUdfDouble(generateUdfDouble(UDF_SDFEATURE_DETAILBUDGET, 4));
        udf.setUdfList(generateUdfList(UDF_SDFEATURE_TYPE, OWN));
        udf.setSecondUdfList(generateUdfList(UDF_SDFEATURE_GENUSE, GENERAL, USERDATA_WIKI.id));
        udf.setUdfString(generateUdfString(UDF_SDFEATURE_AGREEDDECISION, generateComment()));
        udf.setUdfUser(generateUdfUser(STDT_HANDLER, ARUTYANIN_YURIY));
        slaTask.refreshUdf(udf);
        slaTask.setHandlerUser(generateUser(ARUTYANIN_YURIY));
        slaFeatureController.msgBeginCostPre(slaTask);
        ApiAsserts.assertThat(slaFeatureController.getResponse())
                .isCorrectResponseCode(HTTP_OK)
                .isParseableBody(SlaResponseBody.class);
    }

    @Test(priority = 5, dependsOnMethods = "msgSlaFeatureBeginCostPre", description = "Сообщить предварительные условия реализации")
    @Description("Сообщить предварительные условия реализации")
    public void msgSlaFeatureSendCostPre() {
        udf = refreshUdf();
        udf.setUdfList(generateUdfList(UDF_SDFEATURE_PAYDCS, FREE_LAW));
        udf.setSecondUdfList(generateUdfList(UDF_SLA_CLIENTGENUSE, NOTCUSTOM));
        udf.setUdfString(generateUdfString(UDF_SDFEATURE_AGREEDDECISION, generateComment()));
        udf.setSecondUdfString(generateUdfString(UDF_SLA_AWAITCOST, COST.value));
        udf.setThirdUdfString(generateUdfString(UDF_SLA_IMPLPLANTD_PRE, "4"));
        udf.setUdfDate(generateUdfDate(UDF_SLA_FINALESTIMATIONDATE));
        slaTask.refreshUdf(udf);
        slaFeatureController.performCommonOperation(slaTask, SENDCOST_PRE);
        ApiAsserts.assertThat(slaFeatureController.getResponse())
                .isCorrectResponseCode(HTTP_OK)
                .isParseableBody(SlaResponseBody.class);
    }

    @Test(priority = 6, dependsOnMethods = "msgSlaFeatureSendCostPre", description = "Задать вопрос или предложить альтернативные вопросы реализации")
    @Description("Задать вопрос или предложить альтернативные вопросы реализации")
    public void msgSlaFeatureAlternateCost() {
        slaTask.refreshUdf();
        slaFeatureController.performCommonOperation(slaTask, ALTERNATECOST);
        ApiAsserts.assertThat(slaFeatureController.getResponse())
                .isCorrectResponseCode(HTTP_OK)
                .isParseableBody(SlaResponseBody.class);
    }

    @Test(priority = 7, dependsOnMethods = "msgSlaFeatureAlternateCost", description = "Сообщить повторно предварительные условия реализации")
    @Description("Сообщить повторно предварительные условия реализации")
    public void msgSlaFeatureSendCostPreRetry() {
        udf = refreshUdf();
        udf.setUdfList(generateUdfList(UDF_SDFEATURE_PAYDCS, FREE_LAW));
        udf.setSecondUdfList(generateUdfList(UDF_SLA_CLIENTGENUSE, NOTCUSTOM));
        udf.setUdfString(generateUdfString(UDF_SDFEATURE_AGREEDDECISION, generateComment()));
        udf.setSecondUdfString(generateUdfString(UDF_SLA_AWAITCOST, COST.value));
        udf.setThirdUdfString(generateUdfString(UDF_SLA_IMPLPLANTD_PRE, "4"));
        udf.setUdfDate(generateUdfDate(UDF_SLA_FINALESTIMATIONDATE));
        slaTask.refreshUdf(udf);
        slaFeatureController.performCommonOperation(slaTask, SENDCOST_PRE);
        ApiAsserts.assertThat(slaFeatureController.getResponse())
                .isCorrectResponseCode(HTTP_OK)
                .isParseableBody(SlaResponseBody.class);
    }

    @Test(priority = 8, dependsOnMethods = "msgSlaFeatureSendCostPreRetry", description = "Принять предварительные условия реализации")
    @Description("Принять предварительные условия реализации")
    public void msgSlaFeatureAcceptPreCost() {
        slaTask.refreshUdf();
        slaFeatureController.performCommonOperation(slaTask, ACCEPTPRECOST);
        ApiAsserts.assertThat(slaFeatureController.getResponse())
                .isCorrectResponseCode(HTTP_OK)
                .isParseableBody(SlaResponseBody.class);
    }

    @Test(priority = 9, dependsOnMethods = "msgSlaFeatureAcceptPreCost", description = "Запросить уточнение требований")
    @Description("Запросить уточнение требований")
    public void msgSlaFeatureRequestReqInfoRetry() {
        slaTask.refreshUdf();
        slaFeatureController.msgRequestReqInfo(slaTask);
        ApiAsserts.assertThat(slaFeatureController.getResponse())
                .isCorrectResponseCode(HTTP_OK)
                .isParseableBody(SlaResponseBody.class);
    }

    @Test(priority = 10, dependsOnMethods = "msgSlaFeatureRequestReqInfoRetry", description = "Сообщить уточненные требования")
    @Description("Сообщить уточненные требования")
    public void msgSlaFeatureProvideReqInfoRetry() {
        slaTask.refreshUdf();
        slaFeatureController.msgProvideReqInfo(slaTask);
        ApiAsserts.assertThat(slaFeatureController.getResponse())
                .isCorrectResponseCode(HTTP_OK)
                .isParseableBody(SlaResponseBody.class);
    }

    @Test(priority = 11, dependsOnMethods = "msgSlaFeatureProvideReqInfoRetry", description = "Сообщить окончательные условия реализации")
    @Description("Сообщить окончательные условия реализации")
    public void msgSlaFeatureSenCostFinal() {
        udf = refreshUdf();
        udf.setUdfDate(generateUdfDate(UDF_SDFEATUREPLANTD));
        udf.setUdfList(generateUdfList(UDF_SDFEATURE_PAYDCS, FREE_LAW));
        udf.setSecondUdfList(generateUdfList(UDF_SLA_CLIENTGENUSE, NOTCUSTOM));
        udf.setUdfString(generateUdfString(UDF_SLA_IMPLDEADLINE, "12"));
        udf.setSecondUdfString(generateUdfString(UDF_SLA_RESULTCOST, COST.value));
        slaTask.refreshUdf(udf);
        slaFeatureController.performCommonOperation(slaTask, SENDCOST_FINAL);
        ApiAsserts.assertThat(slaFeatureController.getResponse())
                .isCorrectResponseCode(HTTP_OK)
                .isParseableBody(SlaResponseBody.class);
    }

    @Test(priority = 12, dependsOnMethods = "msgSlaFeatureSenCostFinal", description = "Принять окончательные условия реализации")
    @Description("Принять окончательные условия реализации")
    public void msgSlaFeatureAcceptConditions() {
        slaTask.refreshUdf();
        slaFeatureController.performCommonOperation(slaTask, ACCEPTCONDITIONS);
        ApiAsserts.assertThat(slaFeatureController.getResponse())
                .isCorrectResponseCode(HTTP_OK)
                .isParseableBody(SlaResponseBody.class);
    }

    @Test(priority = 13, dependsOnMethods = "msgSlaFeatureAcceptConditions", description = "Передать в разработку")
    @Description("Передать в разработку")
    public void msgSlaFeatureStart() {
        udf = refreshUdf();
        udf.setUdfDate(generateUdfDate(UDF_SDFEATUREPLANTD));
        udf.setUdfUser(generateUdfUser(STDT_HANDLER, BABUSHKIN_IVAN));
        slaTask.refreshUdf(udf);
        slaFeatureController.msgStart(slaTask);
        ApiAsserts.assertThat(slaFeatureController.getResponse())
                .isCorrectResponseCode(HTTP_OK)
                .isParseableBody(SlaResponseBody.class);
    }

    @Test(priority = 14, dependsOnMethods = "msgSlaFeatureStart", description = "Запросить информацию")
    @Description("Запросить информацию")
    public void msgSlaFeatureRequestInfo() {
        slaTask.refreshUdf();
        slaFeatureController.performCommonOperation(slaTask, REQUESTINFO);
        ApiAsserts.assertThat(slaFeatureController.getResponse())
                .isCorrectResponseCode(HTTP_OK)
                .isParseableBody(SlaResponseBody.class);
    }

    @Test(priority = 15, dependsOnMethods = "msgSlaFeatureRequestInfo", description = "Предоставить информацию")
    @Description("Предоставить информацию")
    public void msgSlaFeatureProvideInfo() {
        slaTask.refreshUdf();
        slaFeatureController.performCommonOperation(slaTask, PROVIDEINFO);
        ApiAsserts.assertThat(slaFeatureController.getResponse())
                .isCorrectResponseCode(HTTP_OK)
                .isParseableBody(SlaResponseBody.class);
    }

    @Test(priority = 16, dependsOnMethods = "msgSlaFeatureProvideInfo", description = "Запросить информацию")
    @Description("Запросить информацию")
    public void msgSlaFeatureRequestInfoRetry() {
        slaTask.refreshUdf();
        slaFeatureController.performCommonOperation(slaTask, REQUESTINFO);
        ApiAsserts.assertThat(slaFeatureController.getResponse())
                .isCorrectResponseCode(HTTP_OK)
                .isParseableBody(SlaResponseBody.class);
    }

    @Test(priority = 17, dependsOnMethods = "msgSlaFeatureRequestInfoRetry", description = "Отменить запрос информации")
    @Description("Отменить запрос информации")
    public void msgSlaFeatureUndoRequestInfo() {
        slaTask.refreshUdf();
        slaFeatureController.performCommonOperation(slaTask, UNDOREQUESTINFO);
        ApiAsserts.assertThat(slaFeatureController.getResponse())
                .isCorrectResponseCode(HTTP_OK)
                .isParseableBody(SlaResponseBody.class);
    }

    @Test(priority = 18, dependsOnMethods = "msgSlaFeatureUndoRequestInfo", description = "Завершить выполнение работы")
    @Description("Завершить выполнение работы")
    public void msgSlaFeatureFinish() {
        slaTask.refreshUdf();
        slaFeatureController.performCommonOperation(slaTask, FINISH);
        ApiAsserts.assertThat(slaFeatureController.getResponse())
                .isCorrectResponseCode(HTTP_OK)
                .isParseableBody(SlaResponseBody.class);
    }

    @Test(priority = 19, dependsOnMethods = "msgSlaFeatureFinish", description = "Передать на проверку клиенту")
    @Description("Передать на проверку клиенту")
    public void msgSlaFeatureToClientTest() {
        slaTask.refreshUdf();
        slaFeatureController.performCommonOperation(slaTask, TOCLIENTTEST);
        ApiAsserts.assertThat(slaFeatureController.getResponse())
                .isCorrectResponseCode(HTTP_OK)
                .isParseableBody(SlaResponseBody.class);
    }

    @Test(priority = 20, dependsOnMethods = "msgSlaFeatureToClientTest", description = "Сообщить о замечании")
    @Description("Сообщить о замечании")
    public void msgSlaFeatureBuGonAccept() {
        slaTask.refreshUdf();
        slaFeatureController.performCommonOperation(slaTask, BUGONACCEPT);
        ApiAsserts.assertThat(slaFeatureController.getResponse())
                .isCorrectResponseCode(HTTP_OK)
                .isParseableBody(SlaResponseBody.class);
    }

    @Test(priority = 21, dependsOnMethods = "msgSlaFeatureBuGonAccept", description = "Передать на проверку клиента")
    @Description("Передать на проверку клиента")
    public void msgSlaFeatureToClientTestRetry() {
        slaTask.refreshUdf();
        slaFeatureController.performCommonOperation(slaTask, TOCLIENTTEST);
        ApiAsserts.assertThat(slaFeatureController.getResponse())
                .isCorrectResponseCode(HTTP_OK)
                .isParseableBody(SlaResponseBody.class);
    }

    @Test(priority = 22, dependsOnMethods = "msgSlaFeatureToClientTestRetry", description = "Утвердить доработку")
    @Description("Утвердить доработку")
    public void msgSlaFeatureAcceptFeature() {
        slaFeatureController.performCommonOperation(slaTask, ACCEPTFEATURE);
        ApiAsserts.assertThat(slaFeatureController.getResponse())
                .isCorrectResponseCode(HTTP_OK)
                .isParseableBody(SlaResponseBody.class);
    }

    @Test(priority = 23, dependsOnMethods = "msgSlaFeatureAcceptFeature", description = "Отправить патч")
    @Description("Отправить патч")
    public void msgSlaFeatureSend() {
        slaFeatureController.performCommonOperation(slaTask, SEND);
        ApiAsserts.assertThat(slaFeatureController.getResponse())
                .isCorrectResponseCode(HTTP_OK)
                .isParseableBody(SlaResponseBody.class);
    }

    @Test(priority = 24, dependsOnMethods = "msgSlaFeatureSend", description = "Сообщить о замечании")
    @Description("Сообщить о замечании")
    public void msgSlaFeatureBuGonAcceptRetry() {
        slaTask.refreshUdf();
        slaFeatureController.performCommonOperation(slaTask, BUGONACCEPT);
        ApiAsserts.assertThat(slaFeatureController.getResponse())
                .isCorrectResponseCode(HTTP_OK)
                .isParseableBody(SlaResponseBody.class);
    }

    @Test(priority = 25, dependsOnMethods = "msgSlaFeatureBuGonAcceptRetry", description = "Передать на включение в патч")
    @Description("Передать на включение в патч")
    public void msgSlaFeatureReadyPatch() {
        slaFeatureController.performCommonOperation(slaTask, READYTOPATCH);
        ApiAsserts.assertThat(slaFeatureController.getResponse())
                .isCorrectResponseCode(HTTP_OK)
                .isParseableBody(SlaResponseBody.class);
    }

    @Test(priority = 26, dependsOnMethods = "msgSlaFeatureReadyPatch", description = "Отправить в патч")
    @Description("Отправить в патч")
    public void msgSlaFeatureSendRetry() {
        slaTask.refreshUdf();
        slaFeatureController.performCommonOperation(slaTask, SEND);
        ApiAsserts.assertThat(slaFeatureController.getResponse())
                .isCorrectResponseCode(HTTP_OK)
                .isParseableBody(SlaResponseBody.class);
    }

    @Test(priority = 27, dependsOnMethods = "msgSlaFeatureSendRetry", description = "Установить в производственную среду")
    @Description("Установить в производственную среду")
    public void msgSlaFeatureInstall() {
        slaFeatureController.performCommonOperation(slaTask, INSTALL);
        ApiAsserts.assertThat(slaFeatureController.getResponse())
                .isCorrectResponseCode(HTTP_OK)
                .isParseableBody(SlaResponseBody.class);
    }

    @Test(priority = 28, dependsOnMethods = "msgSlaFeatureInstall", description = "Закрыть(поставщик)")
    @Description("Закрыть(поставщик)")
    public void msgSlaFeatureClose() {
        slaFeatureController.performCommonOperation(slaTask, CLOSE);
        ApiAsserts.assertThat(slaFeatureController.getResponse())
                .isCorrectResponseCode(HTTP_OK)
                .isParseableBody(SlaResponseBody.class);
    }
}
