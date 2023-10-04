package com.ts.integration.tests.proc_work_task.dev_task_replan;

import com.ts.common.application.controllers.TrackStudioHttpStatusCodes;
import com.ts.common.application.database.dbEntities.GrTaskDbEntity;
import com.ts.common.application.database.dbTables.GrTaskTable;
import com.ts.common.asserts.ApiAsserts;
import com.ts.common.asserts.CommonAssert;
import com.ts.common.controllers.TaskResponseBody;
import com.ts.common.controllers.workTask.DevTaskController;
import com.ts.common.entitites.commonEntities.Parent;
import com.ts.common.entitites.commonEntities.Task;
import com.ts.common.entitites.commonEntities.User;
import com.ts.common.entitites.commonEntities.udf.UdfTask;
import com.ts.common.entitites.tasks.GeneralTask;
import com.ts.common.enums.Operations;
import com.ts.common.enums.TaskType;
import com.ts.common.utils.DateUtils;
import com.ts.common.utils.InitEntities;
import com.ts.integration.tests.BaseIntegrationTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static com.ts.common.application.database.DbQueryHelper.Operators.*;
import static com.ts.common.entitites.commonEntities.List.Constants.*;
import static com.ts.common.entitites.commonEntities.Task.Constants.*;
import static com.ts.common.entitites.commonEntities.Udfs.UdfSd.*;
import static com.ts.common.entitites.commonEntities.User.Constants.ABDULLAEV_BAHODIR;
import static com.ts.common.entitites.commonEntities.User.Constants.BABUSHKIN_IVAN;
import static com.ts.common.enums.Operations.*;
import static com.ts.common.enums.Resolutions.*;
import static com.ts.common.enums.TaskStatuses.*;
import static com.ts.common.utils.InitEntities.*;
import static com.ts.common.utils.InitEntities.generateUdfTask;
import static com.ts.common.utils.RandomUtils.generateString;

public class DevTaskReplanTest extends BaseIntegrationTest {
    public DevTaskController devTaskController;
    private GeneralTask task;
    private String misService;
    private String cdpBl;
    private UdfTask productTask;
    private UdfTask bdkuTask;
    private Parent parent;
    private GrTaskTable grTaskTable;
    private GrTaskDbEntity parentTaskFromDb;
    private String expectedCompletionDate;
    private String expectedCompletionDate2;
    private String expectedCompletionDate3;
    private String plannedStartDate;

    private UdfTask customerRequest;
    private User creator;
    private User handlerUser;
    private Integer estimationLaborInput;
    private Integer initialAssessmentLaborIntensity;


    @BeforeClass(alwaysRun = true)
    public void beforeClass() {
        devTaskController = apiController.getDevTaskController();
        userController = apiController.getUserController();
        grTaskTable = dbHelper.getGrTaskTable();
        parentTaskFromDb = (GrTaskDbEntity) grTaskTable.receiveRandomTask(
                "task_category", EQUAL.operator, "CAT_GENPLAN",
                AND.operator,
                "task_status", EQUAL.operator, STATUS_PROJECT_PLANNED.name(),
                AND.operator,
                "task_path", LIKE.operator, "%/2405/758009%");
        parent = InitEntities.generateParent(parentTaskFromDb.getTask_id(), parentTaskFromDb.getTask_number());
        task = InitEntities.getGeneralTask(TaskType.DEV_TASK, Operations.CAT);
        var tasks = devTaskController.getTaskForSDRequest(parent.getNumber());
        productTask = tasks.get("UDF_PRODUCT");
        bdkuTask = tasks.get("UDF_BDKU_CONFIGURATION");
        var taskSlaBug = (GrTaskDbEntity) grTaskTable.receiveByCategory("CAT_SLABUG");
        customerRequest = InitEntities.generateUdfTask(UDF_WORKTASK_SDREQUEST, new Task(taskSlaBug.getTask_id(), taskSlaBug.getTask_number()));
        misService = devTaskController.getMisService().get(0);
        cdpBl = devTaskController.getCdpBl();
        System.out.println(parent);
        System.out.println(parent);
        var employees = userController.receiveUserByTask(parent.getNumber());
        creator = employees.stream().filter(f -> f.getAssignedRole().getName().equals("Менеджер проекта")).findFirst().get().getForUser();
        handlerUser = employees.stream().filter(f -> f.getAssignedRole().getName().equals("Участник проекта") && !f.getForUser().getLogin().equals(creator.getLogin())).findFirst().get().getForUser();
    }


    @Test(groups = {"DevTask", "Regression"}, description = "Создание запроса на разработку")
    public void devTask() {
        apiController.updateToken(InitEntities.generateAuthToken(creator));
        task.setParent(parent);
        task.setName("Создание запроса на разработку");
        task.setDescription(task.getDescription() + generateString());
        task.setHandlerUser(handlerUser);

        udf = refreshUdf();
        udf.setEighthUdfList(generateUdfList(UDF_WORKTASK_COMPLEXITYLEVEL, TASK_LEVEL_7));
        udf.setUdfUser(generateUdfUser(UDF_WORKTASK_SUPERVISER, ABDULLAEV_BAHODIR));
        udf.setSecondUdfUser(generateUdfUser(UDF_WATCHER, BABUSHKIN_IVAN));
        if (cdpBl != null) {
            udf.setUdfList(generateUdfList(UDF_CDP_BL, cdpBl));
        }
        udf.setUdfTask(generateUdfTask(UDF_SD_MODULE, CORE));
        udf.setUdfString(generateUdfString(UDF_SD_NOMODULE_REASON, generateString()));
        udf.setSecondUdfList(generateUdfList(UDF_CDP_ACCEPTANCE, UDF_CDP_ACCEPTANCE_NO));
        udf.setUdfDate(generateUdfDate(UDF_WORKTASK_ANALYSISFD, 1));
        udf.setSecondUdfString(generateUdfString(UDF_WORKTASK_BRANCH, "hg:apng:default [Аnalitic platform new generation]"));
        udf.setSecondUdfTask(generateUdfTask(UDF_PRODUCT, productTask.getTaskValue()[0]));
        udf.setThirdUdfList(generateUdfList(UDF_WORKTASK_WAYCODEREVIEW, WAY_CODE_REVIEW_OFF));
        udf.setFourthUdfList(generateUdfList(UDF_SDFEATURE_GENUSE, GENERAL, "{\"username\":\"babdullayev\",\"name\":\"Абдуллаев Баходир\"}"));
        udf.setThirdUdfString(generateUdfString(UDF_WORKTASK_ANNOTATION, generateString()));
        udf.setFifthUdfList(generateUdfList(UDF_WORKTASK_CHANGEWORKERINRQST, YES_AND_LEAVE_THIS_ROLE_TO_THE_CURRENT_PERFORMER));
        udf.setSixthUdfList(generateUdfList(UDF_WORKTASK_ADDWATCHERINREQST, UDF_WORKTASK_ADDWATCHERINREQST_YES));
        udf.setSeventhUdfList(generateUdfList(UDF_WORKTASK_ADDTRSTWATCHINREQST, UDF_WORKTASK_ADDTRSTWATCHINREQST_YES));
        udf.setFourthUdfTask(generateUdfTask(UDF_BDKU_CONFIGURATION, bdkuTask.getTaskValueSelector()[0]));
        udf.setSixthUdfTask(generateUdfTask(UDF_WORKTASK_DEPENDBF, MTBANK));
        udf.setSeventhUdfTask(generateUdfTask(UDF_SD_LINKEDREQUEST, AKKREDITIVES));
        udf.setFifthUdfTask(customerRequest);
        if (misService != null)
            udf.setNinethUdfList(generateUdfList(UDF_MIS_SERVICE, misService));
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
        apiController.updateToken(generateAuthToken(handlerUser));
        udf = refreshUdf();

        expectedCompletionDate = DateUtils.getCurrentDate(1);
        estimationLaborInput = 2;
        initialAssessmentLaborIntensity = 2;
        udf.setUdfDate(generateUdfDate(UDF_WORKTASK_AWAITTD, expectedCompletionDate));
        udf.setUdfDouble(generateUdfDouble(UDF_WORKTASK_AWAITBUDGET, estimationLaborInput));
        udf.setSecondUdfDouble(generateUdfDouble(UDF_CDP_NORMBUDGET, initialAssessmentLaborIntensity));
        udf.setUdfMemo(generateUdfMemo(UDF_CDP_STEPPLAN,
                "[{\"id\":\"8181816c88683b1801894f980bef6f6a\",\"name\":\"Предварительный анализ\",\"order\":0,\"taskId\":\"" + task.getId() +
                        "\",\"weight\":0,\"budget\":7200,\"progress\":0,\"description\":\"\",\"status\":\"ACTUAL\",\"workTypeId\":\"402881c2516c220101516c711ff80024\",\"workTypeNorm\":2,\"hrs\":0,\"deletable\":true,\"actualBudget\":0,\"planby\":\"budget\",\"workTypeAsString\":\"[0701] Иное\",\"workTypeDraftAsString\":\"-\",\"draftChanged\":true,\"orderDraft\":0,\"nameDraft\":\"Предварительный анализ\",\"descriptionDraft\":\"\",\"statusDraft\":\"ACTUAL\",\"weightDraft\":0,\"budgetDraft\":7200,\"budgetHrs\":2,\"budgetMinutes\":0,\"workTypeIdDraft\":\"402881c2516c220101516c711ff80024\",\"workTypeNormDraft\":2}]",
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

        var response = apiController.receiveTask(task.getNumber());
        CommonAssert
                .assertThat(response)
                .isCorrectUdfDouble("Оценка трудоемкости", UDF_WORKTASK_PLANBUDGET, 2)
                .isCorrectUdfDouble("Трудоемкость по нормам", UDF_CDP_NORMBUDGET, 2)
                .isCorrectUdfDate(UDF_WORKTASK_PLANTD, expectedCompletionDate);
    }

    @Test(groups = {"DevTask", "Regression"}, description = "Принять в работу", dependsOnMethods = "changePlan")
    public void taskStart() {
        apiController.updateToken(generateAuthToken(handlerUser));
        task.setHandlerUser(handlerUser);
        task.refreshUdf();
        udf.setUdfMemo(generateUdfMemo(UDF_CDP_STEPPROGRESS, "[{}]"));
        udf.setUdfUser(generateUdfUser(STDT_HANDLER, handlerUser));
        devTaskController.performCommonOperation(task, ACCEPT_IN_WORK);
        ApiAsserts.assertThat(devTaskController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isCorrectStatus(STATUS_WORKTASK_INWORK);
    }

    @Test(groups = {"DevTask", "Regression"}, description = "Комментарий", dependsOnMethods = "taskStart")
    public void taskComment() {
        apiController.updateToken(generateAuthToken(handlerUser));
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
        apiController.updateToken(generateAuthToken(handlerUser));
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
                .isCorrectResolution(RESOLUTION_DEPENDS_ON_ANOTHER_TASK)
                .isCorrectStatus(STATUS_WORKTASK_POSTPONED);

        var response = apiController.receiveTask(task.getNumber());
        CommonAssert
                .assertThat(response)
                .isCorrectUdfTask(UDF_WORKTASK_DEPENDBF, MTBANK);
    }

    @Test(groups = {"DevTask", "Regression"}, description = "Принять в работу", dependsOnMethods = "taskPostpone")
    public void taskStart2() {
        apiController.updateToken(generateAuthToken(handlerUser));
        task.setHandlerUser(handlerUser);
        task.refreshUdf();
        udf.setUdfMemo(generateUdfMemo(UDF_CDP_STEPPROGRESS, "[{}]"));
        udf.setUdfUser(generateUdfUser(STDT_HANDLER, handlerUser));
        devTaskController.performCommonOperation(task, ACCEPT_IN_WORK);
        ApiAsserts.assertThat(devTaskController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isCorrectStatus(STATUS_WORKTASK_INWORK);
    }

    @Test(groups = {"DevTask", "Regression"}, description = "Отложить", dependsOnMethods = "taskStart2")
    public void taskPostpone2() {
        apiController.updateToken(generateAuthToken(handlerUser));
        task.setResolution(generateResolution(RESOLUTION_AWAITS_UNTIL_DATE));
        task.setDescription(generateString());
        udf = refreshUdf();
        plannedStartDate = DateUtils.getCurrentDate(5);
        udf.setUdfDate(generateUdfDate(UDF_WORKTASK_PLANFD, plannedStartDate));
        udf.setUdfTask(generateUdfTask(UDF_WORKTASK_DEPENDBF, MTBANK));
        udf.setUdfMemo(generateUdfMemo(UDF_CDP_STEPPROGRESS, "[{}]"));
        task.refreshUdf(udf);
        devTaskController.performCommonOperation(task, Operations.POSTPONE);
        ApiAsserts.assertThat(devTaskController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isCorrectResolution(RESOLUTION_AWAITS_UNTIL_DATE)
                .isCorrectStatus(STATUS_WORKTASK_POSTPONED);

        var response = apiController.receiveTask(task.getNumber());
        CommonAssert
                .assertThat(response)
                .isCorrectUdfDate(UDF_WORKTASK_PLANFD, plannedStartDate);
    }

    @Test(groups = {"DevTask", "Regression"}, description = "Принять в работу", dependsOnMethods = "taskPostpone2")
    public void taskStart3() {
        apiController.updateToken(generateAuthToken(handlerUser));
        udf = refreshUdf();
        task.setHandlerUser(handlerUser);
        udf.setUdfMemo(generateUdfMemo(UDF_CDP_STEPPROGRESS,
                "[{\"id\":\"8181816c88683b1801894f980bef6f6a\",\"name\":\"Предварительный анализ\",\"order\":0,\"taskId\":\"8181816c88683b1801894f97fa9a6f43\",\"weight\":0,\"budget\":7200,\"progress\":0,\"description\":\"\",\"status\":\"ACTUAL\",\"workTypeId\":\"402881c2516c220101516c711ff80024\",\"workTypeNorm\":2,\"hrs\":0,\"deletable\":true,\"actualBudget\":0,\"planby\":\"budget\",\"workTypeAsString\":\"[0701] Иное\",\"workTypeDraftAsString\":\"-\",\"draftChanged\":true,\"orderDraft\":0,\"nameDraft\":\"Предварительный анализ\",\"descriptionDraft\":\"\",\"statusDraft\":\"ACTUAL\",\"weightDraft\":0,\"budgetDraft\":7200,\"budgetHrs\":2,\"budgetMinutes\":0,\"workTypeIdDraft\":\"402881c2516c220101516c711ff80024\",\"workTypeNormDraft\":2}]"));
        udf.setUdfUser(generateUdfUser(STDT_HANDLER, handlerUser));
        task.refreshUdf(udf);
        devTaskController.performCommonOperation(task, ACCEPT_IN_WORK);
        ApiAsserts.assertThat(devTaskController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isCorrectStatus(STATUS_WORKTASK_INWORK);

        var response = apiController.receiveTask(task.getNumber());
        CommonAssert
                .assertThat(response)
                .isCorrectUdfDate(UDF_WORKTASK_PLANFD, plannedStartDate);
    }

    @Test(groups = {"DevTask", "Regression"}, description = "Отложить", dependsOnMethods = "taskStart3")
    public void taskPostpone3() {
        apiController.updateToken(generateAuthToken(handlerUser));
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
                .isCorrectResolution(RESOLUTION_WORK_SUSPENDED_INDEFINITELY)
                .isCorrectStatus(STATUS_WORKTASK_POSTPONED);
    }

    @Test(groups = {"DevTask", "Regression"}, description = "Принять в работу", dependsOnMethods = "taskPostpone3")
    public void taskStart4() {
        apiController.updateToken(generateAuthToken(handlerUser));
        task.setHandlerUser(handlerUser);
        task.refreshUdf();
        udf.setUdfMemo(generateUdfMemo(UDF_CDP_STEPPROGRESS, "[{}]"));
        udf.setUdfUser(generateUdfUser(STDT_HANDLER, handlerUser));
        devTaskController.performCommonOperation(task, ACCEPT_IN_WORK);
        ApiAsserts.assertThat(devTaskController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isCorrectStatus(STATUS_WORKTASK_INWORK);
    }

    @Test(groups = {"DevTask", "Regression"}, description = "Отклонить", dependsOnMethods = "taskStart4")
    public void taskDecline() {
        apiController.updateToken(generateAuthToken(handlerUser));
        task.setResolution(generateResolution(CANNOT_BE_COMPLETED_WITHIN_THE_SPECIFIED_TIME_FRAME));
        udf = refreshUdf();
        udf.setUdfMemo(generateUdfMemo(UDF_CDP_STEPPROGRESS, "[{}]"));
        task.refreshUdf(udf);
        devTaskController.performCommonOperation(task, DECLINE);
        ApiAsserts.assertThat(devTaskController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isCorrectResolution(CANNOT_BE_COMPLETED_WITHIN_THE_SPECIFIED_TIME_FRAME)
                .isCorrectHandlerUser(creator)
                .isCorrectStatus(STATUS_WORKTASK_DECLINED);
    }

    @Test(groups = {"DevTask", "Regression"}, description = "Передать на предварительный анализ", dependsOnMethods = "taskStart4")
    public void taskSubmitForAnalysis() {
        apiController.updateToken(generateAuthToken(creator));
        task.setHandlerUser(handlerUser);
        task.setDescription(generateString());
        udf = refreshUdf();

        var analysisCompletionDate = DateUtils.getCurrentDate(10);
        udf.setUdfDate(generateUdfDate(UDF_WORKTASK_ANALYSISFD, analysisCompletionDate));
        udf.setUdfMemo(generateUdfMemo(UDF_CDP_STEPPROGRESS, "[{}]"));
        udf.setUdfUser(generateUdfUser(STDT_HANDLER, handlerUser));
        udf.setUdfList(generateUdfList(UDF_WORKTASK_CHANGEWORKERINRQST, YES_AND_LEAVE_THIS_ROLE_TO_THE_CURRENT_PERFORMER));
        task.refreshUdf(udf);
        devTaskController.performCommonOperation(task, INANALYSIS);
        ApiAsserts.assertThat(devTaskController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isCorrectHandlerUser(handlerUser)
                .isCorrectStatus(STATUS_WORKTASK_ONANALYSIS);

        var response = apiController.receiveTask(task.getNumber());
        CommonAssert
                .assertThat(response)
                .isCorrectUdfDate(UDF_WORKTASK_ANALYSISFD, analysisCompletionDate);
    }

    @Test(groups = {"DevTask", "Regression"}, description = "Коррекция плана", dependsOnMethods = "taskSubmitForAnalysis")
    public void changePlan2() {
        apiController.updateToken(generateAuthToken(handlerUser));
        task.setDescription(generateString());
        task.setConfirmed(true);
        udf = refreshUdf();
        var expectedAwaitedDate = DateUtils.getCurrentDate(5);
        udf.setUdfDate(generateUdfDate(UDF_WORKTASK_PLANFD, 0));
        udf.setSecondUdfDate(generateUdfDate(UDF_WORKTASK_PLANTD, 1));
        udf.setThirdUdfDate(generateUdfDate(UDF_WORKTASK_AWAITTD, expectedAwaitedDate));
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

        var response = apiController.receiveTask(task.getNumber());
        CommonAssert
                .assertThat(response)
                .isCorrectUdfDate(UDF_WORKTASK_PLANTD, expectedCompletionDate)
                .isCorrectUdfDouble("Оценка трудоемкости", UDF_WORKTASK_PLANBUDGET, estimationLaborInput)
                .isCorrectUdfDouble("Первоначальная оценка трудоёмкости", UDF_WORKTASK_FIRSTPLANBUDGET, initialAssessmentLaborIntensity)
                .isCorrectUdfDouble("Оценка трудоемкости исполнителем", UDF_WORKTASK_AWAITBUDGET, 20)
                .isCorrectUdfDouble("Трудоемкость по нормам", UDF_CDP_NORMBUDGET, 20)
                .isCorrectUdfDate(UDF_WORKTASK_AWAITTD, expectedAwaitedDate);
    }

    @Test(groups = {"DevTask", "Regression"}, description = "Передать в работу", dependsOnMethods = "changePlan2")
    public void taskAssign() {
        apiController.updateToken(generateAuthToken(creator));
        task.setDescription(generateString());
        task.setHandlerUser(handlerUser);
        udf = refreshUdf();
        udf.setUdfList(generateUdfList(UDF_WORKTASK_CHANGEWORKERINRQST, YES_AND_LEAVE_THIS_ROLE_TO_THE_CURRENT_PERFORMER));
        udf.setUdfMemo(generateUdfMemo(UDF_WORKTASK_TASKALLOCATION, "{\"planFinishDate\":\"\",\"userLogin\":\"" + handlerUser.getLogin() +
                "\",\"allocations\":[]}"));
        expectedCompletionDate2 = DateUtils.getCurrentDate(1);
        udf.setSecondUdfDate(generateUdfDate(UDF_WORKTASK_PLANTD, expectedCompletionDate2));
        udf.setSecondUdfMemo(generateUdfMemo(UDF_CDP_STEPPROGRESS, "[{}]"));
        udf.setUdfUser(generateUdfUser(STDT_HANDLER, handlerUser));

        task.refreshUdf(udf);
        devTaskController.performCommonOperation(task, WORKTASK_ASSIGN);
        ApiAsserts.assertThat(devTaskController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isCorrectStatus(STATUS_WORKTASK_ASSIGNED);

        var response = apiController.receiveTask(task.getNumber());
        CommonAssert
                .assertThat(response)
                .isCorrectUdfDate(UDF_WORKTASK_PLANTD, expectedCompletionDate2);
    }

    @Test(groups = {"DevTask", "Regression"}, description = "Принять в работу", dependsOnMethods = "taskAssign")
    public void taskAccept() {
        apiController.updateToken(generateAuthToken(handlerUser));
        task.setDescription(generateString());
        task.setHandlerUser(handlerUser);
        udf = refreshUdf();
        udf.setUdfMemo(generateUdfMemo(UDF_CDP_STEPPROGRESS, "[{}]"));
        udf.setUdfUser(generateUdfUser(STDT_HANDLER, handlerUser));
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
        apiController.updateToken(generateAuthToken(handlerUser));
        task.setDescription(generateString());
        task.setConfirmed(false);
        udf = refreshUdf();

        expectedCompletionDate3 = DateUtils.getCurrentDate(7);
        udf.setUdfDate(generateUdfDate(UDF_WORKTASK_AWAITTD, expectedCompletionDate3));
        udf.setUdfDouble(generateUdfDouble(UDF_WORKTASK_AWAITBUDGET, 25));
        udf.setSecondUdfDouble(generateUdfDouble(UDF_CDP_NORMBUDGET, 25));
        udf.setUdfMemo(generateUdfMemo(UDF_CDP_STEPPLAN,
                "[{\"id\":\"81818169899b318a0189a6bd18920e95\",\"name\":\"312\",\"order\":1,\"taskId\":\"" + task.getId() +
                        "\",\"weight\":1,\"budget\":72000,\"progress\":0,\"description\":\"\",\"status\":\"ACTUAL\",\"workTypeId\":\"402881c2516c220101516c711ff80024\",\"workTypeNorm\":20,\"hrs\":0,\"deletable\":true,\"actualBudget\":0,\"planby\":\"budget\",\"workTypeAsString\":\"[0701] Иное\",\"workTypeDraftAsString\":\"-\",\"draftChanged\":true,\"orderDraft\":1,\"nameDraft\":\"312\",\"descriptionDraft\":\"\",\"statusDraft\":\"ACTUAL\",\"weightDraft\":1,\"budgetDraft\":90000,\"budgetHrs\":\"25\",\"budgetMinutes\":0,\"workTypeIdDraft\":\"402881c2516c220101516c711ff80024\",\"workTypeNormDraft\":25}]"));
        udf.setSecondUdfMemo(generateUdfMemo(UDF_CDP_STEPPROGRESS, "[{\"id\":\"81818169899b318a0189a6bcc8dc0e17\",\"name\":\"Предварительный анализ\"," +
                "\"order\":0,\"taskId\":\"" + task.getId() +
                "\",\"progress\":0,\"description\":\"\",\"status\":\"DELETE\",\"hrs\":0," +
                "\"deletable\":true,\"actualBudget\":0,\"planby\":\"budget\",\"workTypeAsString\":\"-\",\"workTypeDraftAsString\":\"-\",\"draftChanged\":true}," +
                "{\"id\":\"81818169899b318a0189a6bd18920e95\",\"name\":\"312\",\"order\":1,\"taskId\":\"" + task.getId() +
                "\",\"weight\":1,\"budget\":72000," +
                "\"progress\":0,\"description\":\"\",\"status\":\"ACTUAL\",\"workTypeId\":\"402881c2516c220101516c711ff80024\",\"workTypeNorm\":20.0,\"hrs\":0,\"deletable\":true," +
                "\"actualBudget\":0,\"planby\":\"budget\",\"workTypeAsString\":\"[0701] Иное\",\"workTypeDraftAsString\":\"-\",\"draftChanged\":true}]"));
        udf.setUdfUser(generateUdfUser(UDF_WORKTASK_RESPFOREPLAN, handlerUser));
        udf.setUdfList(generateUdfList(UDF_WORKTASK_REASONFOREPLAN, DEVELOPMENT_ERRORS));
        task.refreshUdf(udf);
        devTaskController.performCommonOperation(task, CHANGE_TIME);
        ApiAsserts.assertThat(devTaskController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isCorrectStatus(STATUS_WORKTASK_INWORK);

        var response = apiController.receiveTask(task.getNumber());
        CommonAssert
                .assertThat(response)
                .isCorrectUdfList(UDF_WORKTASK_INREPLAN, UDF_WORKTASK_INREPLAN_YES.id);
    }

    @Test(groups = {"DevTask", "Regression"}, description = "Согласовать перепланирование", dependsOnMethods = "taskChangeTime")
    public void taskAgreeChangeTime() {
        apiController.updateToken(generateAuthToken(creator));
        task.setDescription(generateString());

        udf = refreshUdf();
        udf.setUdfMemo(generateUdfMemo(UDF_WORKTASK_TASKALLOCATION, "{\"planFinishDate\":\"\",\"userLogin\":\"" + handlerUser.getLogin() +
                "\",\"allocations\":[]}"));
        udf.setUdfDate(generateUdfDate(UDF_WORKTASK_PLANTD, expectedCompletionDate2));
        udf.setSecondUdfMemo(generateUdfMemo(UDF_CDP_STEPPROGRESS, "[{}]"));
        task.refreshUdf(udf);
        devTaskController.performCommonOperation(task, WORKTASK_CONFORMREPLAN);
        ApiAsserts.assertThat(devTaskController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isCorrectStatus(STATUS_WORKTASK_INWORK);

        var response = apiController.receiveTask(task.getNumber());
        CommonAssert
                .assertThat(response)
                .isCorrectUdfDate(UDF_WORKTASK_AWAITTD, expectedCompletionDate3)
                .isCorrectUdfDate(UDF_WORKTASK_PLANTD, expectedCompletionDate2);
    }

    @Test(groups = {"DevTask", "Regression"}, description = "Закончить разработку", dependsOnMethods = "taskAgreeChangeTime")
    public void completeTask() {
        apiController.updateToken(generateAuthToken(handlerUser));

        udf = refreshUdf();
        var randomText = generateString();
        udf.setUdfMemo(generateUdfMemo(UDF_WORKTASK_TESTPLAN, randomText));
        udf.setUdfList(generateUdfList(UDF_SDFEATURE_DOCREVISION, DOC_REVISION_NO));
        udf.setSecondUdfMemo(generateUdfMemo(UDF_CDP_STEPPROGRESS, "[{}]"));
        task.setConfirmed(true);
        task.refreshUdf(udf);
        devTaskController.performCommonOperation(task, WORKTASK_FINISHDEV);
        ApiAsserts.assertThat(devTaskController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isCorrectStatus(STATUS_WORKTASK_CLOSED);

        var response = apiController.receiveTask(task.getNumber());
        CommonAssert
                .assertThat(response)
                .isCorrectUdfMemo(UDF_WORKTASK_TESTPLAN, randomText)
                .isCorrectUdfList(UDF_SDFEATURE_DOCREVISION, DOC_REVISION_NO.id);
    }
}
