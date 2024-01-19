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
import com.ts.common.entitites.commonEntities.Task;
import com.ts.common.entitites.commonEntities.User;
import com.ts.common.entitites.tasks.GeneralTask;
import com.ts.common.enums.Operations;
import com.ts.common.enums.TaskType;
import com.ts.common.utils.DateUtils;
import com.ts.common.utils.InitEntities;
import com.ts.common.utils.JsonUtils;
import com.ts.integration.tests.BaseIntegrationTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static com.ts.common.application.database.DbQueryHelper.Operators.*;
import static com.ts.common.entitites.commonEntities.List.Constants.*;
import static com.ts.common.entitites.commonEntities.Status.Priority.NORMAL;
import static com.ts.common.entitites.commonEntities.Task.Constants.CORE;
import static com.ts.common.entitites.commonEntities.Task.Constants.KZ_KZI;
import static com.ts.common.entitites.commonEntities.Udfs.UdfSd.*;
import static com.ts.common.enums.Operations.*;
import static com.ts.common.enums.TaskStatuses.*;
import static com.ts.common.enums.TaskType.WORK_TASK;
import static com.ts.common.utils.InitEntities.*;
import static com.ts.common.utils.RandomUtils.generateString;

public class BugTaskReplan1Test extends BaseIntegrationTest {
    public BugTaskController bugTaskController;
    private GeneralTask task;
    private Parent parent;
    private User creator;
    private User handlerUser;
    private String moduleReason;
    private String awaitDate;
    private String firstPlanTdDate;
    private String step8PlanDate;
    private int normBudget;
    private int planBudget;
    private String MIS_SERVICE;
    private String CDP_BL;
    private Task[] PRODUCT;


    @BeforeClass(alwaysRun = true)
    public void beforeClass() {
        bugTaskController = apiController.getBugTaskController();
        userController = apiController.getUserController();
        GrTaskTable grTaskTable = dbHelper.getGrTaskTable();
        GrTaskDbEntity parentTaskFromDb = (GrTaskDbEntity) grTaskTable.receiveRandomTask("task_category", EQUAL.operator, "CAT_GENPLAN", AND.operator, "task_status", EQUAL.operator, STATUS_PROJECT_PLANNED.name(), AND.operator, "task_number", EQUAL.operator, "951569", AND.operator, "task_path", LIKE.operator, "%/2405/758009%");
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


    @Test(groups = {"BugTask", "Regression"}, description = "создание CAT_BUGTASK")
    public void bugTask() {
        apiController.updateToken(InitEntities.generateAuthToken(creator));
        task.setParent(parent);
        task.setName("Задача на исправление ошибки: " + DateUtils.getCurrentDate(0));
        String description = "<table border=\"1\" cellpadding=\"0\" cellspacing=\"0\" class=\"general\" " + "style=\"border:1px solid\">\n\t<tbody>\n\t\t<tr>\n\t\t\t<th style=\"text-align: left; background-color: " + "rgb(235, 241, 245); border-color: rgb(119, 119, 119);\" width=\"30%\"><span style=\"font-size:11px\">" + "<strong>" + generateString() + "</strong><strong>*</strong></span></th>\n\t\t\t<td data-required=\"true\" " + "style=\"border-color:#777777; height:1px; text-align:left\"width=\"70%\">1000x2000</td>\n\t\t</tr>\n\t\t" + "<tr>\n\t\t\t<th style=\"text-align: left; background-color: rgb(235, 241, 245); border-color: rgb(119, 119, 119);\" " + "width=\"30%\"><span style=\"font-size:11px\"><span style=\"font-family:Arial,Helvetica,sans-serif\"><strong>Клиентская часть*" + "</strong></span></span></th>\n\t\t\t<td data-required=\"true\" style=\"border-color:#777777; height:1px; text-align:left\"" + "width=\"70%\"><span style=\"font-size:11px\"><span style=\"font-family:Arial,Helvetica,sans-serif\">" + "<strong>Клиентская часть*</strong></span></span></td>\n\t\t</tr>\n\t\t<tr>\n\t\t\t<th style=\"text-align: " + "left; background-color: rgb(235, 241, 245); border-color: rgb(119, 119, 119);\"><span style=\"font-size:11px\">" + "<span style=\"font-family:Arial,Helvetica,sans-serif\"><strong>Инстанция для тестирования*</strong></span" + "></span></th>\n\t\t\t<td data-required=\"true\" style=\"border-color:#777777; height:1px; text-align:left\">" + "<span style=\"font-size:11px\"><span style=\"font-family:Arial,Helvetica,sans-serif\"><strong>Инстанция для тестирования*" + "</strong></span></span></td>\n\t\t</tr>\n\t\t<tr>\n\t\t\t<th style=\"text-align: left; background-color: rgb(235, 241, 245); " + "border-color: rgb(119, 119, 119);\"><span style=\"font-size:11px\"><span style=\"font-family:Arial,Helvetica,sans-serif\">" + "<strong>Пользователь / пароль*</strong></span></span></th>\n\t\t\t<td data-required=\"true\" style=\"border-color:#777777;" + "height:1px; text-align:left\"><span style=\"font-size:11px\"><span style=\"font-family:Arial,Helvetica,sans-serif\">" + "<strong>Пользователь / пароль*</strong></span></span></td>\n\t\t</tr>\n\t\t<tr>\n\t\t\t<th style=\"text-align: left; background-color: " + "rgb(235, 241, 245); border-color: rgb(119, 119, 119);\"><span style=\"font-size:11px\"><span style=\"font-family:Arial,Helvetica,sans-serif\">" + "<strong>АРМ пользователя*</strong></span></span></th>\n\t\t\t<td data-required=\"true\" style=\"border-color:#777777; height:1px; text-align:left\">" + "<span style=\"font-size:11px\"><span style=\"font-family:Arial,Helvetica,sans-serif\"><strong>АРМ пользователя*</strong></span></span>" + "</td>\n\t\t</tr>\n\t\t<tr>\n\t\t\t<th style=\"text-align: left; background-color: rgb(235, 241, 245); border-color: rgb(119, 119, 119);\">" + "<span style=\"font-size:11px\"><span style=\"font-family:Arial,Helvetica,sans-serif\"><strong>Версия модуля*</strong></span></span></th>\n\t\t\t" + "<td data-required=\"true\" style=\"border-color:#777777; height:1px; text-align:left\"><span style=\"font-size:11px\"><span style=\"font-family:Arial," + "Helvetica,sans-serif\"><strong>Версия модуля*</strong></span></span></td>\n\t\t</tr>\n\t\t<tr>\n\t\t\t<th style=\"text-align: left; background-color: " + "rgb(235, 241, 245); border-color: rgb(119, 119, 119);\"><span style=\"font-size:11px\"><span style=\"font-family:Arial,Helvetica,sans-serif\">" + "<strong>Операционный день*</strong></span></span></th>\n\t\t\t<td data-required=\"true\" style=\"border-color:#777777; text-align:left\">" + "<span style=\"font-size:11px\"><span style=\"font-family:Arial,Helvetica,sans-serif\"><strong>Операционный день*</strong></span>" + "</span></td>\n\t\t</tr>\n\t\t<tr>\n\t\t\t<th style=\"text-align: left; background-color: rgb(235, 241, 245); border-color: " + "rgb(119, 119, 119);\"><span style=\"font-size:11px\"><span style=\"font-family:Arial,Helvetica,sans-serif\"><strong>" + "Воспроизведение</strong></span></span></th>\n\t\t\t<td style=\"border-color:#777777; height:1px; text-align:left\"><span " + "style=\"font-size:11px\"><span style=\"font-family:Arial,Helvetica,sans-serif\"><strong>Воспроизведение</strong></span>" + "</span></td>\n\t\t</tr>\n\t\t<tr>\n\t\t\t<th style=\"text-align: left; background-color: rgb(235, 241, 245); border-color: " + "rgb(119, 119, 119);\"><span style=\"font-size:11px\"><span style=\"font-family:Arial,Helvetica,sans-serif\">" + "<strong>Ожидаемый результат</strong></span></span></th>\n\t\t\t<td style=\"border-color:#777777; " + "height:1px; text-align:left\"><span style=\"font-size:11px\"><span style=\"font-family:Arial,Helvetica,sans-serif\">" + "<strong>Ожидаемый результат</strong></span></span></td>\n\t\t</tr>\n\t\t<tr>\n\t\t\t<th style=\"text-align: left; " + "background-color: rgb(235, 241, 245); border-color: rgb(119, 119, 119);\"><span style=\"font-size:11px\">" + "<span style=\"font-family:Arial,Helvetica,sans-serif\"><strong>Фактический результат</strong></span></span>" + "</th>\n\t\t\t<td style=\"border-color:#777777; height:1px; text-align:left\"><span style=\"font-size:11px\">" + "<span style=\"font-family:Arial,Helvetica,sans-serif\"><strong>Фактический результат</strong></span></span>" + "</td>\n\t\t</tr>\n\t</tbody>\n</table>\n";
        task.setDescription(description);
        task.setPriority(Status.builder().id(NORMAL.getId()).build());
        task.setHandlerUser(handlerUser);
        udf = refreshUdf();
        udf.setUdfList(generateUdfList(UDF_WORKTASK_SEVERITY, UDF_WORKTASK_SEVERITY_TRIVIAL));
        if (CDP_BL != null) udf.setThirdUdfList(generateUdfList(UDF_CDP_BL, CDP_BL));
        udf.setUdfTask(generateUdfTask(UDF_SD_MODULE, CORE));
        moduleReason = generateString();
        udf.setUdfString(generateUdfString(UDF_SD_NOMODULE_REASON, moduleReason));
        if (MIS_SERVICE != null) udf.setFourthUdfList(generateUdfList(UDF_MIS_SERVICE, MIS_SERVICE));
        udf.setFifthUdfList(generateUdfList(UDF_CDP_ACCEPTANCE, UDF_CDP_ACCEPTANCE_NO));
        udf.setSixthUdfList(generateUdfList(UDF_WORKTASK_ANALYSIS, YES_V2));
        udf.setUdfDate(generateUdfDate(UDF_WORKTASK_ANALYSISFD, 1));
        udf.setSecondUdfTask(generateUdfTask(UDF_PRODUCT, PRODUCT[0]));
        udf.setSeventhUdfList(generateUdfList(UDF_WORKTASK_WAYCODEREVIEW, WAY_CODE_REVIEW_OFF));
        udf.setSeventhUdfTask(generateUdfTask(UDF_WORKTASK_WORK, KZ_KZI));
        task.refreshUdf(udf);
        bugTaskController.createBagTask(task);
        var response = bugTaskController.getResponse();
        ApiAsserts.assertThat(response).isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK).isParseableBody(TaskResponseBody.class).assertTask().isCorrectStatus(STATUS_WORKTASK_ONANALYSIS).isEquals(task);
    }

    @Test(groups = {"DevTask", "Regression"}, description = "Продлить предварительный анализ", dependsOnMethods = "bugTask")
    public void extendAnalysis() {
        apiController.updateToken(generateAuthToken(handlerUser));
        task.refreshTask();
        udf = refreshUdf();
        task.setDescription(generateString());
        var analysisDate = DateUtils.getCurrentDate(2);
        udf.setUdfDate(generateUdfDate(UDF_WORKTASK_ANALYSISFD, analysisDate));
        udf.setSecondUdfMemo(generateUdfMemo(UDF_CDP_STEPPROGRESS, "[{\"id\":\"818181698b6d8e12018b8a10b24106c7\",\"name\":\"Предварительный анализ\",\"order\":0,\"taskId\":\"" + task.getId() + "\",\"weight\":0,\"budget\":7200,\"progress\":0,\"description\":\"\",\"status\":\"ACTUAL\",\"workTypeId\":\"402881c2516c220101516c711ff80024\",\"workTypeNorm\":2.0,\"hrs\":0,\"deletable\":true,\"actualBudget\":0,\"planby\":\"budget\",\"workTypeAsString\":\"[0701] Иное\",\"workTypeDraftAsString\":\"-\",\"draftChanged\":true}]"));
        task.refreshUdf(udf);
        bugTaskController.performCommonOperation(task, EXTEND_ANALYSIS);
        ApiAsserts.assertThat(bugTaskController.getResponse()).isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK).isParseableBody(TaskResponseBody.class).assertTask().isCorrectStatus(STATUS_WORKTASK_ONANALYSIS);

        var taskDetail = apiController.receiveTask(task.getNumber());
        CommonAssert.assertThat(taskDetail).isCorrectUdfDate(UDF_WORKTASK_ANALYSISFD, analysisDate);
    }

    @Test(groups = {"DevTask", "Regression"}, description = "Коррекция плана", dependsOnMethods = "extendAnalysis")
    public void changePlan() {
        apiController.updateToken(generateAuthToken(handlerUser));
        task.refreshTask();
        udf = refreshUdf();
        planBudget = 2;
        normBudget = 2;
        awaitDate = DateUtils.getCurrentDate(3);
        udf.setUdfDate(generateUdfDate(UDF_WORKTASK_AWAITTD, awaitDate));
        udf.setUdfDouble(generateUdfDouble(UDF_WORKTASK_AWAITBUDGET, planBudget + normBudget));
        udf.setSecondUdfDouble(generateUdfDouble(UDF_CDP_NORMBUDGET, normBudget + planBudget));
        udf.setUdfMemo(generateUdfMemo(UDF_CDP_STEPPLAN, "[{\"id\":\"818181698b6d8e12018b8fa5eac21135\",\"name\":\"Предварительный анализ\",\"order\":0,\"taskId\":\"" + task.getId() + "\",\"weight\":0,\"budget\":7200,\"progress\":0,\"description\":\"\",\"status\":\"ACTUAL\",\"workTypeId\":\"402881c2516c220101516c711ff80024\",\"workTypeNorm\":2,\"hrs\":0,\"deletable\":true,\"actualBudget\":0,\"planby\":\"budget\",\"workTypeAsString\":\"[0701] Иное\",\"workTypeDraftAsString\":\"-\",\"draftChanged\":true,\"orderDraft\":0,\"nameDraft\":\"Предварительный анализ\",\"descriptionDraft\":\"\",\"statusDraft\":\"ACTUAL\",\"weightDraft\":0,\"budgetDraft\":7200,\"budgetHrs\":\"2\",\"budgetMinutes\":0,\"workTypeIdDraft\":\"402881c2516c220101516c711ff80024\",\"workTypeNormDraft\":2},{\"orderDraft\":1,\"weightDraft\":1,\"budgetDraft\":7200,\"planby\":\"budget\",\"deletable\":true,\"statusDraft\":\"NEW\",\"nameDraft\":\"Предварительный анализ 2\",\"budgetHrs\":\"2\",\"workTypeIdDraft\":\"402881c2516c220101516c711ff80024\",\"workTypeNormDraft\":2}]", "{\"autoFinishAnalysis\": \"true\"}"));
        udf.setSecondUdfMemo(generateUdfMemo(UDF_CDP_STEPPROGRESS, "[{\"id\":\"818181698b6d8e12018b8fa5eac21135\",\"name\":\"Предварительный анализ\",\"order\":0,\"taskId\":\"" + task.getId() + "\",\"weight\":0,\"budget\":7200,\"progress\":0,\"description\":\"\",\"status\":\"ACTUAL\",\"workTypeId\":\"402881c2516c220101516c711ff80024\",\"workTypeNorm\":2.0,\"hrs\":0,\"deletable\":true,\"actualBudget\":0,\"planby\":\"budget\",\"workTypeAsString\":\"[0701] Иное\",\"workTypeDraftAsString\":\"-\",\"draftChanged\":true}]"));
        task.refreshUdf(udf);
        task.setConfirmed(true);
        bugTaskController.performCommonOperation(task, CHANGE_PLAN);
        ApiAsserts.assertThat(bugTaskController.getResponse()).isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK).isParseableBody(TaskResponseBody.class).assertTask().isCorrectStatus(STATUS_WORKTASK_ASSIGNED);

        var taskDetail = apiController.receiveTask(task.getNumber());
        CommonAssert.assertThat(taskDetail).isCorrectUdfDate(UDF_WORKTASK_AWAITTD, awaitDate)
//                .isCorrectUdfDate(UDF_WORKTASK_PLANFD, awaitDate)
                .isCorrectUdfDate(UDF_WORKTASK_PLANTD, awaitDate).isCorrectUdfDouble("Оценка трудоемкости", UDF_WORKTASK_PLANBUDGET, planBudget).isCorrectUdfDouble("Трудоемкость по нормам", UDF_CDP_NORMBUDGET, normBudget + planBudget);
    }

    @Test(groups = {"BugTask", "Regression"}, description = "Принять в работу", dependsOnMethods = "changePlan")
    public void taskStart() {
        apiController.updateToken(generateAuthToken(handlerUser));
        task.setDescription(generateString());
        task.setHandlerUser(handlerUser);
        udf = refreshUdf();
        firstPlanTdDate = DateUtils.getCurrentDate(4);
        planBudget = 2;
        udf.setUdfDate(generateUdfDate(UDF_WORKTASK_PLANTD, firstPlanTdDate));
        udf.setUdfDouble(generateUdfDouble(UDF_WORKTASK_PLANBUDGET, planBudget));
        udf.setUdfMemo(generateUdfMemo(UDF_CDP_STEPPROGRESS, "[{\"id\":\"818181698b6d8e12018b8fb7dcfc12c9\",\"name\":\"Предварительный анализ\",\"order\":0,\"taskId\":\"" + task.getId() + "\",\"progress\":0,\"description\":\"\",\"status\":\"DELETE\",\"hrs\":0,\"deletable\":true,\"actualBudget\":0,\"planby\":\"budget\",\"workTypeAsString\":\"-\",\"workTypeDraftAsString\":\"-\",\"draftChanged\":true},{\"id\":\"818181698b6d8e12018b8fb7efd612de\",\"name\":\"Предварительный анализ 2\",\"order\":1,\"taskId\":\"" + task.getId() + "\",\"weight\":1,\"budget\":7200,\"progress\":0,\"description\":\"\",\"status\":\"ACTUAL\",\"workTypeId\":\"402881c2516c220101516c711ff80024\",\"workTypeNorm\":2.0,\"hrs\":0,\"deletable\":true,\"actualBudget\":0,\"planby\":\"budget\",\"workTypeAsString\":\"[0701] Иное\",\"workTypeDraftAsString\":\"-\",\"draftChanged\":true}]"));
        udf.setUdfUser(generateUdfUser(STDT_HANDLER, handlerUser));
        task.refreshUdf(udf);
        bugTaskController.performCommonOperation(task, ACCEPT_IN_WORK);
        ApiAsserts.assertThat(bugTaskController.getResponse()).isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK).isParseableBody(TaskResponseBody.class).assertTask().isCorrectStatus(STATUS_WORKTASK_INWORK);

        var response = apiController.receiveTask(task.getNumber());
        CommonAssert.assertThat(response).isCorrectUdfDate(UDF_WORKTASK_PLANTD, firstPlanTdDate);
    }

    @Test(groups = {"BugTask", "Regression"}, description = "Перепланировать", dependsOnMethods = "taskStart")
    public void taskChangeTime() {
        apiController.updateToken(generateAuthToken(handlerUser));
        task.setDescription(generateString());
        task.setConfirmed(false);
        udf = refreshUdf();
        step8PlanDate = DateUtils.getCurrentDate(5);
        udf.setUdfDate(generateUdfDate(UDF_WORKTASK_AWAITTD, step8PlanDate));
        planBudget = 20;
        normBudget = 2;
        udf.setUdfDouble(generateUdfDouble(UDF_WORKTASK_AWAITBUDGET, planBudget + normBudget));
        udf.setSecondUdfDouble(generateUdfDouble(UDF_CDP_NORMBUDGET, normBudget + planBudget));


        udf.setUdfMemo(generateUdfMemo(UDF_CDP_STEPPLAN, "[{\"id\":\"818181698b6d8e12018b8fb7efd612de\",\"name\":\"Предварительный анализ 2\",\"order\":1,\"taskId\":\"" + task.getId() + "\",\"weight\":1,\"budget\":7200,\"progress\":0,\"description\":\"\",\"status\":\"ACTUAL\",\"workTypeId\":\"402881c2516c220101516c711ff80024\",\"workTypeNorm\":2,\"hrs\":0,\"deletable\":true,\"actualBudget\":0,\"planby\":\"budget\",\"workTypeAsString\":\"[0701] Иное\",\"workTypeDraftAsString\":\"-\",\"draftChanged\":true,\"orderDraft\":1,\"nameDraft\":\"Предварительный анализ 2\",\"descriptionDraft\":\"\",\"statusDraft\":\"ACTUAL\",\"weightDraft\":1,\"budgetDraft\":7200,\"budgetHrs\":2,\"budgetMinutes\":0,\"workTypeIdDraft\":\"402881c2516c220101516c711ff80024\",\"workTypeNormDraft\":2},{\"orderDraft\":2,\"weightDraft\":1,\"budgetDraft\":72000,\"planby\":\"budget\",\"deletable\":true,\"statusDraft\":\"NEW\",\"workTypeIdDraft\":\"402881c2516c220101516c711ff80024\",\"workTypeNormDraft\":20,\"budgetHrs\":\"20\",\"nameDraft\":\"Предварительный анализ 3\"}]"));
        udf.setUdfUser(generateUdfUser(UDF_WORKTASK_RESPFOREPLAN, handlerUser));
        udf.setUdfList(generateUdfList(UDF_WORKTASK_REASONFOREPLAN, DISTRACTION_TO_OTHER_WORK));
        udf.setSecondUdfMemo(generateUdfMemo(UDF_CDP_STEPPROGRESS, "[{\"id\":\"818181698b6d8e12018b8fb7dcfc12c9\",\"name\":\"Предварительный анализ\",\"order\":0,\"taskId\":\"" + task.getId() + "\",\"progress\":0,\"description\":\"\",\"status\":\"DELETE\",\"hrs\":0,\"deletable\":true,\"actualBudget\":0,\"planby\":\"budget\",\"workTypeAsString\":\"-\",\"workTypeDraftAsString\":\"-\",\"draftChanged\":true},{\"id\":\"818181698b6d8e12018b8fb7efd612de\",\"name\":\"Предварительный анализ 2\",\"order\":1,\"taskId\":\"818181698b6d8e12018b8fb7d9cf12b0\",\"weight\":1,\"budget\":7200,\"progress\":0,\"description\":\"\",\"status\":\"ACTUAL\",\"workTypeId\":\"402881c2516c220101516c711ff80024\",\"workTypeNorm\":2.0,\"hrs\":0,\"deletable\":true,\"actualBudget\":0,\"planby\":\"budget\",\"workTypeAsString\":\"[0701] Иное\",\"workTypeDraftAsString\":\"-\",\"draftChanged\":true}]"));
        task.refreshUdf(udf);
        bugTaskController.performCommonOperation(task, CHANGE_TIME);
        ApiAsserts.assertThat(bugTaskController.getResponse()).isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK).isParseableBody(TaskResponseBody.class).assertTask().isCorrectStatus(STATUS_WORKTASK_INWORK);

        var response = apiController.receiveTask(task.getNumber());
        CommonAssert.assertThat(response).isCorrectUdfDate(UDF_WORKTASK_AWAITTD, step8PlanDate).isCorrectUdfDouble("Оценка трудоемкости исполнителем", UDF_WORKTASK_AWAITBUDGET, planBudget + normBudget).isCorrectUdfDouble("Трудоемкость по нормам", UDF_CDP_NORMBUDGET, normBudget + planBudget).isCorrectUdfList(UDF_WORKTASK_INREPLAN, UDF_WORKTASK_INREPLAN_YES.id);
    }

    @Test(groups = {"DevTask", "Regression"}, description = "Проверка формы MSG_WORKTASK_CONFORMREPLAN", dependsOnMethods = "taskChangeTime")
    public void taskConfirmReplanForm() {
        apiController.updateToken(generateAuthToken(creator));
        var response = bugTaskController.receiveContextByOperation(InitEntities.generateOperationID(WORK_TASK, WORKTASK_CONFORMREPLAN).getId(), task.getNumber());
        ApiAsserts.assertThat(response).isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK);
        CommonAssert.assertThat(response).isCorrectUdfDate(UDF_WORKTASK_PLANTD, step8PlanDate);
    }

    @Test(groups = {"BugTask", "Regression"}, description = "Согласовать перепланирование", dependsOnMethods = "taskConfirmReplanForm")
    public void taskConfirmReplan() {
        apiController.updateToken(generateAuthToken(creator));
        task.setDescription(generateString());
        task.setConfirmed(false);
        udf = refreshUdf();
        udf.setUdfMemo(generateUdfMemo(UDF_WORKTASK_TASKALLOCATION, "{\"planFinishDate\":\"\",\"userLogin\":\"" + handlerUser.getLogin() + "\",\"allocations\":[]}"));
        udf.setUdfDate(generateUdfDate(UDF_WORKTASK_PLANTD, step8PlanDate));
        udf.setUdfMemo(generateUdfMemo(UDF_CDP_STEPPROGRESS, "[{\"id\":\"818181698b6d8e12018b8fb7dcfc12c9\",\"name\":\"Предварительный анализ\",\"order\":0,\"taskId\":\"" + task.getId() + "\",\"progress\":0,\"description\":\"\",\"status\":\"DELETE\",\"nameDraft\":\"Предварительный анализ\",\"statusDraft\":\"DELETE\",\"hrs\":0,\"deletable\":true,\"actualBudget\":0,\"planby\":\"budget\",\"workTypeAsString\":\"-\",\"workTypeDraftAsString\":\"-\",\"draftChanged\":true},{\"id\":\"818181698b6d8e12018b8fca90cd137b\",\"name\":\"\",\"order\":0,\"taskId\":\"" + task.getId() + "\",\"progress\":0,\"description\":\"\",\"status\":\"\",\"nameDraft\":\"Предварительный анализ 3\",\"orderDraft\":2,\"weightDraft\":1,\"budgetDraft\":72000,\"statusDraft\":\"NEW\",\"workTypeIdDraft\":\"402881c2516c220101516c711ff80024\",\"workTypeNormDraft\":20.0,\"hrs\":0,\"deletable\":true,\"actualBudget\":0,\"planby\":\"budget\",\"workTypeAsString\":\"-\",\"workTypeDraftAsString\":\"[0701] Иное\",\"draftChanged\":true},{\"id\":\"818181698b6d8e12018b8fb7efd612de\",\"name\":\"Предварительный анализ 2\",\"order\":1,\"taskId\":\"" + task.getId() + "\",\"weight\":1,\"budget\":7200,\"progress\":0,\"description\":\"\",\"status\":\"ACTUAL\",\"workTypeId\":\"402881c2516c220101516c711ff80024\",\"workTypeNorm\":2.0,\"nameDraft\":\"Предварительный анализ 2\",\"orderDraft\":1,\"weightDraft\":1,\"budgetDraft\":7200,\"statusDraft\":\"ACTUAL\",\"workTypeIdDraft\":\"402881c2516c220101516c711ff80024\",\"workTypeNormDraft\":2.0,\"hrs\":0,\"deletable\":true,\"actualBudget\":0,\"planby\":\"budget\",\"workTypeAsString\":\"[0701] Иное\",\"workTypeDraftAsString\":\"[0701] Иное\",\"draftChanged\":true}]"));
        task.refreshUdf(udf);
        bugTaskController.performCommonOperation(task, WORKTASK_CONFORMREPLAN);
        ApiAsserts.assertThat(bugTaskController.getResponse()).isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK).isParseableBody(TaskResponseBody.class).assertTask().isCorrectStatus(STATUS_WORKTASK_INWORK);

        var response = apiController.receiveTask(task.getNumber());
        CommonAssert.assertThat(response).isCorrectUdfDate(UDF_WORKTASK_AWAITTD, step8PlanDate);
    }
}