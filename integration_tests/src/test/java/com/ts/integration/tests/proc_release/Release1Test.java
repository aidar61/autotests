package com.ts.integration.tests.proc_release;

import com.ts.common.application.controllers.TrackStudioHttpStatusCodes;
import com.ts.common.asserts.ApiAsserts;
import com.ts.common.asserts.TaskAsserts;
import com.ts.common.controllers.TaskResponseBody;
import com.ts.common.controllers.release.ReleaseModuleController;
import com.ts.common.entitites.commonEntities.Task;
import com.ts.common.entitites.tasks.GeneralTask;
import com.ts.common.enums.Users;
import com.ts.common.utils.InitEntities;
import com.ts.common.utils.RandomDataUtils;
import com.ts.common.utils.RandomUtils;
import com.ts.integration.tests.BaseIntegrationTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static com.ts.common.entitites.commonEntities.Task.Constants.APNG;
import static com.ts.common.entitites.commonEntities.Udfs.UdfSd.*;
import static com.ts.common.enums.Operations.CAT;
import static com.ts.common.enums.Parents.OPERATION_ACC_CLIENT;
import static com.ts.common.enums.Parents.RELEASE;
import static com.ts.common.enums.TaskType.RELEASE_MODULE;
import static com.ts.common.utils.InitEntities.*;

public class Release1Test extends BaseIntegrationTest {
    private ReleaseModuleController releaseModuleController;
    private GeneralTask task;
    private static String prefix;
    private static String shortName;

    @BeforeClass(alwaysRun = true)
    public void beforeClass() {
        prefix = RandomUtils.generatePrefix();
        shortName = RandomUtils.generateCodeShortName();
        releaseModuleController = apiController.getReleaseModuleController();
    }

    @Test(groups = {"PROC_RELEASE", "Regression"}, description = "Создать CAT_RELEASEMODULE")
    public void catReleaseModule() {
        apiController.updateToken(generateAuthToken(Users.ROOT));
        task = InitEntities.getGeneralTask(RELEASE_MODULE, CAT);
        task.setParent(InitEntities.getParent(RELEASE));
        task.setPriority(InitEntities.generatePriority(2));
        task.setShortName(shortName);
        Task udfReleaseModuleTask = RandomDataUtils.receiveRandomTaskForUdf(UDF_RELEASE_MODULE_DEPENDENCIES, task.getParent().getNumber(), OPERATION_ACC_CLIENT.tuskNumber); // receive task
        udf = refreshUdf();
        udf.setUdfTask(generateUdfTask(UDF_PRODUCT, APNG));
        udf.setUdfString(generateUdfString(UDF_RELEASE_PREFIX, prefix));
        udf.setUdfLink(generateUdfLink(UDF_RELEASE_REPOURL));
        udf.setSecondUdfTask(generateUdfTask(UDF_RELEASE_MODULE_DEPENDENCIES, udfReleaseModuleTask));
        udf.setSecondUdfString(generateUdfString(UDF_RELEASE_INITREV, RandomUtils.generateString()));
        udf.setThirdUdfString(generateUdfString(UDF_RELEASE_FUNCT, RandomUtils.generateString()));
        task.refreshUdf(udf);
        releaseModuleController.createCatReleaseModule(task);// Сreate Task
        ApiAsserts.assertThat(releaseModuleController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class);
    }

    @Test(groups = {"PROC_RELEASE", "Regression"}, description = "Получить форму", dependsOnMethods = "catReleaseModule")
    public void receiveForm() {
        apiController.updateToken(generateAuthToken(Users.ROOT));
        releaseModuleController.receiveForm(task);
        TaskAsserts.assertThat(task)
                .isCorrectName(shortName, prefix)
                .isCorrectShortName(prefix);
    }
}
