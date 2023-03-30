package com.ts.integration.tests.proc_sla_feature;

import com.ts.common.asserts.ApiAsserts;
import com.ts.common.controllers.sla.SlaResponseBody;
import com.ts.common.controllers.sla.slaFeature.SlaFeatureController;
import com.ts.common.entitites.commonEntities.Udfs;
import com.ts.common.entitites.sla.SlaTask;
import com.ts.integration.tests.BaseIntegrationTest;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static com.ts.common.application.controllers.TrackStudioHttpStatusCodes.HTTP_OK;
import static com.ts.common.entitites.commonEntities.List.Constants.*;
import static com.ts.common.entitites.commonEntities.Udfs.UdfSd.*;
import static com.ts.common.entitites.commonEntities.User.Constants.ABDULLAEV_BAHODIR;
import static com.ts.common.entitites.commonEntities.udf.UdfString.Constants.COST;
import static com.ts.common.enums.ComSlaOperations.*;
import static com.ts.common.enums.SlaType.SLA_FEATURE;
import static com.ts.common.enums.Users.CLIENT;
import static com.ts.common.enums.Users.EMPLOYEE;
import static com.ts.common.utils.InitEntities.*;
import static com.ts.common.utils.RandomUtils.generateComment;

public class SlaFeatureThirdScenarioTest extends BaseIntegrationTest {
    private static SlaFeatureController slaFeatureController;
    private SlaTask slaTask;
    private Udfs udf;
    private SlaTask actualSlaTask;

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
        slaFeatureController.setAuthToken(generateAuthToken(CLIENT));
        slaFeatureController.createSlaFeatureTask(slaTask);
        ApiAsserts.assertThat(slaFeatureController.getResponse())
                .isCorrectResponseCode(HTTP_OK)
                .isParseableBody(SlaResponseBody.class);
        actualSlaTask = apiController.receiveSlaTask(slaTask.getNumber());
//        TaskAsserts.assertThat(actualSlaTask).isEquals(slaTask);
    }

    @AfterMethod(alwaysRun = true)
    public void afterMethod() {
        ApiAsserts.assertThat(slaFeatureController.getResponse())
                .isCorrectResponseCode(HTTP_OK)
                .isParseableBody(SlaResponseBody.class);
    }

    @Test(priority = 0, description = "Начать предварительную оценку")
    public void msgSlaFeatureTopreCost() {
        udf = refreshUdf();
        udf.setUdfUser(generateUdfUser(STDT_HANDLER, ABDULLAEV_BAHODIR));
        slaTask.setUdfs(udf);
        slaTask.setHandlerUser(generateUser(ABDULLAEV_BAHODIR));
        slaFeatureController.setAuthToken(generateAuthToken(EMPLOYEE));
        slaFeatureController.msgToprecost(slaTask);
//        TaskAsserts.assertThat(actualSlaTask.getFinishStatus())
//                .isCorrectStatus();
    }

    @Test(priority = 1, description = "изменить ответственную роль")
    public void msgSlaFeatureChangeCurrentRole() {
        udf = refreshUdf();
        udf.setUdfList(generateUdfList(UDF_ROLE_CURRENT, ANALYST));
        udf.setUdfUser(generateUdfUser(UDF_ROLE_WORKER, ABDULLAEV_BAHODIR));
        udf.setSecondUdfList(generateUdfList(UDF_ROLE_RESET, YES));
        slaTask.refreshUdf(udf);
        slaFeatureController.setAuthToken(generateAuthToken(EMPLOYEE));
        slaFeatureController.performCommonOperation(slaTask, CHANGE_CURRENT_ROLE);
        actualSlaTask = apiController.receiveSlaTask(slaTask.getNumber());
//        TaskAsserts.assertThat(slaTask).isEquals(actualSlaTask);
    }

    @Test(priority = 2, description = "Сообщить предварительные условия реализации")
    public void msgSlaFeatureSendCostPre() {
        udf = refreshUdf();
        udf.setUdfList(generateUdfList(UDF_SDFEATURE_PAYDCS, FREE_LAW));
        udf.setSecondUdfList(generateUdfList(UDF_SLA_CLIENTGENUSE, NOTCUSTOM));
        udf.setUdfString(generateUdfString(UDF_SDFEATURE_AGREEDDECISION, generateComment()));
        udf.setSecondUdfString(generateUdfString(UDF_SLA_AWAITCOST, COST.value));
        udf.setThirdUdfString(generateUdfString(UDF_SLA_IMPLPLANTD_PRE, "12"));
        udf.setUdfDate(generateUdfDate(UDF_SLA_FINALESTIMATIONDATE));
        slaTask.refreshUdf(udf);
        slaFeatureController.setAuthToken(generateAuthToken(EMPLOYEE)); // Сотрудник
        slaFeatureController.performCommonOperation(slaTask, SENDCOST_PRE);
    }

    @Test(priority = 3, description = "Принять предварительные условия реализации")
    public void msgSlaFeatureAcceptPreCost() {
        slaTask.refreshUdf();
        slaFeatureController.setAuthToken(generateAuthToken(CLIENT));
        slaFeatureController.performCommonOperation(slaTask, ACCEPTPRECOST);
    }

    @Test(priority = 4, description = "Сообщить окончательные условия реализации")
    public void msgSlaFeatureSendCostFinal() {
        udf = refreshUdf();
        udf.setUdfDate(generateUdfDate(UDF_SDFEATUREPLANTD));
        udf.setUdfList(generateUdfList(UDF_SDFEATURE_PAYDCS, FREE_LAW));
        udf.setSecondUdfList(generateUdfList(UDF_SLA_CLIENTGENUSE, NOTCUSTOM));
        udf.setUdfString(generateUdfString(UDF_SLA_IMPLDEADLINE, "12"));
        udf.setSecondUdfString(generateUdfString(UDF_SLA_RESULTCOST, COST.value));
        slaTask.refreshUdf(udf);
        slaFeatureController.setAuthToken(generateAuthToken(EMPLOYEE));
        slaFeatureController.performCommonOperation(slaTask, SENDCOST_FINAL);
    }

    @Test(priority = 5, description = "Принять окончательные условия реализации")
    public void msgSlaFeatureAcceptConditions() {
        slaTask.refreshUdf();
        slaFeatureController.setAuthToken(generateAuthToken(CLIENT));
        slaFeatureController.performCommonOperation(slaTask, ACCEPTCONDITIONS);
    }

    @Test(priority = 6, description = "Изменить решение и постановку на реализацию")
    public void msgSlaFeatureChangeDesicion() {
        slaTask.refreshUdf();
        slaFeatureController.performCommonOperation(slaTask, CHANGE_DECISION);
    }
}