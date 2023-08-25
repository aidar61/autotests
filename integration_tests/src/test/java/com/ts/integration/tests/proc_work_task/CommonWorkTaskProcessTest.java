package com.ts.integration.tests.proc_work_task;

import com.ts.common.application.database.dbEntities.GrTaskDbEntity;
import com.ts.common.application.database.dbTables.GrTaskTable;
import com.ts.common.asserts.ApiAsserts;
import com.ts.common.asserts.CommonAssert;
import com.ts.common.controllers.TaskResponseBody;
import com.ts.common.controllers.workTask.WorkTaskController;
import com.ts.common.entitites.commonEntities.Parent;
import com.ts.common.entitites.commonEntities.User;
import com.ts.common.entitites.commonEntities.UserRole;
import com.ts.common.entitites.tasks.GeneralTask;
import com.ts.common.enums.Resolutions;
import com.ts.common.enums.TaskType;
import com.ts.common.utils.DateUtils;
import com.ts.common.utils.InitEntities;
import com.ts.integration.tests.BaseIntegrationTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.*;

import static com.ts.common.application.controllers.TrackStudioHttpStatusCodes.HTTP_OK;
import static com.ts.common.application.database.DbQueryHelper.Operators.*;
import static com.ts.common.entitites.commonEntities.List.Constants.*;
import static com.ts.common.entitites.commonEntities.Status.Priority.NORMAL;
import static com.ts.common.entitites.commonEntities.Task.Constants.AKKREDITIVES;
import static com.ts.common.entitites.commonEntities.Udfs.UdfSd.*;
import static com.ts.common.entitites.commonEntities.User.Constants.ABDULLAEV_BAHODIR;
import static com.ts.common.entitites.commonEntities.udf.UdfMemo.StringValue.*;
import static com.ts.common.entitites.commonEntities.udf.UdfMemo.UserData.AUTO_FINISH;
import static com.ts.common.enums.Operations.*;
import static com.ts.common.enums.Resolutions.NOT_IN_MY_PROFESSIONAL_SKILL;
import static com.ts.common.enums.Resolutions.RESOLUTION_AWAITS_UNTIL_DATE;
import static com.ts.common.enums.TaskStatuses.*;
import static com.ts.common.enums.TaskType.WORK_TASK;
import static com.ts.common.utils.InitEntities.*;

public class CommonWorkTaskProcessTest extends BaseIntegrationTest {
    public WorkTaskController workTaskController;
    private GeneralTask task;
    private User AUTHOR;
    private User HANDLER_USER;
    private GrTaskDbEntity parentTaskFromDb;
    private GrTaskDbEntity slaBugTaskFromDb;
    private Parent parent;
    private GrTaskTable grTaskTable;
    private Map<TaskType.WorkTask, GeneralTask> allCategoriesOfWorkTaskProcess;
    private static final Integer expectedDoubleValue = 2;
    private static final String expectedDate = DateUtils.getCurrentDate(1);

    @DataProvider(name = "workTaskCategories")
    public Object[][] workTaskCategories() {
        TaskType.WorkTask[] workTaskCategories = TaskType.WorkTask.values();
        Object[][] objects = new Object[workTaskCategories.length][];
        for (int i = 0; i < workTaskCategories.length; i++) {
            objects[i] = new TaskType.WorkTask[]{workTaskCategories[i]};
        }
        return objects;
    }

    @BeforeClass(alwaysRun = true)
    public void beforeClass() {
        workTaskController = apiController.getWorkTaskController();
        grTaskTable = dbHelper.getGrTaskTable();
        userController = apiController.getUserController();
        parentTaskFromDb = (GrTaskDbEntity) grTaskTable.receiveRandomTask(
                "task_category", EQUAL.operator, "CAT_GENPLAN",
                AND.operator,
                "task_status", EQUAL.operator, STATUS_PROJECT_PLANNED.name(),
                AND.operator,
                "task_path", LIKE.operator, "%/2405/758009%");
        parent = InitEntities.generateParent(parentTaskFromDb.getTask_id(), parentTaskFromDb.getTask_number());
        slaBugTaskFromDb = (GrTaskDbEntity) grTaskTable.receiveByCategory("CAT_SLABUG");

        List<UserRole> userRoles = userController.receiveUserByTask(parent.getNumber());
        AUTHOR = userController.receiveUserByRole(userRoles, "Менеджер проекта", "root").getForUser();
        HANDLER_USER = userController.receiveUserByRole(userRoles, "Участник проекта", AUTHOR.getLogin()).getForUser();
        allCategoriesOfWorkTaskProcess = new HashMap<>();
    }


    @Test(groups = {"WorkTask", "Regression"}
            , description = "Создание задачи для всех категорий"
            , dataProvider = "workTaskCategories")
    public void createTask(TaskType.WorkTask category) {

        apiController.updateToken(generateAuthToken(AUTHOR));
        task = InitEntities.getGeneralTask(WORK_TASK, CAT);
        task.setParent(parent);
        task.setPriority(generatePriority(NORMAL));
        task.setHandlerUser(HANDLER_USER);

        udf = refreshUdf();
        udf.setUdfTask(generateUdfTask(UDF_SD_MODULE, AKKREDITIVES));
        udf.setUdfList(generateUdfList(UDF_MIS_SERVICE, UDF_MIS_SERVICE_1));
        udf.setSecondUdfList(generateUdfList(UDF_CDP_ACCEPTANCE, REQBYCREATOR));
        udf.setThirdUdfList(generateUdfList(UDF_WORKTASK_ANALYSIS, YES_V2));
        udf.setUdfDate(generateUdfDate(UDF_WORKTASK_ANALYSISFD, 0));

        task.refreshUdf(udf);


        task.setCategory(generateCategoryForWorkTask(category));
        workTaskController.createAbstractWorkTask(task);

        ApiAsserts.assertThat(workTaskController.getResponse())
                .isCorrectResponseCode(HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isCorrectStatus(STATUS_WORKTASK_ONANALYSIS)
                .isEquals(task);

        allCategoriesOfWorkTaskProcess.put(category, task);

    }


    @Test(groups = {"WorkTask", "Regression"}
            , description = "Коррекция плана для всех созданных задач"
            , dependsOnMethods = "createTask"
            , dataProvider = "workTaskCategories")
    public void changePlan(TaskType.WorkTask category) {


        apiController.updateToken(generateAuthToken(HANDLER_USER));
        task = allCategoriesOfWorkTaskProcess.get(category);
        task.setConfirmed(true);

        udf = refreshUdf();
        udf.setUdfDate(generateUdfDate(UDF_WORKTASK_AWAITTD, expectedDate));
        udf.setUdfDouble(generateUdfDouble(UDF_WORKTASK_AWAITBUDGET, expectedDoubleValue));
        udf.setSecondUdfDouble(generateUdfDouble(UDF_CDP_NORMBUDGET, expectedDoubleValue));
        udf.setUdfMemo(generateUdfMemo(UDF_CDP_STEPPLAN, PREVARITELNIY_ANALIZ.value, AUTO_FINISH.value));
        udf.setSecondUdfMemo(generateUdfMemo(UDF_CDP_STEPPROGRESS, HZ.value));
        task.refreshUdf(udf);
        workTaskController.performCommonOperation(task, CHANGE_PLAN);

        ApiAsserts.assertThat(workTaskController.getResponse())
                .isCorrectResponseCode(HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isCorrectStatus(STATUS_WORKTASK_ASSIGNED);

        apiController.receiveTask(task.getNumber());
        if (category.equals(TaskType.WorkTask.DOC_TASK)
                || category.equals(TaskType.WorkTask.AUTOTEST)
                || category.equals(TaskType.WorkTask.ANAL_TASK)
                || category.equals(TaskType.WorkTask.CONSTR_ANAL_TASK)
                || category.equals(TaskType.WorkTask.ACCEPT_TASK)
                || category.equals(TaskType.WorkTask.DEV_AUTOTEST)
                || category.equals(TaskType.WorkTask.REG_TEST_TASK)
                || category.equals(TaskType.WorkTask.TEST_TASK)) {
            CommonAssert.assertThat(apiController.getResponse())
                    .isCorrectUdfDate(UDF_WORKTASK_PLANFD, DateUtils.getCurrentDate(0))
                    .isCorrectUdfDate(UDF_WORKTASK_PLANTD, expectedDate)
                    .isCorrectUdfDouble(UDF_WORKTASK_PLANBUDGET, expectedDoubleValue)
                    .isCorrectUdfDate(UDF_WORKTASK_AWAITTD, expectedDate);
        } else {
            CommonAssert.assertThat(apiController.getResponse())
                    .isCorrectUdfDate(UDF_WORKTASK_PLANFD, DateUtils.getCurrentDate(0))
                    .isCorrectUdfDate(UDF_WORKTASK_PLANTD, expectedDate)
                    .isCorrectUdfDouble(UDF_WORKTASK_PLANBUDGET, expectedDoubleValue)
                    .isCorrectUdfDate(UDF_WORKTASK_AWAITTD, expectedDate)
                    .isCorrectUdfDouble(UDF_CDP_NORMBUDGET, expectedDoubleValue);
        }
    }

    @Test(groups = {"WorkTask", "Regression"}
            , description = "Принять в работу для всех созданных задач"
            , dependsOnMethods = "changePlan"
            , dataProvider = "workTaskCategories")
    public void acceptInWork(TaskType.WorkTask category) {
        apiController.updateToken(generateAuthToken(HANDLER_USER));

        task = allCategoriesOfWorkTaskProcess.get(category);
        task.setHandlerUser(HANDLER_USER);

        udf = refreshUdf();
        udf.setUdfDate(generateUdfDate(UDF_WORKTASK_PLANTD, expectedDate));
        udf.setUdfDouble(generateUdfDouble(UDF_WORKTASK_PLANBUDGET, expectedDoubleValue));
        udf.setUdfMemo(generateUdfMemo(UDF_CDP_STEPPROGRESS, ANALYZE.value));
        udf.setUdfUser(generateUdfUser(STDT_HANDLER, HANDLER_USER));
        task.refreshUdf(udf);
        workTaskController.performCommonOperation(task, ACCEPT_IN_WORK);

        ApiAsserts.assertThat(workTaskController.getResponse())
                .isCorrectResponseCode(HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isCorrectStatus(STATUS_WORKTASK_INWORK);
    }

    @Test(groups = {"WorkTask", "Regression"}
            , description = "Отложить для всех созданных задач"
            , dependsOnMethods = "acceptInWork"
            , dataProvider = "workTaskCategories")
    public void postPone(TaskType.WorkTask category) {
        final String expectedStartDate = DateUtils.getCurrentDate(2);
        final Resolutions expectedResolution = RESOLUTION_AWAITS_UNTIL_DATE;

        apiController.updateToken(generateAuthToken(HANDLER_USER));

        task = allCategoriesOfWorkTaskProcess.get(category);
        task.setResolution(generateResolution(expectedResolution));

        udf = refreshUdf();
        udf.setUdfDate(generateUdfDate(UDF_WORKTASK_PLANFD, expectedStartDate));
        udf.setUdfMemo(generateUdfMemo(UDF_CDP_STEPPROGRESS, ANALYZE.value));
        task.refreshUdf(udf);
        workTaskController.performCommonOperation(task, POSTPONE);

        ApiAsserts.assertThat(workTaskController.getResponse())
                .isCorrectResponseCode(HTTP_OK).isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isCorrectStatus(STATUS_WORKTASK_POSTPONED).isCorrectResolution(expectedResolution);

        apiController.receiveTask(task.getNumber());
        CommonAssert.assertThat(apiController.getResponse())
                .isCorrectUdfDate(UDF_WORKTASK_PLANFD, expectedStartDate);
    }

    @Test(groups = {"WorkTask", "Regression"}
            , description = "Принять в работу для всех созданных задач"
            , dependsOnMethods = "postPone"
            , dataProvider = "workTaskCategories")
    public void acceptInWorkRetry(TaskType.WorkTask category) {
        apiController.updateToken(generateAuthToken(HANDLER_USER));

        task = allCategoriesOfWorkTaskProcess.get(category);
        task.setHandlerUser(HANDLER_USER);

        udf = refreshUdf();
        udf.setUdfDate(generateUdfDate(UDF_WORKTASK_PLANTD, expectedDate));
        udf.setUdfDouble(generateUdfDouble(UDF_WORKTASK_PLANBUDGET, expectedDoubleValue));
        udf.setUdfMemo(generateUdfMemo(UDF_CDP_STEPPROGRESS, ANALYZE.value));
        udf.setUdfUser(generateUdfUser(STDT_HANDLER, HANDLER_USER));
        task.refreshUdf(udf);
        workTaskController.performCommonOperation(task, ACCEPT_IN_WORK);

        ApiAsserts.assertThat(workTaskController.getResponse())
                .isCorrectResponseCode(HTTP_OK).isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isCorrectStatus(STATUS_WORKTASK_INWORK);

        apiController.receiveTask(task.getNumber());
        if (category.equals(TaskType.WorkTask.AUTOTEST) || category.equals(TaskType.WorkTask.DEV_AUTOTEST)) {
            CommonAssert.assertThat(apiController.getResponse())
                    .isCorrectUdfDate(UDF_WORKTASK_PLANTD, expectedDate)
                    .isCorrectUdfDouble(UDF_WORKTASK_PLANBUDGET, expectedDoubleValue);
        } else {
            CommonAssert.assertThat(apiController.getResponse())
                    .isCorrectUdfDate(UDF_WORKTASK_PLANTD, expectedDate)
                    .isCorrectUdfDouble(UDF_WORKTASK_PLANBUDGET, expectedDoubleValue)
                    .isCorrectUdfDouble(UDF_WORKTASK_FIRSTPLANBUDGET, expectedDoubleValue);
        }
    }

    @Test(groups = {"WorkTask", "Regression"}
            , description = "Отклонить для всех созданных задач"
            , dependsOnMethods = "postPone"
            , dataProvider = "workTaskCategories")
    public void decline(TaskType.WorkTask category) {
        final Resolutions expectedResolution = NOT_IN_MY_PROFESSIONAL_SKILL;
        apiController.updateToken(generateAuthToken(HANDLER_USER));

        task = allCategoriesOfWorkTaskProcess.get(category);
        task.setResolution(generateResolution(expectedResolution));

        udf = refreshUdf();
        udf.setUdfMemo(generateUdfMemo(UDF_CDP_STEPPROGRESS, ANALYZE.value));
        task.refreshUdf(udf);
        workTaskController.performCommonOperation(task, DECLINE);

        ApiAsserts.assertThat(workTaskController.getResponse())
                .isCorrectResponseCode(HTTP_OK).isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isCorrectStatus(STATUS_WORKTASK_DECLINED).isCorrectResolution(expectedResolution).isCorrectHandlerUser(AUTHOR);
    }

    @Test(groups = {"WorkTask", "Regression"}
            , description = "Вернуть в работу для всех созданных задач"
            , dependsOnMethods = "decline"
            , dataProvider = "workTaskCategories")
    public void returnTask(TaskType.WorkTask category) {
        apiController.updateToken(generateAuthToken(AUTHOR));

        task = allCategoriesOfWorkTaskProcess.get(category);
        task.setHandlerUser(HANDLER_USER);

        udf = refreshUdf();
        udf.setUdfUser(generateUdfUser(STDT_HANDLER, HANDLER_USER));
        task.refreshUdf(udf);
        workTaskController.performCommonOperation(task, RETURN);

        ApiAsserts.assertThat(workTaskController.getResponse())
                .isCorrectResponseCode(HTTP_OK).isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isCorrectStatus(STATUS_WORKTASK_ASSIGNED).isCorrectHandlerUser(HANDLER_USER);
    }

    @Test(groups = {"WorkTask", "Regression"}
            , description = "Назначить контроллера для всех созданных задач"
            , dependsOnMethods = "returnTask"
            , dataProvider = "workTaskCategories")
    public void superVice(TaskType.WorkTask category) {
        final User.Constants expectedControllerUser = ABDULLAEV_BAHODIR;
        apiController.updateToken(generateAuthToken(HANDLER_USER));
        task = allCategoriesOfWorkTaskProcess.get(category);

        udf = refreshUdf();
        udf.setUdfUser(generateUdfUser(UDF_WORKTASK_SUPERVISER, expectedControllerUser));
        udf.setUdfMemo(generateUdfMemo(UDF_CDP_STEPPROGRESS, ANALYZE.value));
        task.refreshUdf(udf);
        workTaskController.performCommonOperation(task, WORKTASK_SUPERVISE);

        ApiAsserts.assertThat(workTaskController.getResponse())
                .isCorrectResponseCode(HTTP_OK).isParseableBody(TaskResponseBody.class);

        apiController.receiveTask(task.getNumber());
        CommonAssert.assertThat(apiController.getResponse())
                .isCorrectUdfUSer(UDF_WORKTASK_SUPERVISER, generateUser(expectedControllerUser));
    }

    @Test(groups = {"WorkTask", "Regression"}
            , description = "Назначить наблюдателя для всех созданных задач"
            , dependsOnMethods = "superVice"
            , dataProvider = "workTaskCategories")
    public void watch(TaskType.WorkTask category) {
        final User.Constants expectedWatcherUser = ABDULLAEV_BAHODIR;

        apiController.updateToken(generateAuthToken(HANDLER_USER));
        task = allCategoriesOfWorkTaskProcess.get(category);

        udf = refreshUdf();
        udf.setUdfUser(generateUdfUser(UDF_WATCHER, expectedWatcherUser));
        udf.setUdfMemo(generateUdfMemo(UDF_CDP_STEPPROGRESS, ANALYZE.value));
        task.refreshUdf(udf);
        workTaskController.performCommonOperation(task, WATCH);

        ApiAsserts.assertThat(workTaskController.getResponse())
                .isCorrectResponseCode(HTTP_OK).isParseableBody(TaskResponseBody.class);

        apiController.receiveTask(task.getNumber());
        CommonAssert.assertThat(apiController.getResponse())
                .isCorrectUdfUSer(UDF_WATCHER, generateUser(expectedWatcherUser));
    }

    @Test(groups = {"WorkTask", "Regression"}
            , description = "Изменить услугу для всех созданных задач"
            , dependsOnMethods = "watch"
            , dataProvider = "workTaskCategories")
    public void changeService(TaskType.WorkTask category) {
        com.ts.common.entitites.commonEntities.List.Constants expectedListValue = UDF_MIS_SERVICE_1;

        apiController.updateToken(generateAuthToken(HANDLER_USER));
        task = allCategoriesOfWorkTaskProcess.get(category);

        udf = refreshUdf();
        udf.setUdfList(generateUdfList(UDF_MIS_SERVICE, expectedListValue));
        task.refreshUdf(udf);
        workTaskController.performCommonOperation(task, CHANGE_SERVICE);
        ApiAsserts.assertThat(workTaskController.getResponse())
                .isCorrectResponseCode(HTTP_OK).isParseableBody(TaskResponseBody.class);

        apiController.receiveTask(task.getNumber());
        CommonAssert.assertThat(apiController.getResponse())
                .isCorrectUdfList(UDF_MIS_SERVICE, new com.ts.common.entitites.commonEntities.List(expectedListValue.getId()));
    }

    @Test(groups = {"WorkTask", "Regression"}
            , description = "Принять в работу для всех созданных задач"
            , dependsOnMethods = "changeService"
            , dataProvider = "workTaskCategories")
    public void acceptInWorkRetrySecondTime(TaskType.WorkTask category) {
        apiController.updateToken(generateAuthToken(HANDLER_USER));

        task = allCategoriesOfWorkTaskProcess.get(category);
        task.setHandlerUser(HANDLER_USER);

        udf = refreshUdf();
        udf.setUdfDate(generateUdfDate(UDF_WORKTASK_PLANTD, expectedDate));
        udf.setUdfDouble(generateUdfDouble(UDF_WORKTASK_PLANBUDGET, expectedDoubleValue));
        udf.setUdfMemo(generateUdfMemo(UDF_CDP_STEPPROGRESS, ANALYZE.value));
        udf.setUdfUser(generateUdfUser(STDT_HANDLER, HANDLER_USER));
        task.refreshUdf(udf);
        workTaskController.performCommonOperation(task, ACCEPT_IN_WORK);

        ApiAsserts.assertThat(workTaskController.getResponse())
                .isCorrectResponseCode(HTTP_OK).isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isCorrectStatus(STATUS_WORKTASK_INWORK);
    }

    @Test(groups = {"WorkTask", "Regression"}
            , description = "Принять в работу для всех созданных задач"
            , dependsOnMethods = "changeService"
            , dataProvider = "workTaskCategories")
    public void toAcceptance(TaskType.WorkTask category) {
        apiController.updateToken(generateAuthToken(HANDLER_USER));

        task = allCategoriesOfWorkTaskProcess.get(category);
        task.setHandlerUser(AUTHOR);

        udf = refreshUdf();
        udf.setUdfList(generateUdfList(UDF_SDFEATURE_DOCREVISION, NO));
        udf.setUdfMemo(generateUdfMemo(UDF_CDP_STEPPROGRESS, ANALYZE.value));
        udf.setUdfUser(generateUdfUser(STDT_HANDLER, AUTHOR));
        task.refreshUdf(udf);
        if (category.equals(TaskType.WorkTask.ACCEPT_TASK)) {
            udf = refreshUdf();
            udf.setUdfMemo(generateUdfMemo(UDF_CDP_STEPPROGRESS, ANALYZE.value));
            task.setHandlerUser(null);
            task.refreshUdf(udf);
            workTaskController.performCommonOperation(task, WORKTASK_FINISHACCEPT);
        } else {
            workTaskController.performCommonOperation(task, WORKTASK_TO_ACCEPTANCE);
        }

        ApiAsserts.assertThat(workTaskController.getResponse())
                .isCorrectResponseCode(HTTP_OK).isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isCorrectStatus(STATUS_WORKTASK_CLOSED);
    }

}
