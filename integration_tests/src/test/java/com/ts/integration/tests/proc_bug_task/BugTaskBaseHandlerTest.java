package com.ts.integration.tests.proc_bug_task;

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
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import static com.ts.common.application.database.DbQueryHelper.Operators.*;
import static com.ts.common.entitites.commonEntities.List.Constants.*;
import static com.ts.common.entitites.commonEntities.Task.Constants.*;
import static com.ts.common.entitites.commonEntities.Udfs.UdfSd.*;
import static com.ts.common.entitites.commonEntities.User.Constants.ABDULLAEV_BAHODIR;
import static com.ts.common.entitites.commonEntities.User.Constants.BABUSHKIN_IVAN;
import static com.ts.common.enums.Operations.*;
import static com.ts.common.enums.Resolutions.MSG_WORKTASK_BUGDECLINE;
import static com.ts.common.enums.Resolutions.RESOLUTION_WORK_SUSPENDED_INDEFINITELY;
import static com.ts.common.enums.TaskStatuses.*;
import static com.ts.common.utils.InitEntities.*;
import static com.ts.common.utils.RandomUtils.generateString;

public class BugTaskBaseHandlerTest extends BaseIntegrationTest {
    public BugTaskController bugTaskController;
    private GeneralTask task;
    private String misService;
    private String cdpBl;
    private UdfTask productTask;
    private UdfTask bdkuTask;
    private Parent parent;
    private GrTaskTable grTaskTable;
    private GrTaskDbEntity parentTaskFromDb;
    private UdfTask customerRequest;
    private User creator;
    private User handlerUser;
    private Map<List.Constants, UserData> dependTaskFNC;


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
                "task_status", NOT_EQUAL.operator, STATUS_WORKTASK_CLOSED.name());
        customerRequest = InitEntities.generateUdfTask(UDF_WORKTASK_SDREQUEST, new Task(taskSlaBug.getTask_id(), taskSlaBug.getTask_number()));
        misService = bugTaskController.getMisService();
        cdpBl = bugTaskController.getCdpBl();
        var employees = userController.receiveUserByTask(parent.getNumber());
        creator = employees.stream().filter(f -> f.getAssignedRole().getName().equals("Менеджер проекта")).findFirst().get().getForUser();
        handlerUser = employees.stream().filter(f -> f.getAssignedRole().getName().equals("Участник проекта") && !f.getForUser().getLogin().equals(creator.getLogin())).findFirst().get().getForUser();
    }


    @Test(groups = {"BugTask", "Regression"}, description = "создание CAT_BUGTASK")
    public void bugTask() {
        apiController.updateToken(InitEntities.generateAuthToken(creator));
        task.setParent(parent);
        task.setName("Задача на исправление ошибки: " + DateUtils.getCurrentDate(0));
        task.setDescription("<table border=\"1\" cellpadding=\"0\" cellspacing=\"0\" class=\"general\" style=\"border:1px solid\">\n\t<tbody>\n\t\t<tr>\n\t\t\t<th style=\"text-align: left; background-color: rgb(235, 241, 245); border-color: rgb(119, 119, 119);\" width=\"30%\"><span style=\"font-size:11px\"><strong>Разрешение экрана, версия ОС</strong><strong>*</strong></span></th>\n\t\t\t<td data-required=\"true\" style=\"border-color:#777777; height:1px; text-align:left\" width=\"70%\">1000x2000</td>\n\t\t</tr>\n\t\t<tr>\n\t\t\t<th style=\"text-align: left; background-color: rgb(235, 241, 245); border-color: rgb(119, 119, 119);\" width=\"30%\"><span style=\"font-size:11px\"><span style=\"font-family:Arial,Helvetica,sans-serif\"><strong>Клиентская часть*</strong></span></span></th>\n\t\t\t<td data-required=\"true\" style=\"border-color:#777777; height:1px; text-align:left\" width=\"70%\"><span style=\"font-size:11px\"><span style=\"font-family:Arial,Helvetica,sans-serif\"><strong>Клиентская часть*</strong></span></span></td>\n\t\t</tr>\n\t\t<tr>\n\t\t\t<th style=\"text-align: left; background-color: rgb(235, 241, 245); border-color: rgb(119, 119, 119);\"><span style=\"font-size:11px\"><span style=\"font-family:Arial,Helvetica,sans-serif\"><strong>Инстанция для тестирования*</strong></span></span></th>\n\t\t\t<td data-required=\"true\" style=\"border-color:#777777; height:1px; text-align:left\"><span style=\"font-size:11px\"><span style=\"font-family:Arial,Helvetica,sans-serif\"><strong>Инстанция для тестирования*</strong></span></span></td>\n\t\t</tr>\n\t\t<tr>\n\t\t\t<th style=\"text-align: left; background-color: rgb(235, 241, 245); border-color: rgb(119, 119, 119);\"><span style=\"font-size:11px\"><span style=\"font-family:Arial,Helvetica,sans-serif\"><strong>Пользователь / пароль*</strong></span></span></th>\n\t\t\t<td data-required=\"true\" style=\"border-color:#777777; height:1px; text-align:left\"><span style=\"font-size:11px\"><span style=\"font-family:Arial,Helvetica,sans-serif\"><strong>Пользователь / пароль*</strong></span></span></td>\n\t\t</tr>\n\t\t<tr>\n\t\t\t<th style=\"text-align: left; background-color: rgb(235, 241, 245); border-color: rgb(119, 119, 119);\"><span style=\"font-size:11px\"><span style=\"font-family:Arial,Helvetica,sans-serif\"><strong>АРМ пользователя*</strong></span></span></th>\n\t\t\t<td data-required=\"true\" style=\"border-color:#777777; height:1px; text-align:left\"><span style=\"font-size:11px\"><span style=\"font-family:Arial,Helvetica,sans-serif\"><strong>АРМ пользователя*</strong></span></span></td>\n\t\t</tr>\n\t\t<tr>\n\t\t\t<th style=\"text-align: left; background-color: rgb(235, 241, 245); border-color: rgb(119, 119, 119);\"><span style=\"font-size:11px\"><span style=\"font-family:Arial,Helvetica,sans-serif\"><strong>Версия модуля*</strong></span></span></th>\n\t\t\t<td data-required=\"true\" style=\"border-color:#777777; height:1px; text-align:left\"><span style=\"font-size:11px\"><span style=\"font-family:Arial,Helvetica,sans-serif\"><strong>Версия модуля*</strong></span></span></td>\n\t\t</tr>\n\t\t<tr>\n\t\t\t<th style=\"text-align: left; background-color: rgb(235, 241, 245); border-color: rgb(119, 119, 119);\"><span style=\"font-size:11px\"><span style=\"font-family:Arial,Helvetica,sans-serif\"><strong>Операционный день*</strong></span></span></th>\n\t\t\t<td data-required=\"true\" style=\"border-color:#777777; text-align:left\"><span style=\"font-size:11px\"><span style=\"font-family:Arial,Helvetica,sans-serif\"><strong>Операционный день*</strong></span></span></td>\n\t\t</tr>\n\t\t<tr>\n\t\t\t<th style=\"text-align: left; background-color: rgb(235, 241, 245); border-color: rgb(119, 119, 119);\"><span style=\"font-size:11px\"><span style=\"font-family:Arial,Helvetica,sans-serif\"><strong>Воспроизведение</strong></span></span></th>\n\t\t\t<td style=\"border-color:#777777; height:1px; text-align:left\"><span style=\"font-size:11px\"><span style=\"font-family:Arial,Helvetica,sans-serif\"><strong>Воспроизведение</strong></span></span></td>\n\t\t</tr>\n\t\t<tr>\n\t\t\t<th style=\"text-align: left; background-color: rgb(235, 241, 245); border-color: rgb(119, 119, 119);\"><span style=\"font-size:11px\"><span style=\"font-family:Arial,Helvetica,sans-serif\"><strong>Ожидаемый результат</strong></span></span></th>\n\t\t\t<td style=\"border-color:#777777; height:1px; text-align:left\"><span style=\"font-size:11px\"><span style=\"font-family:Arial,Helvetica,sans-serif\"><strong>Ожидаемый результат</strong></span></span></td>\n\t\t</tr>\n\t\t<tr>\n\t\t\t<th style=\"text-align: left; background-color: rgb(235, 241, 245); border-color: rgb(119, 119, 119);\"><span style=\"font-size:11px\"><span style=\"font-family:Arial,Helvetica,sans-serif\"><strong>Фактический результат</strong></span></span></th>\n\t\t\t<td style=\"border-color:#777777; height:1px; text-align:left\"><span style=\"font-size:11px\"><span style=\"font-family:Arial,Helvetica,sans-serif\"><strong>Фактический результат</strong></span></span></td>\n\t\t</tr>\n\t</tbody>\n</table>\n");
        task.setHandlerUser(handlerUser);
////        task.setPriority(generateStatus(TaskStatuses.));
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
        udf.setUdfDouble(generateUdfDouble(UDF_WORKTASK_PLANBUDGET, 15));
        udf.setUdfMemo(generateUdfMemo(UDF_CDP_STEPPLAN, "[{\"orderDraft\":1,\"weightDraft\":1,\"budgetDraft\":0,\"planby\":\"budget\",\"deletable\":true,\"statusDraft\":\"NEW\",\"nameDraft\":\"Testing Пошаговый план\",\"workTypeIdDraft\":\"402881c25124956701513912e0f0081d\",\"workTypeNormDraft\":0,\"budgetHrs\":\"\",\"budgetMinutes\":\"\"}]"));
        var userData1 = new UserData(null, "{\"type\":\"REQUIRED\",\"comment\":\"Required\"}");
        var userData2 = new UserData(null, "{\"type\":\"RECOMMENDED\",\"comment\":\"RECOMMENDED\"}");
        var userData3 = new UserData(null, "{\"type\":\"OPTIONAL\",\"option\":\"949177\",\"optionNot\":\"773226\",\"comment\":\"Optional\"}");
        dependTaskFNC.put(UDF_PRGAREA_BNK, userData1);
        dependTaskFNC.put(UDF_PRGAREA_CDW, userData2);
        dependTaskFNC.put(UDF_PRGAREA_ISB, userData3);
        udf.setUdfMultiList(generateUdfMultiList(UDF_WORKTASK_DEPENDTASKFNC, dependTaskFNC));
        udf.setSecondUdfString(generateUdfString(UDF_WORKTASK_BRANCH, "hg:apng:default [Аnalitic platform new generation]"));
        udf.setSecondUdfTask(generateUdfTask(UDF_PRODUCT, productTask.getTaskValue()[0]));
        udf.setSeventhUdfList(generateUdfList(UDF_WORKTASK_WAYCODEREVIEW, WAY_CODE_REVIEW_NO));
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
        udf.setSixthUdfTask(generateUdfTask(UDF_WORKTASK_TESTTASK, WORKTASK_TESTTASK));
        udf.setTwelfthUdfList(generateUdfList(UDF_CDP_BP, UDF_CDP_BP_TROUBLESHOOTING, "{\"TC\":[\"0901\",\"0201\",\"0505\",\"0506\",\"0707\",\"0714\",\"0803\"]}"));
        udf.setSeventhUdfTask(generateUdfTask(UDF_WORKTASK_WORK, KZ_KZI));
        udf.setEighthUdfTask(generateUdfTask(UDF_WORKTASK_DEPENDBF, new Task.Constants[]{MTBANK, NOTIFICATION_SERVICE, HEAD_BOOK}));
        udf.setThirteenthUdfList(generateUdfList(UDF_WORKTASK_CHANGEWORKERINRQST, YES_AND_LEAVE_THIS_ROLE_TO_THE_CURRENT_PERFORMER));
        udf.setFourteenthUdfList(generateUdfList(UDF_WORKTASK_ADDWATCHERINREQST, UDF_WORKTASK_ADDWATCHERINREQST_YES));
        udf.setFifteenthUdfList(generateUdfList(UDF_WORKTASK_ADDTRSTWATCHINREQST, UDF_WORKTASK_ADDTRSTWATCHINREQST_NO));
        task.refreshUdf(udf);
        bugTaskController.createBagTask(task);

        ApiAsserts.assertThat(bugTaskController.getResponse())
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
        udf.setUdfMemo(generateUdfMemo(UDF_CDP_STEPPROGRESS, "[{\"id\":\"8181816a8997826e018a3bfd2ab9164c\",\"name\":\"Testing Пошаговый план\",\"order\":1,\"taskId\":\"8181816a8997826e018a3bfd105b15f8\",\"weight\":1,\"budget\":0,\"progress\":0,\"description\":\"\",\"status\":\"BASE\",\"workTypeId\":\"402881c25124956701513912e0f0081d\",\"workTypeNorm\":0.0,\"nameDraft\":\"Testing Пошаговый план\",\"orderDraft\":1,\"weightDraft\":1,\"budgetDraft\":0,\"statusDraft\":\"BASE\",\"workTypeIdDraft\":\"402881c25124956701513912e0f0081d\",\"workTypeNormDraft\":0.0,\"hrs\":0,\"deletable\":true,\"actualBudget\":0,\"planby\":\"budget\",\"workTypeAsString\":\"[0103] Создание документа - таблица, бизнес\",\"workTypeDraftAsString\":\"[0103] Создание документа - таблица, бизнес\",\"draftChanged\":true}]"));
        udf.setUdfUser(generateUdfUser(STDT_HANDLER, handlerUser));
        udf.setUdfDate(generateUdfDate(UDF_WORKTASK_PLANTD, 1));
        udf.setUdfDouble(generateUdfDouble(UDF_WORKTASK_PLANBUDGET, 15));
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
        udf.setUdfDate(generateUdfDate(UDF_WORKTASK_PLANTD, 1));
        udf.setUdfDouble(generateUdfDouble(UDF_WORKTASK_PLANBUDGET, 15));
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

    @Test(groups = {"DevTask", "Regression"}, description = "Отложить", dependsOnMethods = "taskStart2")
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
        udf.setUdfDouble(generateUdfDouble(UDF_WORKTASK_PLANBUDGET, 15));
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
}