package com.ts.integration.tests.proc_work_task.bug_task;

import com.ts.common.application.controllers.TrackStudioHttpStatusCodes;
import com.ts.common.application.database.dbEntities.GrTaskDbEntity;
import com.ts.common.application.database.dbTables.GrTaskTable;
import com.ts.common.asserts.ApiAsserts;
import com.ts.common.asserts.CommonAssert;
import com.ts.common.controllers.TaskResponseBody;
import com.ts.common.controllers.bug.BugTaskController;
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
import java.util.List;

import static com.ts.common.application.database.DbQueryHelper.Operators.*;
import static com.ts.common.entitites.commonEntities.List.Constants.*;
import static com.ts.common.entitites.commonEntities.Task.Constants.*;
import static com.ts.common.entitites.commonEntities.Udfs.UdfSd.*;
import static com.ts.common.entitites.commonEntities.User.Constants.*;
import static com.ts.common.enums.Operations.*;
import static com.ts.common.enums.Resolutions.MSG_WORKTASK_BUGDECLINE;
import static com.ts.common.enums.Resolutions.RESOLUTION_WORK_SUSPENDED_INDEFINITELY;
import static com.ts.common.enums.TaskStatuses.*;
import static com.ts.common.utils.InitEntities.*;
import static com.ts.common.utils.RandomUtils.generateComment;
import static com.ts.common.utils.RandomUtils.generateString;

public class BugTaskBaseHandlerTest extends BaseIntegrationTest {
    public BugTaskController bugTaskController;
    private GeneralTask task;
    private String misService;
    private String misServiceForChange;
    private String cdpBl;
    private UdfTask productTask;
    private UdfTask bdkuTask;
    private Parent parent;
    private GrTaskTable grTaskTable;
    private GrTaskDbEntity parentTaskFromDb;
    private UdfTask customerRequest;
    private UdfTask udfTestTask;
    private User creator;
    private User handlerUser;
    private User constructorUser;
    private String expectedCompletionDate;
    private String firstPlanTdDate;
    private int firstPlanBudget = 15;
    private int planBudget = 25;
    private int normBudget = 25;
    private String BDKUName;
    private Map<Task.Constants, UserData> dependTaskFNC;
    List<String> branches;


    @BeforeClass(alwaysRun = true)
    public void beforeClass() {
        dependTaskFNC = new HashMap<>();
        bugTaskController = apiController.getBugTaskController();
        userController = apiController.getUserController();
        grTaskTable = dbHelper.getGrTaskTable();
        parentTaskFromDb = (GrTaskDbEntity) grTaskTable.receiveRandomTask(
                "task_category", EQUAL.operator, "CAT_GENPLAN",
                AND.operator,
                "task_status", EQUAL.operator, STATUS_PROJECT_PLANNED.name(),
                AND.operator,
                "task_path", LIKE.operator, "%/2405/758009%");
        parent = InitEntities.generateParent(parentTaskFromDb.getTask_id(), parentTaskFromDb.getTask_number());
        task = InitEntities.getGeneralTask(TaskType.BUG_TASK, Operations.CAT);
        var tasks = bugTaskController.getTaskForSDRequest(parent.getNumber());
        productTask = tasks.get("UDF_PRODUCT");
        bdkuTask = tasks.get("UDF_BDKU_CONFIGURATION");
        var taskSlaBug = (GrTaskDbEntity) grTaskTable.receiveRandomTask(
                "task_category", EQUAL.operator, "CAT_GENPLAN",
                AND.operator,
                "task_status", NOT_EQUAL.operator, STATUS_SLABUG_CLOSED.name());
        var testTask = (GrTaskDbEntity) grTaskTable.receiveRandomTask(
                "task_category", EQUAL.operator, "CAT_TESTTASK",
                AND.operator,
                "task_status", NOT_EQUAL.operator, STATUS_WORKTASK_CLOSED.name());
        udfTestTask = InitEntities.generateUdfTask(UDF_WORKTASK_TESTTASK, new Task(testTask.getTask_id(), testTask.getTask_number()));
        customerRequest = InitEntities.generateUdfTask(UDF_WORKTASK_SDREQUEST, new Task(taskSlaBug.getTask_id(), taskSlaBug.getTask_number()));
        misService = bugTaskController.getMisService().get(0);
        misServiceForChange = bugTaskController.getMisService().get(1);
        cdpBl = bugTaskController.getCdpBl();
        var employees = userController.receiveUserByTask(parent.getNumber());
        creator = employees.stream().filter(f -> f.getAssignedRole().getName().equals("Менеджер проекта")).findFirst().get().getForUser();
        handlerUser = employees.stream().filter(f -> f.getAssignedRole().getName().equals("Участник проекта") && !f.getForUser().getLogin().equals(creator.getLogin())).findFirst().get().getForUser();
        var parentTaskPayload = apiController.receiveParentTaskPayload(parent.getNumber(), "CAT_BUGTASK").asString().replace("\\&", "\\\\&");
        branches = new JsonPath(parentTaskPayload).getList("udfs.UDF_WORKTASK_BRANCH.stringValueSelector", String.class);
        BDKUName = bugTaskController.getBDKUTaskName();
    }


    @Test(groups = {"BugTask", "Regression"}, description = "создание CAT_BUGTASK")
    public void bugTask() {
        apiController.updateToken(InitEntities.generateAuthToken(creator));
        task.setParent(parent);
        task.setName("Задача на исправление ошибки: " + DateUtils.getCurrentDate(0));
        task.setDescription("<table border=\"1\" cellpadding=\"0\" cellspacing=\"0\" class=\"general\" style=\"border:1px solid\">\n\t<tbody>\n\t\t<tr>\n\t\t\t<th style=\"text-align: left; background-color: rgb(235, 241, 245); border-color: rgb(119, 119, 119);\" width=\"30%\"><span style=\"font-size:11px\"><strong>" + generateString() +
                "</strong><strong>*</strong></span></th>\n\t\t\t<td data-required=\"true\" style=\"border-color:#777777; height:1px; text-align:left\" width=\"70%\">1000x2000</td>\n\t\t</tr>\n\t\t<tr>\n\t\t\t<th style=\"text-align: left; background-color: rgb(235, 241, 245); border-color: rgb(119, 119, 119);\" width=\"30%\"><span style=\"font-size:11px\"><span style=\"font-family:Arial,Helvetica,sans-serif\"><strong>Клиентская часть*</strong></span></span></th>\n\t\t\t<td data-required=\"true\" style=\"border-color:#777777; height:1px; text-align:left\" width=\"70%\"><span style=\"font-size:11px\"><span style=\"font-family:Arial,Helvetica,sans-serif\"><strong>Клиентская часть*</strong></span></span></td>\n\t\t</tr>\n\t\t<tr>\n\t\t\t<th style=\"text-align: left; background-color: rgb(235, 241, 245); border-color: rgb(119, 119, 119);\"><span style=\"font-size:11px\"><span style=\"font-family:Arial,Helvetica,sans-serif\"><strong>Инстанция для тестирования*</strong></span></span></th>\n\t\t\t<td data-required=\"true\" style=\"border-color:#777777; height:1px; text-align:left\"><span style=\"font-size:11px\"><span style=\"font-family:Arial,Helvetica,sans-serif\"><strong>Инстанция для тестирования*</strong></span></span></td>\n\t\t</tr>\n\t\t<tr>\n\t\t\t<th style=\"text-align: left; background-color: rgb(235, 241, 245); border-color: rgb(119, 119, 119);\"><span style=\"font-size:11px\"><span style=\"font-family:Arial,Helvetica,sans-serif\"><strong>Пользователь / пароль*</strong></span></span></th>\n\t\t\t<td data-required=\"true\" style=\"border-color:#777777; height:1px; text-align:left\"><span style=\"font-size:11px\"><span style=\"font-family:Arial,Helvetica,sans-serif\"><strong>Пользователь / пароль*</strong></span></span></td>\n\t\t</tr>\n\t\t<tr>\n\t\t\t<th style=\"text-align: left; background-color: rgb(235, 241, 245); border-color: rgb(119, 119, 119);\"><span style=\"font-size:11px\"><span style=\"font-family:Arial,Helvetica,sans-serif\"><strong>АРМ пользователя*</strong></span></span></th>\n\t\t\t<td data-required=\"true\" style=\"border-color:#777777; height:1px; text-align:left\"><span style=\"font-size:11px\"><span style=\"font-family:Arial,Helvetica,sans-serif\"><strong>АРМ пользователя*</strong></span></span></td>\n\t\t</tr>\n\t\t<tr>\n\t\t\t<th style=\"text-align: left; background-color: rgb(235, 241, 245); border-color: rgb(119, 119, 119);\"><span style=\"font-size:11px\"><span style=\"font-family:Arial,Helvetica,sans-serif\"><strong>Версия модуля*</strong></span></span></th>\n\t\t\t<td data-required=\"true\" style=\"border-color:#777777; height:1px; text-align:left\"><span style=\"font-size:11px\"><span style=\"font-family:Arial,Helvetica,sans-serif\"><strong>Версия модуля*</strong></span></span></td>\n\t\t</tr>\n\t\t<tr>\n\t\t\t<th style=\"text-align: left; background-color: rgb(235, 241, 245); border-color: rgb(119, 119, 119);\"><span style=\"font-size:11px\"><span style=\"font-family:Arial,Helvetica,sans-serif\"><strong>Операционный день*</strong></span></span></th>\n\t\t\t<td data-required=\"true\" style=\"border-color:#777777; text-align:left\"><span style=\"font-size:11px\"><span style=\"font-family:Arial,Helvetica,sans-serif\"><strong>Операционный день*</strong></span></span></td>\n\t\t</tr>\n\t\t<tr>\n\t\t\t<th style=\"text-align: left; background-color: rgb(235, 241, 245); border-color: rgb(119, 119, 119);\"><span style=\"font-size:11px\"><span style=\"font-family:Arial,Helvetica,sans-serif\"><strong>Воспроизведение</strong></span></span></th>\n\t\t\t<td style=\"border-color:#777777; height:1px; text-align:left\"><span style=\"font-size:11px\"><span style=\"font-family:Arial,Helvetica,sans-serif\"><strong>Воспроизведение</strong></span></span></td>\n\t\t</tr>\n\t\t<tr>\n\t\t\t<th style=\"text-align: left; background-color: rgb(235, 241, 245); border-color: rgb(119, 119, 119);\"><span style=\"font-size:11px\"><span style=\"font-family:Arial,Helvetica,sans-serif\"><strong>Ожидаемый результат</strong></span></span></th>\n\t\t\t<td style=\"border-color:#777777; height:1px; text-align:left\"><span style=\"font-size:11px\"><span style=\"font-family:Arial,Helvetica,sans-serif\"><strong>Ожидаемый результат</strong></span></span></td>\n\t\t</tr>\n\t\t<tr>\n\t\t\t<th style=\"text-align: left; background-color: rgb(235, 241, 245); border-color: rgb(119, 119, 119);\"><span style=\"font-size:11px\"><span style=\"font-family:Arial,Helvetica,sans-serif\"><strong>Фактический результат</strong></span></span></th>\n\t\t\t<td style=\"border-color:#777777; height:1px; text-align:left\"><span style=\"font-size:11px\"><span style=\"font-family:Arial,Helvetica,sans-serif\"><strong>Фактический результат</strong></span></span></td>\n\t\t</tr>\n\t</tbody>\n</table>\n");
        task.setHandlerUser(handlerUser);
        udf = refreshUdf();
        udf.setUdfList(generateUdfList(UDF_WORKTASK_SEVERITY, UDF_WORKTASK_SEVERITY_TRIVIAL));
        udf.setSecondUdfList(generateUdfList(UDF_WORKTASK_COMPLEXITYLEVEL, TASK_LEVEL_7));
        udf.setUdfUser(generateUdfUser(UDF_WORKTASK_SUPERVISER, ABDULLAEV_BAHODIR));
        udf.setSecondUdfUser(generateUdfUser(UDF_WATCHER, BABUSHKIN_IVAN));
        if (cdpBl != null)
            udf.setThirdUdfList(generateUdfList(UDF_CDP_BL, cdpBl));
        udf.setUdfTask(generateUdfTask(UDF_SD_MODULE, CORE));
        udf.setUdfString(generateUdfString(UDF_SD_NOMODULE_REASON, generateString()));
        if (misService != null)
            udf.setFourthUdfList(generateUdfList(UDF_MIS_SERVICE, misService));
        udf.setFifthUdfList(generateUdfList(UDF_CDP_ACCEPTANCE, UDF_CDP_ACCEPTANCE_NO));
        udf.setSixthUdfList(generateUdfList(UDF_WORKTASK_ANALYSIS, UDF_WORKTASK_ANALYSIS_NO));
        udf.setUdfDate(generateUdfDate(UDF_WORKTASK_PLANTD, 1));
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
        udf.setSecondUdfString(generateUdfString(UDF_WORKTASK_BRANCH, branches.get(0)));
        udf.setSecondUdfTask(generateUdfTask(UDF_PRODUCT, productTask.getTaskValue()[0]));
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
        udf.setFifthUdfTask(generateUdfTask(UDF_BDKU_CONFIGURATION, bdkuTask.getTaskValueSelector()[0]));
        udf.setFifthUdfTask(customerRequest);
        udf.setSixthUdfTask(udfTestTask);
        udf.setTwelfthUdfList(generateUdfList(UDF_CDP_BP, UDF_CDP_BP_TROUBLESHOOTING, "{\"TC\":[\"0901\",\"0201\",\"0505\",\"0506\",\"0707\",\"0714\",\"0803\"]}"));
        udf.setSeventhUdfTask(generateUdfTask(UDF_WORKTASK_WORK, KZ_KZI));
        udf.setEighthUdfTask(generateUdfTask(UDF_WORKTASK_DEPENDBF, new Task.Constants[]{MTBANK, NOTIFICATION_SERVICE, HEAD_BOOK}));
        udf.setThirteenthUdfList(generateUdfList(UDF_WORKTASK_CHANGEWORKERINRQST, YES_AND_LEAVE_THIS_ROLE_TO_THE_CURRENT_PERFORMER));
        udf.setFourteenthUdfList(generateUdfList(UDF_WORKTASK_ADDWATCHERINREQST, UDF_WORKTASK_ADDWATCHERINREQST_YES));
        udf.setFifteenthUdfList(generateUdfList(UDF_WORKTASK_ADDTRSTWATCHINREQST, UDF_WORKTASK_ADDTRSTWATCHINREQST_NO));
        task.refreshUdf(udf);
        bugTaskController.createBagTask(task);
        var response = bugTaskController.getResponse();
        constructorUser = JsonPath.from(response.asString()).getObject("udfs.UDF_SD_MODULE.taskValue[0].udfs.UDF_CONSTRUCTOR.userValue[0]", User.class);
        ApiAsserts.assertThat(response)
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isCorrectStatus(STATUS_WORKTASK_ASSIGNED)
                .isEquals(task);
    }

    @Test(groups = {"BugTask", "Regression"}, description = "Принять в работу", dependsOnMethods = "bugTask")
    public void taskStart() {
        apiController.updateToken(generateAuthToken(handlerUser));
        task.setHandlerUser(handlerUser);
        task.setDescription(generateString());
        udf = refreshUdf();
        firstPlanTdDate = DateUtils.getCurrentDate(1);
        udf.setUdfMemo(generateUdfMemo(UDF_CDP_STEPPROGRESS, "[{\"id\":\"8181816a8997826e018a3bfd2ab9164c\",\"name\":\"Testing Пошаговый план\",\"order\":1,\"taskId\":\"8181816a8997826e018a3bfd105b15f8\",\"weight\":1,\"budget\":0,\"progress\":0,\"description\":\"\",\"status\":\"BASE\",\"workTypeId\":\"402881c25124956701513912e0f0081d\",\"workTypeNorm\":0.0,\"nameDraft\":\"Testing Пошаговый план\",\"orderDraft\":1,\"weightDraft\":1,\"budgetDraft\":0,\"statusDraft\":\"BASE\",\"workTypeIdDraft\":\"402881c25124956701513912e0f0081d\",\"workTypeNormDraft\":0.0,\"hrs\":0,\"deletable\":true,\"actualBudget\":0,\"planby\":\"budget\",\"workTypeAsString\":\"[0103] Создание документа - таблица, бизнес\",\"workTypeDraftAsString\":\"[0103] Создание документа - таблица, бизнес\",\"draftChanged\":true}]"));
        udf.setUdfUser(generateUdfUser(STDT_HANDLER, handlerUser));
        udf.setUdfDate(generateUdfDate(UDF_WORKTASK_PLANTD, firstPlanTdDate));
        udf.setUdfDouble(generateUdfDouble(UDF_WORKTASK_PLANBUDGET, firstPlanBudget));
        task.refreshUdf(udf);
        bugTaskController.changeTaskType(TaskType.WORK_TASK);
        bugTaskController.performCommonOperation(task, ACCEPT_IN_WORK);
        ApiAsserts.assertThat(bugTaskController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isCorrectStatus(STATUS_WORKTASK_INWORK);

        var response = apiController.receiveTask(task.getNumber());
        CommonAssert
                .assertThat(response)
                .isCorrectUdfDate(UDF_WORKTASK_PLANTD, firstPlanTdDate)
                .isCorrectUDfDouble(UDF_WORKTASK_FIRSTPLANBUDGET, UDF_WORKTASK_PLANBUDGET);
    }

    @Test(groups = {"BugTask", "Regression"}, description = "Комментарий", dependsOnMethods = "taskStart")
    public void taskComment() {
        apiController.updateToken(generateAuthToken(handlerUser));
        task.setHandlerUser(handlerUser);
        var comment = generateString();
        task.setDescription(comment);
        udf = refreshUdf();
        task.refreshUdf(udf);
        bugTaskController.performCommonOperation(task, COMMENT);
        var updateResponse = bugTaskController.getResponse();
        ApiAsserts.assertThat(updateResponse)
                .checkingResponseMessageField("description", comment)
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isCorrectStatus(STATUS_WORKTASK_INWORK);
    }

    @Test(groups = {"BugTask", "Regression"}, description = "Отклонить ошибку", dependsOnMethods = "taskComment")
    public void taskDecline() {
        apiController.updateToken(generateAuthToken(handlerUser));
        task.setHandlerUser(handlerUser);
        task.setDescription(generateString());
        task.setResolution(generateResolution(MSG_WORKTASK_BUGDECLINE));
        udf = refreshUdf();
        udf.setUdfList(generateUdfList(UDF_WORKTASK_REASONERROR, UDF_WORKTASK_REASONERROR_IMPL));
        udf.setUdfMemo(generateUdfMemo(UDF_WORKTASK_ERRORDESCRIPTION, "Описание ошибки и/или причины отсутствия источника"));
        udf.setSecondUdfMemo(generateUdfMemo(UDF_CDP_STEPPROGRESS, "[{\"id\":\"8181816a8997826e018a40a7df531b82\",\"name\":\"Testing Пошаговый план\",\"order\":1,\"taskId\":\"" + task.getId() +
                "\",\"weight\":1,\"budget\":0,\"progress\":0,\"description\":\"\",\"status\":\"ACTUAL\",\"workTypeId\":\"402881c25124956701513912e0f0081d\",\"workTypeNorm\":0.0,\"hrs\":0,\"deletable\":true,\"actualBudget\":0,\"planby\":\"budget\",\"workTypeAsString\":\"[0103] Создание документа - таблица, бизнес\",\"workTypeDraftAsString\":\"-\",\"draftChanged\":true}]"));

        task.refreshUdf(udf);
        bugTaskController.performCommonOperation(task, BUGDECLINE);
        var updateResponse = bugTaskController.getResponse();
        ApiAsserts.assertThat(updateResponse)
                .checkingResponseMessageField("handlerUser.login", creator.getLogin())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isCorrectStatus(STATUS_WORKTASK_DECLINED);
    }

    @Test(groups = {"BugTask", "Regression"}, description = "Вернуть в работу", dependsOnMethods = "taskDecline")
    public void taskReturn() {
        apiController.updateToken(generateAuthToken(creator));
        task.setHandlerUser(handlerUser);
        task.setDescription(generateString());
        udf = refreshUdf();
        udf.setUdfUser(generateUdfUser(STDT_HANDLER, handlerUser));
        task.refreshUdf(udf);
        bugTaskController.performCommonOperation(task, RETURN);
        var updateResponse = bugTaskController.getResponse();
        ApiAsserts.assertThat(updateResponse)
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isCorrectStatus(STATUS_WORKTASK_ASSIGNED);
    }


    @Test(groups = {"BugTask", "Regression"}, description = "Принять в работу", dependsOnMethods = "taskReturn")
    public void taskStart2() {
        apiController.updateToken(generateAuthToken(handlerUser));
        task.setHandlerUser(handlerUser);
        task.setDescription(generateString());
        udf = refreshUdf();
        udf.setUdfMemo(generateUdfMemo(UDF_CDP_STEPPROGRESS, "[{\"id\":\"8181816a8997826e018a44d4d7f61f86\",\"name\":\"Testing Пошаговый план\",\"order\":1,\"taskId\":\"" + task.getId() +
                "\",\"weight\":1,\"budget\":0,\"progress\":0,\"description\":\"\",\"status\":\"ACTUAL\",\"workTypeId\":\"402881c25124956701513912e0f0081d\",\"workTypeNorm\":0.0,\"hrs\":0,\"deletable\":true,\"actualBudget\":0,\"planby\":\"budget\",\"workTypeAsString\":\"[0103] Создание документа - таблица, бизнес\",\"workTypeDraftAsString\":\"-\",\"draftChanged\":true}]"));
        udf.setUdfUser(generateUdfUser(STDT_HANDLER, handlerUser));
        udf.setUdfDate(generateUdfDate(UDF_WORKTASK_PLANTD, firstPlanTdDate));
        udf.setUdfDouble(generateUdfDouble(UDF_WORKTASK_PLANBUDGET, firstPlanBudget));
        task.refreshUdf(udf);

        bugTaskController.performCommonOperation(task, ACCEPT_IN_WORK);
        ApiAsserts.assertThat(bugTaskController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isCorrectStatus(STATUS_WORKTASK_INWORK);

        var response = apiController.receiveTask(task.getNumber());
        CommonAssert
                .assertThat(response)
                .isCorrectUDfDouble(UDF_WORKTASK_FIRSTPLANBUDGET, UDF_WORKTASK_PLANBUDGET);
    }

    @Test(groups = {"BugTask", "Regression"}, description = "Отложить", dependsOnMethods = "taskStart2")
    public void taskPostpone() {
        apiController.updateToken(generateAuthToken(handlerUser));
        task.setResolution(generateResolution(RESOLUTION_WORK_SUSPENDED_INDEFINITELY));
        udf = refreshUdf();
        udf.setUdfDate(generateUdfDate(UDF_WORKTASK_PLANFD, 1));
        udf.setUdfMemo(generateUdfMemo(UDF_CDP_STEPPROGRESS, "[{\"id\":\"8181816a8997826e018a44d4d7f61f86\",\"name\":\"Testing Пошаговый план\",\"order\":1,\"taskId\":\"" + task.getId() +
                "\",\"weight\":1,\"budget\":0,\"progress\":0,\"description\":\"\",\"status\":\"ACTUAL\",\"workTypeId\":\"402881c25124956701513912e0f0081d\",\"workTypeNorm\":0.0,\"hrs\":0,\"deletable\":true,\"actualBudget\":0,\"planby\":\"budget\",\"workTypeAsString\":\"[0103] Создание документа - таблица, бизнес\",\"workTypeDraftAsString\":\"-\",\"draftChanged\":true}]"));
        task.refreshUdf(udf);
        bugTaskController.performCommonOperation(task, Operations.POSTPONE);
        ApiAsserts.assertThat(bugTaskController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isCorrectStatus(STATUS_WORKTASK_POSTPONED);
    }

    @Test(groups = {"BugTask", "Regression"}, description = "Принять в работу", dependsOnMethods = "taskPostpone")
    public void taskStart3() {
        apiController.updateToken(generateAuthToken(handlerUser));
        task.setHandlerUser(handlerUser);
        task.setDescription(generateString());
        udf = refreshUdf();
        udf.setUdfMemo(generateUdfMemo(UDF_CDP_STEPPROGRESS, "[{\"id\":\"8181816a8997826e018a44d4d7f61f86\",\"name\":\"Testing Пошаговый план\",\"order\":1,\"taskId\":\"" + task.getId() +
                "\",\"weight\":1,\"budget\":0,\"progress\":0,\"description\":\"\",\"status\":\"ACTUAL\",\"workTypeId\":\"402881c25124956701513912e0f0081d\",\"workTypeNorm\":0.0,\"hrs\":0,\"deletable\":true,\"actualBudget\":0,\"planby\":\"budget\",\"workTypeAsString\":\"[0103] Создание документа - таблица, бизнес\",\"workTypeDraftAsString\":\"-\",\"draftChanged\":true}]"));
        udf.setUdfUser(generateUdfUser(STDT_HANDLER, handlerUser));
        udf.setUdfDate(generateUdfDate(UDF_WORKTASK_PLANTD, 1));
        udf.setUdfDouble(generateUdfDouble(UDF_WORKTASK_PLANBUDGET, firstPlanBudget));
        task.refreshUdf(udf);

        bugTaskController.performCommonOperation(task, ACCEPT_IN_WORK);
        ApiAsserts.assertThat(bugTaskController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isCorrectStatus(STATUS_WORKTASK_INWORK);

        var response = apiController.receiveTask(task.getNumber());
        CommonAssert
                .assertThat(response)
                .isCorrectUDfDouble(UDF_WORKTASK_FIRSTPLANBUDGET, UDF_WORKTASK_PLANBUDGET);
    }

    @Test(groups = {"BugTask", "Regression"}, description = "Связь с ККПО", dependsOnMethods = "taskStart3")
    public void changePrgArea() {
        apiController.updateToken(generateAuthToken(handlerUser));
        task.refreshUdf();
        udf.setUdfList(generateUdfList(UDF_PRGAREA, UDF_PRGAREA_BNK));
        task.refreshUdf(udf);
        bugTaskController.performCommonOperation(task, WORKTASK_CHANGEPRGAREA);
        ApiAsserts.assertThat(bugTaskController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isCorrectStatus(STATUS_WORKTASK_INWORK);

        var catSanctionTask = apiController.receiveSubTaskByCategory(task.getNumber(), "CAT_SANCTION");

        var catSanctionResponse = apiController.receiveTask(catSanctionTask.getNumber());
        CommonAssert
                .assertThat(catSanctionResponse)
                .isCorrectTaskStatus(STATUS_ADVICE_AWAIT)
                .isCorrectSubmitterUser(handlerUser.getLogin())
                .isCorrectUdfList(UDF_PRGAREA, UDF_PRGAREA_BNK.id)
                .isCorrectTaskDescription("Запрос на санкционирование КПО BNK по задаче")
                .isCorrectTaskLink(task.getNumber());

        var devTaskResponse = apiController.receiveTask(task.getNumber());
        CommonAssert
                .assertThat(devTaskResponse)
                .isCorrectReviewMode(UDF_PRGAREA, UDF_PRGAREA_BNK.getId(), "OFF")
                .isCorrectPrgCode(UDF_PRGAREA, UDF_PRGAREA_BNK.getId(), "BNK");
    }

    @Test(groups = {"BugTask", "Regression"}, description = "Перепланировать", dependsOnMethods = "changePrgArea")
    public void taskChangeTime() {
        apiController.updateToken(generateAuthToken(handlerUser));
        task.setDescription(generateString());
        task.setConfirmed(false);
        udf = refreshUdf();
        expectedCompletionDate = DateUtils.getCurrentDate(7);
        udf.setUdfDate(generateUdfDate(UDF_WORKTASK_AWAITTD, expectedCompletionDate));
        udf.setUdfDouble(generateUdfDouble(UDF_WORKTASK_AWAITBUDGET, planBudget));
        udf.setSecondUdfDouble(generateUdfDouble(UDF_CDP_NORMBUDGET, normBudget));
        udf.setUdfMemo(generateUdfMemo(UDF_CDP_STEPPLAN, "[{\"id\":\"8181816a8997826e018a451503e22640\",\"name\":\"Testing Пошаговый план\",\"order\":1,\"taskId\":\"" + task.getId() +
                "\",\"weight\":1,\"budget\":0,\"progress\":0,\"description\":\"\",\"status\":\"ACTUAL\",\"workTypeId\":\"402881c25124956701513912e0f0081d\",\"workTypeNorm\":0,\"hrs\":0,\"deletable\":true,\"actualBudget\":0,\"planby\":\"budget\",\"workTypeAsString\":\"[0103] Создание документа - таблица, бизнес\",\"workTypeDraftAsString\":\"-\",\"draftChanged\":true,\"orderDraft\":1,\"nameDraft\":\"Testing Пошаговый план 2\",\"descriptionDraft\":\"\",\"statusDraft\":\"ACTUAL\",\"weightDraft\":1,\"budgetDraft\":90000,\"budgetHrs\":\"25\",\"budgetMinutes\":0,\"workTypeIdDraft\":\"402881c2516c220101516c711ff80024\",\"workTypeNormDraft\":25}]"));
        udf.setUdfUser(generateUdfUser(UDF_WORKTASK_RESPFOREPLAN, handlerUser));
        udf.setUdfList(generateUdfList(UDF_WORKTASK_REASONFOREPLAN, DEVELOPMENT_ERRORS));
        udf.setSecondUdfMemo(generateUdfMemo(UDF_CDP_STEPPROGRESS, "[{\"id\":\"8181816a8997826e018a451503e22640\",\"name\":\"Testing Пошаговый план\",\"order\":1,\"taskId\":\"" + task.getId() +
                "\",\"weight\":1,\"budget\":0,\"progress\":0,\"description\":\"\",\"status\":\"ACTUAL\",\"workTypeId\":\"402881c25124956701513912e0f0081d\",\"workTypeNorm\":0.0,\"hrs\":0,\"deletable\":true,\"actualBudget\":0,\"planby\":\"budget\",\"workTypeAsString\":\"[0103] Создание документа - таблица, бизнес\",\"workTypeDraftAsString\":\"-\",\"draftChanged\":true}]"));
        task.refreshUdf(udf);
        bugTaskController.performCommonOperation(task, CHANGE_TIME);
        ApiAsserts.assertThat(bugTaskController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isCorrectStatus(STATUS_WORKTASK_INWORK);

        var response = apiController.receiveTask(task.getNumber());
        CommonAssert
                .assertThat(response)
                .isCorrectUdfList(UDF_WORKTASK_INREPLAN, UDF_WORKTASK_INREPLAN_YES.id);
    }

    @Test(groups = {"BugTask", "Regression"}, description = "Согласовать перепланирование", dependsOnMethods = "taskChangeTime")
    public void taskAgreeChangeTime() {
        apiController.updateToken(generateAuthToken(creator));
        task.setDescription(generateString());

        udf = refreshUdf();
        udf.setUdfMemo(generateUdfMemo(UDF_WORKTASK_TASKALLOCATION,
                "{\"planFinishDate\":\"\",\"userLogin\":\"" + handlerUser.getLogin() +
                        "\",\"allocations\":[]}"));
        udf.setUdfDate(generateUdfDate(UDF_WORKTASK_PLANTD, expectedCompletionDate));
        udf.setSecondUdfMemo(generateUdfMemo(UDF_CDP_STEPPROGRESS, "[{\"id\":\"8181816c89cef25c018a4611876c6257\",\"name\":\"Testing Пошаговый план\",\"order\":1,\"taskId\":\"" + task.getId() +
                "\",\"weight\":1,\"budget\":0,\"progress\":0,\"description\":\"\",\"status\":\"ACTUAL\",\"workTypeId\":\"402881c25124956701513912e0f0081d\",\"workTypeNorm\":0.0,\"nameDraft\":\"Testing Пошаговый план\",\"statusDraft\":\"DELETE\",\"hrs\":0,\"deletable\":true,\"actualBudget\":0,\"planby\":\"budget\",\"workTypeAsString\":\"[0103] Создание документа - таблица, бизнес\",\"workTypeDraftAsString\":\"-\",\"draftChanged\":true}]"));
        task.refreshUdf(udf);
        bugTaskController.performCommonOperation(task, WORKTASK_CONFORMREPLAN);
        ApiAsserts.assertThat(bugTaskController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isCorrectStatus(STATUS_WORKTASK_INWORK);

        var response = apiController.receiveTask(task.getNumber());
        CommonAssert
                .assertThat(response)
                .isCorrectUdfDouble("Первоначальная оценка трудоёмкости", UDF_WORKTASK_FIRSTPLANBUDGET, firstPlanBudget)
                .isCorrectUdfDouble("Трудоемкость по нормам", UDF_CDP_NORMBUDGET, normBudget)
                .isCorrectUdfDouble("Оценка трудоемкости", UDF_WORKTASK_PLANBUDGET, planBudget)
                .isCorrectUdfInteger("Количество перепланирований", UDF_WORKTASK_REPLAN_N, 1)
                .isCorrectUdfDate(UDF_WORKTASK_AWAITTD, expectedCompletionDate)
                .isCorrectUdfDate(UDF_WORKTASK_PLANTD, expectedCompletionDate);
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
        bugTaskController.performCommonOperation(task, WORKTASK_DISTTOALL);
        ApiAsserts.assertThat(bugTaskController.getResponse())
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
        bugTaskController.performCommonOperation(task, WORKTASK_DISTTOALL);
        var response = bugTaskController.getResponse();
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
        bugTaskController.performCommonOperation(task, WORKTASK_DISTTOALL);
        var response = bugTaskController.getResponse();
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
        udf.setUdfString(generateUdfString(UDF_WORKTASK_BRANCH, branches.get(1)));
        udf.setUdfMemo(generateUdfMemo(UDF_CDP_STEPPROGRESS, "[{\"id\":\"8181816c89cef25c018a6f32b56b4b3c\",\"name\":\"Предварительный анализ\",\"order\":0,\"taskId\":\"" + task.getId() +
                "\",\"progress\":0,\"description\":\"\",\"status\":\"DELETE\",\"hrs\":0,\"deletable\":true,\"actualBudget\":0,\"planby\":\"budget\",\"workTypeAsString\":\"-\",\"workTypeDraftAsString\":\"-\",\"draftChanged\":true},{\"id\":\"8181816c89cef25c018a6f33e9c14c13\",\"name\":\"Пошаговый план 1\",\"order\":1,\"taskId\":\"" + task.getId() +
                "\",\"weight\":1,\"budget\":7200,\"progress\":0,\"description\":\"\",\"status\":\"ACTUAL\",\"workTypeId\":\"402881c2516c220101516c711ff80024\",\"workTypeNorm\":2.0,\"hrs\":0,\"deletable\":true,\"actualBudget\":0,\"planby\":\"budget\",\"workTypeAsString\":\"[0701] Иное\",\"workTypeDraftAsString\":\"-\",\"draftChanged\":true},{\"id\":\"8181816c89cef25c018a6f33e9c14c14\",\"name\":\"Пошаговый план 2\",\"order\":2,\"taskId\":\"" + task.getId() +
                "\",\"weight\":1,\"budget\":7200,\"progress\":0,\"description\":\"\",\"status\":\"ACTUAL\",\"workTypeId\":\"402881c2516c220101516c711ff80024\",\"workTypeNorm\":2.0,\"hrs\":0,\"deletable\":true,\"actualBudget\":0,\"planby\":\"budget\",\"workTypeAsString\":\"[0701] Иное\",\"workTypeDraftAsString\":\"-\",\"draftChanged\":true}]"));

        task.refreshUdf(udf);
        bugTaskController.performCommonOperation(task, WORKTASK_CHANGEBRANCH);
        var response = bugTaskController.getResponse();
        ApiAsserts.assertThat(response)
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK);
    }

    @Test(groups = {"BugTask", "Regression"}, description = "Установить функциональную зависимость от другой задачи", dependsOnMethods = "taskChangeBranch")
    public void taskDependOtherTask() {
        apiController.updateToken(generateAuthToken(handlerUser));
        task.setDescription(generateString());
        udf = refreshUdf();
        dependTaskFNC.clear();
        var userData1 = new UserData(null, "{\"type\":\"REQUIRED\",\"comment\":\"Required\"}");
        dependTaskFNC.put(HEAD_BOOK, userData1);
        udf.setUdfTask(generateUdfTask(UDF_WORKTASK_DEPENDTASKFNC, dependTaskFNC));
        task.refreshUdf(udf);
        bugTaskController.performCommonOperation(task, WORKTASK_DEPENDOTHERTASK);
        var response = bugTaskController.getResponse();
        ApiAsserts.assertThat(response)
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK);
    }


    @Test(groups = {"BugTask", "Regression"}, description = "Изменить план тестирования", dependsOnMethods = "taskDependOtherTask")
    public void changeTestPlanTask() {
        var description = generateComment();
        apiController.updateToken(generateAuthToken(handlerUser));
        udf = refreshUdf();
        task.refreshTask();
        udf.setUdfMemo(generateUdfMemo(UDF_WORKTASK_TESTPLAN, description));
        task.refreshUdf(udf);
        bugTaskController.performCommonOperation(task, WORKTASK_CHANGETESTPLAN);
        var response = bugTaskController.getResponse();
        ApiAsserts.assertThat(response)
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK);
        var taskDetail = apiController.receiveTask(task.getNumber());
        CommonAssert
                .assertThat(taskDetail)
                .isCorrectUdfMemo(UDF_WORKTASK_TESTPLAN, description);
    }

    @Test(groups = {"BugTask", "Regression"}, description = "Изменить список связанных задач", dependsOnMethods = "changeTestPlanTask")
    public void changeLinkedTask() {
        apiController.updateToken(generateAuthToken(handlerUser));
        task.setDescription(generateString());
        udf = refreshUdf();
        task.refreshTask();
        var linkedTask = new com.ts.common.entitites.commonEntities.Task.Constants[]{
                AKKREDITIVES,
                SERVICE_DESK
        };
        udf.setUdfTask(generateUdfTask(UDF_SD_LINKEDREQUEST, linkedTask));
        task.refreshUdf(udf);
        bugTaskController.performCommonOperation(task, CHANGE_LINKED_TASKS);
        var response = bugTaskController.getResponse();
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
        apiController.updateToken(generateAuthToken(handlerUser));
        task.setDescription(generateString());
        udf = refreshUdf();
        task.refreshTask();
        udf.setUdfList(generateUdfList(UDF_MIS_SERVICE, misServiceForChange));
        task.refreshUdf(udf);
        bugTaskController.performCommonOperation(task, CHANGE_SERVICE);
        var response = bugTaskController.getResponse();
        ApiAsserts.assertThat(response)
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK);

        var taskDetail = apiController.receiveTask(task.getNumber());
        CommonAssert
                .assertThat(taskDetail)
                .isCorrectUdfList(UDF_MIS_SERVICE, misServiceForChange);

    }

    @Test(groups = {"BugTask", "Regression"}, description = "Назначить контролёра", dependsOnMethods = "taskChangeService")
    public void taskChangeSupervisor() {
        apiController.updateToken(generateAuthToken(handlerUser));
        task.setDescription(generateString());
        udf = refreshUdf();
        task.refreshTask();
        udf.setUdfUser(generateUdfUser(UDF_WORKTASK_SUPERVISER, new User.Constants[]{ABDULLAEV_BAHODIR, AKSENOV_ANDREY}));
        task.refreshUdf(udf);
        bugTaskController.performCommonOperation(task, SUPERVISE);
        var response = bugTaskController.getResponse();
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
        apiController.updateToken(generateAuthToken(handlerUser));
        task.setDescription(generateString());
        udf = refreshUdf();
        task.refreshTask();
        udf.setUdfUser(generateUdfUser(UDF_WATCHER, new User.Constants[]{BABUSHKIN_IVAN, ALTUNIN_NIKOLAY}));
        udf.setUdfMemo(generateUdfMemo(UDF_CDP_STEPPROGRESS, "[{\"id\":\"8181816c89cef25c018a4f111b5d0094\",\"name\":\"Testing Пошаговый план\",\"order\":0,\"taskId\":\"" + task.getId() +
                "\",\"progress\":0,\"description\":\"\",\"status\":\"DELETE\",\"hrs\":0,\"deletable\":true,\"actualBudget\":0,\"planby\":\"budget\",\"workTypeAsString\":\"-\",\"workTypeDraftAsString\":\"-\",\"draftChanged\":true}]"));
        task.refreshUdf(udf);
        bugTaskController.performCommonOperation(task, WATCH);
        var response = bugTaskController.getResponse();
        ApiAsserts.assertThat(response)
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK);

        var taskDetail = apiController.receiveTask(task.getNumber());
        CommonAssert
                .assertThat(taskDetail)
                .isCorrectUdfUSer(UDF_WATCHER, BABUSHKIN_IVAN.login)
                .isCorrectUdfUSer(UDF_WATCHER, ALTUNIN_NIKOLAY.login);
    }

    @Test(groups = {"BugTask", "Regression"}, description = "Привязать к источнику ошибки", dependsOnMethods = "taskChangeWatcher")
    public void taskBindErrorTask() {
        apiController.updateToken(generateAuthToken(handlerUser));
        task.setDescription(generateString());
        udf = refreshUdf();
        task.refreshTask();
        udf.setUdfList(generateUdfList(UDF_WORKTASK_REASONERROR, UDF_WORKTASK_REASONERROR_DBL));
        udf.setSecondUdfList(generateUdfList(UDF_WORKTASK_FROMSDREQUEST, UDF_WORKTASK_FROMSDREQUEST_NO));
        udf.setUdfTask(generateUdfTask(UDF_WORKTASK_ERRORTASK, MODERN_COLVIR_PRODUCT));
        udf.setUdfMemo(generateUdfMemo(UDF_WORKTASK_ERRORDESCRIPTION, "Описание ошибки и/или причины отсутствия " + generateString()));
        udf.setThirdUdfList(generateUdfList(UDF_WORKTASK_DEVREGVIOLATION, UDF_WORKTASK_DEVREGVIOLATION_YES));
        task.refreshUdf(udf);
        bugTaskController.performCommonOperation(task, WORKTASK_BINDERRORTASK);
        var response = bugTaskController.getResponse();
        ApiAsserts.assertThat(response)
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK);

        var taskDetail = apiController.receiveTask(task.getNumber());
        CommonAssert
                .assertThat(taskDetail)
                .isCorrectUdfTask(UDF_WORKTASK_ERRORTASK, MODERN_COLVIR_PRODUCT);
    }

    @Test(groups = {"BugTask", "Regression"}, description = "Создать подзадачу копированием", dependsOnMethods = "taskChangeWatcher")
    public void taskCreateSubtaskWithCopy() {
        apiController.updateToken(generateAuthToken(handlerUser));
        task.setDescription(generateString());
        udf = refreshUdf();
        task.refreshTask();
        task.refreshUdf(udf);
        bugTaskController.performCommonOperation(task, WORKTASK_CREATESUBTASKS);
        var response = bugTaskController.getResponse();
        ApiAsserts.assertThat(response)
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK);
        var catBugTask = apiController.receiveSubTaskByCategory(task.getNumber(), "CAT_BUGTASK");
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

    @Test(groups = {"BugTask", "Regression"}, description = "Установить общеполезность", dependsOnMethods = "taskCreateSubtaskWithCopy")
    public void taskChangeGenuse() {
        apiController.updateToken(generateAuthToken(handlerUser));
        task.setDescription(generateString());
        udf = refreshUdf();
        task.refreshTask();
        udf.setUdfList(generateUdfList(UDF_SDFEATURE_GENUSE, LOCAL, "{\"username\":\"shamsaddin.gadirov@azems.az\",\"name\":\"Шамсаддин Гадиров\"}"));
        udf.setUdfMemo(generateUdfMemo(UDF_CDP_STEPPROGRESS, "[{\"id\":\"8181816c89cef25c018a4f5c5958084f\",\"name\":\"Testing Пошаговый план\",\"order\":0,\"taskId\":\"" + task.getId() +
                "\",\"progress\":0,\"description\":\"\",\"status\":\"DELETE\",\"hrs\":0,\"deletable\":true,\"actualBudget\":0,\"planby\":\"budget\",\"workTypeAsString\":\"-\",\"workTypeDraftAsString\":\"-\",\"draftChanged\":true}]"));
        task.refreshUdf(udf);
        bugTaskController.performCommonOperation(task, WORKTASK_GENUSE);
        var response = bugTaskController.getResponse();
        ApiAsserts.assertThat(response)
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK);
        var taskDetail = apiController.receiveTask(task.getNumber());
        CommonAssert
                .assertThat(taskDetail)
                .isCorrectUdfList(UDF_SDFEATURE_GENUSE, LOCAL.getId());
    }
}