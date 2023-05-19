package com.ts.integration.tests.proc_potential_gap;

import com.ts.common.asserts.ApiAsserts;
import com.ts.common.controllers.TaskResponseBody;
import com.ts.common.controllers.gap.PotentialGapController;
import com.ts.common.entitites.tasks.GeneralTask;
import com.ts.common.enums.ComSlaOperations;
import com.ts.common.enums.SlaType;
import com.ts.common.utils.InitEntities;
import com.ts.integration.tests.BaseIntegrationTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static com.ts.common.application.controllers.TrackStudioHttpStatusCodes.HTTP_OK;
import static com.ts.common.entitites.commonEntities.Udfs.UdfSd.STDT_HANDLER;
import static com.ts.common.entitites.commonEntities.User.Constants.ARTEMEVA_MARINA;
import static com.ts.common.entitites.commonEntities.User.Constants.ARUTYANIN_YURIY;
import static com.ts.common.enums.ComSlaOperations.*;
import static com.ts.common.enums.Users.SECOND_EMPLOYEE;
import static com.ts.common.utils.InitEntities.*;

public class PotentialGap1Test extends BaseIntegrationTest {

    private PotentialGapController potentialGapController;
    private GeneralTask task;

    @BeforeClass(alwaysRun = true)
    public void beforeClass() {
        potentialGapController = apiController.getPotentialGapController();
    }

    @Test(groups = {"PotentialGap", "Regression"}, description = "Создание потенциального Gap")
    public void catPotentialGap() {
        apiController.updateToken(generateAuthToken(SECOND_EMPLOYEE));
        udf = refreshUdf();
        task = InitEntities.getSlaTask(SlaType.POTENTIAL_GAP, ComSlaOperations.CAT);
        task.setHandlerUser(generateUser(ARTEMEVA_MARINA));
        task.refreshUdf(udf);
        potentialGapController.createPotentialGap(task);
        ApiAsserts.assertThat(potentialGapController.getResponse())
                .isCorrectResponseCode(HTTP_OK)
                .isParseableBody(TaskResponseBody.class);
    }

    @Test(groups = {"PotentialGap", "Regression"}, description = "Передать на согласование", dependsOnMethods = "catPotentialGap")
    public void msgGapPassForApproval() {
        apiController.updateToken(generateAuthToken(SECOND_EMPLOYEE));
        udf = refreshUdf();
        udf.setUdfUser(generateUdfUser(STDT_HANDLER, ARUTYANIN_YURIY));
        task.setHandlerUser(generateUser(ARUTYANIN_YURIY));
        task.refreshUdf(udf);
        potentialGapController.performCommonOperation(task, PASS_FOR_APPROVAL);
        ApiAsserts.assertThat(potentialGapController.getResponse())
                .isCorrectResponseCode(HTTP_OK)
                .isParseableBody(TaskResponseBody.class);
    }

    @Test(groups = {"PotentialGap", "Regression"}, description = "Вернуть на анализ", dependsOnMethods = "msgGapPassForApproval")
    public void msgGapReturnToAnal() {
        apiController.updateToken(generateAuthToken(SECOND_EMPLOYEE));
        udf = refreshUdf();
        udf.setUdfUser(generateUdfUser(STDT_HANDLER, ARTEMEVA_MARINA));
        task.setHandlerUser(generateUser(ARTEMEVA_MARINA));
        task.refreshUdf(udf);
        potentialGapController.performCommonOperation(task, RETURN_TO_ANAL);
        ApiAsserts.assertThat(potentialGapController.getResponse())
                .isCorrectResponseCode(HTTP_OK)
                .isParseableBody(TaskResponseBody.class);
    }

    @Test(groups = {"PotentialGap", "Regression"}, description = "Передать на согласование", dependsOnMethods = "msgGapReturnToAnal")
    public void msgGapPassForApprovalRetry() {
        apiController.updateToken(generateAuthToken(SECOND_EMPLOYEE));
        udf = refreshUdf();
        udf.setUdfUser(generateUdfUser(STDT_HANDLER, ARUTYANIN_YURIY));
        task.setHandlerUser(generateUser(ARUTYANIN_YURIY));
        task.refreshUdf(udf);
        potentialGapController.performCommonOperation(task, PASS_FOR_APPROVAL);
        ApiAsserts.assertThat(potentialGapController.getResponse())
                .isCorrectResponseCode(HTTP_OK)
                .isParseableBody(TaskResponseBody.class);
    }

    @Test(groups = {"PotentialGap", "Regression"}, description = "Подтвердить и опубликовать", dependsOnMethods = "msgGapPassForApprovalRetry")
    public void msgGapConfirm() {
        apiController.updateToken(generateAuthToken(SECOND_EMPLOYEE));
        udf = refreshUdf();
        udf.setUdfUser(generateUdfUser(STDT_HANDLER, ARTEMEVA_MARINA));
        task.setHandlerUser(generateUser(ARTEMEVA_MARINA));
        task.refreshUdf(udf);
        potentialGapController.performCommonOperation(task, CONFIRM);
        ApiAsserts.assertThat(potentialGapController.getResponse())
                .isCorrectResponseCode(HTTP_OK)
                .isParseableBody(TaskResponseBody.class);
    }

    @Test(groups = {"PotentialGap", "Regression"}, description = "Вернуть на анализ", dependsOnMethods = "msgGapConfirm")
    public void msgGapReturnToAnalRetry() {
        apiController.updateToken(generateAuthToken(SECOND_EMPLOYEE));
        udf = refreshUdf();
        udf.setUdfUser(generateUdfUser(STDT_HANDLER, ARTEMEVA_MARINA));
        task.setHandlerUser(generateUser(ARTEMEVA_MARINA));
        task.refreshUdf(udf);
        potentialGapController.performCommonOperation(task, RETURN_TO_ANAL);
        ApiAsserts.assertThat(potentialGapController.getResponse())
                .isCorrectResponseCode(HTTP_OK)
                .isParseableBody(TaskResponseBody.class);
    }

    @Test(groups = {"PotentialGap", "Regression"}, description = "Передать на согласование", dependsOnMethods = "msgGapReturnToAnalRetry")
    public void msgGapPassForApprovalRetryMore() {
        apiController.updateToken(generateAuthToken(SECOND_EMPLOYEE));
        udf = refreshUdf();
        udf.setUdfUser(generateUdfUser(STDT_HANDLER, ARUTYANIN_YURIY));
        task.setHandlerUser(generateUser(ARUTYANIN_YURIY));
        task.refreshUdf(udf);
        potentialGapController.performCommonOperation(task, PASS_FOR_APPROVAL);
        ApiAsserts.assertThat(potentialGapController.getResponse())
                .isCorrectResponseCode(HTTP_OK)
                .isParseableBody(TaskResponseBody.class);
    }

    @Test(groups = {"PotentialGap", "Regression"}, description = "Подтвердить и опубликовать", dependsOnMethods = "msgGapPassForApprovalRetryMore")
    public void msgGapConfirmRetry() {
        apiController.updateToken(generateAuthToken(SECOND_EMPLOYEE));
        udf = refreshUdf();
        udf.setUdfUser(generateUdfUser(STDT_HANDLER, ARTEMEVA_MARINA));
        task.setHandlerUser(generateUser(ARTEMEVA_MARINA));
        task.refreshUdf(udf);
        potentialGapController.performCommonOperation(task, CONFIRM);
        ApiAsserts.assertThat(potentialGapController.getResponse())
                .isCorrectResponseCode(HTTP_OK)
                .isParseableBody(TaskResponseBody.class);
    }

    @Test(groups = {"PotentialGap", "Regression"}, description = "Завершить работу с GAP", dependsOnMethods = "msgGapConfirmRetry")
    public void msgGapFinish() {
        apiController.updateToken(generateAuthToken(SECOND_EMPLOYEE));
        task.refreshUdf();
        potentialGapController.performCommonOperation(task, FINISH);
        ApiAsserts.assertThat(potentialGapController.getResponse())
                .isCorrectResponseCode(HTTP_OK)
                .isParseableBody(TaskResponseBody.class);
    }

}

