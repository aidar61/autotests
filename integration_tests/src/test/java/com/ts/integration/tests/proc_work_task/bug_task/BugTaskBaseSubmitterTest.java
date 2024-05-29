package com.ts.integration.tests.proc_work_task.bug_task;

import com.ts.common.application.controllers.TrackStudioHttpStatusCodes;
import com.ts.common.application.database.dbEntities.GrTaskDbEntity;
import com.ts.common.application.database.dbTables.GrTaskTable;
import com.ts.common.asserts.ApiAsserts;
import com.ts.common.asserts.CommonAssert;
import com.ts.common.controllers.TaskResponseBody;
import com.ts.common.controllers.bug.BugTaskController;
import com.ts.common.entitites.commonEntities.Parent;
import com.ts.common.entitites.commonEntities.Task;
import com.ts.common.entitites.commonEntities.User;
import com.ts.common.entitites.commonEntities.UserData;
import com.ts.common.entitites.commonEntities.udf.UdfTask;
import com.ts.common.entitites.tasks.GeneralTask;
import com.ts.common.enums.Operations;
import com.ts.common.enums.TaskType;
import com.ts.common.utils.DateUtils;
import com.ts.common.utils.InitEntities;
import com.ts.common.utils.JsonUtils;
import com.ts.integration.tests.BaseIntegrationTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.ts.common.application.database.DbQueryHelper.Operators.*;
import static com.ts.common.entitites.commonEntities.List.Constants.*;
import static com.ts.common.entitites.commonEntities.Task.Constants.*;
import static com.ts.common.entitites.commonEntities.Udfs.UdfSd.*;
import static com.ts.common.entitites.commonEntities.User.Constants.*;
import static com.ts.common.enums.Operations.*;
import static com.ts.common.enums.Resolutions.WILL_NOT_BE_IMPLEMENTED;
import static com.ts.common.enums.TaskStatuses.*;
import static com.ts.common.enums.TaskType.WORK_TASK;
import static com.ts.common.utils.InitEntities.*;
import static com.ts.common.utils.RandomUtils.generateString;

public class BugTaskBaseSubmitterTest extends BaseIntegrationTest {
    private final int firstPlanBudget = 15;
    public BugTaskController bugTaskController;
    java.util.List<String> branches;
    private GeneralTask task;
    private Parent parent;
    private UdfTask customerRequest;
    private UdfTask udfTestTask;
    private User creator;
    private User handlerUser;
    private String expectedCompletionDate;
    private String MIS_SERVICE;
    private String CDP_BL;
    private Task[] PRODUCT;
    private Task[] BDKU_CONFIGURATION;

    @BeforeClass(alwaysRun = true)
    public void beforeClass() {
        bugTaskController = apiController.getBugTaskController();
        userController = apiController.getUserController();
        GrTaskTable grTaskTable = dbHelper.getGrTaskTable();
        GrTaskDbEntity parentTaskFromDb = (GrTaskDbEntity) grTaskTable.receiveRandomTask("task_category", EQUAL.operator, "CAT_GENPLAN", AND.operator, "task_status", EQUAL.operator, STATUS_PROJECT_PLANNED.name(), AND.operator, "task_path", LIKE.operator, "%/2405/758009%");
        parent = InitEntities.generateParent(parentTaskFromDb.getTask_id(), parentTaskFromDb.getTask_number());
        task = InitEntities.getGeneralTask(TaskType.BUG_TASK, Operations.CAT);
        var taskSlaBug = (GrTaskDbEntity) grTaskTable.receiveRandomTask("task_category", EQUAL.operator, "CAT_GENPLAN", AND.operator, "task_status", NOT_EQUAL.operator, STATUS_SLABUG_CLOSED.name());
        var testTask = (GrTaskDbEntity) grTaskTable.receiveRandomTask("task_category", EQUAL.operator, "CAT_TESTTASK", AND.operator, "task_status", NOT_EQUAL.operator, STATUS_WORKTASK_CLOSED.name());
        udfTestTask = InitEntities.generateUdfTask(UDF_WORKTASK_TESTTASK, new Task(testTask.getTask_id(), testTask.getTask_number()));
        customerRequest = InitEntities.generateUdfTask(UDF_WORKTASK_SDREQUEST, new Task(taskSlaBug.getTask_id(), taskSlaBug.getTask_number()));

        var parentPayloadResponse = bugTaskController.getParentPayload(parent.getNumber(), "CAT_BUGTASK");
        ApiAsserts.assertThat(parentPayloadResponse).isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK);
        var parentPayload = JsonUtils.removeExtraCharacters(parentPayloadResponse);
        MIS_SERVICE = bugTaskController.getParent_UDF_MIS_SERVICE(parentPayload).get(0);
        CDP_BL = bugTaskController.getParent_UDF_CDP_BL(parentPayload).get(0);
        PRODUCT = bugTaskController.getParent_UDF_PRODUCT(parentPayload);
        BDKU_CONFIGURATION = bugTaskController.getParent_UDF_BDKU_CONFIGURATION(parentPayload);
        branches = bugTaskController.getBranches(parentPayload);

        var employees = userController.receiveUserByTask(parent.getNumber());
        creator = employees.stream().filter(f -> f.getAssignedRole().getName().equals("Менеджер проекта") && f.getForUser().getActive()).findFirst().get().getForUser();
        handlerUser = employees.stream().filter(f -> f.getAssignedRole().getName().equals("Участник проекта") && f.getForUser().getActive() && !f.getForUser().getLogin().equals(creator.getLogin())).findFirst().get().getForUser();
    }


    @Test(groups = {"PROC_WORKTASK", "Regression"}, description = "создание CAT_BUGTASK")
    public void bugTask() {
        apiController.updateToken(InitEntities.generateAuthToken(creator));
        task.setParent(parent);
        task.setName("Задача на исправление ошибки: " + DateUtils.getCurrentDate(0));
        task.setDescription("<table border=\"1\" cellpadding=\"0\" cellspacing=\"0\" class=\"general\" style=\"border:1px solid\">\n\t<tbody>\n\t\t<tr>\n\t\t\t<th style=\"text-align: left; background-color: rgb(235, 241, 245); border-color: rgb(119, 119, 119);\" width=\"30%\"><span style=\"font-size:11px\"><strong>" + generateString() + "</strong><strong>*</strong></span></th>\n\t\t\t<td data-required=\"true\" style=\"border-color:#777777; height:1px; text-align:left\" width=\"70%\">1000x2000</td>\n\t\t</tr>\n\t\t<tr>\n\t\t\t<th style=\"text-align: left; background-color: rgb(235, 241, 245); border-color: rgb(119, 119, 119);\" width=\"30%\"><span style=\"font-size:11px\"><span style=\"font-family:Arial,Helvetica,sans-serif\"><strong>Клиентская часть*</strong></span></span></th>\n\t\t\t<td data-required=\"true\" style=\"border-color:#777777; height:1px; text-align:left\" width=\"70%\"><span style=\"font-size:11px\"><span style=\"font-family:Arial,Helvetica,sans-serif\"><strong>Клиентская часть*</strong></span></span></td>\n\t\t</tr>\n\t\t<tr>\n\t\t\t<th style=\"text-align: left; background-color: rgb(235, 241, 245); border-color: rgb(119, 119, 119);\"><span style=\"font-size:11px\"><span style=\"font-family:Arial,Helvetica,sans-serif\"><strong>Инстанция для тестирования*</strong></span></span></th>\n\t\t\t<td data-required=\"true\" style=\"border-color:#777777; height:1px; text-align:left\"><span style=\"font-size:11px\"><span style=\"font-family:Arial,Helvetica,sans-serif\"><strong>Инстанция для тестирования*</strong></span></span></td>\n\t\t</tr>\n\t\t<tr>\n\t\t\t<th style=\"text-align: left; background-color: rgb(235, 241, 245); border-color: rgb(119, 119, 119);\"><span style=\"font-size:11px\"><span style=\"font-family:Arial,Helvetica,sans-serif\"><strong>Пользователь / пароль*</strong></span></span></th>\n\t\t\t<td data-required=\"true\" style=\"border-color:#777777; height:1px; text-align:left\"><span style=\"font-size:11px\"><span style=\"font-family:Arial,Helvetica,sans-serif\"><strong>Пользователь / пароль*</strong></span></span></td>\n\t\t</tr>\n\t\t<tr>\n\t\t\t<th style=\"text-align: left; background-color: rgb(235, 241, 245); border-color: rgb(119, 119, 119);\"><span style=\"font-size:11px\"><span style=\"font-family:Arial,Helvetica,sans-serif\"><strong>АРМ пользователя*</strong></span></span></th>\n\t\t\t<td data-required=\"true\" style=\"border-color:#777777; height:1px; text-align:left\"><span style=\"font-size:11px\"><span style=\"font-family:Arial,Helvetica,sans-serif\"><strong>АРМ пользователя*</strong></span></span></td>\n\t\t</tr>\n\t\t<tr>\n\t\t\t<th style=\"text-align: left; background-color: rgb(235, 241, 245); border-color: rgb(119, 119, 119);\"><span style=\"font-size:11px\"><span style=\"font-family:Arial,Helvetica,sans-serif\"><strong>Версия модуля*</strong></span></span></th>\n\t\t\t<td data-required=\"true\" style=\"border-color:#777777; height:1px; text-align:left\"><span style=\"font-size:11px\"><span style=\"font-family:Arial,Helvetica,sans-serif\"><strong>Версия модуля*</strong></span></span></td>\n\t\t</tr>\n\t\t<tr>\n\t\t\t<th style=\"text-align: left; background-color: rgb(235, 241, 245); border-color: rgb(119, 119, 119);\"><span style=\"font-size:11px\"><span style=\"font-family:Arial,Helvetica,sans-serif\"><strong>Операционный день*</strong></span></span></th>\n\t\t\t<td data-required=\"true\" style=\"border-color:#777777; text-align:left\"><span style=\"font-size:11px\"><span style=\"font-family:Arial,Helvetica,sans-serif\"><strong>Операционный день*</strong></span></span></td>\n\t\t</tr>\n\t\t<tr>\n\t\t\t<th style=\"text-align: left; background-color: rgb(235, 241, 245); border-color: rgb(119, 119, 119);\"><span style=\"font-size:11px\"><span style=\"font-family:Arial,Helvetica,sans-serif\"><strong>Воспроизведение</strong></span></span></th>\n\t\t\t<td style=\"border-color:#777777; height:1px; text-align:left\"><span style=\"font-size:11px\"><span style=\"font-family:Arial,Helvetica,sans-serif\"><strong>Воспроизведение</strong></span></span></td>\n\t\t</tr>\n\t\t<tr>\n\t\t\t<th style=\"text-align: left; background-color: rgb(235, 241, 245); border-color: rgb(119, 119, 119);\"><span style=\"font-size:11px\"><span style=\"font-family:Arial,Helvetica,sans-serif\"><strong>Ожидаемый результат</strong></span></span></th>\n\t\t\t<td style=\"border-color:#777777; height:1px; text-align:left\"><span style=\"font-size:11px\"><span style=\"font-family:Arial,Helvetica,sans-serif\"><strong>Ожидаемый результат</strong></span></span></td>\n\t\t</tr>\n\t\t<tr>\n\t\t\t<th style=\"text-align: left; background-color: rgb(235, 241, 245); border-color: rgb(119, 119, 119);\"><span style=\"font-size:11px\"><span style=\"font-family:Arial,Helvetica,sans-serif\"><strong>Фактический результат</strong></span></span></th>\n\t\t\t<td style=\"border-color:#777777; height:1px; text-align:left\"><span style=\"font-size:11px\"><span style=\"font-family:Arial,Helvetica,sans-serif\"><strong>Фактический результат</strong></span></span></td>\n\t\t</tr>\n\t</tbody>\n</table>\n");
        task.setHandlerUser(handlerUser);
        udf = refreshUdf();
        udf.setUdfList(generateUdfList(UDF_WORKTASK_SEVERITY, UDF_WORKTASK_SEVERITY_TRIVIAL));
        udf.setSecondUdfList(generateUdfList(UDF_WORKTASK_COMPLEXITYLEVEL, TASK_LEVEL_7));
        udf.setUdfUser(generateUdfUser(UDF_WORKTASK_SUPERVISER, ABDULLAEV_BAHODIR));
        udf.setSecondUdfUser(generateUdfUser(UDF_WATCHER, BABUSHKIN_IVAN));
        if (CDP_BL != null) udf.setThirdUdfList(generateUdfList(UDF_CDP_BL, CDP_BL));
        udf.setUdfTask(generateUdfTask(UDF_SD_MODULE, CORE));
        udf.setUdfString(generateUdfString(UDF_SD_NOMODULE_REASON, generateString()));
        if (MIS_SERVICE != null) udf.setFourthUdfList(generateUdfList(UDF_MIS_SERVICE, MIS_SERVICE));
        udf.setFifthUdfList(generateUdfList(UDF_CDP_ACCEPTANCE, UDF_CDP_ACCEPTANCE_NO));
        udf.setSixthUdfList(generateUdfList(UDF_WORKTASK_ANALYSIS, UDF_WORKTASK_ANALYSIS_NO));

        expectedCompletionDate = DateUtils.getCurrentDate(1);
        udf.setUdfDate(generateUdfDate(UDF_WORKTASK_PLANTD, expectedCompletionDate));
        udf.setUdfDouble(generateUdfDouble(UDF_WORKTASK_PLANBUDGET, firstPlanBudget));
        udf.setUdfMemo(generateUdfMemo(UDF_CDP_STEPPLAN, "[{\"orderDraft\":1,\"weightDraft\":1,\"budgetDraft\":0,\"planby\":\"budget\",\"deletable\":true,\"statusDraft\":\"NEW\",\"nameDraft\":\"Testing Пошаговый план\",\"workTypeIdDraft\":\"402881c25124956701513912e0f0081d\",\"workTypeNormDraft\":0,\"budgetHrs\":\"\",\"budgetMinutes\":\"\"}]"));
        Map<Task.Constants, UserData> dependTaskFNC = new HashMap<>();
        var userData1 = new UserData(null, "{\"type\":\"REQUIRED\",\"comment\":\"Required\"}");
        var userData2 = new UserData(null, "{\"type\":\"RECOMMENDED\",\"comment\":\"RECOMMENDED\"}");
        var userData3 = new UserData(null, "{\"type\":\"OPTIONAL\",\"option\":\"949177\",\"optionNot\":\"773226\",\"comment\":\"Optional\"}");
        dependTaskFNC.put(RYSGAL_BANK, userData1);
        dependTaskFNC.put(WORKTASK_TESTTASK, userData2);
        dependTaskFNC.put(CUSTOMER_REQUEST, userData3);
        udf.setNinethUdfTask(generateUdfTask(UDF_WORKTASK_DEPENDTASKFNC, dependTaskFNC));
        Collections.shuffle(new ArrayList<>(branches));
        udf.setSecondUdfString(generateUdfString(UDF_WORKTASK_BRANCH, branches.stream().filter(x -> x.toLowerCase().contains("разработ")).findAny().orElse(null)));
        udf.setSecondUdfTask(generateUdfTask(UDF_PRODUCT, PRODUCT[0]));
        udf.setSeventhUdfList(generateUdfList(UDF_WORKTASK_WAYCODEREVIEW, WAY_CODE_REVIEW_OFF));
        udf.setSecondUdfMultiList(generateUdfMultiList(UDF_L10N, UDF_L10N_KG));
        udf.setThirdUdfTask(generateUdfTask(UDF_COMPONENT, APP_SERVER));
        udf.setEighthUdfList(generateUdfList(UDF_PROBLEMAREA, UDF_PROBLEMAREA_COMFORT));
        udf.setNinethUdfList(generateUdfList(UDF_COREBUILDTYPE, UDF_COREBUILDTYPE_RELEASE));
        udf.setThirdUdfString(generateUdfString(UDF_COREBUILD, "Версия ядра"));
        udf.setTenthUdfList(generateUdfList(UDF_COMPONENTBUILDTYPE, UDF_COMPONENTBUILDTYPE_RELEASE));
        udf.setFourthUdfString(generateUdfString(UDF_COMPONENTBUILD, "Версия компонента"));
        udf.setFifthUdfString(generateUdfString(UDF_DATABASES, "Базы данных"));
        udf.setEleventhUdfList(generateUdfList(UDF_SDFEATURE_GENUSE, GENERAL, "{\"username\":\"babdullayev\",\"name\":\"Абдуллаев Баходир\"}"));
        udf.setFourthUdfTask(generateUdfTask(UDF_SD_LINKEDREQUEST, AKKREDITIVES));
        udf.setFifthUdfTask(generateUdfTask(UDF_BDKU_CONFIGURATION, BDKU_CONFIGURATION[0]));
        udf.setFifthUdfTask(customerRequest);
        udf.setSixthUdfTask(udfTestTask);
        udf.setTwelfthUdfList(generateUdfList(UDF_CDP_BP, UDF_CDP_BP_TROUBLESHOOTING, "{\"TC\":[\"0901\",\"0201\",\"0505\",\"0506\",\"0707\",\"0714\",\"0803\"]}"));
        udf.setSeventhUdfTask(generateUdfTask(UDF_WORKTASK_WORK, KZ_KZI));
        udf.setEighthUdfTask(generateUdfTask(UDF_WORKTASK_DEPENDBF, new Task.Constants[]{MTBANK, NOTIFICATION_SERVICE, HEAD_BOOK}));
        udf.setFourteenthUdfList(generateUdfList(UDF_WORKTASK_ADDWATCHERINREQST, UDF_WORKTASK_ADDWATCHERINREQST_YES));
        task.refreshUdf(udf);
        bugTaskController.createBagTask(task);
        var response = bugTaskController.getResponse();
        ApiAsserts.assertThat(response).isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK).isParseableBody(TaskResponseBody.class).assertTask().isCorrectStatus(STATUS_WORKTASK_ASSIGNED).isEquals(task);
    }

    @Test(groups = {"PROC_WORKTASK", "Regression"}, description = "Принять в работу", dependsOnMethods = "bugTask")
    public void taskStart() {
        apiController.updateToken(generateAuthToken(handlerUser));
        task.setHandlerUser(handlerUser);
        task.setDescription(generateString());
        udf = refreshUdf();
        String firstPlanTdDate = DateUtils.getCurrentDate(1);
        udf.setUdfMemo(generateUdfMemo(UDF_CDP_STEPPROGRESS, "[{\"id\":\"8181816a8997826e018a3bfd2ab9164c\",\"name\":\"Testing Пошаговый план\",\"order\":1,\"taskId\":\"8181816a8997826e018a3bfd105b15f8\",\"weight\":1,\"budget\":0,\"progress\":0,\"description\":\"\",\"status\":\"BASE\",\"workTypeId\":\"402881c25124956701513912e0f0081d\",\"workTypeNorm\":0.0,\"nameDraft\":\"Testing Пошаговый план\",\"orderDraft\":1,\"weightDraft\":1,\"budgetDraft\":0,\"statusDraft\":\"BASE\",\"workTypeIdDraft\":\"402881c25124956701513912e0f0081d\",\"workTypeNormDraft\":0.0,\"hrs\":0,\"deletable\":true,\"actualBudget\":0,\"planby\":\"budget\",\"workTypeAsString\":\"[0103] Создание документа - таблица, бизнес\",\"workTypeDraftAsString\":\"[0103] Создание документа - таблица, бизнес\",\"draftChanged\":true}]"));
        udf.setUdfUser(generateUdfUser(STDT_HANDLER, handlerUser));
        udf.setUdfDate(generateUdfDate(UDF_WORKTASK_PLANTD, firstPlanTdDate));
        udf.setUdfDouble(generateUdfDouble(UDF_WORKTASK_PLANBUDGET, firstPlanBudget));
        task.refreshUdf(udf);
        bugTaskController.changeTaskType(TaskType.WORK_TASK);
        bugTaskController.performCommonOperation(task, ACCEPT_IN_WORK);
        ApiAsserts.assertThat(bugTaskController.getResponse()).isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK).isParseableBody(TaskResponseBody.class).assertTask().isCorrectStatus(STATUS_WORKTASK_INWORK);

        var response = apiController.receiveTask(task.getNumber());
        CommonAssert.assertThat(response).isCorrectUdfDate(UDF_WORKTASK_PLANTD, firstPlanTdDate).isCorrectUDfDouble(UDF_WORKTASK_FIRSTPLANBUDGET, UDF_WORKTASK_PLANBUDGET);
    }

    @Test(groups = {"PROC_WORKTASK", "Regression"}, description = "Изменить участников", dependsOnMethods = "taskStart")
    public void changeMembers() {
        udf = refreshUdf();
        task.refreshTask();
        apiController.updateToken(generateAuthToken(creator));
        task.setDescription(generateString());
        User.Constants[] members = {ARUTYANIN_YURIY, ARTEMEVA_MARINA, BABUSHKIN_IVAN};
        udf.setUdfUser(generateUdfUser(UDF_PARTICIPANTS, members));
        task.refreshUdf(udf);
        bugTaskController.performCommonOperation(task, Operations.CHANGE_MEMBERS);
        ApiAsserts.assertThat(bugTaskController.getResponse()).isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK).isParseableBody(TaskResponseBody.class).assertTask();
        var taskDetail = apiController.receiveTask(task.getNumber());
        CommonAssert.assertThat(taskDetail).isCorrectUdfUSer(UDF_PARTICIPANTS, ARUTYANIN_YURIY.getLogin()).isCorrectUdfUSer(UDF_PARTICIPANTS, ARTEMEVA_MARINA.getLogin()).isCorrectUdfUSer(UDF_PARTICIPANTS, BABUSHKIN_IVAN.getLogin());
    }

    @Test(groups = {"PROC_WORKTASK", "Regression"}, description = "Проверить форму операции MSG_WORKTASK_CHNGTASKALLOCATION", dependsOnMethods = "changeMembers")
    public void changeForm() {
        var response = bugTaskController.receiveContextByOperation(InitEntities.generateOperationID(WORK_TASK, CHNGTASKAL_LOCATION).getId(), task.getNumber());
        ApiAsserts.assertThat(response).isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK);
        CommonAssert.assertThat(response).isCorrectUdfDate(UDF_WORKTASK_PLANTD, expectedCompletionDate);
    }

    @Test(groups = {"PROC_WORKTASK", "Regression"}, description = "Изменить уровень сложности", dependsOnMethods = "changeCodeReview")
    public void changeLevel() {
        udf = refreshUdf();
        task.refreshTask();
        apiController.updateToken(generateAuthToken(creator));
        task.setDescription(generateString());
        udf.setEighthUdfList(generateUdfList(UDF_WORKTASK_COMPLEXITYLEVEL, TASK_LEVEL_10));
        task.refreshUdf(udf);
        bugTaskController.performCommonOperation(task, CHANGE_COMPLEXITY_LEV);
        ApiAsserts.assertThat(bugTaskController.getResponse()).isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK).isParseableBody(TaskResponseBody.class).assertTask();
        var taskDetail = apiController.receiveTask(task.getNumber());
        CommonAssert.assertThat(taskDetail).isCorrectUdfList(UDF_WORKTASK_COMPLEXITYLEVEL, TASK_LEVEL_10.getId());
    }

    @Test(groups = {"PROC_WORKTASK", "Regression"}, description = "Изменить способ обзора кода", dependsOnMethods = "changeForm")
    public void changeCodeReview() {
        udf = refreshUdf();
        task.refreshTask();
        apiController.updateToken(generateAuthToken(creator));
        task.setDescription(generateString());
        udf.setUdfList(generateUdfList(UDF_WORKTASK_WAYCODEREVIEW, WAY_CODE_REVIEW_BLOCKING));
        udf.setUdfMemo(generateUdfMemo(UDF_CDP_STEPPROGRESS, "[{\"id\":\"8181816c89cef25c018a69d596ac3509\",\"name\":\"Предварительный анализ\",\"order\":0,\"taskId\":\"" + task.getId() + "\",\"progress\":0,\"description\":\"\",\"status\":\"DELETE\",\"hrs\":0,\"deletable\":true,\"actualBudget\":0,\"planby\":\"budget\",\"workTypeAsString\":\"-\",\"workTypeDraftAsString\":\"-\",\"draftChanged\":true}]"));
        task.refreshUdf(udf);
        bugTaskController.performCommonOperation(task, Operations.CHANGE_WAY_CODE_REVIEW);
        ApiAsserts.assertThat(bugTaskController.getResponse()).isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK).isParseableBody(TaskResponseBody.class).assertTask();
        var taskDetail = apiController.receiveTask(task.getNumber());
        CommonAssert.assertThat(taskDetail).isCorrectUdfList(UDF_WORKTASK_WAYCODEREVIEW, WAY_CODE_REVIEW_BLOCKING.getId());
    }

    @Test(groups = {"PROC_WORKTASK", "Regression"}, description = "Изменить критичность задачи", dependsOnMethods = "changeForm")
    public void changeSeverity() {
        udf = refreshUdf();
        task.refreshTask();
        apiController.updateToken(generateAuthToken(creator));
        task.setDescription(generateString());
        task.refreshUdf(udf);
        bugTaskController.performCommonOperation(task, Operations.CHANGE_CHANGE_SEVERITY);
        ApiAsserts.assertThat(bugTaskController.getResponse()).isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK).isParseableBody(TaskResponseBody.class).assertTask();
    }

    @Test(groups = {"PROC_WORKTASK", "Regression"}, description = "Изменить автора", dependsOnMethods = "changeSeverity")
    public void changeAuthor() {
        udf = refreshUdf();
        task.refreshTask();
        apiController.updateToken(generateAuthToken(creator));
        task.setDescription(generateString());
        udf.setUdfUser(generateUdfUser(UDF_WORKTASK_NEWAUTHOR_MSG, handlerUser));
        task.refreshUdf(udf);
        bugTaskController.performCommonOperation(task, CHANGE_AUTHOR);
        ApiAsserts.assertThat(bugTaskController.getResponse()).isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK).isParseableBody(TaskResponseBody.class).assertTask();
        var taskDetail = apiController.receiveTask(task.getNumber());
        CommonAssert.assertThat(taskDetail).isCorrectSubmitterUser(handlerUser.getLogin());
    }

    @Test(groups = {"PROC_WORKTASK", "Regression"}, description = "Снять задачу", dependsOnMethods = "changeAuthor")
    public void taskClose() {
        udf = refreshUdf();
        task.refreshTask();
        apiController.updateToken(generateAuthToken(handlerUser));
        task.setDescription(generateString());
        task.setResolution(generateResolution(WILL_NOT_BE_IMPLEMENTED));
        udf.setUdfMemo(generateUdfMemo(UDF_WORKTASK_ERRORDESCRIPTION, generateString()));
        udf.setUdfList(generateUdfList(UDF_WORKTASK_QUALITY, WORK_TASK_QUALITY_GOOD));
        udf.setSecondUdfList(generateUdfList(UDF_WORKTASK_DEVREGVIOLATION, UDF_WORKTASK_DEVREGVIOLATION_NO));
        udf.setThirdUdfList(generateUdfList(UDF_WORKTASK_REASONERROR, UDF_WORKTASK_REASONERROR_REQ));
        udf.setFourthUdfList(generateUdfList(UDF_WORKTASK_FROMSDREQUEST, UDF_WORKTASK_FROMSDREQUEST_NO));
        task.refreshUdf(udf);
        bugTaskController.performCommonOperation(task, CANCEL_BUG);
        ApiAsserts.assertThat(bugTaskController.getResponse()).isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK).isParseableBody(TaskResponseBody.class).assertTask().isCorrectResolution(WILL_NOT_BE_IMPLEMENTED).isCorrectStatus(STATUS_WORKTASK_CLOSED);
        var taskDetail = apiController.receiveTask(task.getNumber());
        CommonAssert.assertThat(taskDetail).isCorrectUdfList(UDF_WORKTASK_QUALITY, WORK_TASK_QUALITY_GOOD.getId());
    }
}