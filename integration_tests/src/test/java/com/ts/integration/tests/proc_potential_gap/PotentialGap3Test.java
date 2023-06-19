package com.ts.integration.tests.proc_potential_gap;

import com.ts.common.asserts.ApiAsserts;
import com.ts.common.controllers.TaskResponseBody;
import com.ts.common.controllers.gap.PotentialGapController;
import com.ts.common.entitites.tasks.GeneralTask;
import com.ts.common.enums.Operations;
import com.ts.common.enums.TaskType;
import com.ts.common.utils.InitEntities;
import com.ts.integration.tests.BaseIntegrationTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static com.ts.common.application.controllers.TrackStudioHttpStatusCodes.HTTP_OK;
import static com.ts.common.entitites.commonEntities.Task.Constants.TASK_TS_DEV;
import static com.ts.common.entitites.commonEntities.Udfs.UdfSd.*;
import static com.ts.common.entitites.commonEntities.User.Constants.*;
import static com.ts.common.enums.Operations.*;
import static com.ts.common.enums.Users.SECOND_EMPLOYEE;
import static com.ts.common.utils.InitEntities.*;

public class PotentialGap3Test extends BaseIntegrationTest {
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
        task = InitEntities.getGeneralTask(TaskType.POTENTIAL_GAP, Operations.CAT);
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

    @Test(groups = {"PotentialGap", "Regression"}, description = "Приватный комментарий", dependsOnMethods = "msgGapPassForApproval")
    public void msgGapPrivateComment() {
        apiController.updateToken(generateAuthToken(SECOND_EMPLOYEE));
        task.refreshUdf();
        potentialGapController.performCommonOperation(task, PRIVATE_COMMENT);
        ApiAsserts.assertThat(potentialGapController.getResponse())
                .isCorrectResponseCode(HTTP_OK)
                .isParseableBody(TaskResponseBody.class);
    }

    @Test(groups = {"PotentialGap", "Regression"}, description = "Изменить аналитика", dependsOnMethods = "msgGapPrivateComment")
    public void msgGapChangeAnalyst() {
        apiController.updateToken(generateAuthToken(SECOND_EMPLOYEE));
        task.refreshUdf();
        potentialGapController.performCommonOperation(task, CHANGE_ANALYST);
        ApiAsserts.assertThat(potentialGapController.getResponse())
                .isCorrectResponseCode(HTTP_OK)
                .isParseableBody(TaskResponseBody.class);
    }

    @Test(groups = {"PotentialGap", "Regression"}, description = "Изменить аналитика", dependsOnMethods = "msgGapChangeAnalyst")
    public void msgGapChangeLinkedTasks() {
        apiController.updateToken(generateAuthToken(SECOND_EMPLOYEE));
        udf = refreshUdf();
        udf.setUdfTask(generateUdfTask(UDF_SD_LINKEDREQUEST, TASK_TS_DEV));
        task.refreshUdf(udf);
        potentialGapController.performCommonOperation(task, CHANGE_LINKED_TASKS);
        ApiAsserts.assertThat(potentialGapController.getResponse())
                .isCorrectResponseCode(HTTP_OK)
                .isParseableBody(TaskResponseBody.class);
    }

    @Test(groups = {"PotentialGap", "Regression"}, description = "Назначить наблюдателя", dependsOnMethods = "msgGapChangeLinkedTasks")
    public void msgGapWatch() {
        apiController.updateToken(generateAuthToken(SECOND_EMPLOYEE));
        udf = refreshUdf();
        udf.setUdfUser(generateUdfUser(UDF_WATCHER, ABDULLAEV_BAHODIR));
        task.refreshUdf(udf);
        potentialGapController.performCommonOperation(task, WATCH);
        ApiAsserts.assertThat(potentialGapController.getResponse())
                .isCorrectResponseCode(HTTP_OK)
                .isParseableBody(TaskResponseBody.class);
    }

    @Test(groups = {"PotentialGap", "Regression"}, description = "Привязать вопрос клиенту",dependsOnMethods = "msgGapWatch")
    public void msgGapQuestionLink() {
        apiController.updateToken(generateAuthToken(SECOND_EMPLOYEE));
        udf = refreshUdf();
        udf.setUdfTask(generateUdfTask(UDF_SDQUESTION_LINK, TASK_TS_DEV));
        task.refreshUdf(udf);
        potentialGapController.performCommonOperation(task, SD_QUESTION_LINK);
        ApiAsserts.assertThat(potentialGapController.getResponse())
                .isCorrectResponseCode(HTTP_OK)
                .isParseableBody(TaskResponseBody.class);
    }

    @Test(groups = {"PotentialGap", "Regression"}, description = "Установить связь с GAP",dependsOnMethods = "msgGapQuestionLink")
    public void msgGapLink() {
        apiController.updateToken(generateAuthToken(SECOND_EMPLOYEE));
        udf = refreshUdf();
        udf.setUdfTask(generateUdfTask(UDF_REQ_LINKED, TASK_TS_DEV));
        task.refreshUdf(udf);
        potentialGapController.performCommonOperation(task, LINK);
        ApiAsserts.assertThat(potentialGapController.getResponse())
                .isCorrectResponseCode(HTTP_OK)
                .isParseableBody(TaskResponseBody.class);
    }

}
