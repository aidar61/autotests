package com.ts.integration.tests.sla;

import com.ts.common.asserts.ApiAsserts;
import com.ts.common.controllers.sla.SlaResponseBody;
import com.ts.common.controllers.sla.slaFeature.SlaFeatureController;
import com.ts.common.entitites.commonEntities.Udfs;
import com.ts.common.entitites.sla.SlaTask;
import com.ts.common.listeners.LogCatchListener;
import com.ts.integration.tests.BaseIntegrationTest;
import jdk.jfr.Description;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import static com.ts.common.application.controllers.TrackStudioHttpStatusCodes.HTTP_OK;
import static com.ts.common.entitites.commonEntities.List.Constants.*;
import static com.ts.common.entitites.commonEntities.Udfs.UdfSd.*;
import static com.ts.common.entitites.commonEntities.User.Constants.ABDULLAEV_BAHODIR;
import static com.ts.common.entitites.commonEntities.User.Constants.ARUTYANIN_YURIY;
import static com.ts.common.entitites.commonEntities.udf.UdfString.Constants.COST;
import static com.ts.common.enums.ComSlaOperations.*;
import static com.ts.common.enums.SlaType.SLA_FEATURE;
import static com.ts.common.utils.InitEntities.*;
import static com.ts.common.utils.RandomUtils.generateComment;

@Listeners({LogCatchListener.class})
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

    @AfterClass(alwaysRun = true)
    public void afterClass() {
        udf = refreshUdf();
        udf.setUdfList(generateUdfList(UDF_SDFEATURE_CANCELREASON, CLIENTIGNOREANL));
        slaTask.refreshUdf(udf);
        slaFeatureController.performCommonOperation(slaTask, REMOVE_REQUEST);
        ApiAsserts.assertThat(slaFeatureController.getResponse())
                .isCorrectResponseCode(HTTP_OK)
                .isParseableBody(SlaResponseBody.class);
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

    @Test(priority = 1)
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

    @Test(priority = 2, dependsOnMethods = "msgSlaFeatureTopreCost")
    @Description("Запросить уточненные требования")
    public void msgSlaFeatureRequestReqInfo() { // запросить уточненные требования
        slaTask.refreshUdf();
        slaFeatureController.msgRequestReqInfo(slaTask);
        ApiAsserts.assertThat(slaFeatureController.getResponse())
                .isCorrectResponseCode(HTTP_OK)
                .isParseableBody(SlaResponseBody.class);
    }

    @Test(priority = 3, dependsOnMethods = "msgSlaFeatureRequestReqInfo")
    @Description("Сообщить уточненные требования")
    public void msgSlaFeatureProvideReqInfo() { // сообщить уточненные требования
        slaTask.refreshUdf();
        slaFeatureController.msgProvideReqInfo(slaTask);
        ApiAsserts.assertThat(slaFeatureController.getResponse())
                .isCorrectResponseCode(HTTP_OK)
                .isParseableBody(SlaResponseBody.class);
    }


    @Test(priority = 4, dependsOnMethods = "msgSlaFeatureTopreCost")
    @Description("Передать на предварительную оценку аккаунт менеджеру")
    public void msgSlaFeatureBeginCostPre() {
        udf = refreshUdf();
        udf.setUdfDouble(generateUdfDouble(UDF_SDFEATURE_IMPLBUDGET, 4));
        udf.setSecondUdfDouble(generateUdfDouble(UDF_SDFEATURE_DETAILBUDGET, 4));
        udf.setUdfList(generateUdfList(UDF_SDFEATURE_TYPE, OWN));
        udf.setSecondUdfList(generateUdfList(UDF_SDFEATURE_GENUSE, GENERAL));
        udf.setUdfString(generateUdfString(UDF_SDFEATURE_AGREEDDECISION, generateComment()));
        udf.setUdfUser(generateUdfUser(STDT_HANDLER, ARUTYANIN_YURIY));
        slaTask.refreshUdf(udf);
        slaFeatureController.msgBeginCostPre(slaTask);
        ApiAsserts.assertThat(slaFeatureController.getResponse())
                .isCorrectResponseCode(HTTP_OK)
                .isParseableBody(SlaResponseBody.class);
    }

    @Test(priority = 5, dependsOnMethods = "msgSlaFeatureBeginCostPre")
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

    @Test(priority = 6, dependsOnMethods = "msgSlaFeatureSendCostPre")
    @Description("Задать вопрос или предложить альтернативные вопросы реализации")
    public void msgSlaFeatureAlternateCost() {
        slaTask.refreshUdf();
        slaFeatureController.performCommonOperation(slaTask, ALTERNATECOST);
        ApiAsserts.assertThat(slaFeatureController.getResponse())
                .isCorrectResponseCode(HTTP_OK)
                .isParseableBody(SlaResponseBody.class);
    }

    @Test(priority = 7, dependsOnMethods = "msgSlaFeatureAlternateCost")
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

    @Test(priority = 8, dependsOnMethods = "msgSlaFeatureSendCostPreRetry")
    @Description("Принять предварительные условия реализации")
    public void msgSlaFeatureAcceptPreCost() {
        slaTask.refreshUdf();
        slaFeatureController.performCommonOperation(slaTask, ACCEPTPRECOST);
        ApiAsserts.assertThat(slaFeatureController.getResponse())
                .isCorrectResponseCode(HTTP_OK)
                .isParseableBody(SlaResponseBody.class);
    }

    @Test(priority = 9, dependsOnMethods = "msgSlaFeatureAcceptPreCost")
    @Description("Запросить уточнение требований")
    public void msgSlaFeatureRequestReqInfoRetry() {
        slaTask.refreshUdf();
        slaFeatureController.msgRequestReqInfo(slaTask);
        ApiAsserts.assertThat(slaFeatureController.getResponse())
                .isCorrectResponseCode(HTTP_OK)
                .isParseableBody(SlaResponseBody.class);
    }

    @Test(priority = 10, dependsOnMethods = "msgSlaFeatureRequestReqInfoRetry")
    @Description("Сообщить уточненные требования")
    public void msgSlaFeatureProvideReqInfoRetry() {
        slaTask.refreshUdf();
        slaFeatureController.msgProvideReqInfo(slaTask);
        ApiAsserts.assertThat(slaFeatureController.getResponse())
                .isCorrectResponseCode(HTTP_OK)
                .isParseableBody(SlaResponseBody.class);
    }

    @Test(priority = 11, dependsOnMethods = "msgSlaFeatureProvideReqInfoRetry")
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

}
