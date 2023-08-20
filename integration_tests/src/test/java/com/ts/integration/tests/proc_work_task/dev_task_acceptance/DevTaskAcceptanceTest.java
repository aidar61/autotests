package com.ts.integration.tests.proc_work_task.dev_task_acceptance;

import com.ts.common.application.controllers.TrackStudioHttpStatusCodes;
import com.ts.common.application.database.dbEntities.GrTaskDbEntity;
import com.ts.common.application.database.dbTables.GrTaskTable;
import com.ts.common.asserts.ApiAsserts;
import com.ts.common.asserts.CommonAssert;
import com.ts.common.controllers.TaskResponseBody;
import com.ts.common.controllers.dev.DevTaskController;
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
import static com.ts.common.entitites.commonEntities.Udfs.UdfSd.UDF_CDP_WT;
import static com.ts.common.entitites.commonEntities.User.Constants.ABDULLAEV_BAHODIR;
import static com.ts.common.entitites.commonEntities.User.Constants.BABUSHKIN_IVAN;
import static com.ts.common.enums.Operations.*;
import static com.ts.common.enums.TaskStatuses.*;
import static com.ts.common.utils.InitEntities.*;
import static com.ts.common.utils.RandomUtils.generateString;

public class DevTaskAcceptanceTest extends BaseIntegrationTest {

    public DevTaskController devTaskController;
    private String cdpBl;
    private GeneralTask task;
    private String misService;
    private UdfTask productTask;
    private UdfTask bdkuTask;
    private Parent parent;
    private GrTaskTable grTaskTable;
    private GrTaskDbEntity parentTaskFromDb;
    private String expectedCompletionDate;
    private String expectedCompletionDate2;
    private String expectedCompletionDate3;
    private String plannedStartDate;
    private UdfTask sdRequestTask;
    private User creator;
    private User handlerUser;
    private Task catAcceptTask;

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
        cdpBl = devTaskController.getCdpBl();
        var taskSlaBug = (GrTaskDbEntity) grTaskTable.receiveByCategory("CAT_SLABUG");
        sdRequestTask = InitEntities.generateUdfTask(UDF_WORKTASK_SDREQUEST, new Task(taskSlaBug.getTask_id(), taskSlaBug.getTask_number()));
        misService = devTaskController.getMisService();
        var taskEmployees = userController.receiveUserByTask(parent.getNumber());
        creator = userController.receiveUserByRole(taskEmployees, "Менеджер проекта", "root").getForUser();
        handlerUser = userController.receiveUserByRole(taskEmployees, "Участник проекта", creator.getLogin()).getForUser();
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
        udf.setSecondUdfList(generateUdfList(UDF_CDP_ACCEPTANCE, REQBYCREATOR));
        udf.setUdfDate(generateUdfDate(UDF_WORKTASK_ANALYSISFD, 1));
        udf.setSecondUdfString(generateUdfString(UDF_WORKTASK_BRANCH, "hg:apng:default [Аnalitic platform new generation]"));
        udf.setSecondUdfTask(generateUdfTask(UDF_PRODUCT, productTask.getTaskValue()[0]));
        udf.setThirdUdfList(generateUdfList(UDF_WORKTASK_WAYCODEREVIEW, WAY_CODE_REVIEW_NO));
        udf.setFourthUdfList(generateUdfList(UDF_SDFEATURE_GENUSE, GENERAL, "{\"username\":\"babdullayev\",\"name\":\"Абдуллаев Баходир\"}"));
        udf.setThirdUdfString(generateUdfString(UDF_WORKTASK_ANNOTATION, generateString()));
        udf.setFifthUdfList(generateUdfList(UDF_WORKTASK_CHANGEWORKERINRQST, YES_AND_LEAVE_THIS_ROLE_TO_THE_CURRENT_PERFORMER));
        udf.setSixthUdfList(generateUdfList(UDF_WORKTASK_ADDWATCHERINREQST, UDF_WORKTASK_ADDWATCHERINREQST_YES));
        udf.setSeventhUdfList(generateUdfList(UDF_WORKTASK_ADDTRSTWATCHINREQST, UDF_WORKTASK_ADDTRSTWATCHINREQST_YES));
        udf.setFourthUdfTask(generateUdfTask(UDF_BDKU_CONFIGURATION, bdkuTask.getTaskValueSelector()[0]));
        udf.setSixthUdfTask(generateUdfTask(UDF_WORKTASK_DEPENDBF, MTBANK));
        udf.setSeventhUdfTask(generateUdfTask(UDF_SD_LINKEDREQUEST, AKKREDITIVES));
        udf.setFifthUdfTask(sdRequestTask);
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

        expectedCompletionDate = DateUtils.getCurrentDate(0);
        udf.setUdfDate(generateUdfDate(UDF_WORKTASK_AWAITTD, expectedCompletionDate));
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

        var response = apiController.receiveTask(task.getNumber());
        CommonAssert
                .assertThat(response)
                .isCorrectUdfDouble(UDF_WORKTASK_PLANBUDGET, 2)
                .isCorrectUdfDouble(UDF_CDP_NORMBUDGET, 2)
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

    @Test(groups = {"DevTask", "Regression"}, description = "Передать на приёмку", dependsOnMethods = "taskStart")
    public void taskAcceptance() {
        apiController.updateToken(generateAuthToken(handlerUser));
        udf = refreshUdf();
        task.setConfirmed(true);
        task.setHandlerUser(creator);

        udf.setUdfMemo(generateUdfMemo(UDF_WORKTASK_TESTPLAN, generateString()));
        udf.setUdfList(generateUdfList(UDF_SDFEATURE_DOCREVISION, DOC_REVISION_YES));
        udf.setSecondUdfMemo(generateUdfMemo(UDF_WORKTASK_FILES, "le1.txt, file2.tx"));
        udf.setThirdUdfMemo(generateUdfMemo(UDF_CDP_STEPPROGRESS, "[{}]"));
        udf.setUdfUser(generateUdfUser(STDT_HANDLER, creator));

        task.refreshUdf(udf);
        devTaskController.performCommonOperation(task, WORKTASK_TO_ACCEPTANCE);
        ApiAsserts.assertThat(devTaskController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isCorrectStatus(STATUS_WORKTASK_CLOSED);

        var response = apiController.receiveSubTask(task.getNumber());
        var subTasks = response.jsonPath()
                .getList("tasks", com.ts.common.entitites.tasks.Task.class);
        var acceptTask = subTasks.stream().filter(x -> x.getCategory().getId().equals("CAT_ACCEPTTASK")).findFirst().get();
        catAcceptTask = new Task(acceptTask.getId(), acceptTask.getNumber());
        CommonAssert
                .assertThat(response)
                .isCorrectSubTaskStatus("CAT_ACCEPTTASK", STATUS_WORKTASK_ASSIGNED);

        var responseTask = apiController.receiveTask(task.getNumber());
        CommonAssert.assertThat(responseTask)
                .isCorrectUdfList(UDF_CDP_ACCEPTANCE_STATUS, UDF_CDP_ACCEPTANCE_STATUS_ACCEPTANCE.id);

    }

    @Test(groups = {"DevTask", "Regression"}, description = "Проверить CAT_ACCEPTTASK", dependsOnMethods = "taskAcceptance")
    public void checkAcceptTask() {
        apiController.updateToken(generateAuthToken(creator));
        var response = apiController.receiveTask(catAcceptTask.getNumber());
        CommonAssert
                .assertThat(response)
                .isCorrectTaskName("Приёмка доработки: Создание запроса на разработку")
                .isCorrectTaskDescription("Создана автоматически при закрытии задачи", task.getNumber())
                .isCorrectHandlerUser(creator.getLogin());
    }

    @Test(groups = {"DevTask", "Regression"}, description = "Принять в работу CAT_ACCEPTTASK", dependsOnMethods = "checkAcceptTask")
    public void startAcceptTask() {
        apiController.updateToken(generateAuthToken(creator));
        udf = refreshUdf();
        var subTask = new GeneralTask();
        subTask.setAttachments(new String[0]);
        subTask.setDescription(generateString());
        subTask.setId(catAcceptTask.getId());
        subTask.setNumber(catAcceptTask.getNumber());
        subTask.setHandlerUser(handlerUser);
        udf.setUdfDate(generateUdfDate(UDF_WORKTASK_PLANTD, 1));
        udf.setUdfDouble(generateUdfDouble(UDF_WORKTASK_PLANBUDGET, 8));
        udf.setUdfUser(generateUdfUser(STDT_HANDLER, handlerUser));

        subTask.refreshUdf(udf);

        devTaskController.performCommonOperation(subTask, ACCEPT_IN_WORK);
        ApiAsserts.assertThat(devTaskController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isCorrectStatus(STATUS_WORKTASK_INWORK);
    }

    @Test(groups = {"DevTask", "Regression"}, description = "Закончить приёмку CAT_ACCEPTTASK", dependsOnMethods = "startAcceptTask")
    public void completeAcceptTask() {
        apiController.updateToken(generateAuthToken(creator));
        udf = refreshUdf();
        var subTask = new GeneralTask();
        subTask.setId(catAcceptTask.getId());
        subTask.setNumber(catAcceptTask.getNumber());
        subTask.setAttachments(new String[0]);
        subTask.refreshUdf(udf);
        devTaskController.performCommonOperation(subTask, WORKTASK_FINISHACCEPT);
        ApiAsserts.assertThat(devTaskController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isCorrectStatus(STATUS_WORKTASK_CLOSED);
    }

    @Test(groups = {"DevTask", "Regression"}, description = "Проверить CAT_DEVTASK", dependsOnMethods = "completeAcceptTask")
    public void checkCatDevTask() {
        apiController.updateToken(generateAuthToken(creator));
        var devTask = apiController.receiveTask(task.getNumber());
        CommonAssert
                .assertThat(devTask)
                .isCorrectUdfListCode(UDF_CDP_ACCEPTANCE_STATUS, "ACCEPTED");
    }
}
