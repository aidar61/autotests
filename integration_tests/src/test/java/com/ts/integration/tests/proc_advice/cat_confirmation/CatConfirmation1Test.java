package com.ts.integration.tests.proc_advice.cat_confirmation;

import com.ts.common.application.controllers.TrackStudioHttpStatusCodes;
import com.ts.common.application.database.dbEntities.GrTaskDbEntity;
import com.ts.common.application.database.dbTables.GrTaskTable;
import com.ts.common.asserts.ApiAsserts;
import com.ts.common.asserts.TaskAsserts;
import com.ts.common.controllers.TaskResponseBody;
import com.ts.common.controllers.advice.ConfirmationController;
import com.ts.common.entitites.commonEntities.Parent;
import com.ts.common.entitites.commonEntities.Task;
import com.ts.common.entitites.commonEntities.User;
import com.ts.common.entitites.tasks.GeneralTask;
import com.ts.common.enums.Operations;
import com.ts.common.enums.Resolutions;
import com.ts.common.enums.TaskType;
import com.ts.common.utils.InitEntities;
import com.ts.integration.tests.BaseIntegrationTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static com.ts.common.entitites.commonEntities.List.Constants.ANALITIK_PLATFORM;
import static com.ts.common.entitites.commonEntities.Task.Constants.AKKREDITIVES;
import static com.ts.common.entitites.commonEntities.Udfs.UdfSd.*;
import static com.ts.common.entitites.commonEntities.User.Constants.ABDULLAEV_BAHODIR;
import static com.ts.common.entitites.commonEntities.User.Constants.QA;
import static com.ts.common.enums.Operations.*;
import static com.ts.common.enums.TaskStatuses.*;
import static com.ts.common.utils.InitEntities.*;
import static com.ts.common.utils.InitEntities.generateUdfTask;
import static com.ts.common.utils.RandomUtils.generateString;

public class CatConfirmation1Test extends BaseIntegrationTest {
    public ConfirmationController confirmationController;
    private GeneralTask task;
    private Parent parent;
    private GrTaskTable grTaskTable;
    private GrTaskDbEntity parentTaskFromDb;
    private Task parenOfParentTaskFromDb;
    private User HANDLER_USER_FROM_PARENT;

    @BeforeClass(alwaysRun = true)
    public void beforeClass() {
        confirmationController = apiController.getConfirmationController();
        grTaskTable = dbHelper.getGrTaskTable();
        parentTaskFromDb = (GrTaskDbEntity) grTaskTable.receiveByCategoryAndTaskStatus("CAT_BUGTASK", STATUS_WORKTASK_INWORK);
        parent = InitEntities.generateParent(parentTaskFromDb.getTask_id(), parentTaskFromDb.getTask_number());
        parenOfParentTaskFromDb = parentTaskFromDb.receiveParentTask();
        HANDLER_USER_FROM_PARENT = apiController.receiveGeneralTask(parent.getNumber()).getHandlerUser();
        task = InitEntities.getGeneralTask(TaskType.CONFIRMATION, Operations.CAT);
    }

    @Test(groups = {"Advice", "Regression"}, description = "Создание запроса на подтверждение решения")
    public void catConfirmation() {
        apiController.updateToken(InitEntities.generateAuthToken(HANDLER_USER_FROM_PARENT));
        task.setParent(parent);
        task.setHandlerUser(HANDLER_USER_FROM_PARENT);
        task.setDescription(task.getDescription() + generateString());
        udf = refreshUdf();
        udf.setUdfUser(generateUdfUser(UDF_WATCHER, ABDULLAEV_BAHODIR));
        udf.setUdfList(generateUdfList(UDF_CDP_BL, ANALITIK_PLATFORM));
        udf.setUdfTask(generateUdfTask(UDF_SD_MODULE, AKKREDITIVES));
        udf.setUdfString(generateUdfString(UDF_SD_NOMODULE_REASON, generateString()));
        udf.setUdfDate(generateUdfDate(UDF_ADVICE_PLANTD, 0));
        udf.setSecondUdfTask(generateUdfTask(UDF_WORKTASK_SDREQUEST, parenOfParentTaskFromDb));
        udf.setThirdUdfTask(generateUdfTask(UDF_WORKTASK_WORK, parentTaskFromDb.mapTo()));
        task.refreshUdf(udf);
        confirmationController.createConfirmation(task);
        ApiAsserts.assertThat(confirmationController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isCorrectStatus(STATUS_ADVICE_AWAIT)
                .isEquals(task);
    }

    @Test(groups = {"Advice", "Regression"}, description = "Комментарий", dependsOnMethods = "catConfirmation")
    public void comment() {
        apiController.updateToken(generateAuthToken(HANDLER_USER_FROM_PARENT));
        udf = refreshUdf();
        task.refreshUdf();
        confirmationController.performCommonOperation(task, Operations.COMMENT);
        ApiAsserts.assertThat(confirmationController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class);
        GeneralTask generalTask = apiController.receiveGeneralTask(task.getNumber());
        TaskAsserts.assertThat(generalTask)
                .isNotEmpty(generalTask.getDescription());
    }

    @Test(groups = {"Advice", "Regression"}, description = "Изменить ответственного конструктора", dependsOnMethods = "comment")
    public void assignConstructor() {
        User expectedUser = generateUser(QA);
        apiController.updateToken(generateAuthToken(HANDLER_USER_FROM_PARENT));
        udf = refreshUdf();
        udf.setUdfDate(generateUdfDate(UDF_ADVICE_PLANTD, 0));
        udf.setUdfUser(generateUdfUser(STDT_HANDLER, expectedUser));
        task.refreshUdf(udf);
        task.setHandlerUser(expectedUser);
        confirmationController.performCommonOperation(task, ASSIGN_CONSTRUCTOR);
        ApiAsserts.assertThat(confirmationController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isCorrectHandlerUser(generateUser(expectedUser))
                .isCorrectStatus(STATUS_ADVICE_AWAIT);
    }

    @Test(groups = {"Advice", "Regression"}, description = "Изменить ответственного конструктора", dependsOnMethods = "assignConstructor")
    public void assignConstructorRetry() {
        apiController.updateToken(generateAuthToken(HANDLER_USER_FROM_PARENT));
        udf = refreshUdf();
        udf.setUdfDate(generateUdfDate(UDF_ADVICE_PLANTD,0));
        udf.setUdfUser(generateUdfUser(STDT_HANDLER, HANDLER_USER_FROM_PARENT));
        task.refreshUdf(udf);
        task.setHandlerUser(HANDLER_USER_FROM_PARENT);
        confirmationController.performCommonOperation(task, ASSIGN_CONSTRUCTOR);
        ApiAsserts.assertThat(confirmationController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isCorrectHandlerUser(HANDLER_USER_FROM_PARENT)
                .isCorrectStatus(STATUS_ADVICE_AWAIT);
    }

    @Test(groups = {"Advice", "Regression"}, description = "Изменить решение", dependsOnMethods = "assignConstructorRetry")
    public void changeDecision() {
        apiController.updateToken(generateAuthToken(HANDLER_USER_FROM_PARENT));
        udf = refreshUdf();
        udf.setUdfDate(generateUdfDate(UDF_ADVICE_PLANTD, 0));
        task.setHandlerUser(null);
        task.refreshUdf(udf);
        confirmationController.performCommonOperation(task, CHANGE_DECISION);
        ApiAsserts.assertThat(confirmationController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isCorrectStatus(STATUS_ADVICE_CHANGED);
    }

    @Test(groups = {"Advice", "Regression"}, description = "Уточнить решение", dependsOnMethods = "changeDecision")
    public void clearDecision() {
        apiController.updateToken(generateAuthToken(HANDLER_USER_FROM_PARENT));
        udf = refreshUdf();
        udf.setUdfDate(generateUdfDate(UDF_ADVICE_PLANTD,0));
        task.refreshUdf(udf);
        confirmationController.performCommonOperation(task, CLEAR_DECISION);
        ApiAsserts.assertThat(confirmationController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isCorrectStatus(STATUS_ADVICE_AWAIT);
    }

    @Test(groups = {"Advice", "Regression"}, description = "Подтвердить", dependsOnMethods = "clearDecision")
    public void confirm() {
        apiController.updateToken(generateAuthToken(HANDLER_USER_FROM_PARENT));
        udf = refreshUdf();
        task.refreshUdf();
        task.setHandlerUser(null);
        confirmationController.performCommonOperation(task, CONFIRM);
        ApiAsserts.assertThat(confirmationController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isCorrectStatus(STATUS_ADVICE_CONFIRM);
    }

    @Test(groups = {"Advice", "Regression"}, description = "Отклонить запрос", dependsOnMethods = "confirm")
    public void reject() {
        apiController.updateToken(generateAuthToken(HANDLER_USER_FROM_PARENT));
        udf = refreshUdf();
        task.refreshUdf();
        task.setResolution(generateResolution(Resolutions.CANNOT_ANSWER));
        confirmationController.performCommonOperation(task, REJECT);
        ApiAsserts.assertThat(confirmationController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isCorrectStatus(STATUS_ADVICE_DECLINED);
    }

    @Test(groups = {"Advice", "Regression"}, description = "Повторить запрос", dependsOnMethods = "reject")
    public void repeatRequest() {
        apiController.updateToken(generateAuthToken(HANDLER_USER_FROM_PARENT));
        udf = refreshUdf();
        udf.setUdfDate(generateUdfDate(UDF_ADVICE_PLANTD,0));
        udf.setUdfUser(generateUdfUser(STDT_HANDLER, HANDLER_USER_FROM_PARENT));
        task.setUdfs(udf);
        task.setHandlerUser(HANDLER_USER_FROM_PARENT);
        confirmationController.performCommonOperation(task, REPEAT_REQUEST);
        ApiAsserts.assertThat(confirmationController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isCorrectStatus(STATUS_ADVICE_AWAIT);
    }

    @Test(groups = {"Advice", "Regression"}, description = "Подтвердить с оценкой запроса", dependsOnMethods = "repeatRequest")
    public void scoreConfirm() {
        apiController.updateToken(generateAuthToken(HANDLER_USER_FROM_PARENT));
        udf = refreshUdf();
        task.setHandlerUser(null);
        task.refreshUdf();
        confirmationController.performCommonOperation(task, SCORE_CONFIRM);
        ApiAsserts.assertThat(confirmationController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isCorrectStatus(STATUS_ADVICE_CONFIRM);
    }

    @Test(groups = {"Advice", "Regression"}, description = "Закрыть продтверждения решения", dependsOnMethods = "scoreConfirm")
    public void closeConfirm() {
        apiController.updateToken(generateAuthToken(HANDLER_USER_FROM_PARENT));
        udf = refreshUdf();
        task.refreshUdf();
        task.setHandlerUser(null);
        confirmationController.performCommonOperation(task, CLOSE_CONFIRM);
        ApiAsserts.assertThat(confirmationController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isCorrectStatus(STATUS_ADVICE_CLOSED);
    }

    @Test(groups = {"Advice", "Regression"}, description = "Вернуть в работу", dependsOnMethods = "closeConfirm")
    public void returnToWork() {
        apiController.updateToken(generateAuthToken(HANDLER_USER_FROM_PARENT));
        udf = refreshUdf();
        udf.setUdfDate(generateUdfDate(UDF_ADVICE_PLANTD,0));
        udf.setUdfUser(generateUdfUser(STDT_HANDLER, HANDLER_USER_FROM_PARENT));
        task.refreshUdf(udf);
        task.setHandlerUser(HANDLER_USER_FROM_PARENT);
        confirmationController.performCommonOperation(task, RETURN);
        ApiAsserts.assertThat(confirmationController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isCorrectStatus(STATUS_ADVICE_AWAIT);
    }

    @Test(groups = {"Advice", "Regression"}, description = "Снять задачу", dependsOnMethods = "returnToWork")
    public void cancel() {
        apiController.updateToken(generateAuthToken(HANDLER_USER_FROM_PARENT));
        udf = refreshUdf();
        task.refreshUdf();
        task.setHandlerUser(null);
        confirmationController.performCommonOperation(task, CANCEL);
        ApiAsserts.assertThat(confirmationController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isCorrectStatus(STATUS_ADVICE_CLOSED);
    }

}