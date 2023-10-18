package com.ts.integration.tests.proc_work_task.dev_task.dev_task_base_handler;

import com.ts.common.application.controllers.TrackStudioHttpStatusCodes;
import com.ts.common.application.database.dbEntities.GrTaskDbEntity;
import com.ts.common.application.database.dbTables.GrTaskTable;
import com.ts.common.asserts.ApiAsserts;
import com.ts.common.asserts.CommonAssert;
import com.ts.common.controllers.TaskResponseBody;
import com.ts.common.controllers.workTask.DevTaskController;
import com.ts.common.entitites.commonEntities.*;
import com.ts.common.entitites.commonEntities.udf.UdfTask;
import com.ts.common.entitites.tasks.GeneralTask;
import com.ts.common.enums.Operations;
import com.ts.common.enums.TaskType;
import com.ts.common.utils.DateUtils;
import com.ts.common.utils.InitEntities;
import com.ts.integration.tests.BaseIntegrationTest;
import io.restassured.path.json.JsonPath;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import static com.ts.common.application.database.DbQueryHelper.Operators.*;
import static com.ts.common.entitites.commonEntities.List.Constants.*;
import static com.ts.common.entitites.commonEntities.Task.Constants.*;
import static com.ts.common.entitites.commonEntities.Udfs.UdfSd.*;
import static com.ts.common.entitites.commonEntities.User.Constants.*;
import static com.ts.common.entitites.commonEntities.User.Constants.AKSENOV_ANDREY;
import static com.ts.common.enums.Operations.*;
import static com.ts.common.enums.Resolutions.CANNOT_BE_COMPLETED_WITHIN_THE_SPECIFIED_TIME_FRAME;
import static com.ts.common.enums.Resolutions.RESOLUTION_DEPENDS_ON_ANOTHER_TASK;
import static com.ts.common.enums.TaskStatuses.*;
import static com.ts.common.utils.InitEntities.*;
import static com.ts.common.utils.RandomUtils.generateString;

public class DevTaskBaseHandlerTest extends BaseIntegrationTest {
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
    private UdfTask customerRequest;
    private User creator;
    private User handlerUser;
    private Integer awaitBudgetValue;
    private Integer normBudgetValue;
    java.util.List<String> branches;
    private User constructorUser;
    private String BDKUName;
    private String misServiceForChange;


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
        misServiceForChange = devTaskController.getMisService().get(1);
        cdpBl = devTaskController.getCdpBl();
        var employees = userController.receiveUserByTask(parent.getNumber());
        creator = employees.stream().filter(f -> f.getAssignedRole().getName().equals("Менеджер проекта")).findFirst().get().getForUser();
        handlerUser = employees.stream().filter(f -> f.getAssignedRole().getName().equals("Участник проекта") && !f.getForUser().getLogin().equals(creator.getLogin())).findFirst().get().getForUser();
        var parentTaskPayload = apiController.receiveParentTaskPayload(parent.getNumber(), "CAT_DEVTASK").asString().replace("\\&", "\\\\&");
        branches = new JsonPath(parentTaskPayload).getList("udfs.UDF_WORKTASK_BRANCH.stringValueSelector", String.class);
        BDKUName = devTaskController.getBDKUTaskName();
    }


    @Test(groups = {"DevTask", "Regression"}, description = "Создание запроса на разработку")
    public void devTask() {
        udf = refreshUdf();
        task.refreshTask();
        apiController.updateToken(InitEntities.generateAuthToken(creator));
        task.setParent(parent);
        task.setName("Создание запроса на разработку");
        task.setDescription(task.getDescription() + generateString());
        task.setHandlerUser(handlerUser);
        udf.setEighthUdfList(generateUdfList(UDF_WORKTASK_COMPLEXITYLEVEL, TASK_LEVEL_7));
        udf.setUdfUser(generateUdfUser(UDF_WORKTASK_SUPERVISER, ABDULLAEV_BAHODIR));
        udf.setSecondUdfUser(generateUdfUser(UDF_WATCHER, BABUSHKIN_IVAN));
        if (cdpBl != null) {
            udf.setUdfList(generateUdfList(UDF_CDP_BL, cdpBl));
        }
        udf.setUdfTask(generateUdfTask(UDF_SD_MODULE, CORE));
        udf.setSecondUdfString(generateUdfString(UDF_WORKTASK_BRANCH, branches.stream().filter(s -> s.toLowerCase().contains(BDKUName.toLowerCase())).findAny().get()));
        udf.setUdfString(generateUdfString(UDF_SD_NOMODULE_REASON, generateString()));
        udf.setSecondUdfList(generateUdfList(UDF_CDP_ACCEPTANCE, UDF_CDP_ACCEPTANCE_NO));
        udf.setUdfDate(generateUdfDate(UDF_WORKTASK_ANALYSISFD, 1));
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

        Map<Task.Constants, UserData> dependTaskFNC = new HashMap<>();
        var userData1 = new UserData(null, "{\"type\":\"REQUIRED\",\"comment\":\"Required\"}");
        var userData2 = new UserData(null, "{\"type\":\"RECOMMENDED\",\"comment\":\"RECOMMENDED\"}");
        var userData3 = new UserData(null, "{\"type\":\"OPTIONAL\",\"option\":\"949177\",\"optionNot\":\"773226\",\"comment\":\"Optional\"}");
        dependTaskFNC.put(RYSGAL_BANK, userData1);
        dependTaskFNC.put(WORKTASK_TESTTASK, userData2);
        dependTaskFNC.put(CUSTOMER_REQUEST, userData3);
        udf.setEighthUdfTask(generateUdfTask(UDF_WORKTASK_DEPENDTASKFNC, dependTaskFNC));
        task.refreshUdf(udf);
        devTaskController.createDevTask(task);
        var response = devTaskController.getResponse();
        constructorUser = JsonPath.from(response.asString()).getObject("udfs.UDF_SD_MODULE.taskValue[0].udfs.UDF_CONSTRUCTOR.userValue[0]", User.class);
        ApiAsserts.assertThat(response)
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isCorrectStatus(STATUS_WORKTASK_ONANALYSIS)
                .isEquals(task);
    }

    @Test(groups = {"DevTask", "Regression"}, description = "Коррекция плана", dependsOnMethods = "devTask")
    public void changePlan() {
        udf = refreshUdf();
        task.refreshTask();
        apiController.updateToken(generateAuthToken(handlerUser));
        task.setDescription(generateString());
        expectedCompletionDate = DateUtils.getCurrentDate(1);
        awaitBudgetValue = 2;
        normBudgetValue = 2;
        udf.setUdfDate(generateUdfDate(UDF_WORKTASK_AWAITTD, expectedCompletionDate));
        udf.setUdfDouble(generateUdfDouble(UDF_WORKTASK_AWAITBUDGET, awaitBudgetValue));
        udf.setSecondUdfDouble(generateUdfDouble(UDF_CDP_NORMBUDGET, normBudgetValue));
        udf.setUdfMemo(generateUdfMemo(UDF_CDP_STEPPLAN, "[{\"id\":\"8181816c89cef25c018a647031eb6c13\",\"name\":\"Предварительный анализ\",\"order\":0,\"taskId\":\"" + task.getId() +
                        "\",\"weight\":0,\"budget\":7200,\"progress\":0,\"description\":\"\",\"status\":\"ACTUAL\",\"workTypeId\":\"402881c2516c220101516c711ff80024\",\"workTypeNorm\":2,\"hrs\":0,\"deletable\":true,\"actualBudget\":0,\"planby\":\"budget\",\"workTypeAsString\":\"[0701] Иное\",\"workTypeDraftAsString\":\"-\",\"draftChanged\":true,\"orderDraft\":0,\"nameDraft\":\"Предварительный анализ\",\"descriptionDraft\":\"\",\"statusDraft\":\"ACTUAL\",\"weightDraft\":0,\"budgetDraft\":7200,\"budgetHrs\":2,\"budgetMinutes\":0,\"workTypeIdDraft\":\"402881c2516c220101516c711ff80024\",\"workTypeNormDraft\":2}]",
                "{\"autoFinishAnalysis\": \"true\"}"));
        udf.setSecondUdfMemo(generateUdfMemo(UDF_CDP_STEPPROGRESS, "[{\"id\":\"8181816c89cef25c018a647031eb6c13\",\"name\":\"Предварительный анализ\",\"order\":0,\"taskId\":\"" + task.getId() +
                "\",\"weight\":0,\"budget\":7200,\"progress\":0,\"description\":\"\",\"status\":\"ACTUAL\",\"workTypeId\":\"402881c2516c220101516c711ff80024\",\"workTypeNorm\":2.0,\"hrs\":0,\"deletable\":true,\"actualBudget\":0,\"planby\":\"budget\",\"workTypeAsString\":\"[0701] Иное\",\"workTypeDraftAsString\":\"-\",\"draftChanged\":true}]"));
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
                .isCorrectUdfDouble("Оценка трудоемкости", UDF_WORKTASK_PLANBUDGET, awaitBudgetValue)
                .isCorrectUdfDouble("Трудоемкость по нормам", UDF_CDP_NORMBUDGET, normBudgetValue)
                .isCorrectUdfDate(UDF_WORKTASK_PLANTD, expectedCompletionDate);
    }

    @Test(groups = {"DevTask", "Regression"}, description = "Принять в работу", dependsOnMethods = "changePlan")
    public void taskStart() {
        udf = refreshUdf();
        task.refreshTask();
        apiController.updateToken(generateAuthToken(handlerUser));
        task.setHandlerUser(handlerUser);
        udf.setUdfMemo(generateUdfMemo(UDF_CDP_STEPPROGRESS, "[{\"id\":\"8181816c89cef25c018a69d596ac3509\",\"name\":\"Предварительный анализ\",\"order\":0,\"taskId\":\"" + task.getId() +
                "\",\"progress\":0,\"description\":\"\",\"status\":\"DELETE\",\"hrs\":0,\"deletable\":true,\"actualBudget\":0,\"planby\":\"budget\",\"workTypeAsString\":\"-\",\"workTypeDraftAsString\":\"-\",\"draftChanged\":true}]"));
        udf.setUdfUser(generateUdfUser(STDT_HANDLER, handlerUser));

        task.refreshUdf(udf);
        devTaskController.performCommonOperation(task, ACCEPT_IN_WORK);
        ApiAsserts.assertThat(devTaskController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isCorrectStatus(STATUS_WORKTASK_INWORK);
    }

    @Test(groups = {"DevTask", "Regression"}, description = "Комментарий", dependsOnMethods = "taskStart")
    public void taskComment() {
        udf = refreshUdf();
        task.refreshTask();
        apiController.updateToken(generateAuthToken(handlerUser));
        task.setDescription(generateString());
        task.refreshUdf();
        devTaskController.performCommonOperation(task, Operations.COMMENT);
        ApiAsserts.assertThat(devTaskController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask();
    }

    @Test(groups = {"DevTask", "Regression"}, description = "Отложить", dependsOnMethods = "taskComment")
    public void taskPostpone() {
        udf = refreshUdf();
        task.refreshTask();
        apiController.updateToken(generateAuthToken(handlerUser));
        task.setResolution(generateResolution(RESOLUTION_DEPENDS_ON_ANOTHER_TASK));
        udf.setUdfDate(generateUdfDate(UDF_WORKTASK_PLANFD, expectedCompletionDate));
        udf.setUdfTask(generateUdfTask(UDF_WORKTASK_DEPENDBF, MTBANK));
        udf.setUdfMemo(generateUdfMemo(UDF_CDP_STEPPROGRESS, "[{\"id\":\"8181816c89cef25c018a69ee1ae1367d\",\"name\":\"Предварительный анализ\",\"order\":0,\"taskId\":\"" + task.getId() +
                "\",\"progress\":0,\"description\":\"\",\"status\":\"DELETE\",\"hrs\":0,\"deletable\":true,\"actualBudget\":0,\"planby\":\"budget\",\"workTypeAsString\":\"-\",\"workTypeDraftAsString\":\"-\",\"draftChanged\":true}]"));
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
        udf = refreshUdf();
        task.refreshTask();
        apiController.updateToken(generateAuthToken(handlerUser));
        task.setHandlerUser(handlerUser);
        udf.setUdfMemo(generateUdfMemo(UDF_CDP_STEPPROGRESS, "[{\"id\":\"8181816c89cef25c018a69f61e6d374f\",\"name\":\"Предварительный анализ\",\"order\":0,\"taskId\":\"" + task.getId() +
                "\",\"progress\":0,\"description\":\"\",\"status\":\"DELETE\",\"hrs\":0,\"deletable\":true,\"actualBudget\":0,\"planby\":\"budget\",\"workTypeAsString\":\"-\",\"workTypeDraftAsString\":\"-\",\"draftChanged\":true}]"));
        udf.setUdfUser(generateUdfUser(STDT_HANDLER, handlerUser));
        task.refreshUdf(udf);
        devTaskController.performCommonOperation(task, ACCEPT_IN_WORK);
        ApiAsserts.assertThat(devTaskController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isCorrectStatus(STATUS_WORKTASK_INWORK);
    }

    @Test(groups = {"DevTask", "Regression"}, description = "Отклонить", dependsOnMethods = "taskStart2")
    public void taskDecline() {
        udf = refreshUdf();
        task.refreshTask();
        apiController.updateToken(generateAuthToken(handlerUser));
        task.setResolution(generateResolution(CANNOT_BE_COMPLETED_WITHIN_THE_SPECIFIED_TIME_FRAME));
        udf.setUdfMemo(generateUdfMemo(UDF_CDP_STEPPROGRESS, "[{\"id\":\"8181816c89cef25c018a6a0419cb38f8\",\"name\":\"Предварительный анализ\",\"order\":0,\"taskId\":\"" + task.getId() +
                "\",\"progress\":0,\"description\":\"\",\"status\":\"DELETE\",\"hrs\":0,\"deletable\":true,\"actualBudget\":0,\"planby\":\"budget\",\"workTypeAsString\":\"-\",\"workTypeDraftAsString\":\"-\",\"draftChanged\":true}]"));

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

    @Test(groups = {"DevTask", "Regression"}, description = "Передать на предварительный анализ", dependsOnMethods = "taskDecline")
    public void taskSubmitForAnalysis() {
        udf = refreshUdf();
        task.refreshTask();
        apiController.updateToken(generateAuthToken(creator));
        task.setHandlerUser(handlerUser);
        task.setDescription(generateString());
        udf.setUdfDate(generateUdfDate(UDF_WORKTASK_ANALYSISFD, expectedCompletionDate));
        udf.setUdfMemo(generateUdfMemo(UDF_CDP_STEPPROGRESS, "[{\"id\":\"8181816c89cef25c018a6a23ae5e3add\",\"name\":\"Предварительный анализ\",\"order\":0,\"taskId\":\"" + task.getId() +
                "\",\"progress\":0,\"description\":\"\",\"status\":\"DELETE\",\"hrs\":0,\"deletable\":true,\"actualBudget\":0,\"planby\":\"budget\",\"workTypeAsString\":\"-\",\"workTypeDraftAsString\":\"-\",\"draftChanged\":true}]"));
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
                .isCorrectUdfDate(UDF_WORKTASK_ANALYSISFD, expectedCompletionDate);
    }

    @Test(groups = {"DevTask", "Regression"}, description = "Коррекция плана", dependsOnMethods = "taskSubmitForAnalysis")
    public void changePlan2() {
        udf = refreshUdf();
        task.refreshTask();
        apiController.updateToken(generateAuthToken(handlerUser));
        task.setDescription(generateString());
        task.setConfirmed(true);
        udf.setUdfDate(generateUdfDate(UDF_WORKTASK_PLANFD, expectedCompletionDate));
        udf.setSecondUdfDate(generateUdfDate(UDF_WORKTASK_PLANTD, 1));
        udf.setThirdUdfDate(generateUdfDate(UDF_WORKTASK_AWAITTD, expectedCompletionDate));
        udf.setUdfDouble(generateUdfDouble(UDF_WORKTASK_AWAITBUDGET, 4));
        udf.setSecondUdfDouble(generateUdfDouble(UDF_CDP_NORMBUDGET, 4));
        udf.setUdfMemo(generateUdfMemo(UDF_CDP_STEPPLAN, "[{\"orderDraft\":1,\"weightDraft\":1,\"budgetDraft\":7200,\"planby\":\"budget\",\"deletable\":true,\"statusDraft\":\"NEW\",\"workTypeIdDraft\":\"402881c2516c220101516c711ff80024\",\"workTypeNormDraft\":2,\"nameDraft\":\"Пошаговый план 1\",\"budgetHrs\":\"2\",\"budgetMinutes\":\"0\"},{\"orderDraft\":2,\"weightDraft\":1,\"budgetDraft\":7200,\"planby\":\"budget\",\"deletable\":true,\"statusDraft\":\"NEW\",\"nameDraft\":\"Пошаговый план 2\",\"workTypeIdDraft\":\"402881c2516c220101516c711ff80024\",\"workTypeNormDraft\":2,\"budgetHrs\":\"2\",\"budgetMinutes\":\"0\"}]"
                , "{\"autoFinishAnalysis\": \"true\"}"));
        udf.setSecondUdfMemo(generateUdfMemo(UDF_CDP_STEPPROGRESS, "[{\"id\":\"8181816c89cef25c018a6a292d5d3bda\",\"name\":\"Предварительный анализ\",\"order\":0,\"taskId\":\"" + task.getId() +
                "\",\"progress\":0,\"description\":\"\",\"status\":\"DELETE\",\"hrs\":0,\"deletable\":true,\"actualBudget\":0,\"planby\":\"budget\",\"workTypeAsString\":\"-\",\"workTypeDraftAsString\":\"-\",\"draftChanged\":true}]"));

        task.refreshUdf(udf);
        devTaskController.performCommonOperation(task, CHANGE_PLAN);
        ApiAsserts.assertThat(devTaskController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isCorrectStatus(STATUS_WORKTASK_ASSIGNED);

        var response = apiController.receiveTask(task.getNumber());
        CommonAssert
                .assertThat(response)
                .isCorrectTaskField("Планируемая дата завершения = Ожидаемая дата завершения", new JsonPath(response.asString()).getString("udfs.UDF_WORKTASK_PLANTD.dateValue"), "udfs.UDF_WORKTASK_AWAITTD.dateValue")
                .isCorrectUdfDouble("Оценка трудоемкости", UDF_WORKTASK_PLANBUDGET, 4)
                .isCorrectUdfDouble("Трудоемкость по нормам", UDF_CDP_NORMBUDGET, 4);
    }

    @Test(groups = {"DevTask", "Regression"}, description = "Принять в работу", dependsOnMethods = "changePlan2")
    public void taskStart3() {
        udf = refreshUdf();
        task.refreshTask();
        apiController.updateToken(generateAuthToken(handlerUser));
        task.setHandlerUser(handlerUser);
        udf.setUdfMemo(generateUdfMemo(UDF_CDP_STEPPROGRESS, "[{\"id\":\"8181816c89cef25c018a6a292d5d3bda\",\"name\":\"Предварительный анализ\",\"order\":0,\"taskId\":\"" + task.getId() +
                "\",\"progress\":100,\"description\":\"\",\"status\":\"DELETE\",\"hrs\":0,\"deletable\":true,\"actualBudget\":0,\"planby\":\"budget\",\"workTypeAsString\":\"-\",\"workTypeDraftAsString\":\"-\",\"draftChanged\":true},{\"id\":\"8181816c89cef25c018a6a2eae533cb0\",\"name\":\"Пошаговый план 1\",\"order\":1,\"taskId\":\"" + task.getId() +
                "\",\"weight\":1,\"budget\":7200,\"progress\":0,\"description\":\"\",\"status\":\"ACTUAL\",\"workTypeId\":\"402881c2516c220101516c711ff80024\",\"workTypeNorm\":2.0,\"hrs\":0,\"deletable\":true,\"actualBudget\":0,\"planby\":\"budget\",\"workTypeAsString\":\"[0701] Иное\",\"workTypeDraftAsString\":\"-\",\"draftChanged\":true},{\"id\":\"8181816c89cef25c018a6a2eae533cb1\",\"name\":\"Пошаговый план 2\",\"order\":2,\"taskId\":\"" + task.getId() +
                "\",\"weight\":1,\"budget\":7200,\"progress\":0,\"description\":\"\",\"status\":\"ACTUAL\",\"workTypeId\":\"402881c2516c220101516c711ff80024\",\"workTypeNorm\":2.0,\"hrs\":0,\"deletable\":true,\"actualBudget\":0,\"planby\":\"budget\",\"workTypeAsString\":\"[0701] Иное\",\"workTypeDraftAsString\":\"-\",\"draftChanged\":true}]"));
        udf.setUdfUser(generateUdfUser(STDT_HANDLER, handlerUser));

        task.refreshUdf(udf);
        devTaskController.performCommonOperation(task, ACCEPT_IN_WORK);
        ApiAsserts.assertThat(devTaskController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isCorrectStatus(STATUS_WORKTASK_INWORK);
    }

    @Test(groups = {"BugTask", "Regression"}, description = "Запретить/Разрешить тиражирование во все ветки: Запретить", dependsOnMethods = "taskStart3")
    public void taskDistToAllBan() {
        udf = refreshUdf();
        task.refreshTask();
        apiController.updateToken(generateAuthToken(handlerUser));
        var description = generateString();
        task.setDescription(description);
        udf.setUdfList(generateUdfList(UDF_WORKTASK_DISTTOALL, UDF_WORKTASK_DISTTOALL_BAN));

        task.refreshUdf(udf);
        devTaskController.performCommonOperation(task, WORKTASK_DISTTOALL);
        ApiAsserts.assertThat(devTaskController.getResponse())
                .checkingResponseMessageField("description", "<br/>Запрещено тиражирование задачи во все ветки. Причина: " + description)
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK);
    }

    @Test(groups = {"BugTask", "Regression"}, description = "(Ответственный) Запретить/Разрешить тиражирование во все ветки: Разрешить", dependsOnMethods = "taskDistToAllBan")
    public void taskDistToAllAllowWithHandlerUser() {
        udf = refreshUdf();
        task.refreshTask();
        apiController.updateToken(generateAuthToken(handlerUser));
        task.setDescription(generateString());
        udf.setUdfList(generateUdfList(UDF_WORKTASK_DISTTOALL, UDF_WORKTASK_DISTTOALL_ALLOW));

        task.refreshUdf(udf);
        devTaskController.performCommonOperation(task, WORKTASK_DISTTOALL);
        var response = devTaskController.getResponse();
        ApiAsserts.assertThat(response)
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_BAD_REQUEST)
                .isCorrectErrorMessage("Разрешить тиражирование во все ветки может только конструктор модуля.<br>Вы не является конструктором модуля либо в задаче не указан модуль системы");
    }

    @Test(groups = {"BugTask", "Regression"}, description = "(Пользователь из поля Конструктор) Запретить/Разрешить тиражирование во все ветки: Разрешить", dependsOnMethods = "taskDistToAllAllowWithHandlerUser")
    public void taskDistToAllAllow() {
        udf = refreshUdf();
        task.refreshTask();
        apiController.updateToken(generateAuthToken(constructorUser));
        var description = generateString();
        task.setDescription(description);
        udf.setUdfList(generateUdfList(UDF_WORKTASK_DISTTOALL, UDF_WORKTASK_DISTTOALL_ALLOW));

        task.refreshUdf(udf);
        devTaskController.performCommonOperation(task, WORKTASK_DISTTOALL);
        var response = devTaskController.getResponse();
        ApiAsserts.assertThat(response)
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .checkingResponseMessageField("description", "<br/>Разрешено тиражирование задачи во все ветки. Причина: " + description);
    }

    @Test(groups = {"BugTask", "Regression"}, description = "Изменить ветку для разработки", dependsOnMethods = "taskDistToAllAllow")
    public void taskChangeBranch() {
        udf = refreshUdf();
        task.refreshTask();
        apiController.updateToken(generateAuthToken(handlerUser));
        task.setDescription(generateString());
        udf.setUdfString(generateUdfString(UDF_WORKTASK_BRANCH, branches.stream().filter(s -> s.toLowerCase().contains(BDKUName.toLowerCase())).findAny().get()));
        udf.setUdfMemo(generateUdfMemo(UDF_CDP_STEPPROGRESS, "[{\"id\":\"8181816c89cef25c018a6f32b56b4b3c\",\"name\":\"Предварительный анализ\",\"order\":0,\"taskId\":\"" + task.getId() +
                "\",\"progress\":0,\"description\":\"\",\"status\":\"DELETE\",\"hrs\":0,\"deletable\":true,\"actualBudget\":0,\"planby\":\"budget\",\"workTypeAsString\":\"-\",\"workTypeDraftAsString\":\"-\",\"draftChanged\":true},{\"id\":\"8181816c89cef25c018a6f33e9c14c13\",\"name\":\"Пошаговый план 1\",\"order\":1,\"taskId\":\"" + task.getId() +
                "\",\"weight\":1,\"budget\":7200,\"progress\":0,\"description\":\"\",\"status\":\"ACTUAL\",\"workTypeId\":\"402881c2516c220101516c711ff80024\",\"workTypeNorm\":2.0,\"hrs\":0,\"deletable\":true,\"actualBudget\":0,\"planby\":\"budget\",\"workTypeAsString\":\"[0701] Иное\",\"workTypeDraftAsString\":\"-\",\"draftChanged\":true},{\"id\":\"8181816c89cef25c018a6f33e9c14c14\",\"name\":\"Пошаговый план 2\",\"order\":2,\"taskId\":\"" + task.getId() +
                "\",\"weight\":1,\"budget\":7200,\"progress\":0,\"description\":\"\",\"status\":\"ACTUAL\",\"workTypeId\":\"402881c2516c220101516c711ff80024\",\"workTypeNorm\":2.0,\"hrs\":0,\"deletable\":true,\"actualBudget\":0,\"planby\":\"budget\",\"workTypeAsString\":\"[0701] Иное\",\"workTypeDraftAsString\":\"-\",\"draftChanged\":true}]"));

        task.refreshUdf(udf);
        devTaskController.performCommonOperation(task, WORKTASK_CHANGEBRANCH);
        var response = devTaskController.getResponse();
        ApiAsserts.assertThat(response)
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK);
    }

    @Test(groups = {"BugTask", "Regression"}, description = "Изменить ветку для разработки на пустую", dependsOnMethods = "taskChangeBranch")
    public void taskChangeBranch1() {
        udf = refreshUdf();
        task.refreshTask();
        apiController.updateToken(generateAuthToken(handlerUser));
        task.setDescription(generateString());
        udf.setUdfString(generateUdfString(UDF_WORKTASK_BRANCH, null));
        udf.setUdfMemo(generateUdfMemo(UDF_CDP_STEPPROGRESS, "[{\"id\":\"8181816c89cef25c018a6f32b56b4b3c\",\"name\":\"Предварительный анализ\",\"order\":0,\"taskId\":\"" + task.getId() +
                "\",\"progress\":0,\"description\":\"\",\"status\":\"DELETE\",\"hrs\":0,\"deletable\":true,\"actualBudget\":0,\"planby\":\"budget\",\"workTypeAsString\":\"-\",\"workTypeDraftAsString\":\"-\",\"draftChanged\":true},{\"id\":\"8181816c89cef25c018a6f33e9c14c13\",\"name\":\"Пошаговый план 1\",\"order\":1,\"taskId\":\"" + task.getId() +
                "\",\"weight\":1,\"budget\":7200,\"progress\":0,\"description\":\"\",\"status\":\"ACTUAL\",\"workTypeId\":\"402881c2516c220101516c711ff80024\",\"workTypeNorm\":2.0,\"hrs\":0,\"deletable\":true,\"actualBudget\":0,\"planby\":\"budget\",\"workTypeAsString\":\"[0701] Иное\",\"workTypeDraftAsString\":\"-\",\"draftChanged\":true},{\"id\":\"8181816c89cef25c018a6f33e9c14c14\",\"name\":\"Пошаговый план 2\",\"order\":2,\"taskId\":\"" + task.getId() +
                "\",\"weight\":1,\"budget\":7200,\"progress\":0,\"description\":\"\",\"status\":\"ACTUAL\",\"workTypeId\":\"402881c2516c220101516c711ff80024\",\"workTypeNorm\":2.0,\"hrs\":0,\"deletable\":true,\"actualBudget\":0,\"planby\":\"budget\",\"workTypeAsString\":\"[0701] Иное\",\"workTypeDraftAsString\":\"-\",\"draftChanged\":true}]"));

        task.refreshUdf(udf);
        devTaskController.performCommonOperation(task, WORKTASK_CHANGEBRANCH);
        var response = devTaskController.getResponse();
        ApiAsserts.assertThat(response)
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK);
    }

    @Test(groups = {"BugTask", "Regression"}, description = "Установить функциональную зависимость от другой задачи", dependsOnMethods = "taskChangeBranch1")
    public void taskDependOtherTask() {
        udf = refreshUdf();
        task.refreshTask();
        apiController.updateToken(generateAuthToken(handlerUser));
        task.setDescription(generateString());
        Map<Task.Constants, UserData> dependTaskFNC = new HashMap<>();
        var userData1 = new UserData(null, "{\"type\":\"REQUIRED\",\"comment\":\"Required\"}");
        dependTaskFNC.put(HEAD_BOOK, userData1);
        udf.setUdfTask(generateUdfTask(UDF_WORKTASK_DEPENDTASKFNC, dependTaskFNC));

        task.refreshUdf(udf);
        devTaskController.performCommonOperation(task, WORKTASK_DEPENDOTHERTASK);
        var response = devTaskController.getResponse();
        ApiAsserts.assertThat(response)
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK);
    }

    @Test(groups = {"BugTask", "Regression"}, description = "Изменить план тестирования", dependsOnMethods = "taskDependOtherTask")
    public void changeTestPlanTask() {
        udf = refreshUdf();
        task.refreshTask();
        var description = generateString();
        apiController.updateToken(generateAuthToken(handlerUser));
        task.setDescription(description);

        task.refreshUdf(udf);
        devTaskController.performCommonOperation(task, WORKTASK_CHANGETESTPLAN);
        var response = devTaskController.getResponse();
        ApiAsserts.assertThat(response)
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK);

        CommonAssert
                .assertThat(response)
                .isCorrectMessageField("description", description);
    }

    @Test(groups = {"BugTask", "Regression"}, description = "Изменить список связанных задач", dependsOnMethods = "changeTestPlanTask")
    public void changeLinkedTask() {
        udf = refreshUdf();
        task.refreshTask();
        apiController.updateToken(generateAuthToken(handlerUser));
        task.setDescription(generateString());
        var linkedTask = new com.ts.common.entitites.commonEntities.Task.Constants[]{
                AKKREDITIVES,
                SERVICE_DESK
        };
        udf.setUdfTask(generateUdfTask(UDF_SD_LINKEDREQUEST, linkedTask));

        task.refreshUdf(udf);
        devTaskController.performCommonOperation(task, CHANGE_LINKED_TASKS);
        var response = devTaskController.getResponse();
        ApiAsserts.assertThat(response)
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK);

        var taskDetail = apiController.receiveTask(task.getNumber());
        CommonAssert
                .assertThat(taskDetail)
                .isCorrectUdfTask(UDF_SD_LINKEDREQUEST, AKKREDITIVES)
                .isCorrectUdfTask(UDF_SD_LINKEDREQUEST, SERVICE_DESK);
    }

    @Test(groups = {"BugTask", "Regression"}, description = "Изменить услугу", dependsOnMethods = "changeLinkedTask")
    public void taskChangeService() {
        udf = refreshUdf();
        task.refreshTask();
        apiController.updateToken(generateAuthToken(handlerUser));
        task.setDescription(generateString());
        udf.setUdfList(generateUdfList(UDF_MIS_SERVICE, misServiceForChange));
        task.refreshUdf(udf);
        devTaskController.performCommonOperation(task, CHANGE_SERVICE);
        var response = devTaskController.getResponse();
        ApiAsserts.assertThat(response)
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK);

        var taskDetail = apiController.receiveTask(task.getNumber());
        CommonAssert
                .assertThat(taskDetail)
                .isCorrectUdfList(UDF_MIS_SERVICE, misServiceForChange);

    }

    @Test(groups = {"BugTask", "Regression"}, description = "Назначить контролёра", dependsOnMethods = "taskChangeService")
    public void taskChangeSupervisor() {
        udf = refreshUdf();
        task.refreshTask();
        apiController.updateToken(generateAuthToken(handlerUser));
        task.setDescription(generateString());
        udf.setUdfUser(generateUdfUser(UDF_WORKTASK_SUPERVISER, new User.Constants[]{ABDULLAEV_BAHODIR, AKSENOV_ANDREY}));

        task.refreshUdf(udf);
        devTaskController.performCommonOperation(task, WORKTASK_SUPERVISE);
        var response = devTaskController.getResponse();
        ApiAsserts.assertThat(response)
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK);

        var taskDetail = apiController.receiveTask(task.getNumber());
        CommonAssert
                .assertThat(taskDetail)
                .isCorrectUdfUSer(UDF_WORKTASK_SUPERVISER, ABDULLAEV_BAHODIR.login)
                .isCorrectUdfUSer(UDF_WORKTASK_SUPERVISER, AKSENOV_ANDREY.login);
    }

    @Test(groups = {"BugTask", "Regression"}, description = "Назначить наблюдателя", dependsOnMethods = "taskChangeSupervisor")
    public void taskChangeWatcher() {
        udf = refreshUdf();
        task.refreshTask();
        apiController.updateToken(generateAuthToken(handlerUser));
        task.setDescription(generateString());
        udf.setUdfUser(generateUdfUser(UDF_WATCHER, new User.Constants[]{BABUSHKIN_IVAN, ALTUNIN_NIKOLAY}));
        udf.setUdfMemo(generateUdfMemo(UDF_CDP_STEPPROGRESS, "[{\"id\":\"8181816c89cef25c018a6f32b56b4b3c\",\"name\":\"Предварительный анализ\",\"order\":0,\"taskId\":\"" + task.getId() +
                "\",\"progress\":0,\"description\":\"\",\"status\":\"DELETE\",\"hrs\":0,\"deletable\":true,\"actualBudget\":0,\"planby\":\"budget\",\"workTypeAsString\":\"-\",\"workTypeDraftAsString\":\"-\",\"draftChanged\":true},{\"id\":\"8181816c89cef25c018a6f33e9c14c13\",\"name\":\"Пошаговый план 1\",\"order\":1,\"taskId\":\"" + task.getId() +
                "\",\"weight\":1,\"budget\":7200,\"progress\":0,\"description\":\"\",\"status\":\"ACTUAL\",\"workTypeId\":\"402881c2516c220101516c711ff80024\",\"workTypeNorm\":2.0,\"hrs\":0,\"deletable\":true,\"actualBudget\":0,\"planby\":\"budget\",\"workTypeAsString\":\"[0701] Иное\",\"workTypeDraftAsString\":\"-\",\"draftChanged\":true},{\"id\":\"8181816c89cef25c018a6f33e9c14c14\",\"name\":\"Пошаговый план 2\",\"order\":2,\"taskId\":\"" + task.getId() +
                "\",\"weight\":1,\"budget\":7200,\"progress\":0,\"description\":\"\",\"status\":\"ACTUAL\",\"workTypeId\":\"402881c2516c220101516c711ff80024\",\"workTypeNorm\":2.0,\"hrs\":0,\"deletable\":true,\"actualBudget\":0,\"planby\":\"budget\",\"workTypeAsString\":\"[0701] Иное\",\"workTypeDraftAsString\":\"-\",\"draftChanged\":true}]"));
        task.refreshUdf(udf);
        devTaskController.performCommonOperation(task, WATCH);
        var response = devTaskController.getResponse();
        ApiAsserts.assertThat(response)
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK);

        var taskDetail = apiController.receiveTask(task.getNumber());
        CommonAssert
                .assertThat(taskDetail)
                .isCorrectUdfUSer(UDF_WATCHER, BABUSHKIN_IVAN.login)
                .isCorrectUdfUSer(UDF_WATCHER, ALTUNIN_NIKOLAY.login);
    }

    @Test(groups = {"BugTask", "Regression"}, description = "Установить общеполезность", dependsOnMethods = "taskChangeWatcher")
    public void taskChangeGenuse() {
        apiController.updateToken(generateAuthToken(handlerUser));
        task.setDescription(generateString());
        udf = refreshUdf();
        task.refreshTask();
        udf.setUdfList(generateUdfList(UDF_SDFEATURE_GENUSE, LOCAL, "{\"username\":\"shamsaddin.gadirov@azems.az\",\"name\":\"Шамсаддин Гадиров\"}"));
        udf.setUdfMemo(generateUdfMemo(UDF_CDP_STEPPROGRESS, "[{\"id\":\"8181816c89cef25c018a6f32b56b4b3c\",\"name\":\"Предварительный анализ\",\"order\":0,\"taskId\":\"" + task.getId() +
                "\",\"progress\":0,\"description\":\"\",\"status\":\"DELETE\",\"hrs\":0,\"deletable\":true,\"actualBudget\":0,\"planby\":\"budget\",\"workTypeAsString\":\"-\",\"workTypeDraftAsString\":\"-\",\"draftChanged\":true},{\"id\":\"8181816c89cef25c018a6f33e9c14c13\",\"name\":\"Пошаговый план 1\",\"order\":1,\"taskId\":\"" + task.getId() +
                "\",\"weight\":1,\"budget\":7200,\"progress\":0,\"description\":\"\",\"status\":\"ACTUAL\",\"workTypeId\":\"402881c2516c220101516c711ff80024\",\"workTypeNorm\":2.0,\"hrs\":0,\"deletable\":true,\"actualBudget\":0,\"planby\":\"budget\",\"workTypeAsString\":\"[0701] Иное\",\"workTypeDraftAsString\":\"-\",\"draftChanged\":true},{\"id\":\"8181816c89cef25c018a6f33e9c14c14\",\"name\":\"Пошаговый план 2\",\"order\":2,\"taskId\":\"" + task.getId() +
                "\",\"weight\":1,\"budget\":7200,\"progress\":0,\"description\":\"\",\"status\":\"ACTUAL\",\"workTypeId\":\"402881c2516c220101516c711ff80024\",\"workTypeNorm\":2.0,\"hrs\":0,\"deletable\":true,\"actualBudget\":0,\"planby\":\"budget\",\"workTypeAsString\":\"[0701] Иное\",\"workTypeDraftAsString\":\"-\",\"draftChanged\":true}]"));
        task.refreshUdf(udf);
        devTaskController.performCommonOperation(task, WORKTASK_GENUSE);
        var response = devTaskController.getResponse();
        ApiAsserts.assertThat(response)
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK);
        var taskDetail = apiController.receiveTask(task.getNumber());
        CommonAssert
                .assertThat(taskDetail)
                .isCorrectUdfList(UDF_SDFEATURE_GENUSE, LOCAL.getId());
    }

    @Test(groups = {"BugTask", "Regression"}, description = "Создать подзадачу копированием", dependsOnMethods = "taskChangeGenuse")
    public void taskCreateSubtaskWithCopy() {
        apiController.updateToken(generateAuthToken(handlerUser));
        task.setDescription(generateString());
        udf = refreshUdf();
        task.refreshTask();
        task.refreshUdf(udf);
        devTaskController.performCommonOperation(task, WORKTASK_CREATESUBTASKS);
        var response = devTaskController.getResponse();
        ApiAsserts.assertThat(response)
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK);
        var catBugTask = apiController.receiveSubTaskByCategory(task.getNumber(), "CAT_DEVTASK");
        var taskCopyDetail = apiController.receiveTask(catBugTask.getNumber());
        var taskOriginalDetail = apiController.receiveTask(task.getNumber());

        CommonAssert
                .assertThat(taskOriginalDetail)
                .isCorrectTaskField("Категория", new JsonPath(taskCopyDetail.asString()).getString("category.id"), "category.id")
                .isCorrectTaskField("Причины отсутствия классификации по модулям или направлениям деятельности", new JsonPath(taskCopyDetail.asString()).getString("udfs.UDF_SD_NOMODULE_REASON.stringValue"), "udfs.UDF_SD_NOMODULE_REASON.stringValue")
                .isCorrectTaskField("Модуль системы", new JsonPath(taskCopyDetail.asString()).getString("udfs.UDF_SD_MODULE.taskValue[0].id"), "udfs.UDF_SD_MODULE.taskValue[0].id")
                .isCorrectTaskField("Услуга", new JsonPath(taskCopyDetail.asString()).getString("udfs.UDF_MIS_SERVICE.listValue[0].id"), "udfs.UDF_MIS_SERVICE.listValue[0].id")
                .isCorrectTaskField("Приёмка задачи", new JsonPath(taskCopyDetail.asString()).getString("udfs.UDF_CDP_ACCEPTANCE.listValue[0].id"), "udfs.UDF_CDP_ACCEPTANCE.listValue[0].id")
                .isCorrectTaskField("Планируемая дата завершения", new JsonPath(taskCopyDetail.asString()).getString("udfs.UDF_WORKTASK_PLANTD.dateValue"), "udfs.UDF_WORKTASK_PLANTD.dateValue")
                .isCorrectTaskField("Проект БДКУ", new JsonPath(taskCopyDetail.asString()).getString("udfs.UDF_PRODUCT.taskValue[0].id"), "udfs.UDF_PRODUCT.taskValue[0].id")
                .isCorrectTaskField("Способ обзора кода", new JsonPath(taskCopyDetail.asString()).getString("udfs.UDF_WORKTASK_WAYCODEREVIEW.listValue[0].code"), "udfs.UDF_WORKTASK_WAYCODEREVIEW.listValue[0].code")
                .isCorrectTaskField("Общеполезность", new JsonPath(taskCopyDetail.asString()).getString("udfs.UDF_SDFEATURE_GENUSE.listValue[0].code"), "udfs.UDF_SDFEATURE_GENUSE.listValue[0].code")
                .isCorrectTaskField("Запрос клиента", new JsonPath(taskCopyDetail.asString()).getString("udfs.UDF_WORKTASK_SDREQUEST.taskValue[0].number"), "udfs.UDF_WORKTASK_SDREQUEST.taskValue[0].number")
                .isCorrectTaskField("Направление деятельности", new JsonPath(taskCopyDetail.asString()).getString("udfs.UDF_CDP_BL.listValue[0].id"), "udfs.UDF_CDP_BL.listValue[0].id")
                .isCorrectTaskUser("Наблюдатель1", new JsonPath(taskCopyDetail.asString()).getString("udfs.UDF_WATCHER.userValue[0].login"), "udfs.UDF_WATCHER.userValue.login")
                .isCorrectTaskUser("Наблюдатель2", new JsonPath(taskCopyDetail.asString()).getString("udfs.UDF_WATCHER.userValue[1].login"), "udfs.UDF_WATCHER.userValue.login")
                .isCorrectTaskUser("Контролер1", new JsonPath(taskCopyDetail.asString()).getString("udfs.UDF_WORKTASK_SUPERVISER.userValue[0].login"), "udfs.UDF_WORKTASK_SUPERVISER.userValue.login")
                .isCorrectTaskUser("Контролер2", new JsonPath(taskCopyDetail.asString()).getString("udfs.UDF_WORKTASK_SUPERVISER.userValue[1].login"), "udfs.UDF_WORKTASK_SUPERVISER.userValue.login")
                .isCorrectTaskField("Ответственный", new JsonPath(taskCopyDetail.asString()).getString("handlerUser.login"), "handlerUser.login")
                .isCorrectTaskField("Автор", new JsonPath(taskCopyDetail.asString()).getString("submitterUser.login"), "submitterUser.login")
                .isCorrectTaskField("Статус", new JsonPath(taskCopyDetail.asString()).getString("status.id"), "status.id");
    }

    @Test(groups = {"BugTask", "Regression"}, description = "Проверить открытие формы  MSG_WORKTASK_FINISHDEV", dependsOnMethods = "taskCreateSubtaskWithCopy")
    public void taskFinishDevForm() {
        apiController.updateToken(generateAuthToken(handlerUser));
        var response = apiController.receiveFinishDevForm(task.getNumber());
        CommonAssert
                .assertThat(response)
                .isCorrectTaskField("Функционально зависима от задачи = данные из ш.14", HEAD_BOOK.number, "udfs.UDF_WORKTASK_DEPENDTASKFNC.taskValue[0].number")
                .isCorrectTaskField("Доработка документации = нет", "NO", "udfs.UDF_SDFEATURE_DOCREVISION.listValue[0].code");
    }

    @Test(groups = {"BugTask", "Regression"}, description = "Закончить разработку", dependsOnMethods = "taskFinishDevForm")
    public void taskFinishDev() {
        apiController.updateToken(generateAuthToken(handlerUser));
        task.setDescription(generateString());
        udf = refreshUdf();
        task.refreshTask();
        udf.setUdfMemo(generateUdfMemo(UDF_CDP_STEPPROGRESS, "[{\"id\":\"8181816c89cef25c018a6f32b56b4b3c\",\"name\":\"Предварительный анализ\",\"order\":0,\"taskId\":\"" + task.getId() +
                "\",\"progress\":0,\"description\":\"\",\"status\":\"DELETE\",\"hrs\":0,\"deletable\":true,\"actualBudget\":0,\"planby\":\"budget\",\"workTypeAsString\":\"-\",\"workTypeDraftAsString\":\"-\",\"draftChanged\":true},{\"id\":\"8181816c89cef25c018a6f33e9c14c13\",\"name\":\"Пошаговый план 1\",\"order\":1,\"taskId\":\"" + task.getId() +
                "\",\"weight\":1,\"budget\":7200,\"progress\":0,\"description\":\"\",\"status\":\"ACTUAL\",\"workTypeId\":\"402881c2516c220101516c711ff80024\",\"workTypeNorm\":2.0,\"hrs\":0,\"deletable\":true,\"actualBudget\":0,\"planby\":\"budget\",\"workTypeAsString\":\"[0701] Иное\",\"workTypeDraftAsString\":\"-\",\"draftChanged\":true},{\"id\":\"8181816c89cef25c018a6f33e9c14c14\",\"name\":\"Пошаговый план 2\",\"order\":2,\"taskId\":\"" + task.getId() +
                "\",\"weight\":1,\"budget\":7200,\"progress\":0,\"description\":\"\",\"status\":\"ACTUAL\",\"workTypeId\":\"402881c2516c220101516c711ff80024\",\"workTypeNorm\":2.0,\"hrs\":0,\"deletable\":true,\"actualBudget\":0,\"planby\":\"budget\",\"workTypeAsString\":\"[0701] Иное\",\"workTypeDraftAsString\":\"-\",\"draftChanged\":true}]"));

        var testPLanDescription = generateString();
        udf.setSecondUdfMemo(generateUdfMemo(UDF_WORKTASK_TESTPLAN, testPLanDescription));

        Map<Task.Constants, UserData> dependTaskFNC = new HashMap<>();
        var userData1 = new UserData(null, "{\"type\":\"REQUIRED\",\"comment\":\"Required\"}");
        dependTaskFNC.put(HEAD_BOOK, userData1);
        udf.setUdfTask(generateUdfTask(UDF_WORKTASK_DEPENDTASKFNC, dependTaskFNC));
        udf.setUdfList(generateUdfList(UDF_SDFEATURE_DOCREVISION, DOC_REVISION_NO));
        task.setConfirmed(true);
        task.refreshUdf(udf);
        devTaskController.performCommonOperation(task, WORKTASK_FINISHDEV);
        var response = devTaskController.getResponse();
        ApiAsserts
                .assertThat(response)
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .assertTask()
                .isCorrectStatus(STATUS_WORKTASK_CLOSED);

        var taskDetail = apiController.receiveTask(task.getNumber());
        CommonAssert
                .assertThat(taskDetail)
                .isCorrectUdfMemo(UDF_WORKTASK_TESTPLAN, testPLanDescription)
                .isCorrectUdfList(UDF_SDFEATURE_DOCREVISION, DOC_REVISION_NO.getId());
    }
}
