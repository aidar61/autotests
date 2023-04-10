package com.ts.integration.tests.proc_sla_bug;

import com.ts.common.application.controllers.TrackStudioHttpStatusCodes;
import com.ts.common.asserts.ApiAsserts;
import com.ts.common.controllers.sla.SlaResponseBody;
import com.ts.common.controllers.sla.slaBug.SlaBugController;
import com.ts.common.enums.ComSlaOperations;
import com.ts.common.enums.SlaType;
import com.ts.common.utils.InitEntities;
import com.ts.integration.tests.BaseIntegrationTest;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static com.ts.common.entitites.commonEntities.List.Constants.*;
import static com.ts.common.entitites.commonEntities.Udfs.UdfSd.*;
import static com.ts.common.enums.Users.CLIENT;
import static com.ts.common.utils.InitEntities.*;

public class SlaBug6Test extends BaseIntegrationTest {
    private SlaBugController slaBugController;

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

    @Test(priority = 0)
    public void slaBugCat() {
        udf = refreshUdf();
        udf.setUdfSdModule(InitEntities.getUdfsModuleThrowsJson());
        udf.setUdfList(InitEntities.generateUdfList(UDF_SDBUG_PRIORITYBUG, CRITICAL));
        udf.setUdfsBdkuConfiguration(InitEntities.getBdkuThrowsJson());
        udf.setSecondUdfList(InitEntities.generateUdfList(UDF_SD_REMOTEACCESS, REMOTE_ACCESS));
        slaTask = InitEntities.getSlaTask(SlaType.SLA_BUG, ComSlaOperations.CAT);
        slaTask.refreshUdf(udf);
        apiController.updateToken(generateAuthToken(CLIENT));
        slaBugController.createSlaBugTask(slaTask);
    }

    @Test(priority = 1)
    public void slaBugMsgClose() {
        udf = refreshUdf();
        udf.setUdfList(generateUdfList(UDF_EVALUATING_REQUEST_EXECUTION, FIVE));
        udf.setSecondUdfList(generateUdfList(UDF_SD_CLOSEREASON, SOLVED));
        slaTask.refreshUdf(udf);
        slaBugController.performCommonOperation(slaTask, ComSlaOperations.CLOSE);
    }
}
