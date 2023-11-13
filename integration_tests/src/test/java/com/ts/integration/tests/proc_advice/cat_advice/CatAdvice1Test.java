package com.ts.integration.tests.proc_advice.cat_advice;

import com.ts.common.application.controllers.TrackStudioHttpStatusCodes;
import com.ts.common.application.database.dbEntities.GrTaskDbEntity;
import com.ts.common.application.database.dbTables.GrTaskTable;
import com.ts.common.asserts.ApiAsserts;
import com.ts.common.asserts.CommonAssert;
import com.ts.common.asserts.TaskAsserts;
import com.ts.common.controllers.TaskResponseBody;
import com.ts.common.controllers.advice.AdviceController;
import com.ts.common.entitites.commonEntities.Parent;
import com.ts.common.entitites.commonEntities.Task;
import com.ts.common.entitites.commonEntities.User;
import com.ts.common.entitites.tasks.GeneralTask;
import com.ts.common.enums.Operations;
import com.ts.common.enums.Resolutions;
import com.ts.common.enums.TaskStatuses;
import com.ts.common.enums.TaskType;
import com.ts.common.utils.DateUtils;
import com.ts.common.utils.InitEntities;
import com.ts.integration.tests.BaseIntegrationTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static com.ts.common.entitites.commonEntities.List.Constants.*;
import static com.ts.common.entitites.commonEntities.Task.Constants.AKKREDITIVES;
import static com.ts.common.entitites.commonEntities.Udfs.UdfSd.*;
import static com.ts.common.entitites.commonEntities.User.Constants.ABDULLAEV_BAHODIR;
import static com.ts.common.enums.Operations.*;
import static com.ts.common.enums.Resolutions.ANSWER_IN_CODE;
import static com.ts.common.enums.TaskStatuses.*;
import static com.ts.common.utils.InitEntities.*;
import static com.ts.common.utils.RandomUtils.generateString;

public class CatAdvice1Test extends BaseIntegrationTest {
    public AdviceController adviceController;
    private GeneralTask task;
    private Parent parent;
    private GrTaskTable grTaskTable;
    private GrTaskDbEntity parentTaskFromDb;
    private Task parenOfParentTaskFromDb;
    private User HANDLER_USER_FROM_PARENT;

    @BeforeClass(alwaysRun = true)
    public void beforeClass() {
        adviceController = apiController.getAdviceController();
        grTaskTable = dbHelper.getGrTaskTable();
        parentTaskFromDb = (GrTaskDbEntity) grTaskTable.receiveByCategoryAndTaskStatus("CAT_BUGTASK", STATUS_WORKTASK_INWORK);
        parent = InitEntities.generateParent(parentTaskFromDb.getTask_id(), parentTaskFromDb.getTask_number());
        parenOfParentTaskFromDb = parentTaskFromDb.receiveParentTask();
        HANDLER_USER_FROM_PARENT = apiController.receiveGeneralTask(parent.getNumber()).getHandlerUser();
        task = InitEntities.getGeneralTask(TaskType.ADVICE, Operations.CAT);
    }

    @Test(groups = {"Advice", "Regression"}, description = "Создание запроса на консультацию")
    public void catAdvice() {
        apiController.updateToken(InitEntities.generateAuthToken(HANDLER_USER_FROM_PARENT));
        task.setParent(parent);
        task.setHandlerUser(HANDLER_USER_FROM_PARENT);
        task.setDescription(task.getDescription() + generateString());
        udf = refreshUdf();
        udf.setUdfUser(generateUdfUser(UDF_WORKTASK_SUPERVISER, ABDULLAEV_BAHODIR));
        udf.setSecondUdfUser(generateUdfUser(UDF_WATCHER, ABDULLAEV_BAHODIR));
        udf.setUdfList(generateUdfList(UDF_CDP_BL, ANALITIK_PLATFORM));
        udf.setUdfTask(generateUdfTask(UDF_SD_MODULE, AKKREDITIVES));
        udf.setUdfString(generateUdfString(UDF_SD_NOMODULE_REASON, generateString()));
        udf.setUdfDate(generateUdfDate(UDF_ADVICE_PLANTD, 0));
        udf.setSecondUdfTask(generateUdfTask(UDF_WORKTASK_SDREQUEST, parenOfParentTaskFromDb));
        udf.setThirdUdfTask(generateUdfTask(UDF_WORKTASK_WORK, parentTaskFromDb.mapTo()));
        task.refreshUdf(udf);
        adviceController.createAdvice(task);
        ApiAsserts.assertThat(adviceController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isEquals(task);
    }

    @Test(groups = {"Advice", "Regression"}, description = "Комментарий", dependsOnMethods = "catAdvice")
    public void comment() {
        apiController.updateToken(generateAuthToken(HANDLER_USER_FROM_PARENT));
        udf = refreshUdf();
        task.refreshUdf();
        adviceController.performCommonOperation(task, Operations.COMMENT);
        ApiAsserts.assertThat(adviceController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class);
        GeneralTask generalTask = apiController.receiveGeneralTask(task.getNumber());
        TaskAsserts.assertThat(generalTask)
                .isNotEmpty(generalTask.getDescription());
    }

    @Test(groups = {"Advice", "Regression"}, description = "Изменить крайний срок ответа", dependsOnMethods = "comment")
    public void changePlanTime() {
        apiController.updateToken(generateAuthToken(HANDLER_USER_FROM_PARENT));
        udf = refreshUdf();
        task.refreshUdf();
        task.setHandlerUser(null);
        var planTime = DateUtils.getCurrentDate(1);
        var planTime2 = DateUtils.getCurrentDate(1);
        udf.setUdfDate(generateUdfDate(UDF_ADVICE_PLANTD, planTime));
        task.refreshUdf(udf);
        adviceController.performCommonOperation(task, Operations.CHANGE_PLAN_TIME);
        ApiAsserts.assertThat(adviceController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask();

        var taskDetail = apiController.receiveTask(task.getNumber());
        CommonAssert.assertThat(taskDetail)
                .isCorrectUdfDate(UDF_ADVICE_PLANTD, planTime2);
    }

    @Test(groups = {"Advice", "Regression"}, description = "Предоставить консультацию", dependsOnMethods = "changePlanTime")
    public void provideConsult() {
        apiController.updateToken(generateAuthToken(HANDLER_USER_FROM_PARENT));
        udf = refreshUdf();
        task.refreshUdf();
        task.setHandlerUser(null);
        task.setResolution(generateResolution(ANSWER_IN_CODE));
        adviceController.performCommonOperation(task, Operations.PROVIDE_CONSULT);
        ApiAsserts.assertThat(adviceController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isCorrectStatus(TaskStatuses.STATUS_ADVICE_ANSWERED);
    }

    @Test(groups = {"Advice", "Regression"}, description = "Задать дополнительный вопрос", dependsOnMethods = "provideConsult")
    public void askFurther() {
        apiController.updateToken(generateAuthToken(HANDLER_USER_FROM_PARENT));
        udf = refreshUdf();
        udf.setUdfDate(generateUdfDate(UDF_ADVICE_PLANTD, 0));
        udf.setUdfUser(generateUdfUser(STDT_HANDLER, ABDULLAEV_BAHODIR));
        task.refreshUdf(udf);
        task.setHandlerUser(generateUser(HANDLER_USER_FROM_PARENT));
        adviceController.performCommonOperation(task, Operations.ASK_FURTHER);
        ApiAsserts.assertThat(adviceController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isCorrectStatus(STATUS_ADVICE_AWAIT);
    }

    @Test(groups = {"Advice", "Regression"}, description = "Отклонить запрос", dependsOnMethods = "provideConsult")
    public void reject() {
        apiController.updateToken(generateAuthToken(HANDLER_USER_FROM_PARENT));
        udf = refreshUdf();
        task.refreshUdf();
        task.setHandlerUser(null);
        task.setResolution(generateResolution(Resolutions.CANNOT_ANSWER));
        adviceController.performCommonOperation(task, REJECT);
        ApiAsserts.assertThat(adviceController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isCorrectStatus(STATUS_ADVICE_DECLINED);
    }

    @Test(groups = {"Advice", "Regression"}, description = "Запросить консультацию повторно", dependsOnMethods = "reject")
    public void askExtraConsult() {
        apiController.updateToken(generateAuthToken(HANDLER_USER_FROM_PARENT));
        udf = refreshUdf();
        udf.setUdfDate(generateUdfDate(UDF_ADVICE_PLANTD, 0));
        udf.setUdfUser(generateUdfUser(STDT_HANDLER, ABDULLAEV_BAHODIR));
        task.refreshUdf(udf);
        task.setHandlerUser(generateUser(HANDLER_USER_FROM_PARENT));
        task.setDescription(task.getDescription() + generateString());
        adviceController.performCommonOperation(task, ASK_EXTRA_CONSULT);
        ApiAsserts.assertThat(adviceController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isCorrectStatus(STATUS_ADVICE_AWAIT);
    }

    @Test(groups = {"Advice", "Regression"}, description = "Предоставить консультацию", dependsOnMethods = "askExtraConsult")
    public void provideConsultRetry() {
        apiController.updateToken(generateAuthToken(HANDLER_USER_FROM_PARENT));
        udf = refreshUdf();
        udf.setUdfList(generateUdfList(UDF_CDP_WT, LESSONS_PRACTICE));
        task.refreshUdf(udf);
        task.setHandlerUser(null);
        task.setDescription(task.getDescription() + generateString());
        task.setResolution(generateResolution(ANSWER_IN_CODE));
        adviceController.performCommonOperation(task, PROVIDE_CONSULT);
        ApiAsserts.assertThat(adviceController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isCorrectStatus(STATUS_ADVICE_ANSWERED);
    }

    @Test(groups = {"Advice", "Regression"}, description = "Закрыть запрос", dependsOnMethods = "provideConsultRetry")
    public void close() {
        apiController.updateToken(generateAuthToken(HANDLER_USER_FROM_PARENT));
        udf = refreshUdf();
        task.refreshUdf();
        adviceController.performCommonOperation(task, CLOSE);
        ApiAsserts.assertThat(adviceController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isCorrectStatus(STATUS_ADVICE_CLOSED);
    }

}
