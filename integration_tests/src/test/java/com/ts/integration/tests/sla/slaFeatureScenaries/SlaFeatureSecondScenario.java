package com.ts.integration.tests.sla.slaFeatureScenaries;

import com.ts.common.asserts.ApiAsserts;
import com.ts.common.controllers.sla.SlaResponseBody;
import com.ts.common.controllers.sla.slaFeature.SlaFeatureController;
import com.ts.common.entitites.commonEntities.Udfs;
import com.ts.common.entitites.sla.SlaTask;
import com.ts.integration.tests.BaseIntegrationTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static com.ts.common.application.controllers.TrackStudioHttpStatusCodes.HTTP_OK;
import static com.ts.common.entitites.commonEntities.List.Constants.*;
import static com.ts.common.entitites.commonEntities.Udfs.UdfSd.*;
import static com.ts.common.entitites.commonEntities.User.Constants.ABDULLAEV_BAHODIR;
import static com.ts.common.entitites.commonEntities.User.Constants.ARUTYANIN_YURIY;
import static com.ts.common.enums.ComSlaOperations.*;
import static com.ts.common.enums.SlaType.SLA_FEATURE;
import static com.ts.common.utils.InitEntities.*;

public class SlaFeatureSecondScenario extends BaseIntegrationTest {
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

    @Test(priority = 0, description = "Начать предварительную оценку")
    public void msgSlaFeatureTopreCost() {
        udf = refreshUdf();
        udf.setUdfUser(generateUdfUser(STDT_HANDLER, ABDULLAEV_BAHODIR));
        slaTask.setUdfs(udf);
        slaTask.setHandlerUser(generateUser(ABDULLAEV_BAHODIR));
        slaFeatureController.msgToprecost(slaTask);
        ApiAsserts.assertThat(slaFeatureController.getResponse())
                .isCorrectResponseCode(HTTP_OK)
                .isParseableBody(SlaResponseBody.class);
    }

    @Test(priority = 1, description = "Передать на окончательную оценку аккаунт менеджеру")
    public void msgSlaFeatureBeginCostFinal() {
        udf = refreshUdf();
        udf.setUdfDouble(generateUdfDouble(UDF_SDFEATURE_IMPLBUDGET, 4));
        udf.setUdfList(generateUdfList(UDF_SDFEATURE_DOCREVISION, NO));
        udf.setSecondUdfList(generateUdfList(UDF_SDFEATURE_TYPE, OWN));
        udf.setThirdUdfList(generateUdfList(UDF_SDFEATURE_GENUSE, GENERAL, USERDATA_WIKI.id));
        udf.setUdfUser(generateUdfUser(STDT_HANDLER, ARUTYANIN_YURIY));
        slaTask.refreshUdf(udf);
        slaFeatureController.performCommonOperation(slaTask, BEGINCOST_FINAL);
        ApiAsserts.assertThat(slaFeatureController.getResponse())
                .isCorrectResponseCode(HTTP_OK)
                .isParseableBody(SlaResponseBody.class);
    }

    @Test(priority = 2, description = "Снять запрос")
    public void msgSLaFeatureRemoveRequest() {
        slaTask.refreshUdf();
        slaFeatureController.performCommonOperation(slaTask, REMOVE_REQUEST);
        ApiAsserts.assertThat(slaFeatureController.getResponse())
                .isCorrectResponseCode(HTTP_OK)
                .isParseableBody(SlaResponseBody.class);
    }
}
