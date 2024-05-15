package com.ts.integration.tests.proc_work_task.tech_task;

import com.ts.common.application.controllers.TrackStudioHttpStatusCodes;
import com.ts.common.application.database.dbEntities.GrTaskDbEntity;
import com.ts.common.application.database.dbTables.GrTaskTable;
import com.ts.common.asserts.ApiAsserts;
import com.ts.common.asserts.CommonAssert;
import com.ts.common.controllers.TaskResponseBody;
import com.ts.common.controllers.workTask.TechTaskController;
import com.ts.common.entitites.commonEntities.Parent;
import com.ts.common.entitites.commonEntities.Task;
import com.ts.common.entitites.commonEntities.User;
import com.ts.common.entitites.commonEntities.UserData;
import com.ts.common.entitites.commonEntities.udf.UdfTask;
import com.ts.common.entitites.tasks.GeneralTask;
import com.ts.common.enums.Operations;
import com.ts.common.enums.TaskType;
import com.ts.common.enums.Users;
import com.ts.common.utils.DateUtils;
import com.ts.common.utils.ExtractResponseFieldUtils;
import com.ts.common.utils.InitEntities;
import com.ts.common.utils.JsonUtils;
import com.ts.integration.tests.BaseIntegrationTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

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
import static com.ts.common.utils.RandomUtils.generateString;

public class TechTaskBaseTest extends BaseIntegrationTest {
    public TechTaskController techTaskController;
    private GeneralTask task;
    private Parent parent;
    private String expectedCompletionDate;
    private UdfTask customerRequest;
    private User creator;
    private User handlerUser;
    private Integer awaitBudgetValue;
    private String awaitDate;
    private Integer normBudgetValue;
    private Task dependTask;
    private String MIS_SERVICE;
    private String CDP_BL;
    private Task[] PRODUCT;

    @BeforeClass(alwaysRun = true)
    public void beforeClass() {
        techTaskController = apiController.getTechTaskController();
        userController = apiController.getUserController();
        GrTaskTable grTaskTable = dbHelper.getGrTaskTable();
        GrTaskDbEntity parentTaskFromDb = (GrTaskDbEntity) grTaskTable.receiveRandomTask("task_category", EQUAL.operator, "CAT_GENPLAN", AND.operator, "task_status", EQUAL.operator, STATUS_PROJECT_PLANNED.name(), AND.operator, "task_path", LIKE.operator, "%/2405/758009%");
        parent = InitEntities.generateParent(parentTaskFromDb.getTask_id(), parentTaskFromDb.getTask_number());
        task = InitEntities.getGeneralTask(TaskType.CAT_TECHTASK, Operations.CAT);
        var taskSlaBug = (GrTaskDbEntity) grTaskTable.receiveRandomTask("task_category", EQUAL.operator, "CAT_SLABUG", AND.operator, "task_status", NOT_EQUAL.operator, STATUS_PROJECT_PLANNED.name());
        customerRequest = InitEntities.generateUdfTask(UDF_WORKTASK_SDREQUEST, new Task(taskSlaBug.getTask_id(), taskSlaBug.getTask_number()));
        var dependTaskFromDb = (GrTaskDbEntity) grTaskTable.receiveRandomTask("task_category", EQUAL.operator, "CAT_DEVTASK", AND.operator, "task_status", EQUAL.operator, STATUS_WORKTASK_INWORK.name());
        dependTask = new Task(dependTaskFromDb.getTask_id(), dependTaskFromDb.getTask_number());

        var parentPayloadResponse = techTaskController.getParentPayload(parent.getNumber(), "CAT_TECHTASK");
        ApiAsserts.assertThat(parentPayloadResponse).isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK);
        var parentPayload = JsonUtils.removeExtraCharacters(parentPayloadResponse);
        MIS_SERVICE = techTaskController.getParent_UDF_MIS_SERVICE(parentPayload).get(0);
        CDP_BL = techTaskController.getParent_UDF_CDP_BL(parentPayload).get(0);
        PRODUCT = techTaskController.getParent_UDF_PRODUCT(parentPayload);

        //Employees
        var employees = userController.receiveUserByTask(parent.getNumber());
        Collections.shuffle(employees);
        var creators = employees.stream().filter(f -> f.getAssignedRole().getName().equals("Менеджер проекта") && f.getForUser().getActive()).collect(Collectors.toList());
        var handlers = employees.stream().filter(f -> f.getAssignedRole().getName().equals("Участник проекта") && f.getForUser().getActive()).collect(Collectors.toList());
        creator = creators.get(0).getForUser();
        handlerUser = handlers.get(0).getForUser();
        awaitDate = DateUtils.getCurrentDate(0);
        // TODO: 29.11.2023 Check await date
    }


    @Test(groups = {"PROC_WORKTASK", "Regression"}, description = "Создание CAT_TECHTASK")
    public void techTask() {
        udf = refreshUdf();
        task.refreshTask();
        apiController.updateToken(InitEntities.generateAuthToken(creator));
        task.setParent(parent);
        task.setName("Новая задача категории Технологическая задача" + LocalDateTime.now().getNano());
        task.setDescription(task.getDescription() + generateString());
        task.setHandlerUser(handlerUser);
        expectedCompletionDate = DateUtils.getCurrentDate(1);
        udf.setUdfUser(generateUdfUser(UDF_WORKTASK_SUPERVISER, ABDULLAEV_BAHODIR));
        udf.setSecondUdfUser(generateUdfUser(UDF_WATCHER, BABUSHKIN_IVAN));
        if (CDP_BL != null) udf.setUdfList(generateUdfList(UDF_CDP_BL, CDP_BL));
        udf.setUdfTask(generateUdfTask(UDF_SD_MODULE, CORE));
        udf.setUdfString(generateUdfString(UDF_SD_NOMODULE_REASON, generateString()));
        if (MIS_SERVICE != null) udf.setNinethUdfList(generateUdfList(UDF_MIS_SERVICE, MIS_SERVICE));
        udf.setSecondUdfList(generateUdfList(UDF_CDP_ACCEPTANCE, REQBYAUTHOR));
        udf.setSeventhUdfList(generateUdfList(UDF_WORKTASK_ANALYSIS, YES_V2));
        udf.setUdfDate(generateUdfDate(UDF_WORKTASK_ANALYSISFD, DateUtils.getCurrentDate(0)));
        udf.setSecondUdfDate(generateUdfDate(UDF_WORKTASK_PLANTD, expectedCompletionDate));
        Map<Task.Constants, UserData> dependTaskFNC = new HashMap<>();
        var userData1 = new UserData(null, "{\"type\":\"REQUIRED\",\"comment\":\"Required\"}");
        var userData2 = new UserData(null, "{\"type\":\"RECOMMENDED\",\"comment\":\"RECOMMENDED\"}");
        var userData3 = new UserData(null, "{\"type\":\"OPTIONAL\",\"option\":\"949177\",\"optionNot\":\"773226\",\"comment\":\"Optional\"}");
        dependTaskFNC.put(RYSGAL_BANK, userData1);
        dependTaskFNC.put(WORKTASK_TESTTASK, userData2);
        dependTaskFNC.put(CUSTOMER_REQUEST, userData3);
        udf.setEighthUdfTask(generateUdfTask(UDF_WORKTASK_DEPENDTASKFNC, dependTaskFNC));
        udf.setSecondUdfTask(generateUdfTask(UDF_PRODUCT, PRODUCT[0]));
        udf.setThirdUdfList(generateUdfList(UDF_WORKTASK_WAYCODEREVIEW, WAY_CODE_REVIEW_OFF));
        udf.setSeventhUdfTask(generateUdfTask(UDF_SD_LINKEDREQUEST, AKKREDITIVES));
        udf.setFifthUdfTask(customerRequest);
        udf.setFourthUdfList(generateUdfList(UDF_CDP_BP, INVESTMENTS_IN_PRODUCTS));
        udf.setSixthUdfTask(generateUdfTask(UDF_WORKTASK_DEPENDBF, MTBANK));
        udf.setSixthUdfList(generateUdfList(UDF_WORKTASK_CHANGEWORKERINRQST, CHANGEWORKERINRQST_NO));
        task.refreshUdf(udf);
        techTaskController.create(task);
        var response = techTaskController.getResponse();
        ApiAsserts.assertThat(response).isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK).isParseableBody(TaskResponseBody.class).assertTask().isCorrectStatus(STATUS_WORKTASK_ONANALYSIS).isEquals(task);
    }

    @Test(groups = {"PROC_WORKTASK", "Regression"}, description = "Коррекция плана", dependsOnMethods = "techTask")
    public void changePlan() {
        udf = refreshUdf();
        task.refreshTask();
        apiController.updateToken(generateAuthToken(handlerUser));
        task.setDescription(generateString());
        awaitBudgetValue = 4;
        normBudgetValue = 2;
        udf.setUdfDate(generateUdfDate(UDF_WORKTASK_PLANTD, 1));
        udf.setSecondUdfDate(generateUdfDate(UDF_WORKTASK_AWAITTD, awaitDate));
        udf.setUdfDouble(generateUdfDouble(UDF_WORKTASK_AWAITBUDGET, awaitBudgetValue));
        udf.setSecondUdfDouble(generateUdfDouble(UDF_CDP_NORMBUDGET, normBudgetValue));
        udf.setUdfMemo(generateUdfMemo(UDF_CDP_STEPPLAN, "[{\"id\":\"818181698c0f98a5018c14a17d2c0720\",\"name\":\"Предварительный анализ\",\"order\":0,\"taskId\":\"" + task.getId() + "\",\"weight\":0,\"budget\":7200,\"progress\":0,\"description\":\"\",\"status\":\"ACTUAL\",\"workTypeId\":\"402881c2516c220101516c711ff80024\"," + "\"workTypeNorm\":2,\"hrs\":0,\"deletable\":true,\"actualBudget\":0,\"planby\":\"budget\",\"workTypeDraftAsString\":\"-\",\"workTypeAsString\":" + "\"[0701] Иное\",\"draftChanged\":true,\"orderDraft\":0,\"nameDraft\":\"Предварительный анализ\",\"descriptionDraft\":\"\",\"statusDraft\":\"ACTUAL\"," + "\"weightDraft\":0,\"budgetDraft\":7200,\"budgetHrs\":2,\"budgetMinutes\":0,\"workTypeIdDraft\":\"402881c2516c220101516c711ff80024\",\"workTypeNormDraft\":2}," + "{\"orderDraft\":1,\"weightDraft\":1,\"budgetDraft\":7200,\"planby\":\"budget\",\"deletable\":true,\"statusDraft\":\"NEW\",\"nameDraft\":\"Предварительный анализ 1\"," + "\"workTypeIdDraft\":\"402881c2516c220101516c711ff80024\",\"workTypeNormDraft\":2,\"budgetHrs\":\"2\"}]", "{\"autoFinishAnalysis\": \"true\"}"));
        udf.setSecondUdfMemo(generateUdfMemo(UDF_CDP_STEPPROGRESS, "[{\"id\":\"818181698c0f98a5018c14a17d2c0720\",\"name\":\"Предварительный анализ\",\"order\":0,\"taskId\":\"" + task.getId() + "\",\"weight\":0,\"budget\":7200,\"progress\":0,\"description\":\"\",\"status\":\"ACTUAL\",\"workTypeId\":\"402881c2516c220101516c711ff80024\",\"workTypeNorm\":2.0,\"hrs\":0," + "\"deletable\":true,\"actualBudget\":0,\"planby\":\"budget\",\"workTypeDraftAsString\":\"-\",\"workTypeAsString\":\"[0701] Иное\",\"draftChanged\":true}]"));
        task.refreshUdf(udf);
        task.setConfirmed(true);
        techTaskController.performCommonOperation(task, CHANGE_PLAN);
        ApiAsserts.assertThat(techTaskController.getResponse()).isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK).isParseableBody(TaskResponseBody.class).assertTask().isCorrectStatus(STATUS_WORKTASK_ASSIGNED);

        var response = apiController.receiveTask(task.getNumber());
        CommonAssert.assertThat(response).isCorrectUdfDouble("Оценка трудоемкости", UDF_WORKTASK_PLANBUDGET, normBudgetValue).isCorrectUdfDate(UDF_WORKTASK_AWAITTD, awaitDate);
    }

    @Test(groups = {"PROC_WORKTASK", "Regression"}, description = "Принять в работу", dependsOnMethods = "changePlan")
    public void taskStart() {
        udf = refreshUdf();
        task.refreshTask();
        task.setHandlerUser(handlerUser);

        awaitDate = DateUtils.getCurrentDate(1);
        udf.setUdfDate(generateUdfDate(UDF_WORKTASK_PLANTD, expectedCompletionDate));
        awaitBudgetValue++;
        udf.setUdfDouble(generateUdfDouble(UDF_WORKTASK_PLANBUDGET, awaitBudgetValue));
        udf.setUdfList(generateUdfList(UDF_WORKTASK_ADDWATCHERINREQST, UDF_WORKTASK_ADDWATCHERINREQST_YES));
        udf.setUdfMemo(generateUdfMemo(UDF_CDP_STEPPROGRESS, "[{\"id\":\"818181698c0f98a5018c1698494b09e8\",\"name\":\"Предварительный анализ\",\"order\":0,\"taskId\":\"" + task.getId() + "\",\"weight\":0,\"budget\":7200,\"progress\":100,\"description\":\"\",\"status\":\"ACTUAL\",\"workTypeId\":\"402881c2516c220101516c711ff80024\"," + "\"workTypeNorm\":2.0,\"hrs\":0,\"deletable\":true,\"actualBudget\":0,\"planby\":\"budget\",\"workTypeDraftAsString\":\"-\",\"workTypeAsString\":\"[0701] Иное\"," + "\"draftChanged\":true},{\"id\":\"818181698c0f98a5018c169a3c9d0a25\",\"name\":\"Предварительный анализ-1\",\"order\":1,\"taskId\":\"818181698c0f98a5018c16981da209ac\"," + "\"weight\":1,\"budget\":7200,\"progress\":0,\"description\":\"\",\"status\":\"ACTUAL\",\"workTypeId\":\"402881c2516c220101516c711ff80024\",\"workTypeNorm\":2.0,\"hrs\":0," + "\"deletable\":true,\"actualBudget\":0,\"planby\":\"budget\",\"workTypeDraftAsString\":\"-\",\"workTypeAsString\":\"[0701] Иное\",\"draftChanged\":true}]"));
        udf.setUdfUser(generateUdfUser(STDT_HANDLER, handlerUser));
        task.refreshUdf(udf);
        techTaskController.performCommonOperation(task, ACCEPT_IN_WORK);
        ApiAsserts.assertThat(techTaskController.getResponse()).isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK).isParseableBody(TaskResponseBody.class).assertTask().isCorrectStatus(STATUS_WORKTASK_INWORK);

        var response = apiController.receiveTask(task.getNumber());
        CommonAssert.assertThat(response).isCorrectUdfDate(UDF_WORKTASK_PLANTD, expectedCompletionDate).isCorrectUdfDouble("Оценка трудоемкости", UDF_WORKTASK_PLANBUDGET, awaitBudgetValue).isCorrectUdfDouble("Первоначальная оценка трудоёмкости", UDF_WORKTASK_FIRSTPLANBUDGET, awaitBudgetValue);
    }

    @Test(groups = {"PROC_WORKTASK", "Regression"}, description = "Передать на приёмку", dependsOnMethods = "taskStart")
    public void taskAcceptance() {
        apiController.updateToken(generateAuthToken(handlerUser));

        udf = refreshUdf();
        task.setHandlerUser(handlerUser);

        udf.setUdfMemo(generateUdfMemo(UDF_WORKTASK_TESTPLAN, generateString()));
        udf.setUdfList(generateUdfList(UDF_SDFEATURE_DOCREVISION, DOC_REVISION_YES));
        udf.setSecondUdfList(generateUdfList(UDF_SD_MUSTSETTINGINSTAL, MUST_SETTING_INSTAL_YES));
        udf.setSecondUdfMemo(generateUdfMemo(UDF_WORKTASK_FILES, "sample1.pdf, sample.txt"));
        udf.setThirdUdfMemo(generateUdfMemo(UDF_CDP_STEPPROGRESS, "[{\"id\":\"818181698c0f98a5018c1a81d21f0b3f\",\"name\":\"Предварительный анализ\",\"order\":0,\"taskId\":\"" + task.getId() + "\",\"progress\":0,\"description\":\"\",\"status\":\"DELETE\",\"hrs\":0,\"deletable\":true,\"actualBudget\":0,\"planby\":\"budget\",\"workTypeDraftAsString\":\"-\"," + "\"workTypeAsString\":\"-\",\"draftChanged\":true},{\"id\":\"818181698c0f98a5018c1a81e9020b63\",\"name\":\"Предварительный анализ 1\",\"order\":1,\"taskId\":\"818181698c0f98a5018c1a819ad20b0d\"," + "\"weight\":1,\"budget\":7200,\"progress\":0,\"description\":\"\",\"status\":\"ACTUAL\",\"workTypeId\":\"402881c2516c220101516c711ff80024\",\"workTypeNorm\":2.0,\"hrs\":0,\"deletable\":true," + "\"actualBudget\":0,\"planby\":\"budget\",\"workTypeDraftAsString\":\"-\",\"workTypeAsString\":\"[0701] Иное\",\"draftChanged\":true}]"));
        udf.setUdfUser(generateUdfUser(STDT_HANDLER, handlerUser));
        task.refreshUdf(udf);
        techTaskController.performCommonOperation(task, WORKTASK_TO_ACCEPTANCE);
        ApiAsserts.assertThat(techTaskController.getResponse()).isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK).isParseableBody(TaskResponseBody.class).assertTask().isCorrectStatus(STATUS_WORKTASK_CLOSED);

        var childTasks = techTaskController.getBackLinks(task.getNumber());
        var workTaskWork = ExtractResponseFieldUtils.extractThat(childTasks).extractByPath("BACK_UDF_WORKTASK_WORK", UdfTask.class);
        var docTaskNumber = Objects.requireNonNull(Arrays.stream(workTaskWork.getTaskValue()).findFirst().orElse(null)).getNumber();
        apiController.updateToken(generateAuthToken(Users.ROOT));
        var docTask = apiController.receiveTask(docTaskNumber);

        CommonAssert.assertThat(docTask).isCorrectTaskCategory("CAT_DOCTASK").isCorrectTaskStatus(STATUS_WORKTASK_ASSIGNED).isCorrectSubmitterUser("root").isCorrectHandlerUser("wc_gtd").isCorrectUdfList(UDF_CDP_ACCEPTANCE, "ff8081813fce5b48013fce5de6b40002")//Не требуется
                .isCorrectUdfList(UDF_WORKTASK_ANALYSIS, "ff8080812f8cd356012f908c2bd8005a")//Не требуется
                .isCorrectUdfDouble("Оценка трудоемкости", UDF_WORKTASK_PLANBUDGET, 8);

        var workTaskAccept = ExtractResponseFieldUtils.extractThat(childTasks).extractByPath("BACK_UDF_WORKTASK_ACCEPTTASK", UdfTask.class);

        var acceptTaskNumber = Objects.requireNonNull(Arrays.stream(workTaskAccept.getTaskValue()).findFirst().orElse(null)).getNumber();
        var acceptTask = apiController.receiveTask(acceptTaskNumber);

        CommonAssert.assertThat(acceptTask).isCorrectTaskCategory("CAT_ACCEPTTASK")
                .isCorrectTaskStatus(STATUS_WORKTASK_ASSIGNED)
                .isCorrectSubmitterUser(handlerUser.getLogin())
                .isCorrectHandlerUser(creator.getLogin())
                .isCorrectUdfList(UDF_WORKTASK_ANALYSIS, "ff8080812f8cd356012f908c2bd8005a")//Не требуется
                .isCorrectUdfDouble("Оценка трудоемкости", UDF_WORKTASK_PLANBUDGET, 8);
    }

    @Test(groups = {"PROC_WORKTASK", "Regression"}, description = "Вернуть в работу", dependsOnMethods = "taskAcceptance")
    public void taskReturn() {
        apiController.updateToken(generateAuthToken(creator));
        udf = refreshUdf();
        task.setHandlerUser(handlerUser);
        udf.setUdfUser(generateUdfUser(STDT_HANDLER, handlerUser));
        task.refreshUdf(udf);
        techTaskController.performCommonOperation(task, RETURN);
        ApiAsserts.assertThat(techTaskController.getResponse()).isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK).isParseableBody(TaskResponseBody.class).assertTask().isCorrectStatus(STATUS_WORKTASK_ASSIGNED);
    }

    @Test(groups = {"PROC_WORKTASK", "Regression"}, description = "Отклонить", dependsOnMethods = "taskReturn")
    public void taskDecline() {
        apiController.updateToken(generateAuthToken(handlerUser));
        udf = refreshUdf();
        task.setHandlerUser(handlerUser);
        task.setResolution(generateResolution(CANNOT_BE_COMPLETED_WITHIN_THE_SPECIFIED_TIME_FRAME));

        udf.setUdfMemo(generateUdfMemo(UDF_CDP_STEPPROGRESS, "[{\"id\":\"818181698c0f98a5018c1a81d21f0b3f\",\"name\":\"Предварительный анализ\",\"order\":0,\"taskId\":\"" + task.getId() + "\",\"progress\":0,\"description\":\"\",\"status\":\"DELETE\",\"hrs\":0,\"deletable\":true,\"actualBudget\":0,\"planby\":\"budget\",\"workTypeDraftAsString\":\"-\"," + "\"workTypeAsString\":\"-\",\"draftChanged\":true},{\"id\":\"818181698c0f98a5018c1a81e9020b63\",\"name\":\"Предварительный анализ 1\",\"order\":1,\"taskId\":" + "\"818181698c0f98a5018c1a819ad20b0d\",\"weight\":1,\"budget\":7200,\"progress\":0,\"description\":\"\",\"status\":\"ACTUAL\",\"workTypeId\":\"402881c2516c220101516c711ff80024\"," + "\"workTypeNorm\":2.0,\"hrs\":0,\"deletable\":true,\"actualBudget\":0,\"planby\":\"budget\",\"workTypeDraftAsString\":\"-\",\"workTypeAsString\":\"[0701] Иное\",\"draftChanged\":true}]"));

        task.refreshUdf(udf);
        techTaskController.performCommonOperation(task, DECLINE);
        ApiAsserts.assertThat(techTaskController.getResponse()).isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK).isParseableBody(TaskResponseBody.class).assertTask().isCorrectStatus(STATUS_WORKTASK_DECLINED).isCorrectResolution(CANNOT_BE_COMPLETED_WITHIN_THE_SPECIFIED_TIME_FRAME);
    }

    @Test(groups = {"PROC_WORKTASK", "Regression"}, description = "Вернуть в работу 2", dependsOnMethods = "taskDecline")
    public void taskReturn2() {
        apiController.updateToken(generateAuthToken(creator));
        udf = refreshUdf();
        task.setHandlerUser(handlerUser);
        udf.setUdfUser(generateUdfUser(STDT_HANDLER, handlerUser));
        task.refreshUdf(udf);
        techTaskController.performCommonOperation(task, RETURN);
        ApiAsserts.assertThat(techTaskController.getResponse()).isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK).isParseableBody(TaskResponseBody.class).assertTask().isCorrectStatus(STATUS_WORKTASK_ASSIGNED);
    }

    @Test(groups = {"PROC_WORKTASK", "Regression"}, description = "Принять в работу", dependsOnMethods = "taskReturn2")
    public void taskStart2() {
        apiController.updateToken(generateAuthToken(handlerUser));
        udf = refreshUdf();
        task.refreshTask();
        task.setHandlerUser(handlerUser);

        awaitDate = DateUtils.getCurrentDate(2);
        udf.setUdfDate(generateUdfDate(UDF_WORKTASK_PLANTD, awaitDate));
        awaitBudgetValue++;
        udf.setUdfDouble(generateUdfDouble(UDF_WORKTASK_PLANBUDGET, awaitBudgetValue));
        udf.setUdfList(generateUdfList(UDF_WORKTASK_ADDWATCHERINREQST, UDF_WORKTASK_ADDWATCHERINREQST_YES));
        udf.setUdfMemo(generateUdfMemo(UDF_CDP_STEPPROGRESS, "[{\"id\":\"818181698c0f98a5018c1698494b09e8\",\"name\":\"Предварительный анализ\",\"order\":0,\"taskId\":\"" + task.getId() + "\",\"weight\":0,\"budget\":7200,\"progress\":100,\"description\":\"\",\"status\":\"ACTUAL\",\"workTypeId\":\"402881c2516c220101516c711ff80024\"," + "\"workTypeNorm\":2.0,\"hrs\":0,\"deletable\":true,\"actualBudget\":0,\"planby\":\"budget\",\"workTypeDraftAsString\":\"-\",\"workTypeAsString\":\"[0701] Иное\"," + "\"draftChanged\":true},{\"id\":\"818181698c0f98a5018c169a3c9d0a25\",\"name\":\"Предварительный анализ-1\",\"order\":1,\"taskId\":\"818181698c0f98a5018c16981da209ac\"," + "\"weight\":1,\"budget\":7200,\"progress\":0,\"description\":\"\",\"status\":\"ACTUAL\",\"workTypeId\":\"402881c2516c220101516c711ff80024\",\"workTypeNorm\":2.0,\"hrs\":0," + "\"deletable\":true,\"actualBudget\":0,\"planby\":\"budget\",\"workTypeDraftAsString\":\"-\",\"workTypeAsString\":\"[0701] Иное\",\"draftChanged\":true}]"));
        udf.setUdfUser(generateUdfUser(STDT_HANDLER, handlerUser));
        task.refreshUdf(udf);
        techTaskController.performCommonOperation(task, ACCEPT_IN_WORK);
        ApiAsserts.assertThat(techTaskController.getResponse()).isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK).isParseableBody(TaskResponseBody.class).assertTask().isCorrectStatus(STATUS_WORKTASK_INWORK);

        var response = apiController.receiveTask(task.getNumber());
        CommonAssert.assertThat(response).isCorrectUdfDate(UDF_WORKTASK_PLANTD, awaitDate).isCorrectUdfDouble("Оценка трудоемкости", UDF_WORKTASK_PLANBUDGET, awaitBudgetValue).isCorrectUdfDouble("Первоначальная оценка трудоёмкости", UDF_WORKTASK_FIRSTPLANBUDGET, 5);
    }

    @Test(groups = {"PROC_WORKTASK", "Regression"}, description = "Отложить", dependsOnMethods = "taskStart2")
    public void taskPostpone() {
        apiController.updateToken(generateAuthToken(handlerUser));
        udf = refreshUdf();
        task.refreshTask();
        task.setResolution(generateResolution(RESOLUTION_DEPENDS_ON_ANOTHER_TASK));
        udf.setUdfDate(generateUdfDate(UDF_WORKTASK_PLANFD, DateUtils.getCurrentDate(1)));
        udf.setUdfTask(generateUdfTask(UDF_WORKTASK_DEPENDBF, dependTask));

        udf.setUdfMemo(generateUdfMemo(UDF_CDP_STEPPROGRESS, "[{\"id\":\"818181698c0f98a5018c1a81d21f0b3f\",\"name\":\"Предварительный анализ\",\"order\":0,\"taskId\":\"" + task.getId() + "\",\"progress\":0,\"description\":\"\",\"status\":\"DELETE\",\"hrs\":0,\"deletable\":true,\"actualBudget\":0,\"planby\":\"budget\",\"workTypeDraftAsString\":\"-\",\"workTypeAsString\":\"-\",\"draftChanged\":true},{\"id\":\"818181698c0f98a5018c1a81e9020b63\",\"name\":\"Предварительный анализ 1\",\"order\":1,\"taskId\":\"818181698c0f98a5018c1a819ad20b0d\",\"weight\":1,\"budget\":7200,\"progress\":0,\"description\":\"\",\"status\":\"ACTUAL\",\"workTypeId\":\"402881c2516c220101516c711ff80024\",\"workTypeNorm\":2.0,\"hrs\":0,\"deletable\":true,\"actualBudget\":0,\"planby\":\"budget\",\"workTypeDraftAsString\":\"-\",\"workTypeAsString\":\"[0701] Иное\",\"draftChanged\":true}]"));
        task.refreshUdf(udf);
        techTaskController.performCommonOperation(task, POSTPONE);
        ApiAsserts.assertThat(techTaskController.getResponse()).isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK).isParseableBody(TaskResponseBody.class).assertTask().isCorrectStatus(STATUS_WORKTASK_POSTPONED);
    }

    @Test(groups = {"PROC_WORKTASK", "Regression"}, description = "В свзанной задаче проверить наличие свзи с типом \"Блокирует задачи\"", dependsOnMethods = "taskPostpone")
    public void checkDependTaskExistReference() {
        var response = techTaskController.getBackLinks(dependTask.getNumber());
        CommonAssert.assertThat(response).isDependTaskExistReference(task.getNumber());
    }

    @Test(groups = {"PROC_WORKTASK", "Regression"}, description = "Снять задачу", dependsOnMethods = "checkDependTaskExistReference")
    public void taskCancel() {
        apiController.updateToken(generateAuthToken(creator));
        udf = refreshUdf();
        task.refreshTask();
        task.setResolution(generateResolution(WILL_NOT_BE_IMPLEMENTED_V2));
        udf.setUdfList(generateUdfList(UDF_WORKTASK_QUALITY, UDF_WORKTASK_QUALITY_NORM));
        udf.setUdfMemo(generateUdfMemo(UDF_CDP_STEPPROGRESS, "[{\"id\":\"818181698c1b5016018c1ea5127713e0\",\"name\":\"Предварительный анализ\",\"order\":0,\"taskId\":\"" + task.getId() + "\",\"progress\":0,\"description\":\"\",\"status\":\"DELETE\",\"hrs\":0,\"deletable\":true,\"actualBudget\":0,\"planby\":\"budget\",\"workTypeDraftAsString\":\"-\"," + "\"workTypeAsString\":\"-\",\"draftChanged\":true},{\"id\":\"818181698c1b5016018c1ea53a331412\",\"name\":\"Предварительный анализ 1\",\"order\":1,\"taskId\":\"818181698c1b5016018c1ea5042f13b5\",\"weight\":1,\"budget\":7200,\"progress\":0,\"description\":\"\",\"status\":\"ACTUAL\",\"workTypeId\":\"402881c2516c220101516c711ff80024\",\"workTypeNorm\":2.0,\"hrs\":0,\"deletable\":true,\"actualBudget\":0,\"planby\":\"budget\",\"workTypeDraftAsString\":\"-\",\"workTypeAsString\":\"[0701] Иное\",\"draftChanged\":true}]"));
        task.refreshUdf(udf);
        techTaskController.performCommonOperation(task, CANCEL);
        ApiAsserts.assertThat(techTaskController.getResponse()).isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_BAD_REQUEST).isCorrectErrorMessage("Задача не может быть закрыта пока не закрыты все подзадачи:");
    }

    @Test(groups = {"PROC_WORKTASK", "Regression"}, description = "Снять задачу CAT_ACCEPTTASK", dependsOnMethods = "taskCancel")
    public void cancelChildTechTask() {
        apiController.updateToken(generateAuthToken(handlerUser));
        var response = techTaskController.getBackLinks(task.getNumber());
        var udfAcceptTask = ExtractResponseFieldUtils.extractThat(response).extractByPath("BACK_UDF_WORKTASK_ACCEPTTASK", UdfTask.class);
        var acceptTask = InitEntities.getGeneralTask(TaskType.CAT_ACCEPTTASK, Operations.CAT);
        acceptTask.setNumber(Objects.requireNonNull(Arrays.stream(udfAcceptTask.getTaskValue()).findAny().orElse(null)).getNumber());
        udf = refreshUdf();
        acceptTask.refreshTask();
        acceptTask.setResolution(generateResolution(WILL_NOT_BE_IMPLEMENTED_V2));
        udf.setUdfList(generateUdfList(UDF_WORKTASK_QUALITY, UDF_WORKTASK_QUALITY_NORM));
        acceptTask.refreshUdf(udf);
        techTaskController.performCommonOperation(acceptTask, CANCEL);
        ApiAsserts.assertThat(techTaskController.getResponse()).isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK).isParseableBody(TaskResponseBody.class).assertTask().isCorrectStatus(STATUS_WORKTASK_CLOSED);
    }

    @Test(groups = {"PROC_WORKTASK", "Regression"}, description = "Снять задачу CAT_TECHTASK", dependsOnMethods = "cancelChildTechTask")
    public void taskCancel2() {
        apiController.updateToken(generateAuthToken(creator));
        udf = refreshUdf();
        task.refreshTask();
        task.setResolution(generateResolution(WILL_NOT_BE_IMPLEMENTED_V2));
        udf.setUdfList(generateUdfList(UDF_WORKTASK_QUALITY, UDF_WORKTASK_QUALITY_NORM));
        udf.setUdfMemo(generateUdfMemo(UDF_CDP_STEPPROGRESS, "[{\"id\":\"818181698c1b5016018c1ea5127713e0\",\"name\":\"Предварительный анализ\",\"order\":0,\"taskId\":\"" + task.getId() + "\",\"progress\":0,\"description\":\"\",\"status\":\"DELETE\",\"hrs\":0,\"deletable\":true,\"actualBudget\":0,\"planby\":\"budget\",\"workTypeDraftAsString\":\"-\"," + "\"workTypeAsString\":\"-\",\"draftChanged\":true},{\"id\":\"818181698c1b5016018c1ea53a331412\",\"name\":\"Предварительный анализ 1\",\"order\":1,\"taskId\":\"818181698c1b5016018c1ea5042f13b5\",\"weight\":1,\"budget\":7200,\"progress\":0,\"description\":\"\",\"status\":\"ACTUAL\",\"workTypeId\":\"402881c2516c220101516c711ff80024\",\"workTypeNorm\":2.0,\"hrs\":0,\"deletable\":true,\"actualBudget\":0,\"planby\":\"budget\",\"workTypeDraftAsString\":\"-\",\"workTypeAsString\":\"[0701] Иное\",\"draftChanged\":true}]"));
        task.refreshUdf(udf);
        techTaskController.performCommonOperation(task, CANCEL);
        ApiAsserts.assertThat(techTaskController.getResponse()).isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK).isParseableBody(TaskResponseBody.class).assertTask().isCorrectStatus(STATUS_WORKTASK_CLOSED).isCorrectResolution(WILL_NOT_BE_IMPLEMENTED_V2);
    }

    @Test(groups = {"PROC_WORKTASK", "Regression"}, description = "Вернуть в работу", dependsOnMethods = "taskCancel2")
    public void taskReturn3() {
        apiController.updateToken(generateAuthToken(creator));
        udf = refreshUdf();
        task.setHandlerUser(handlerUser);
        udf.setUdfUser(generateUdfUser(STDT_HANDLER, handlerUser));
        task.refreshUdf(udf);
        techTaskController.performCommonOperation(task, RETURN);
        ApiAsserts.assertThat(techTaskController.getResponse()).isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK).isParseableBody(TaskResponseBody.class).assertTask().isCorrectStatus(STATUS_WORKTASK_ASSIGNED);
    }

    @Test(groups = {"PROC_WORKTASK", "Regression"}, description = "Отменить назначение", dependsOnMethods = "taskReturn3")
    public void taskAssignCancel() {
        apiController.updateToken(generateAuthToken(creator));
        udf = refreshUdf();
        udf.setUdfMemo(generateUdfMemo(UDF_CDP_STEPPROGRESS, "[{\"id\":\"818181698c1b5016018c1ea5127713e0\",\"name\":\"Предварительный анализ\",\"order\":0,\"taskId\":\"" + task.getId() + "\",\"progress\":0,\"description\":\"\",\"status\":\"DELETE\",\"hrs\":0,\"deletable\":true,\"actualBudget\":0,\"planby\":\"budget\",\"workTypeDraftAsString\":\"-\"," + "\"workTypeAsString\":\"-\",\"draftChanged\":true},{\"id\":\"818181698c1b5016018c1ea53a331412\",\"name\":\"Предварительный анализ 1\",\"order\":1,\"taskId\":\"818181698c1b5016018c1ea5042f13b5\",\"weight\":1,\"budget\":7200,\"progress\":0,\"description\":\"\",\"status\":\"ACTUAL\",\"workTypeId\":\"402881c2516c220101516c711ff80024\",\"workTypeNorm\":2.0,\"hrs\":0,\"deletable\":true,\"actualBudget\":0,\"planby\":\"budget\",\"workTypeDraftAsString\":\"-\",\"workTypeAsString\":\"[0701] Иное\",\"draftChanged\":true}]"));
        task.refreshUdf(udf);
        techTaskController.performCommonOperation(task, ASSIGNCANCEL);
        ApiAsserts.assertThat(techTaskController.getResponse()).isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK).isParseableBody(TaskResponseBody.class).assertTask().isCorrectStatus(STATUS_WORKTASK_NEW);
    }
}
