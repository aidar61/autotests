package com.ts.integration.tests.proc_work_task;

import com.ts.common.application.database.dbEntities.GrTaskDbEntity;
import com.ts.common.application.database.dbTables.GrTaskTable;
import com.ts.common.asserts.ApiAsserts;
import com.ts.common.asserts.CommonAssert;
import com.ts.common.controllers.TaskResponseBody;
import com.ts.common.controllers.workTask.WorkTaskController;
import com.ts.common.entitites.commonEntities.Parent;
import com.ts.common.entitites.commonEntities.User;
import com.ts.common.entitites.tasks.GeneralTask;
import com.ts.common.enums.Resolutions;
import com.ts.common.enums.TaskType;
import com.ts.common.utils.DateUtils;
import com.ts.common.utils.InitEntities;
import com.ts.integration.tests.BaseIntegrationTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import static com.ts.common.application.controllers.TrackStudioHttpStatusCodes.HTTP_OK;
import static com.ts.common.config.AppConfigProvider.getUserConfig;
import static com.ts.common.entitites.commonEntities.List.Constants.*;
import static com.ts.common.entitites.commonEntities.Role.RoleConstants.ROLE_WORKER;
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
    private User at_task_manager;
    private User at_task_participant;
    private Parent parent;
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
        userController = apiController.getUserController();

        parent = taskGenerator.getAt_genplan().toParent();

        at_task_manager = userController.getUserBy(userRoles, ROLE_WORKER, getUserConfig().at_task_manager());
        at_task_participant = userController.getUserBy(userRoles, ROLE_WORKER, getUserConfig().at_task_participant());

        allCategoriesOfWorkTaskProcess = new HashMap<>();
    }


    @Test(groups = {"PROC_WORKTASK", "Regression"}
            , description = "Создание задачи для всех категорий"
            , dataProvider = "workTaskCategories")
    public void createTask(TaskType.WorkTask category) {

        apiController.updateToken(generateAuthToken(at_task_manager));
        task = InitEntities.getGeneralTask(WORK_TASK, CAT);
        task.setParent(parent);
        task.setPriority(generatePriority(NORMAL));
        task.setHandlerUser(at_task_participant);

        udf = refreshUdf();
        udf.setUdfTask(generateUdfTask(UDF_SD_MODULE, AKKREDITIVES));
        udf.setUdfList(generateUdfList(UDF_MIS_SERVICE, UDF_MIS_SERVICE_1));
        udf.setSecondUdfList(generateUdfList(UDF_CDP_ACCEPTANCE, REQBYAUTHOR));
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


    @Test(groups = {"PROC_WORKTASK", "Regression"}
            , description = "Коррекция плана для всех созданных задач"
            , dependsOnMethods = "createTask"
            , dataProvider = "workTaskCategories")
    public void changePlan(TaskType.WorkTask category) {


        apiController.updateToken(generateAuthToken(at_task_participant));
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
//                    .isCorrectUdfDate(UDF_WORKTASK_PLANFD, expectedDate)
//                    .isCorrectUdfDate(UDF_WORKTASK_PLANTD, expectedDate)
                    .isCorrectUdfDouble("Оценка трудоемкости", UDF_WORKTASK_PLANBUDGET, expectedDoubleValue);
//                    .isCorrectUdfDate(UDF_WORKTASK_AWAITTD, expectedDate);
        } else {
            CommonAssert.assertThat(apiController.getResponse())
//                    .isCorrectUdfDate(UDF_WORKTASK_PLANFD, expectedDate)
//                    .isCorrectUdfDate(UDF_WORKTASK_PLANTD, expectedDate)
                    .isCorrectUdfDouble("Оценка трудоемкости", UDF_WORKTASK_PLANBUDGET, expectedDoubleValue)
//                    .isCorrectUdfDate(UDF_WORKTASK_AWAITTD, expectedDate)
                    .isCorrectUdfDouble("Трудоемкость по нормам", UDF_CDP_NORMBUDGET, expectedDoubleValue);
        }
    }

    @Test(groups = {"PROC_WORKTASK", "Regression"}
            , description = "Принять в работу для всех созданных задач"
            , dependsOnMethods = "changePlan"
            , dataProvider = "workTaskCategories")
    public void acceptInWork(TaskType.WorkTask category) {
        apiController.updateToken(generateAuthToken(at_task_participant));

        task = allCategoriesOfWorkTaskProcess.get(category);
        task.setHandlerUser(at_task_participant);

        udf = refreshUdf();
        udf.setUdfDate(generateUdfDate(UDF_WORKTASK_PLANTD, expectedDate));
        udf.setUdfDouble(generateUdfDouble(UDF_WORKTASK_PLANBUDGET, expectedDoubleValue));
        udf.setUdfMemo(generateUdfMemo(UDF_CDP_STEPPROGRESS, ANALYZE.value));
        udf.setUdfUser(generateUdfUser(STDT_HANDLER, at_task_participant));
        task.refreshUdf(udf);
        workTaskController.performCommonOperation(task, ACCEPT_IN_WORK);

        ApiAsserts.assertThat(workTaskController.getResponse())
                .isCorrectResponseCode(HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isCorrectStatus(STATUS_WORKTASK_INWORK);
    }

    @Test(groups = {"PROC_WORKTASK", "Regression"}
            , description = "Отложить для всех созданных задач"
            , dependsOnMethods = "acceptInWork"
            , dataProvider = "workTaskCategories")
    public void postPone(TaskType.WorkTask category) {
        final String expectedStartDate = DateUtils.getCurrentDate(2);
        final Resolutions expectedResolution = RESOLUTION_AWAITS_UNTIL_DATE;

        apiController.updateToken(generateAuthToken(at_task_participant));

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

    @Test(groups = {"PROC_WORKTASK", "Regression"}
            , description = "Принять в работу для всех созданных задач"
            , dependsOnMethods = "postPone"
            , dataProvider = "workTaskCategories")
    public void acceptInWorkRetry(TaskType.WorkTask category) {
        apiController.updateToken(generateAuthToken(at_task_participant));

        task = allCategoriesOfWorkTaskProcess.get(category);
        task.setHandlerUser(at_task_participant);

        udf = refreshUdf();
        udf.setUdfDate(generateUdfDate(UDF_WORKTASK_PLANTD, expectedDate));
        udf.setUdfDouble(generateUdfDouble(UDF_WORKTASK_PLANBUDGET, expectedDoubleValue));
        udf.setUdfMemo(generateUdfMemo(UDF_CDP_STEPPROGRESS, ANALYZE.value));
        udf.setUdfUser(generateUdfUser(STDT_HANDLER, at_task_participant));
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
                    .isCorrectUdfDouble("Оценка трудоемкости", UDF_WORKTASK_PLANBUDGET, expectedDoubleValue);
        } else {
            CommonAssert.assertThat(apiController.getResponse())
                    .isCorrectUdfDate(UDF_WORKTASK_PLANTD, expectedDate)
                    .isCorrectUdfDouble("Оценка трудоемкости", UDF_WORKTASK_PLANBUDGET, expectedDoubleValue)
                    .isCorrectUdfDouble("Первоначальная оценка трудоёмкости", UDF_WORKTASK_FIRSTPLANBUDGET, expectedDoubleValue);
        }
    }

    @Test(groups = {"PROC_WORKTASK", "Regression"}
            , description = "Отклонить для всех созданных задач"
            , dependsOnMethods = "postPone"
            , dataProvider = "workTaskCategories")
    public void decline(TaskType.WorkTask category) {
        final Resolutions expectedResolution = NOT_IN_MY_PROFESSIONAL_SKILL;
        apiController.updateToken(generateAuthToken(at_task_participant));

        task = allCategoriesOfWorkTaskProcess.get(category);
        task.setResolution(generateResolution(expectedResolution));

        udf = refreshUdf();
        udf.setUdfMemo(generateUdfMemo(UDF_CDP_STEPPROGRESS, ANALYZE.value));
        task.refreshUdf(udf);
        workTaskController.performCommonOperation(task, DECLINE);

        ApiAsserts.assertThat(workTaskController.getResponse())
                .isCorrectResponseCode(HTTP_OK).isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isCorrectStatus(STATUS_WORKTASK_DECLINED).isCorrectResolution(expectedResolution).isCorrectHandlerUser(at_task_manager);
    }

    @Test(groups = {"PROC_WORKTASK", "Regression"}
            , description = "Вернуть в работу для всех созданных задач"
            , dependsOnMethods = "decline"
            , dataProvider = "workTaskCategories")
    public void returnTask(TaskType.WorkTask category) {
        apiController.updateToken(generateAuthToken(at_task_manager));

        task = allCategoriesOfWorkTaskProcess.get(category);
        task.setHandlerUser(at_task_participant);

        udf = refreshUdf();
        udf.setUdfUser(generateUdfUser(STDT_HANDLER, at_task_participant));
        task.refreshUdf(udf);
        workTaskController.performCommonOperation(task, RETURN);

        ApiAsserts.assertThat(workTaskController.getResponse())
                .isCorrectResponseCode(HTTP_OK).isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isCorrectStatus(STATUS_WORKTASK_ASSIGNED).isCorrectHandlerUser(at_task_participant);
    }

    @Test(groups = {"PROC_WORKTASK", "Regression"}
            , description = "Назначить контроллера для всех созданных задач"
            , dependsOnMethods = "returnTask"
            , dataProvider = "workTaskCategories")
    public void superVice(TaskType.WorkTask category) {
        final User.Constants expectedControllerUser = ABDULLAEV_BAHODIR;
        apiController.updateToken(generateAuthToken(at_task_participant));
        task = allCategoriesOfWorkTaskProcess.get(category);

        udf = refreshUdf();
        udf.setUdfUser(generateUdfUser(UDF_WORKTASK_SUPERVISER, expectedControllerUser));
        udf.setUdfMemo(generateUdfMemo(UDF_CDP_STEPPROGRESS, ANALYZE.value));
        task.refreshUdf(udf);
        workTaskController.performCommonOperation(task, SUPERVISE);

        ApiAsserts.assertThat(workTaskController.getResponse())
                .isCorrectResponseCode(HTTP_OK).isParseableBody(TaskResponseBody.class);

        apiController.receiveTask(task.getNumber());
        CommonAssert.assertThat(apiController.getResponse())
                .isCorrectUdfUSer(UDF_WORKTASK_SUPERVISER, generateUser(expectedControllerUser));
    }

    @Test(groups = {"PROC_WORKTASK", "Regression"}
            , description = "Назначить наблюдателя для всех созданных задач"
            , dependsOnMethods = "superVice"
            , dataProvider = "workTaskCategories")
    public void watch(TaskType.WorkTask category) {
        final User.Constants expectedWatcherUser = ABDULLAEV_BAHODIR;

        apiController.updateToken(generateAuthToken(at_task_participant));
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

    @Test(groups = {"PROC_WORKTASK", "Regression"}
            , description = "Изменить услугу для всех созданных задач"
            , dependsOnMethods = "watch"
            , dataProvider = "workTaskCategories")
    public void changeService(TaskType.WorkTask category) {
        com.ts.common.entitites.commonEntities.List.Constants expectedListValue = UDF_MIS_SERVICE_1;

        apiController.updateToken(generateAuthToken(at_task_participant));
        task = allCategoriesOfWorkTaskProcess.get(category);

        udf = refreshUdf();
        udf.setUdfList(generateUdfList(UDF_MIS_SERVICE, expectedListValue));
        task.refreshUdf(udf);
        workTaskController.performCommonOperation(task, CHANGE_SERVICE);
        ApiAsserts.assertThat(workTaskController.getResponse())
                .isCorrectResponseCode(HTTP_OK).isParseableBody(TaskResponseBody.class);

        apiController.receiveTask(task.getNumber());
        CommonAssert.assertThat(apiController.getResponse())
                .isCorrectUdfList(UDF_MIS_SERVICE, expectedListValue.getId());
    }

    @Test(groups = {"PROC_WORKTASK", "Regression"}
            , description = "Принять в работу для всех созданных задач"
            , dependsOnMethods = "changeService"
            , dataProvider = "workTaskCategories")
    public void acceptInWorkRetrySecondTime(TaskType.WorkTask category) {
        apiController.updateToken(generateAuthToken(at_task_participant));

        task = allCategoriesOfWorkTaskProcess.get(category);
        task.setHandlerUser(at_task_participant);

        udf = refreshUdf();
        udf.setUdfDate(generateUdfDate(UDF_WORKTASK_PLANTD, expectedDate));
        udf.setUdfDouble(generateUdfDouble(UDF_WORKTASK_PLANBUDGET, expectedDoubleValue));
        udf.setUdfMemo(generateUdfMemo(UDF_CDP_STEPPROGRESS, ANALYZE.value));
        udf.setUdfUser(generateUdfUser(STDT_HANDLER, at_task_participant));
        task.refreshUdf(udf);
        workTaskController.performCommonOperation(task, ACCEPT_IN_WORK);

        ApiAsserts.assertThat(workTaskController.getResponse())
                .isCorrectResponseCode(HTTP_OK).isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isCorrectStatus(STATUS_WORKTASK_INWORK);
    }

    @Test(groups = {"PROC_WORKTASK", "Regression"}
            , description = "Принять в работу для всех созданных задач"
            , dependsOnMethods = "acceptInWorkRetrySecondTime"
            , dataProvider = "workTaskCategories")
    public void toAcceptance(TaskType.WorkTask category) {
        apiController.updateToken(generateAuthToken(at_task_participant));

        task = allCategoriesOfWorkTaskProcess.get(category);
        task.setHandlerUser(at_task_manager);

        udf = refreshUdf();
        udf.setUdfList(generateUdfList(UDF_SDFEATURE_DOCREVISION, NO));
        udf.setUdfMemo(generateUdfMemo(UDF_CDP_STEPPROGRESS, ANALYZE.value));
        udf.setUdfUser(generateUdfUser(STDT_HANDLER, at_task_manager));
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
