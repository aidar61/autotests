package com.ts.integration.tests.proc_sdhelp;

import com.ts.common.application.controllers.TrackStudioHttpStatusCodes;
import com.ts.common.asserts.ApiAsserts;
import com.ts.common.controllers.TaskResponseBody;
import com.ts.common.controllers.sdhelp.SdHelpController;
import com.ts.common.entitites.commonEntities.Task;
import com.ts.common.entitites.commonEntities.Udfs;
import com.ts.common.entitites.commonEntities.User;
import com.ts.common.entitites.tasks.GeneralTask;
import com.ts.common.enums.Operations;
import com.ts.common.enums.TaskType;
import com.ts.common.utils.InitEntities;
import com.ts.integration.tests.BaseIntegrationTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static com.ts.common.enums.Operations.CAT;
import static com.ts.common.enums.Operations.REMOVE_REQUEST;
import static com.ts.common.enums.TaskType.SD_HELP;
import static com.ts.common.utils.InitEntities.*;

public class SdHelp1Test extends BaseIntegrationTest {
    SdHelpController sdHelpController;
    GeneralTask sdHelpTask;
    Udfs udf;

    @BeforeClass(alwaysRun = true)
    public void beforeClass() {
        sdHelpController = apiController.getSdHelpController();
    }

    @Test(groups = "SdHelp", description = "Создание sd help")
    public void catSdHelp() {
        udf = refreshUdf();
        sdHelpTask = InitEntities.getGeneralTask(SD_HELP, CAT);
        udf.setUdfUser(generateUdfUser(Udfs.UdfSd.UDF_WATCHER, generateUser(User.Constants.ABDULLAEV_BAHODIR)));
        udf.setUdfTask(generateUdfTask(Udfs.UdfSd.UDF_SD_MODULE, Task.Constants.AKKREDITIVES));
        udf.setSecondUdfTask(generateUdfTask(Udfs.UdfSd.UDF_BDKU_CONFIGURATION, Task.Constants.RYSGAL_BANK));
        sdHelpTask.setParent(generateParent("818181df80d4285f0180e1ee472503bb", "1328786"));
        sdHelpTask.refreshUdf(udf);
        sdHelpController.createSdHelp(sdHelpTask);
        ApiAsserts.assertThat(sdHelpController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class);
    }

    @Test(dependsOnMethods = "catSdHelp")
    public void removeRequest() {
        sdHelpTask.refreshUdf();
        sdHelpController.performCommonOperation(sdHelpTask, REMOVE_REQUEST);
        ApiAsserts.assertThat(sdHelpController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class);
    }
}
