package com.ts.integration.tests.proc_work_task.dev_task;

import com.ts.common.application.controllers.TrackStudioHttpStatusCodes;
import com.ts.common.application.database.dbEntities.GrTaskDbEntity;
import com.ts.common.application.database.dbTables.GrTaskTable;
import com.ts.common.asserts.ApiAsserts;
import com.ts.common.controllers.TaskResponseBody;
import com.ts.common.controllers.dev.DevTaskController;
import com.ts.common.entitites.commonEntities.Parent;
import com.ts.common.entitites.commonEntities.Task;
import com.ts.common.entitites.commonEntities.User;
import com.ts.common.entitites.commonEntities.udf.UdfTask;
import com.ts.common.entitites.tasks.GeneralTask;
import com.ts.common.enums.Operations;
import com.ts.common.enums.TaskType;
import com.ts.common.utils.InitEntities;
import com.ts.integration.tests.BaseIntegrationTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static com.ts.common.entitites.commonEntities.List.Constants.*;
import static com.ts.common.entitites.commonEntities.Task.Constants.CORE;
import static com.ts.common.entitites.commonEntities.Task.Constants.MTBANK;
import static com.ts.common.entitites.commonEntities.Udfs.UdfSd.*;
import static com.ts.common.entitites.commonEntities.User.Constants.ABDULLAEV_BAHODIR;
import static com.ts.common.enums.Operations.*;
import static com.ts.common.enums.Resolutions.*;
import static com.ts.common.enums.TaskStatuses.*;
import static com.ts.common.utils.InitEntities.*;
import static com.ts.common.utils.InitEntities.generateUdfTask;
import static com.ts.common.utils.RandomUtils.generateString;

public class DevTask1Test extends BaseIntegrationTest {
    public DevTaskController devTaskController;
    private GeneralTask task;
    private String misService;
    private UdfTask productTask;
    private UdfTask bdkuTask;
    private Parent parent;
    private GrTaskTable grTaskTable;
    private GrTaskDbEntity parentTaskFromDb;
    private Task parenOfParentTaskFromDb;
    private User SUBMITTER_USER_FROM_PARENT;
    private String tempDate;

    @BeforeClass(alwaysRun = true)
    public void beforeClass() {
        devTaskController = apiController.getDevTaskController();
        grTaskTable = dbHelper.getGrTaskTable();
        parentTaskFromDb = (GrTaskDbEntity) grTaskTable.receiveByTaskNumber("1021687");
        parent = InitEntities.generateParent(parentTaskFromDb.getTask_id(), parentTaskFromDb.getTask_number());
        SUBMITTER_USER_FROM_PARENT = apiController.receiveGeneralTask(parent.getNumber()).getSubmitterUser();
        task = InitEntities.getGeneralTask(TaskType.DEV_TASK, Operations.CAT);
        var tasks = devTaskController.getTaskForSDRequest(parent.getNumber());
        productTask = tasks.get("UDF_PRODUCT");
        bdkuTask = tasks.get("UDF_BDKU_CONFIGURATION");
        parenOfParentTaskFromDb = parentTaskFromDb.receiveParentTask();
        misService = devTaskController.getMisService();
    }


    @Test(groups = {"DevTask", "Regression"}, description = "Создание запроса на разработку")
    public void devTask() {
        apiController.updateToken(InitEntities.generateAuthToken(SUBMITTER_USER_FROM_PARENT));
        task.setParent(parent);
        task.setName("Создание запроса на разработку");
        task.setDescription(task.getDescription() + generateString());
        task.setHandlerUser(SUBMITTER_USER_FROM_PARENT);

        udf = refreshUdf();
        udf.setEighthUdfList(generateUdfList(UDF_WORKTASK_COMPLEXITYLEVEL, TASK_LEVEL_7));
        udf.setUdfUser(generateUdfUser(UDF_WORKTASK_SUPERVISER, ABDULLAEV_BAHODIR));
        udf.setSecondUdfUser(generateUdfUser(UDF_WATCHER, ABDULLAEV_BAHODIR));
        udf.setUdfList(generateUdfList(UDF_CDP_BL, UDF_CDP_BL_PRODUCT));
        udf.setUdfTask(generateUdfTask(UDF_SD_MODULE, CORE));
        udf.setUdfString(generateUdfString(UDF_SD_NOMODULE_REASON, generateString()));
        udf.setSecondUdfList(generateUdfList(UDF_CDP_ACCEPTANCE, UDF_CDP_ACCEPTANCE_NO));
        udf.setUdfDate(generateUdfDate(UDF_WORKTASK_ANALYSISFD, 1));
        udf.setSecondUdfString(generateUdfString(UDF_WORKTASK_BRANCH, "hg:apng:default [Аnalitic platform new generation]"));
        udf.setSecondUdfTask(generateUdfTask(UDF_PRODUCT, productTask.getTaskValue()[0]));
        udf.setThirdUdfList(generateUdfList(UDF_WORKTASK_WAYCODEREVIEW, WAY_CODE_REVIEW_NO));
        udf.setFourthUdfList(generateUdfList(UDF_SDFEATURE_GENUSE, GENERAL, "{\"username\":\"babdullayev\",\"name\":\"Абдуллаев Баходир\"}"));
        udf.setThirdUdfString(generateUdfString(UDF_WORKTASK_ANNOTATION, generateString()));
        udf.setThirdUdfTask(generateUdfTask(UDF_WORKTASK_SDREQUEST, parenOfParentTaskFromDb));
        udf.setFifthUdfList(generateUdfList(UDF_WORKTASK_CHANGEWORKERINRQST, YES_AND_LEAVE_THIS_ROLE_TO_THE_CURRENT_PERFORMER));
        udf.setSixthUdfList(generateUdfList(UDF_WORKTASK_ADDWATCHERINREQST, UDF_WORKTASK_ADDWATCHERINREQST_YES));
        udf.setSeventhUdfList(generateUdfList(UDF_WORKTASK_ADDTRSTWATCHINREQST, UDF_WORKTASK_ADDTRSTWATCHINREQST_YES));
        udf.setThirdUdfTask(generateUdfTask(UDF_WORKTASK_WORK, parentTaskFromDb.mapTo()));
        udf.setFourthUdfTask(generateUdfTask(UDF_BDKU_CONFIGURATION, bdkuTask.getTaskValueSelector()[0]));
        udf.setSixthUdfTask(generateUdfTask(UDF_WORKTASK_DEPENDBF, MTBANK));
        task.refreshUdf(udf);
        devTaskController.createDevTask(task);
        ApiAsserts.assertThat(devTaskController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isCorrectStatus(STATUS_WORKTASK_ONANALYSIS)
                .isEquals(task);
    }

    @Test(groups = {"DevTask", "Regression"}, description = "Коррекция плана", dependsOnMethods = "devTask")
    public void changePlan() {
        apiController.updateToken(generateAuthToken(SUBMITTER_USER_FROM_PARENT));
        udf = refreshUdf();
        udf.setUdfDate(generateUdfDate(UDF_WORKTASK_AWAITTD, 1));
        udf.setUdfDouble(generateUdfDouble(UDF_WORKTASK_AWAITBUDGET, 2));
        udf.setSecondUdfDouble(generateUdfDouble(UDF_CDP_NORMBUDGET, 2));
        udf.setUdfMemo(generateUdfMemo(UDF_CDP_STEPPLAN,
                "[{\"id\":\"8181816c88683b1801894f980bef6f6a\",\"name\":\"Предварительный анализ\",\"order\":0,\"taskId\":\"8181816c88683b1801894f97fa9a6f43\",\"weight\":0,\"budget\":7200,\"progress\":0,\"description\":\"\",\"status\":\"ACTUAL\",\"workTypeId\":\"402881c2516c220101516c711ff80024\",\"workTypeNorm\":2,\"hrs\":0,\"deletable\":true,\"actualBudget\":0,\"planby\":\"budget\",\"workTypeAsString\":\"[0701] Иное\",\"workTypeDraftAsString\":\"-\",\"draftChanged\":true,\"orderDraft\":0,\"nameDraft\":\"Предварительный анализ\",\"descriptionDraft\":\"\",\"statusDraft\":\"ACTUAL\",\"weightDraft\":0,\"budgetDraft\":7200,\"budgetHrs\":2,\"budgetMinutes\":0,\"workTypeIdDraft\":\"402881c2516c220101516c711ff80024\",\"workTypeNormDraft\":2}]",
                "{\"autoFinishAnalysis\": \"true\"}"));
        udf.setSecondUdfMemo(generateUdfMemo(UDF_CDP_STEPPROGRESS, "[{}]"));
        udf.setUdfList(generateUdfList(UDF_CDP_WT, SOFTWARE_DESIGN));
        task.refreshUdf(udf);
        task.setConfirmed(true);
        devTaskController.performCommonOperation(task, CHANGE_PLAN);
        ApiAsserts.assertThat(devTaskController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isCorrectStatus(STATUS_WORKTASK_ASSIGNED);
    }

    @Test(groups = {"DevTask", "Regression"}, description = "Принять в работу", dependsOnMethods = "changePlan")
    public void taskStart() {
        task.refreshUdf();
        devTaskController.performCommonOperation(task, ACCEPT_IN_WORK);
        ApiAsserts.assertThat(devTaskController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isCorrectStatus(STATUS_WORKTASK_INWORK);
    }

    @Test(groups = {"DevTask", "Regression"}, description = "Комментарий", dependsOnMethods = "taskStart")
    public void taskComment() {
        apiController.updateToken(generateAuthToken(SUBMITTER_USER_FROM_PARENT));
        udf = refreshUdf();
        task.refreshUdf();
        devTaskController.performCommonOperation(task, Operations.COMMENT);
        ApiAsserts.assertThat(devTaskController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask();
    }

    @Test(groups = {"DevTask", "Regression"}, description = "Отложить", dependsOnMethods = "taskComment")
    public void taskPostpone() {
        apiController.updateToken(generateAuthToken(SUBMITTER_USER_FROM_PARENT));
        task.setResolution(generateResolution(RESOLUTION_DEPENDS_ON_ANOTHER_TASK));
        udf = refreshUdf();
        udf.setUdfDate(generateUdfDate(UDF_WORKTASK_PLANFD, 0));
        udf.setUdfTask(generateUdfTask(UDF_WORKTASK_DEPENDBF, MTBANK));
        udf.setUdfMemo(generateUdfMemo(UDF_CDP_STEPPROGRESS, "[{}]"));
        task.refreshUdf(udf);
        devTaskController.performCommonOperation(task, Operations.POSTPONE);
        ApiAsserts.assertThat(devTaskController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isCorrectStatus(STATUS_WORKTASK_POSTPONED);
    }

    @Test(groups = {"DevTask", "Regression"}, description = "Принять в работу", dependsOnMethods = "taskPostpone")
    public void taskStart2() {
        task.refreshUdf();
        devTaskController.performCommonOperation(task, ACCEPT_IN_WORK);
        ApiAsserts.assertThat(devTaskController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isCorrectStatus(STATUS_WORKTASK_INWORK);
    }

    @Test(groups = {"DevTask", "Regression"}, description = "Отложить", dependsOnMethods = "taskStart2")
    public void taskPostpone2() {
        apiController.updateToken(generateAuthToken(SUBMITTER_USER_FROM_PARENT));
        task.setResolution(generateResolution(RESOLUTION_AWAITS_UNTIL_DATE));
        task.setDescription(generateString());
        udf = refreshUdf();
        var udfPlanDf = generateUdfDate(UDF_WORKTASK_PLANFD, 10);
        tempDate = udfPlanDf.getDateValue();
        udf.setUdfDate(udfPlanDf);
        udf.setUdfTask(generateUdfTask(UDF_WORKTASK_DEPENDBF, MTBANK));
        udf.setUdfMemo(generateUdfMemo(UDF_CDP_STEPPROGRESS, "[{}]"));
        task.refreshUdf(udf);
        devTaskController.performCommonOperation(task, Operations.POSTPONE);
        ApiAsserts.assertThat(devTaskController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isCorrectStatus(STATUS_WORKTASK_POSTPONED);
    }

    @Test(groups = {"DevTask", "Regression"}, description = "Принять в работу", dependsOnMethods = "taskPostpone2")
    public void taskStart3() {
        udf = refreshUdf();
        task.setHandlerUser(SUBMITTER_USER_FROM_PARENT);
        udf.setUdfMemo(generateUdfMemo(UDF_CDP_STEPPROGRESS,
                "[{\"id\":\"8181816c88683b1801894f980bef6f6a\",\"name\":\"Предварительный анализ\",\"order\":0,\"taskId\":\"8181816c88683b1801894f97fa9a6f43\",\"weight\":0,\"budget\":7200,\"progress\":0,\"description\":\"\",\"status\":\"ACTUAL\",\"workTypeId\":\"402881c2516c220101516c711ff80024\",\"workTypeNorm\":2,\"hrs\":0,\"deletable\":true,\"actualBudget\":0,\"planby\":\"budget\",\"workTypeAsString\":\"[0701] Иное\",\"workTypeDraftAsString\":\"-\",\"draftChanged\":true,\"orderDraft\":0,\"nameDraft\":\"Предварительный анализ\",\"descriptionDraft\":\"\",\"statusDraft\":\"ACTUAL\",\"weightDraft\":0,\"budgetDraft\":7200,\"budgetHrs\":2,\"budgetMinutes\":0,\"workTypeIdDraft\":\"402881c2516c220101516c711ff80024\",\"workTypeNormDraft\":2}]"));
        udf.setUdfUser(generateUdfUser(STDT_HANDLER, SUBMITTER_USER_FROM_PARENT));
        task.refreshUdf(udf);
        devTaskController.performCommonOperation(task, ACCEPT_IN_WORK);
        ApiAsserts.assertThat(devTaskController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isCorrectStatus(STATUS_WORKTASK_INWORK);
    }

    @Test(groups = {"DevTask", "Regression"}, description = "Отложить", dependsOnMethods = "taskStart3")
    public void taskPostpone3() {
        apiController.updateToken(generateAuthToken(SUBMITTER_USER_FROM_PARENT));
        task.setResolution(generateResolution(RESOLUTION_WORK_SUSPENDED_INDEFINITELY));
        udf = refreshUdf();
        udf.setUdfDate(generateUdfDate(UDF_WORKTASK_PLANFD, 0));
        udf.setUdfMemo(generateUdfMemo(UDF_CDP_STEPPROGRESS, "[{}]"));
        task.refreshUdf(udf);
        devTaskController.performCommonOperation(task, Operations.POSTPONE);
        ApiAsserts.assertThat(devTaskController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isCorrectStatus(STATUS_WORKTASK_POSTPONED);
    }

    @Test(groups = {"DevTask", "Regression"}, description = "Принять в работу", dependsOnMethods = "taskPostpone3")
    public void taskStart4() {
        task.refreshUdf();
        devTaskController.performCommonOperation(task, ACCEPT_IN_WORK);
        ApiAsserts.assertThat(devTaskController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isCorrectStatus(STATUS_WORKTASK_INWORK);
    }

    @Test(groups = {"DevTask", "Regression"}, description = "Отклонить", dependsOnMethods = "taskStart4")
    public void taskDecline() {
        apiController.updateToken(generateAuthToken(SUBMITTER_USER_FROM_PARENT));
        task.setResolution(generateResolution(CANNOT_BE_COMPLETED_WITHIN_THE_SPECIFIED_TIME_FRAME));
        udf = refreshUdf();
        udf.setUdfMemo(generateUdfMemo(UDF_CDP_STEPPROGRESS, "[{}]"));
        task.refreshUdf(udf);
        devTaskController.performCommonOperation(task, DECLINE);
        ApiAsserts.assertThat(devTaskController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isCorrectStatus(STATUS_WORKTASK_DECLINED);
    }

    @Test(groups = {"DevTask", "Regression"}, description = "Передать на предварительный анализ", dependsOnMethods = "taskStart4")
    public void taskSubmitForAnalysis() {
        apiController.updateToken(generateAuthToken(SUBMITTER_USER_FROM_PARENT));
        task.setHandlerUser(SUBMITTER_USER_FROM_PARENT);
        task.setDescription(generateString());
        udf = refreshUdf();

        var udfPlanDf = generateUdfDate(UDF_WORKTASK_ANALYSISFD, 10);
        tempDate = udfPlanDf.getDateValue();
        udf.setUdfDate(udfPlanDf);

        udf.setUdfMemo(generateUdfMemo(UDF_CDP_STEPPROGRESS, "[{}]"));
        udf.setUdfUser(generateUdfUser(STDT_HANDLER, SUBMITTER_USER_FROM_PARENT));
        task.refreshUdf(udf);
        devTaskController.performCommonOperation(task, INANALYSIS);
        ApiAsserts.assertThat(devTaskController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isCorrectStatus(STATUS_WORKTASK_ONANALYSIS);
    }

    @Test(groups = {"DevTask", "Regression"}, description = "Коррекция плана", dependsOnMethods = "taskSubmitForAnalysis")
    public void changePlan2() {
        apiController.updateToken(generateAuthToken(SUBMITTER_USER_FROM_PARENT));
        task.setDescription(generateString());
        task.setConfirmed(true);
        udf = refreshUdf();
        udf.setUdfDate(generateUdfDate(UDF_WORKTASK_PLANFD, 0));
        udf.setSecondUdfDate(generateUdfDate(UDF_WORKTASK_PLANTD, 1));
        udf.setThirdUdfDate(generateUdfDate(UDF_WORKTASK_AWAITTD, 5));
        udf.setUdfDouble(generateUdfDouble(UDF_WORKTASK_AWAITBUDGET, 20));
        udf.setSecondUdfDouble(generateUdfDouble(UDF_CDP_NORMBUDGET, 20));
        udf.setUdfMemo(generateUdfMemo(UDF_CDP_STEPPLAN,
                "[{\"orderDraft\":1,\"weightDraft\":1,\"budgetDraft\":72000,\"planby\":\"budget\",\"deletable\":true,\"statusDraft\":\"NEW\",\"nameDraft\":\"312\",\"workTypeIdDraft\":\"402881c2516c220101516c711ff80024\",\"workTypeNormDraft\":20,\"budgetHrs\":\"20\"}]", "{\"autoFinishAnalysis\": \"true\"}"));
        udf.setSecondUdfMemo(generateUdfMemo(UDF_CDP_STEPPROGRESS, "[{\"id\":\"818181698986fc61018995622118431e\",\"name\":\"Предварительный анализ\",\"order\":0,\"taskId\":\"818181698986fc610189956215bd42f1\",\"progress\":0,\"description\":\"\",\"status\":\"DELETE\",\"hrs\":0,\"deletable\":true,\"actualBudget\":0,\"planby\":\"budget\",\"workTypeAsString\":\"-\",\"workTypeDraftAsString\":\"-\",\"draftChanged\":true}]"));
        task.refreshUdf(udf);
        devTaskController.performCommonOperation(task, CHANGE_PLAN);
        ApiAsserts.assertThat(devTaskController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isCorrectStatus(STATUS_WORKTASK_ANALYSISFINISHED);
    }

    @Test(groups = {"DevTask", "Regression"}, description = "Передать в работу", dependsOnMethods = "changePlan2")
    public void taskAssign() {
        apiController.updateToken(generateAuthToken(SUBMITTER_USER_FROM_PARENT));
        task.setDescription(generateString());
        task.setHandlerUser(SUBMITTER_USER_FROM_PARENT);
        udf = refreshUdf();
        udf.setUdfMemo(generateUdfMemo(UDF_WORKTASK_TASKALLOCATION, "{\"planFinishDate\":\"\",\"userLogin\":\"ashlyahtin\",\"allocations\":[]}"));
        udf.setSecondUdfDate(generateUdfDate(UDF_WORKTASK_PLANTD, 1));
        udf.setSecondUdfMemo(generateUdfMemo(UDF_CDP_STEPPROGRESS, "[{}]"));
        udf.setUdfUser(generateUdfUser(STDT_HANDLER, SUBMITTER_USER_FROM_PARENT));

        task.refreshUdf(udf);
        devTaskController.performCommonOperation(task, WORKTASK_ASSIGN);
        ApiAsserts.assertThat(devTaskController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isCorrectStatus(STATUS_WORKTASK_ASSIGNED);
    }

    @Test(groups = {"DevTask", "Regression"}, description = "Принять в работу", dependsOnMethods = "taskAssign")
    public void taskAccept() {
        apiController.updateToken(generateAuthToken(SUBMITTER_USER_FROM_PARENT));
        task.setDescription(generateString());
        task.setHandlerUser(SUBMITTER_USER_FROM_PARENT);
        udf = refreshUdf();
        udf.setUdfMemo(generateUdfMemo(UDF_CDP_STEPPROGRESS, "[{}]"));
        udf.setUdfUser(generateUdfUser(STDT_HANDLER, SUBMITTER_USER_FROM_PARENT));
        task.refreshUdf(udf);
        devTaskController.performCommonOperation(task, ACCEPT_IN_WORK);
        ApiAsserts.assertThat(devTaskController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isCorrectStatus(STATUS_WORKTASK_INWORK);
    }

    @Test(groups = {"DevTask", "Regression"}, description = "Перепланировать", dependsOnMethods = "taskAccept")
    public void taskChangeTime() {
        apiController.updateToken(generateAuthToken(SUBMITTER_USER_FROM_PARENT));
        task.setDescription(generateString());
        task.setConfirmed(false);
        udf = refreshUdf();
        udf.setUdfDate(generateUdfDate(UDF_WORKTASK_AWAITTD, 5));
        udf.setUdfDouble(generateUdfDouble(UDF_WORKTASK_AWAITBUDGET, 25));
        udf.setSecondUdfDouble(generateUdfDouble(UDF_CDP_NORMBUDGET, 25));
        udf.setUdfMemo(generateUdfMemo(UDF_CDP_STEPPLAN,
                "[{\"orderDraft\":1,\"weightDraft\":1,\"budgetDraft\":72000,\"planby\":\"budget\",\"deletable\":true,\"statusDraft\":\"NEW\",\"nameDraft\":\"312\",\"workTypeIdDraft\":\"402881c2516c220101516c711ff80024\",\"workTypeNormDraft\":20,\"budgetHrs\":\"20\"}]"));
        udf.setSecondUdfMemo(generateUdfMemo(UDF_CDP_STEPPROGRESS, "[{}]"));
        udf.setUdfUser(generateUdfUser(UDF_WORKTASK_RESPFOREPLAN, SUBMITTER_USER_FROM_PARENT));
        udf.setUdfList(generateUdfList(UDF_WORKTASK_REASONFOREPLAN, DEVELOPMENT_ERRORS));
        task.refreshUdf(udf);
        devTaskController.performCommonOperation(task, CHANGE_TIME);
        ApiAsserts.assertThat(devTaskController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isCorrectStatus(STATUS_WORKTASK_INWORK);
    }
}
