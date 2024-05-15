package com.ts.integration.tests.proc_advice.cat_code_review;

import com.ts.common.application.database.dbEntities.GrTaskDbEntity;
import com.ts.common.application.database.dbTables.GrTaskTable;
import com.ts.common.asserts.ApiAsserts;
import com.ts.common.asserts.CommonAssert;
import com.ts.common.asserts.TaskAsserts;
import com.ts.common.controllers.TaskResponseBody;
import com.ts.common.controllers.advice.CodeReviewController;
import com.ts.common.entitites.commonEntities.Parent;
import com.ts.common.entitites.commonEntities.Task;
import com.ts.common.entitites.commonEntities.User;
import com.ts.common.entitites.tasks.GeneralTask;
import com.ts.common.enums.Operations;
import com.ts.common.enums.TaskType;
import com.ts.common.utils.InitEntities;
import com.ts.integration.tests.BaseIntegrationTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import static com.ts.common.application.controllers.TrackStudioHttpStatusCodes.HTTP_OK;
import static com.ts.common.entitites.commonEntities.Task.Constants.AKKREDITIVES;
import static com.ts.common.entitites.commonEntities.Udfs.UdfSd.*;
import static com.ts.common.entitites.commonEntities.User.Constants.*;
import static com.ts.common.enums.Operations.*;
import static com.ts.common.enums.TaskStatuses.*;
import static com.ts.common.utils.InitEntities.*;
import static com.ts.common.utils.RandomUtils.generateString;

public class CatCodeReviewTest extends BaseIntegrationTest {
    public CodeReviewController codeReviewController;
    private GeneralTask task;
    private Parent parent;
    private GrTaskTable grTaskTable;
    private GrTaskDbEntity parentTaskFromDb;
    private Task parenOfParentTaskFromDb;
    private User HANDLER_USER_FROM_PARENT;

    private String cdpBl;

    @BeforeClass(alwaysRun = true)
    public void beforeClass() {
        codeReviewController = apiController.getCodeReviewController();
        grTaskTable = dbHelper.getGrTaskTable();
        parentTaskFromDb = (GrTaskDbEntity) grTaskTable.receiveByCategoryAndTaskStatus("CAT_BUGTASK", STATUS_WORKTASK_INWORK);
        parent = InitEntities.generateParent(parentTaskFromDb.getTask_id(), parentTaskFromDb.getTask_number());
        parenOfParentTaskFromDb = parentTaskFromDb.receiveParentTask();
        HANDLER_USER_FROM_PARENT = apiController.receiveGeneralTask(parent.getNumber()).getHandlerUser();
        task = InitEntities.getGeneralTask(TaskType.CODEREVIEW, Operations.CAT);

        var tasks = codeReviewController.getTaskForSDRequest(parent.getNumber());
        cdpBl = codeReviewController.getCdpBl();
    }

    @Test(groups = {"PROC_ADVICE", "Regression"}, description = "Создание запроса на подтверждение решения")
    public void catCodeReview() {
        apiController.updateToken(InitEntities.generateAuthToken(HANDLER_USER_FROM_PARENT));
        task.setParent(parent);
        task.setHandlerUser(HANDLER_USER_FROM_PARENT);
        task.setDescription(task.getDescription() + generateString());
        udf = refreshUdf();
        udf.setUdfUser(generateUdfUser(UDF_WATCHER, ABDULLAEV_BAHODIR));
        udf.setUdfList(generateUdfList(UDF_CDP_BL, cdpBl));
        udf.setUdfTask(generateUdfTask(UDF_SD_MODULE, AKKREDITIVES));
        udf.setUdfString(generateUdfString(UDF_SD_NOMODULE_REASON, generateString()));
        udf.setThirdUdfTask(generateUdfTask(UDF_WORKTASK_WORK, parentTaskFromDb.mapTo()));
        task.refreshUdf(udf);
        codeReviewController.create(task);
        ApiAsserts.assertThat(codeReviewController.getResponse())
                .isCorrectResponseCode(HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isCorrectStatus(STATUS_ADVICE_AWAIT)
                .isEquals(task);
    }

    @Test(groups = {"PROC_ADVICE", "Regression"}, description = "Комментарий", dependsOnMethods = "catCodeReview")
    public void comment() {
        apiController.updateToken(generateAuthToken(HANDLER_USER_FROM_PARENT));
        udf = refreshUdf();
        task.refreshUdf();
        codeReviewController.changeTaskType(TaskType.ADVICE);
        codeReviewController.performCommonOperation(task, Operations.COMMENT);
        ApiAsserts.assertThat(codeReviewController.getResponse())
                .isCorrectResponseCode(HTTP_OK)
                .isParseableBody(TaskResponseBody.class);
        GeneralTask generalTask = apiController.receiveGeneralTask(task.getNumber());
        TaskAsserts.assertThat(generalTask)
                .isNotEmpty(generalTask.getDescription());
    }

    @Test(groups = {"PROC_ADVICE", "Regression"}, description = "Назначить наблюдателя", dependsOnMethods = "comment")
    public void setWatcher() {
        apiController.updateToken(generateAuthToken(HANDLER_USER_FROM_PARENT));
        udf = refreshUdf();
        udf.setUdfUser(generateUdfUser(UDF_WATCHER, new User.Constants[]{AKSENOV_ANDREY, ABDULLAEV_BAHODIR}));
        task.refreshUdf(udf);
        codeReviewController.performCommonOperation(task, WATCH);
        ApiAsserts.assertThat(codeReviewController.getResponse())
                .isCorrectResponseCode(HTTP_OK)
                .isParseableBody(TaskResponseBody.class);

        var taskDetail = apiController.receiveTask(task.getNumber());
        CommonAssert
                .assertThat(taskDetail)
                .isCorrectUdfUSer(UDF_WATCHER, AKSENOV_ANDREY.login);

    }

    @Test(groups = {"PROC_ADVICE", "Regression"}, description = "Назначить контролера", dependsOnMethods = "setWatcher")
    public void setSupervisor() {
        apiController.updateToken(generateAuthToken(HANDLER_USER_FROM_PARENT));
        udf = refreshUdf();
        udf.setUdfUser(generateUdfUser(UDF_WORKTASK_SUPERVISER, ALTUNIN_NIKOLAY));
        task.setHandlerUser(null);
        task.refreshUdf(udf);
        codeReviewController.performCommonOperation(task, SUPERVISE);
        ApiAsserts.assertThat(codeReviewController.getResponse())
                .isCorrectResponseCode(HTTP_OK)
                .isParseableBody(TaskResponseBody.class);
//
//        var taskDetail = apiController.receiveTask(task.getNumber());
//        CommonAssert
//                .assertThat(taskDetail)
//                .isCorrectUdfUSer(UDF_WORKTASK_SUPERVISER, ALTUNIN_NIKOLAY.login);
    }

    @Test(groups = {"PROC_ADVICE", "Regression"}, description = "Отклонить изменения", dependsOnMethods = "setSupervisor")
    public void declineChanges() {
        apiController.updateToken(generateAuthToken(HANDLER_USER_FROM_PARENT));
        udf = refreshUdf();
        task.refreshUdf(udf);
        codeReviewController.performCommonOperation(task, DECLINE_CHANGES);
        ApiAsserts.assertThat(codeReviewController.getResponse())
                .isCorrectResponseCode(HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isCorrectStatus(STATUS_ADVICE_CHANGEDECLINED);
    }

    @Test(groups = {"PROC_ADVICE", "Regression"}, description = "Вернуть на обзор", dependsOnMethods = "declineChanges")
    public void returnToReview() {
        apiController.updateToken(generateAuthToken(HANDLER_USER_FROM_PARENT));
        udf = refreshUdf();
        task.refreshUdf(udf);
        codeReviewController.performCommonOperation(task, RETURN_TO_REVIEW);
        ApiAsserts.assertThat(codeReviewController.getResponse())
                .isCorrectResponseCode(HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isCorrectStatus(STATUS_ADVICE_AWAIT);
    }

    @Test(groups = {"PROC_ADVICE", "Regression"}, description = "Подтвердить изменения", dependsOnMethods = "returnToReview")
    public void confirmChanges() {
        apiController.updateToken(generateAuthToken(HANDLER_USER_FROM_PARENT));
        udf = refreshUdf();
        task.refreshUdf(udf);
        codeReviewController.performCommonOperation(task, CONFIRM_CHANGES);
        ApiAsserts.assertThat(codeReviewController.getResponse())
                .isCorrectResponseCode(HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isCorrectStatus(STATUS_ADVICE_CLOSED);
    }

    @Test(groups = {"PROC_ADVICE", "Regression"}, description = "Вернуть на обзор", dependsOnMethods = "confirmChanges")
    public void returnToReview2() {
        apiController.updateToken(generateAuthToken(HANDLER_USER_FROM_PARENT));
        udf = refreshUdf();
        task.refreshUdf(udf);
        codeReviewController.performCommonOperation(task, RETURN_TO_REVIEW);
        ApiAsserts.assertThat(codeReviewController.getResponse())
                .isCorrectResponseCode(HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isCorrectStatus(STATUS_ADVICE_AWAIT);
    }

    @Test(groups = {"PROC_ADVICE", "Regression"}, description = "Снять задачу", dependsOnMethods = "returnToReview2")
    public void cancel() {
        apiController.updateToken(generateAuthToken(HANDLER_USER_FROM_PARENT));
        udf = refreshUdf();
        task.refreshUdf(udf);
        codeReviewController.performCommonOperation(task, CANCEL);
        ApiAsserts.assertThat(codeReviewController.getResponse())
                .isCorrectResponseCode(HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isCorrectStatus(STATUS_ADVICE_CLOSED);
    }
}