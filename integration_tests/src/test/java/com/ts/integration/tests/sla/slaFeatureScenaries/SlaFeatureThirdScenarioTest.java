package com.ts.integration.tests.sla.slaFeatureScenaries;

import com.ts.common.asserts.ApiAsserts;
import com.ts.common.controllers.sla.SlaResponseBody;
import com.ts.common.controllers.sla.slaFeature.SlaFeatureController;
import com.ts.common.entitites.commonEntities.Udfs;
import com.ts.common.entitites.sla.SlaTask;
import com.ts.integration.tests.BaseIntegrationTest;
import org.testng.annotations.BeforeClass;

import static com.ts.common.application.controllers.TrackStudioHttpStatusCodes.HTTP_OK;
import static com.ts.common.entitites.commonEntities.List.Constants.FREE_LAW;
import static com.ts.common.entitites.commonEntities.List.Constants.OWN;
import static com.ts.common.entitites.commonEntities.Udfs.UdfSd.UDF_SDFEATURE_PAYDCS;
import static com.ts.common.entitites.commonEntities.Udfs.UdfSd.UDF_SDFEATURE_TYPE;
import static com.ts.common.enums.ComSlaOperations.CAT;
import static com.ts.common.enums.SlaType.SLA_FEATURE;
import static com.ts.common.utils.InitEntities.*;

public class SlaFeatureThirdScenarioTest extends BaseIntegrationTest {
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

}
