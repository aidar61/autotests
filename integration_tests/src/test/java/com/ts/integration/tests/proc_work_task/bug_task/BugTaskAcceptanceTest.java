package com.ts.integration.tests.proc_work_task.bug_task;

import com.ts.common.application.controllers.TrackStudioHttpStatusCodes;
import com.ts.common.application.database.dbEntities.GrTaskDbEntity;
import com.ts.common.application.database.dbTables.GrTaskTable;
import com.ts.common.asserts.ApiAsserts;
import com.ts.common.asserts.CommonAssert;
import com.ts.common.controllers.TaskResponseBody;
import com.ts.common.controllers.bug.BugTaskController;
import com.ts.common.entitites.commonEntities.Parent;
import com.ts.common.entitites.commonEntities.Status;
import com.ts.common.entitites.commonEntities.User;
import com.ts.common.entitites.commonEntities.udf.UdfTask;
import com.ts.common.entitites.tasks.GeneralTask;
import com.ts.common.entitites.tasks.Task;
import com.ts.common.enums.Operations;
import com.ts.common.enums.TaskType;
import com.ts.common.utils.DateUtils;
import com.ts.common.utils.InitEntities;
import com.ts.common.utils.JsonUtils;
import com.ts.integration.tests.BaseIntegrationTest;
import io.restassured.path.json.JsonPath;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.List;

import static com.ts.common.application.database.DbQueryHelper.Operators.*;
import static com.ts.common.controllers.bug.BugTaskController.getFirstTask;
import static com.ts.common.controllers.bug.BugTaskController.getLastTask;
import static com.ts.common.entitites.commonEntities.List.Constants.*;
import static com.ts.common.entitites.commonEntities.Status.Priority.NORMAL;
import static com.ts.common.entitites.commonEntities.Task.Constants.CORE;
import static com.ts.common.entitites.commonEntities.Task.Constants.KZ_KZI;
import static com.ts.common.entitites.commonEntities.Udfs.UdfSd.*;
import static com.ts.common.entitites.commonEntities.User.Constants.ABDULLAEV_BAHODIR;
import static com.ts.common.enums.Operations.*;
import static com.ts.common.enums.TaskStatuses.*;
import static com.ts.common.enums.TaskType.ADVICE;
import static com.ts.common.enums.TaskType.WORK_TASK;
import static com.ts.common.utils.InitEntities.*;
import static com.ts.common.utils.RandomUtils.generateString;

public class BugTaskAcceptanceTest extends BaseIntegrationTest {
    public BugTaskController bugTaskController;
    private GeneralTask task;
    private String MIS_SERVICE;
    private String CDP_BL;
    private com.ts.common.entitites.commonEntities.Task[] PRODUCT;
    private Parent parent;
    private User creator;
    private User handlerUser;
    private String description;
    private String moduleReason;
    private String testPlan;
    private String taskNumber;
    private List<Task> subTasksAcceptWork;


    @BeforeClass(alwaysRun = true)
    public void beforeClass() {
        bugTaskController = apiController.getBugTaskController();
        userController = apiController.getUserController();
        GrTaskTable grTaskTable = dbHelper.getGrTaskTable();
        GrTaskDbEntity parentTaskFromDb = (GrTaskDbEntity) grTaskTable.receiveRandomTask(
                "task_category", EQUAL.operator, "CAT_GENPLAN",
                AND.operator,
                "task_status", EQUAL.operator, STATUS_PROJECT_PLANNED.name(),
                AND.operator,
                "task_number", EQUAL.operator, "951569",
                AND.operator,
                "task_path", LIKE.operator, "%/2405/758009%");
        parent = InitEntities.generateParent(parentTaskFromDb.getTask_id(), parentTaskFromDb.getTask_number());
        task = InitEntities.getGeneralTask(TaskType.BUG_TASK, Operations.CAT);

        var parentPayloadResponse = bugTaskController.getParentPayload(parent.getNumber(), "CAT_BUGTASK");
        ApiAsserts.assertThat(parentPayloadResponse).isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK);
        var parentPayload = JsonUtils.removeExtraCharacters(parentPayloadResponse);
        MIS_SERVICE = bugTaskController.getParent_UDF_MIS_SERVICE(parentPayload).get(0);
        CDP_BL = bugTaskController.getParent_UDF_CDP_BL(parentPayload).get(0);
        PRODUCT = bugTaskController.getParent_UDF_PRODUCT(parentPayload);

        var employees = userController.receiveUserByTask(parent.getNumber());
        creator = employees.stream().filter(f -> f.getAssignedRole().getName().equals("Менеджер проекта") && f.getForUser().getActive()).findFirst().get().getForUser();
        handlerUser = employees.stream().filter(f -> f.getAssignedRole().getName().equals("Участник проекта") && f.getForUser().getActive() && !f.getForUser().getLogin().equals(creator.getLogin())).findFirst().get().getForUser();
    }


    @Test(groups = {"PROC_WORKTASK", "Regression"}, description = "создание CAT_BUGTASK")
    public void bugTask() {
        apiController.updateToken(InitEntities.generateAuthToken(creator));
        task.setParent(parent);
        task.setName("Задача на исправление ошибки: " + DateUtils.getCurrentDate(0));
        description = "<table border=\"1\" cellpadding=\"0\" cellspacing=\"0\" class=\"general\" " +
                "style=\"border:1px solid\">\n\t<tbody>\n\t\t<tr>\n\t\t\t<th style=\"text-align: left; background-color: " +
                "rgb(235, 241, 245); border-color: rgb(119, 119, 119);\" width=\"30%\"><span style=\"font-size:11px\">" +
                "<strong>" + generateString() + "</strong><strong>*</strong></span></th>\n\t\t\t<td data-required=\"true\" " +
                "style=\"border-color:#777777; height:1px; text-align:left\"width=\"70%\">1000x2000</td>\n\t\t</tr>\n\t\t" +
                "<tr>\n\t\t\t<th style=\"text-align: left; background-color: rgb(235, 241, 245); border-color: rgb(119, 119, 119);\" " +
                "width=\"30%\"><span style=\"font-size:11px\"><span style=\"font-family:Arial,Helvetica,sans-serif\"><strong>Клиентская часть*" +
                "</strong></span></span></th>\n\t\t\t<td data-required=\"true\" style=\"border-color:#777777; height:1px; text-align:left\"" +
                "width=\"70%\"><span style=\"font-size:11px\"><span style=\"font-family:Arial,Helvetica,sans-serif\">" +
                "<strong>Клиентская часть*</strong></span></span></td>\n\t\t</tr>\n\t\t<tr>\n\t\t\t<th style=\"text-align: " +
                "left; background-color: rgb(235, 241, 245); border-color: rgb(119, 119, 119);\"><span style=\"font-size:11px\">" +
                "<span style=\"font-family:Arial,Helvetica,sans-serif\"><strong>Инстанция для тестирования*</strong></span" +
                "></span></th>\n\t\t\t<td data-required=\"true\" style=\"border-color:#777777; height:1px; text-align:left\">" +
                "<span style=\"font-size:11px\"><span style=\"font-family:Arial,Helvetica,sans-serif\"><strong>Инстанция для тестирования*" +
                "</strong></span></span></td>\n\t\t</tr>\n\t\t<tr>\n\t\t\t<th style=\"text-align: left; background-color: rgb(235, 241, 245); " +
                "border-color: rgb(119, 119, 119);\"><span style=\"font-size:11px\"><span style=\"font-family:Arial,Helvetica,sans-serif\">" +
                "<strong>Пользователь / пароль*</strong></span></span></th>\n\t\t\t<td data-required=\"true\" style=\"border-color:#777777;" +
                "height:1px; text-align:left\"><span style=\"font-size:11px\"><span style=\"font-family:Arial,Helvetica,sans-serif\">" +
                "<strong>Пользователь / пароль*</strong></span></span></td>\n\t\t</tr>\n\t\t<tr>\n\t\t\t<th style=\"text-align: left; background-color: " +
                "rgb(235, 241, 245); border-color: rgb(119, 119, 119);\"><span style=\"font-size:11px\"><span style=\"font-family:Arial,Helvetica,sans-serif\">" +
                "<strong>АРМ пользователя*</strong></span></span></th>\n\t\t\t<td data-required=\"true\" style=\"border-color:#777777; height:1px; text-align:left\">" +
                "<span style=\"font-size:11px\"><span style=\"font-family:Arial,Helvetica,sans-serif\"><strong>АРМ пользователя*</strong></span></span>" +
                "</td>\n\t\t</tr>\n\t\t<tr>\n\t\t\t<th style=\"text-align: left; background-color: rgb(235, 241, 245); border-color: rgb(119, 119, 119);\">" +
                "<span style=\"font-size:11px\"><span style=\"font-family:Arial,Helvetica,sans-serif\"><strong>Версия модуля*</strong></span></span></th>\n\t\t\t" +
                "<td data-required=\"true\" style=\"border-color:#777777; height:1px; text-align:left\"><span style=\"font-size:11px\"><span style=\"font-family:Arial," +
                "Helvetica,sans-serif\"><strong>Версия модуля*</strong></span></span></td>\n\t\t</tr>\n\t\t<tr>\n\t\t\t<th style=\"text-align: left; background-color: " +
                "rgb(235, 241, 245); border-color: rgb(119, 119, 119);\"><span style=\"font-size:11px\"><span style=\"font-family:Arial,Helvetica,sans-serif\">" +
                "<strong>Операционный день*</strong></span></span></th>\n\t\t\t<td data-required=\"true\" style=\"border-color:#777777; text-align:left\">" +
                "<span style=\"font-size:11px\"><span style=\"font-family:Arial,Helvetica,sans-serif\"><strong>Операционный день*</strong></span>" +
                "</span></td>\n\t\t</tr>\n\t\t<tr>\n\t\t\t<th style=\"text-align: left; background-color: rgb(235, 241, 245); border-color: " +
                "rgb(119, 119, 119);\"><span style=\"font-size:11px\"><span style=\"font-family:Arial,Helvetica,sans-serif\"><strong>" +
                "Воспроизведение</strong></span></span></th>\n\t\t\t<td style=\"border-color:#777777; height:1px; text-align:left\"><span " +
                "style=\"font-size:11px\"><span style=\"font-family:Arial,Helvetica,sans-serif\"><strong>Воспроизведение</strong></span>" +
                "</span></td>\n\t\t</tr>\n\t\t<tr>\n\t\t\t<th style=\"text-align: left; background-color: rgb(235, 241, 245); border-color: " +
                "rgb(119, 119, 119);\"><span style=\"font-size:11px\"><span style=\"font-family:Arial,Helvetica,sans-serif\">" +
                "<strong>Ожидаемый результат</strong></span></span></th>\n\t\t\t<td style=\"border-color:#777777; " +
                "height:1px; text-align:left\"><span style=\"font-size:11px\"><span style=\"font-family:Arial,Helvetica,sans-serif\">" +
                "<strong>Ожидаемый результат</strong></span></span></td>\n\t\t</tr>\n\t\t<tr>\n\t\t\t<th style=\"text-align: left; " +
                "background-color: rgb(235, 241, 245); border-color: rgb(119, 119, 119);\"><span style=\"font-size:11px\">" +
                "<span style=\"font-family:Arial,Helvetica,sans-serif\"><strong>Фактический результат</strong></span></span>" +
                "</th>\n\t\t\t<td style=\"border-color:#777777; height:1px; text-align:left\"><span style=\"font-size:11px\">" +
                "<span style=\"font-family:Arial,Helvetica,sans-serif\"><strong>Фактический результат</strong></span></span>" +
                "</td>\n\t\t</tr>\n\t</tbody>\n</table>\n";
        task.setDescription(description);
        task.setPriority(Status.builder().id(NORMAL.getId()).build());
        task.setHandlerUser(handlerUser);
        udf = refreshUdf();
        udf.setUdfList(generateUdfList(UDF_WORKTASK_SEVERITY, UDF_WORKTASK_SEVERITY_TRIVIAL));
        udf.setUdfUser(generateUdfUser(UDF_WORKTASK_SUPERVISER, ABDULLAEV_BAHODIR));
        if (CDP_BL != null)
            udf.setThirdUdfList(generateUdfList(UDF_CDP_BL, CDP_BL));
        udf.setUdfTask(generateUdfTask(UDF_SD_MODULE, CORE));
        moduleReason = generateString();
        udf.setUdfString(generateUdfString(UDF_SD_NOMODULE_REASON, moduleReason));
        if (MIS_SERVICE != null)
            udf.setFourthUdfList(generateUdfList(UDF_MIS_SERVICE, MIS_SERVICE));
        udf.setFifthUdfList(generateUdfList(UDF_CDP_ACCEPTANCE, REQBYAUTHOR));
        udf.setSixthUdfList(generateUdfList(UDF_WORKTASK_ANALYSIS, YES_V2));
        udf.setUdfDate(generateUdfDate(UDF_WORKTASK_ANALYSISFD, 1));
        udf.setSecondUdfTask(generateUdfTask(UDF_PRODUCT, PRODUCT[0]));
        udf.setSeventhUdfList(generateUdfList(UDF_WORKTASK_WAYCODEREVIEW, WAY_CODE_REVIEW_OFF));
        udf.setSeventhUdfTask(generateUdfTask(UDF_WORKTASK_WORK, KZ_KZI));
        task.refreshUdf(udf);
        bugTaskController.createBagTask(task);
        var response = bugTaskController.getResponse();
        ApiAsserts.assertThat(response)
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isCorrectStatus(STATUS_WORKTASK_ONANALYSIS)
                .isEquals(task);
        CommonAssert.assertThat(response)
                .isCorrectUdfList(UDF_CDP_ACCEPTANCE, REQBYAUTHOR.getId());
    }

    @Test(groups = {"PROC_WORKTASK", "Regression"}, description = "Коррекция плана", dependsOnMethods = "bugTask")
    public void changePlan() {
        apiController.updateToken(generateAuthToken(handlerUser));
        task.refreshTask();
        udf = refreshUdf();
        udf.setUdfDate(generateUdfDate(UDF_WORKTASK_AWAITTD, 0));
        udf.setUdfDouble(generateUdfDouble(UDF_WORKTASK_AWAITBUDGET, 4));
        udf.setSecondUdfDouble(generateUdfDouble(UDF_CDP_NORMBUDGET, 4));
        udf.setUdfMemo(generateUdfMemo(UDF_CDP_STEPPLAN,
                "[{\"id\":\"8181816c8a92f9a7018b5af148fa3188\",\"name\":\"Предварительный анализ\",\"order\":0,\"taskId\":\"" + task.getId() +
                        "\",\"weight\":0,\"budget\":7200,\"progress\":0,\"description\":\"\",\"status\":\"ACTUAL\",\"workTypeId\":\"402881c2516c220101516c711ff80024\",\"workTypeNorm\":2,\"hrs\":0,\"deletable\":true,\"actualBudget\":0,\"planby\":\"budget\",\"draftChanged\":true,\"workTypeAsString\":\"[0701] Иное\",\"workTypeDraftAsString\":\"-\",\"orderDraft\":0,\"nameDraft\":\"Предварительный анализ\",\"descriptionDraft\":\"\",\"statusDraft\":\"ACTUAL\",\"weightDraft\":0,\"budgetDraft\":7200,\"budgetHrs\":2,\"budgetMinutes\":0,\"workTypeIdDraft\":\"402881c2516c220101516c711ff80024\",\"workTypeNormDraft\":2},{\"orderDraft\":1,\"weightDraft\":1,\"budgetDraft\":7200,\"planby\":\"budget\",\"deletable\":true,\"statusDraft\":\"NEW\",\"workTypeIdDraft\":\"402881c2516c220101516c711ff80024\",\"workTypeNormDraft\":2,\"budgetHrs\":\"2\",\"budgetMinutes\":\"0\",\"nameDraft\":\"Предварительный анализ 2\"}]",
                "{\"autoFinishAnalysis\": \"true\"}"));
        udf.setSecondUdfMemo(generateUdfMemo(UDF_CDP_STEPPROGRESS, "[{\"id\":\"8181816c8a92f9a7018b5af148fa3188\",\"name\":\"Предварительный анализ\",\"order\":0,\"taskId\":\"" + task.getId() +
                "\",\"weight\":0,\"budget\":7200,\"progress\":0,\"description\":\"\",\"status\":\"ACTUAL\",\"workTypeId\":\"402881c2516c220101516c711ff80024\",\"workTypeNorm\":2.0,\"hrs\":0,\"deletable\":true,\"actualBudget\":0,\"planby\":\"budget\",\"draftChanged\":true,\"workTypeAsString\":\"[0701] Иное\",\"workTypeDraftAsString\":\"-\"}]"));
        task.refreshUdf(udf);
        task.setConfirmed(true);
        bugTaskController.performCommonOperation(task, CHANGE_PLAN);
        ApiAsserts.assertThat(bugTaskController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isCorrectStatus(STATUS_WORKTASK_ASSIGNED);
    }

    @Test(groups = {"PROC_WORKTASK", "Regression"}, description = "Принять в работу", dependsOnMethods = "changePlan")
    public void taskStart() {
        apiController.updateToken(generateAuthToken(handlerUser));
        task.refreshTask();
        udf = refreshUdf();
        task.setHandlerUser(handlerUser);
        task.setDescription(generateString());
        udf.setUdfDate(generateUdfDate(UDF_WORKTASK_PLANTD, 0));
        udf.setUdfDouble(generateUdfDouble(UDF_WORKTASK_PLANBUDGET, 4));
        udf.setUdfMemo(generateUdfMemo(UDF_CDP_STEPPROGRESS, "[{\"id\":\"8181816c8a92f9a7018b5af148fa3188\",\"name\":\"Предварительный анализ\",\"order\":0,\"taskId\":\"" + task.getId() +
                "\",\"weight\":0,\"budget\":7200,\"progress\":100,\"description\":\"\",\"status\":\"ACTUAL\",\"workTypeId\":\"402881c2516c220101516c711ff80024\",\"workTypeNorm\":2.0,\"hrs\":0,\"deletable\":true,\"actualBudget\":0,\"planby\":\"budget\",\"draftChanged\":true,\"workTypeAsString\":\"[0701] Иное\",\"workTypeDraftAsString\":\"-\"},{\"id\":\"8181816c8a92f9a7018b5b105e88320b\",\"name\":\"Предварительный анализ 2\",\"order\":1,\"taskId\":\"8181816c8a92f9a7018b5af10346316b\",\"weight\":1,\"budget\":7200,\"progress\":0,\"description\":\"\",\"status\":\"ACTUAL\",\"workTypeId\":\"402881c2516c220101516c711ff80024\",\"workTypeNorm\":2.0,\"hrs\":0,\"deletable\":true,\"actualBudget\":0,\"planby\":\"budget\",\"draftChanged\":true,\"workTypeAsString\":\"[0701] Иное\",\"workTypeDraftAsString\":\"-\"}]"));
        udf.setUdfUser(generateUdfUser(STDT_HANDLER, handlerUser));
        task.refreshUdf(udf);
        bugTaskController.performCommonOperation(task, ACCEPT_IN_WORK);
        ApiAsserts.assertThat(bugTaskController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isCorrectStatus(STATUS_WORKTASK_INWORK);
    }

    @Test(groups = {"PROC_WORKTASK", "Regression"}, description = "Передать исправление на приёмку", dependsOnMethods = "taskStart")
    public void taskToAcceptanceBug() {
        apiController.updateToken(generateAuthToken(handlerUser));
        task.refreshTask();
        udf = refreshUdf();
        task.setDescription(generateString());
        testPlan = generateString();
        var errorDescription = generateString();
        udf.setUdfMemo(generateUdfMemo(UDF_WORKTASK_TESTPLAN, testPlan));
        udf.setUdfList(generateUdfList(UDF_WORKTASK_REASONERROR, UDF_WORKTASK_REASONERROR_REQ));
        udf.setSecondUdfList(generateUdfList(UDF_WORKTASK_FROMSDREQUEST, UDF_WORKTASK_FROM_SD_REQUEST_YES));
        udf.setSecondUdfMemo(generateUdfMemo(UDF_WORKTASK_ERRORDESCRIPTION, errorDescription));
        udf.setThirdUdfList(generateUdfList(UDF_WORKTASK_DEVREGVIOLATION, UDF_WORKTASK_DEVREGVIOLATION_YES));
        udf.setFourthUdfList(generateUdfList(UDF_SDFEATURE_DOCREVISION, NO));
        udf.setUdfUser(generateUdfUser(UDF_WORKTASK_ACCEPTOR, creator));
        udf.setThirdUdfMemo(generateUdfMemo(UDF_CDP_STEPPROGRESS, "[{\"id\":\"8181816c8a92f9a7018b5af148fa3188\",\"name\":\"Предварительный анализ\",\"order\":0,\"taskId\":\"" + task.getId() +
                "\",\"weight\":0,\"budget\":7200,\"progress\":100,\"description\":\"\",\"status\":\"ACTUAL\",\"workTypeId\":\"402881c2516c220101516c711ff80024\",\"workTypeNorm\":2.0,\"hrs\":0,\"deletable\":true,\"actualBudget\":0,\"planby\":\"budget\",\"draftChanged\":true,\"workTypeAsString\":\"[0701] Иное\",\"workTypeDraftAsString\":\"-\"},{\"id\":\"8181816c8a92f9a7018b5b105e88320b\",\"name\":\"Предварительный анализ 2\",\"order\":1,\"taskId\":\"8181816c8a92f9a7018b5af10346316b\",\"weight\":1,\"budget\":7200,\"progress\":0,\"description\":\"\",\"status\":\"ACTUAL\",\"workTypeId\":\"402881c2516c220101516c711ff80024\",\"workTypeNorm\":2.0,\"hrs\":0,\"deletable\":true,\"actualBudget\":0,\"planby\":\"budget\",\"draftChanged\":true,\"workTypeAsString\":\"[0701] Иное\",\"workTypeDraftAsString\":\"-\"}]"));
        task.refreshUdf(udf);
        bugTaskController.performCommonOperation(task, TO_ACCEPTANCE_BUG);
        ApiAsserts.assertThat(bugTaskController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask();

        var taskDetail = apiController.receiveTask(task.getNumber());
        CommonAssert.assertThat(taskDetail)
                .isCorrectUdfMemo(UDF_WORKTASK_TESTPLAN, testPlan)
                .isCorrectUdfList(UDF_WORKTASK_REASONERROR, UDF_WORKTASK_REASONERROR_REQ.getId())
                .isCorrectUdfList(UDF_WORKTASK_FROMSDREQUEST, UDF_WORKTASK_FROM_SD_REQUEST_YES.getId())
                .isCorrectUdfMemo(UDF_WORKTASK_ERRORDESCRIPTION, errorDescription)
                .isCorrectUdfList(UDF_WORKTASK_DEVREGVIOLATION, UDF_WORKTASK_DEVREGVIOLATION_YES.getId())
                .isCorrectUdfList(UDF_SDFEATURE_DOCREVISION, NO.getId())
                .isCorrectUdfList(UDF_CDP_ACCEPTANCE_STATUS, UDF_CDP_ACCEPTANCE_STATUS_ACCEPTANCE.getId());

        var subTasksResponse = apiController.receiveAllSubTasks(task.getNumber());
        CommonAssert.assertThat(subTasksResponse)
                .isSubtaskCreated("CAT_ACCEPTWORK")
                .isCorrectSubTasksStatus("CAT_ACCEPTWORK", STATUS_ADVICE_AWAIT)
                .isCorrectSubTasksUser("handlerUser", creator.getLogin())
                .isCorrectSubTasksUser("submitterUser", handlerUser.getLogin());
    }

    @Test(groups = {"PROC_WORKTASK", "Regression"}, description = "Отредактировать CAT_BUGTASK", dependsOnMethods = "taskToAcceptanceBug")
    public void updateTask() {
        apiController.updateToken(InitEntities.generateAuthToken(creator));
        task.refreshTask();
        udf = refreshUdf();
        task.setParent(parent);
        task.setName("Задача на исправление ошибки: " + DateUtils.getCurrentDate(0));
        task.setDescription(description);
        task.setPriority(Status.builder().id(NORMAL.getId()).build());
        udf.setUdfString(generateUdfString(UDF_SD_NOMODULE_REASON, moduleReason));
        udf.setUdfMemo(generateUdfMemo(UDF_WORKTASK_TESTPLAN, testPlan));
        udf.setSecondUdfTask(generateUdfTask(UDF_PRODUCT, PRODUCT[0]));
        udf.setUdfTask(generateUdfTask(UDF_SD_MODULE, CORE));
        udf.setSeventhUdfTask(generateUdfTask(UDF_WORKTASK_WORK, KZ_KZI));
        udf.setFifthUdfList(generateUdfList(UDF_CDP_ACCEPTANCE, REQBYCONRTOLLER));
        if (CDP_BL != null)
            udf.setThirdUdfList(generateUdfList(UDF_CDP_BL, CDP_BL));
        task.refreshUdf(udf);
        bugTaskController.updateBugTask(task);
        var response = bugTaskController.getResponse();
        ApiAsserts.assertThat(response)
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isEquals(task);
        CommonAssert
                .assertThat(response)
                .isCorrectUdfList(UDF_CDP_ACCEPTANCE, REQBYCONRTOLLER.getId());
    }

    @Test(groups = {"PROC_WORKTASK", "Regression"}, description = "Передать исправление на приёмку 2", dependsOnMethods = "updateTask")
    public void taskToAcceptanceBug2() {
        apiController.updateToken(generateAuthToken(handlerUser));
        task.refreshTask();
        udf = refreshUdf();
        task.setDescription(generateString());
        testPlan = generateString();
        var errorDescription = generateString();
        udf.setUdfMemo(generateUdfMemo(UDF_WORKTASK_TESTPLAN, testPlan));
        udf.setUdfList(generateUdfList(UDF_WORKTASK_REASONERROR, UDF_WORKTASK_REASONERROR_DBL));
        udf.setSecondUdfList(generateUdfList(UDF_WORKTASK_FROMSDREQUEST, UDF_WORKTASK_FROM_SD_REQUEST_YES));
        udf.setSecondUdfMemo(generateUdfMemo(UDF_WORKTASK_ERRORDESCRIPTION, errorDescription));
        udf.setThirdUdfList(generateUdfList(UDF_WORKTASK_DEVREGVIOLATION, UDF_WORKTASK_DEVREGVIOLATION_YES));
        udf.setFourthUdfList(generateUdfList(UDF_SDFEATURE_DOCREVISION, DOC_REVISION_YES));
        udf.setUdfUser(generateUdfUser(UDF_WORKTASK_ACCEPTOR, ABDULLAEV_BAHODIR));
        udf.setThirdUdfMemo(generateUdfMemo(UDF_CDP_STEPPROGRESS, "[{\"id\":\"8181816c8a92f9a7018b5af148fa3188\",\"name\":\"Предварительный анализ\",\"order\":0,\"taskId\":\"" + task.getId() +
                "\",\"weight\":0,\"budget\":7200,\"progress\":100,\"description\":\"\",\"status\":\"ACTUAL\",\"workTypeId\":\"402881c2516c220101516c711ff80024\",\"workTypeNorm\":2.0,\"hrs\":0,\"deletable\":true,\"actualBudget\":0,\"planby\":\"budget\",\"draftChanged\":true,\"workTypeAsString\":\"[0701] Иное\",\"workTypeDraftAsString\":\"-\"},{\"id\":\"8181816c8a92f9a7018b5b105e88320b\",\"name\":\"Предварительный анализ 2\",\"order\":1,\"taskId\":\"8181816c8a92f9a7018b5af10346316b\",\"weight\":1,\"budget\":7200,\"progress\":0,\"description\":\"\",\"status\":\"ACTUAL\",\"workTypeId\":\"402881c2516c220101516c711ff80024\",\"workTypeNorm\":2.0,\"hrs\":0,\"deletable\":true,\"actualBudget\":0,\"planby\":\"budget\",\"draftChanged\":true,\"workTypeAsString\":\"[0701] Иное\",\"workTypeDraftAsString\":\"-\"}]"));
        task.refreshUdf(udf);
        bugTaskController.performCommonOperation(task, TO_ACCEPTANCE_BUG);
        ApiAsserts.assertThat(bugTaskController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask();

        var taskDetail = apiController.receiveTask(task.getNumber());
        CommonAssert.assertThat(taskDetail)
                .isCorrectUdfMemo(UDF_WORKTASK_TESTPLAN, testPlan)
                .isCorrectUdfList(UDF_WORKTASK_REASONERROR, UDF_WORKTASK_REASONERROR_DBL.getId())
                .isCorrectUdfList(UDF_WORKTASK_FROMSDREQUEST, UDF_WORKTASK_FROM_SD_REQUEST_YES.getId())
                .isCorrectUdfMemo(UDF_WORKTASK_ERRORDESCRIPTION, errorDescription)
                .isCorrectUdfList(UDF_WORKTASK_DEVREGVIOLATION, UDF_WORKTASK_DEVREGVIOLATION_YES.getId())
                .isCorrectUdfList(UDF_SDFEATURE_DOCREVISION, DOC_REVISION_YES.getId())
                .isCorrectUdfList(UDF_CDP_ACCEPTANCE_STATUS, UDF_CDP_ACCEPTANCE_STATUS_ACCEPTANCE.getId());

        var subTasksResponse = apiController.receiveAllSubTasks(task.getNumber());
        subTasksAcceptWork = new JsonPath(subTasksResponse.asString()).getList("tasks", Task.class);

        CommonAssert.assertThat(subTasksResponse)
                .isSubtaskCreatedWithCount("CAT_ACCEPTWORK", 2)
                .isCorrectSubTasksStatus("CAT_ACCEPTWORK", STATUS_ADVICE_AWAIT)
                .isCorrectSubTasksUser("handlerUser", creator.getLogin())
                .isCorrectSubTasksUser("submitterUser", handlerUser.getLogin());
    }

    @Test(groups = {"PROC_WORKTASK", "Regression"}, description = "В первой задаче CAT_ACCEPTWORK выполнить Принять и закрыть", dependsOnMethods = "taskToAcceptanceBug2")
    public void subtaskAcceptAndClose() {
        var firstTask = getFirstTask(subTasksAcceptWork);
        apiController.updateToken(generateAuthToken(firstTask.getHandlerUser()));
        task.refreshTask();
        udf = refreshUdf();
        taskNumber = task.getNumber();
        task.setNumber(firstTask.getNumber());
        task.refreshUdf(udf);
        bugTaskController.changeTaskType(ADVICE);
        bugTaskController.performCommonOperation(task, ACCEPT_AND_CLOSE);
        ApiAsserts.assertThat(bugTaskController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isCorrectStatus(STATUS_ADVICE_CLOSED);

        var taskDetail = apiController.receiveTask(task.getNumber());
        CommonAssert
                .assertThat(taskDetail)
                .isCorrectUdfList(UDF_CDP_ACCEPTANCE_STATUS, UDF_CDP_ACCEPTANCE_STATUS_ACCEPTED.getId());
    }

    @Test(groups = {"PROC_WORKTASK", "Regression"}, description = "Проверить CAT_BUGTASK", dependsOnMethods = "subtaskAcceptAndClose")
    public void checkBugTask() {
        apiController.updateToken(generateAuthToken(handlerUser));
        task.refreshTask();
        task.setNumber(taskNumber);
        bugTaskController.changeTaskType(WORK_TASK);
        var taskDetail = apiController.receiveTask(task.getNumber());
        CommonAssert
                .assertThat(taskDetail)
                .isCorrectUdfList(UDF_CDP_ACCEPTANCE_STATUS, UDF_CDP_ACCEPTANCE_STATUS_ACCEPTANCE.getId());
    }

    @Test(groups = {"PROC_WORKTASK", "Regression"}, description = "Во второй задаче CAT_ACCEPTWORK выполнить Отклонить изменения", dependsOnMethods = "checkBugTask")
    public void subtaskDecline() {
        var lastTask = getLastTask(subTasksAcceptWork);
        apiController.updateToken(generateAuthToken(lastTask.getHandlerUser()));
        task.refreshTask();
        udf = refreshUdf();
        task.setNumber(lastTask.getNumber());
        task.refreshUdf(udf);
        bugTaskController.changeTaskType(ADVICE);
        bugTaskController.performCommonOperation(task, DECLINE_ACCEPT);
        ApiAsserts.assertThat(bugTaskController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isCorrectStatus(STATUS_ADVICE_CHANGEDECLINED);

        var taskDetail = apiController.receiveTask(task.getNumber());
        CommonAssert
                .assertThat(taskDetail)
                .isCorrectUdfList(UDF_CDP_ACCEPTANCE_STATUS, UDF_CDP_ACCEPTANCE_STATUS_REJECTED.getId());
    }

    @Test(groups = {"PROC_WORKTASK", "Regression"}, description = "Проверить CAT_BUGTASK 2", dependsOnMethods = "subtaskDecline")
    public void checkBugTask2() {
        apiController.updateToken(generateAuthToken(handlerUser));
        task.refreshTask();
        task.setNumber(taskNumber);
        bugTaskController.changeTaskType(WORK_TASK);
        var taskDetail = apiController.receiveTask(task.getNumber());
        CommonAssert
                .assertThat(taskDetail)
                .isCorrectUdfList(UDF_CDP_ACCEPTANCE_STATUS, UDF_CDP_ACCEPTANCE_STATUS_REJECTED.getId());
    }

    @Test(groups = {"PROC_WORKTASK", "Regression"}, description = "Передать исправление на приёмку", dependsOnMethods = "checkBugTask2")
    public void taskToAcceptanceBug3() {
        apiController.updateToken(generateAuthToken(handlerUser));
        task.refreshTask();
        udf = refreshUdf();
        task.setDescription(generateString());
        testPlan = generateString();
        var errorDescription = generateString();
        udf.setUdfMemo(generateUdfMemo(UDF_WORKTASK_TESTPLAN, testPlan));
        udf.setUdfList(generateUdfList(UDF_WORKTASK_REASONERROR, UDF_WORKTASK_REASONERROR_REQ));
        udf.setSecondUdfList(generateUdfList(UDF_WORKTASK_FROMSDREQUEST, UDF_WORKTASK_FROM_SD_REQUEST_YES));
        udf.setSecondUdfMemo(generateUdfMemo(UDF_WORKTASK_ERRORDESCRIPTION, errorDescription));
        udf.setThirdUdfList(generateUdfList(UDF_WORKTASK_DEVREGVIOLATION, UDF_WORKTASK_DEVREGVIOLATION_YES));
        udf.setFourthUdfList(generateUdfList(UDF_SDFEATURE_DOCREVISION, NO));
        udf.setUdfUser(generateUdfUser(UDF_WORKTASK_ACCEPTOR, ABDULLAEV_BAHODIR));
        udf.setThirdUdfMemo(generateUdfMemo(UDF_CDP_STEPPROGRESS, "[{\"id\":\"8181816c8a92f9a7018b5af148fa3188\",\"name\":\"Предварительный анализ\",\"order\":0,\"taskId\":\"" + task.getId() +
                "\",\"weight\":0,\"budget\":7200,\"progress\":100,\"description\":\"\",\"status\":\"ACTUAL\",\"workTypeId\":\"402881c2516c220101516c711ff80024\",\"workTypeNorm\":2.0,\"hrs\":0,\"deletable\":true,\"actualBudget\":0,\"planby\":\"budget\",\"draftChanged\":true,\"workTypeAsString\":\"[0701] Иное\",\"workTypeDraftAsString\":\"-\"},{\"id\":\"8181816c8a92f9a7018b5b105e88320b\",\"name\":\"Предварительный анализ 2\",\"order\":1,\"taskId\":\"8181816c8a92f9a7018b5af10346316b\",\"weight\":1,\"budget\":7200,\"progress\":0,\"description\":\"\",\"status\":\"ACTUAL\",\"workTypeId\":\"402881c2516c220101516c711ff80024\",\"workTypeNorm\":2.0,\"hrs\":0,\"deletable\":true,\"actualBudget\":0,\"planby\":\"budget\",\"draftChanged\":true,\"workTypeAsString\":\"[0701] Иное\",\"workTypeDraftAsString\":\"-\"}]"));
        task.refreshUdf(udf);
        bugTaskController.performCommonOperation(task, TO_ACCEPTANCE_BUG);
        ApiAsserts.assertThat(bugTaskController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask();

        var taskDetail = apiController.receiveTask(task.getNumber());
        CommonAssert.assertThat(taskDetail)
                .isCorrectUdfMemo(UDF_WORKTASK_TESTPLAN, testPlan)
                .isCorrectUdfList(UDF_WORKTASK_REASONERROR, UDF_WORKTASK_REASONERROR_REQ.getId())
                .isCorrectUdfList(UDF_WORKTASK_FROMSDREQUEST, UDF_WORKTASK_FROM_SD_REQUEST_YES.getId())
                .isCorrectUdfMemo(UDF_WORKTASK_ERRORDESCRIPTION, errorDescription)
                .isCorrectUdfList(UDF_WORKTASK_DEVREGVIOLATION, UDF_WORKTASK_DEVREGVIOLATION_YES.getId())
                .isCorrectUdfList(UDF_SDFEATURE_DOCREVISION, NO.getId())
                .isCorrectUdfList(UDF_CDP_ACCEPTANCE_STATUS, UDF_CDP_ACCEPTANCE_STATUS_ACCEPTANCE.getId());

        var subTasksResponse = apiController.receiveAllSubTasks(task.getNumber());
        subTasksAcceptWork = new JsonPath(subTasksResponse.asString()).getList("tasks", Task.class);
        CommonAssert.assertThat(subTasksResponse)
                .isSubtaskCreatedWithCount("CAT_ACCEPTWORK", 2)
                .isCorrectSubTasksStatus("CAT_ACCEPTWORK", STATUS_ADVICE_AWAIT)
                .isCorrectSubTasksUser("handlerUser", creator.getLogin())
                .isCorrectSubTasksUser("submitterUser", handlerUser.getLogin());
    }

    @Test(groups = {"PROC_WORKTASK", "Regression"}, description = "В отркытой CAT_ACCEPTWORK выполнить \"Принять и закрыть\"", dependsOnMethods = "taskToAcceptanceBug3")
    public void subtaskAcceptAndClose2() {
        var lastTask = getLastTask(subTasksAcceptWork);
        apiController.updateToken(generateAuthToken(lastTask.getHandlerUser()));
        task.refreshTask();
        udf = refreshUdf();
        taskNumber = task.getNumber();
        task.setNumber(lastTask.getNumber());
        task.refreshUdf(udf);
        bugTaskController.changeTaskType(ADVICE);
        bugTaskController.performCommonOperation(task, ACCEPT_AND_CLOSE);
        ApiAsserts.assertThat(bugTaskController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isCorrectStatus(STATUS_ADVICE_CLOSED);

        var taskDetail = apiController.receiveTask(task.getNumber());
        CommonAssert
                .assertThat(taskDetail)
                .isCorrectUdfList(UDF_CDP_ACCEPTANCE_STATUS, UDF_CDP_ACCEPTANCE_STATUS_ACCEPTED.getId());
    }

    @Test(groups = {"PROC_WORKTASK", "Regression"}, description = "Проверить CAT_BUGTASK 3", dependsOnMethods = "subtaskAcceptAndClose2")
    public void checkBugTask3() {
        apiController.updateToken(generateAuthToken(handlerUser));
        task.refreshTask();
        task.setNumber(taskNumber);
        bugTaskController.changeTaskType(WORK_TASK);
        var taskDetail = apiController.receiveTask(task.getNumber());
        CommonAssert
                .assertThat(taskDetail)
                .isCorrectUdfList(UDF_CDP_ACCEPTANCE_STATUS, UDF_CDP_ACCEPTANCE_STATUS_ACCEPTED.getId());
    }

    @Test(groups = {"PROC_WORKTASK", "Regression"}, description = "В CAT_BUGTASK выполнить Закончить исправление ошибки", dependsOnMethods = "checkBugTask3")
    public void taskFinishBug() {
        apiController.updateToken(generateAuthToken(handlerUser));
        task.refreshTask();
        udf = refreshUdf();
        task.setDescription(generateString());
        testPlan = generateString();
        var errorDescription = generateString();
        udf.setUdfMemo(generateUdfMemo(UDF_WORKTASK_TESTPLAN, testPlan));
        udf.setUdfList(generateUdfList(UDF_WORKTASK_REASONERROR, UDF_WORKTASK_REASONERROR_REQ));
        udf.setSecondUdfList(generateUdfList(UDF_WORKTASK_FROMSDREQUEST, UDF_WORKTASK_FROM_SD_REQUEST_YES));
        udf.setSecondUdfMemo(generateUdfMemo(UDF_WORKTASK_ERRORDESCRIPTION, errorDescription));
        udf.setThirdUdfList(generateUdfList(UDF_WORKTASK_DEVREGVIOLATION, UDF_WORKTASK_DEVREGVIOLATION_YES));
        udf.setFourthUdfList(generateUdfList(UDF_SDFEATURE_DOCREVISION, NO));
        udf.setFifthUdfList(generateUdfList(UDF_WORKTASK_CREATETESTTASK, UDF_WORKTASK_CREATETESTTASK_NO));
        udf.setThirdUdfMemo(generateUdfMemo(UDF_CDP_STEPPROGRESS, "[{\"id\":\"8181816c8a92f9a7018b5af148fa3188\",\"name\":\"Предварительный анализ\",\"order\":0,\"taskId\":\"" + task.getId() +
                "\",\"weight\":0,\"budget\":7200,\"progress\":100,\"description\":\"\",\"status\":\"ACTUAL\",\"workTypeId\":\"402881c2516c220101516c711ff80024\",\"workTypeNorm\":2.0,\"hrs\":0,\"deletable\":true,\"actualBudget\":0,\"planby\":\"budget\",\"draftChanged\":true,\"workTypeAsString\":\"[0701] Иное\",\"workTypeDraftAsString\":\"-\"},{\"id\":\"8181816c8a92f9a7018b5b105e88320b\",\"name\":\"Предварительный анализ 2\",\"order\":1,\"taskId\":\"8181816c8a92f9a7018b5af10346316b\",\"weight\":1,\"budget\":7200,\"progress\":0,\"description\":\"\",\"status\":\"ACTUAL\",\"workTypeId\":\"402881c2516c220101516c711ff80024\",\"workTypeNorm\":2.0,\"hrs\":0,\"deletable\":true,\"actualBudget\":0,\"planby\":\"budget\",\"draftChanged\":true,\"workTypeAsString\":\"[0701] Иное\",\"workTypeDraftAsString\":\"-\"}]"));
        task.refreshUdf(udf);
        bugTaskController.performCommonOperation(task, FINISH_BUG);
        ApiAsserts.assertThat(bugTaskController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isCorrectStatus(STATUS_WORKTASK_CLOSED);
    }

}