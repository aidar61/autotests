package com.ts.integration.tests.proc_sla_help;

import com.ts.common.application.controllers.TrackStudioHttpStatusCodes;
import com.ts.common.application.database.dbEntities.GrTaskDbEntity;
import com.ts.common.application.database.dbTables.GrTaskTable;
import com.ts.common.asserts.ApiAsserts;
import com.ts.common.asserts.CommonAssert;
import com.ts.common.controllers.TaskResponseBody;
import com.ts.common.controllers.sla.SlaHelpController;
import com.ts.common.entitites.commonEntities.Parent;
import com.ts.common.entitites.commonEntities.Task;
import com.ts.common.entitites.commonEntities.User;
import com.ts.common.entitites.commonEntities.UserRole;
import com.ts.common.entitites.tasks.GeneralTask;
import com.ts.common.enums.Operations;
import com.ts.common.enums.TaskStatuses;
import com.ts.common.enums.TaskType;
import com.ts.common.utils.DateUtils;
import com.ts.common.utils.InitEntities;
import com.ts.common.utils.JsonUtils;
import com.ts.integration.tests.BaseIntegrationTest;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static com.ts.common.application.database.DbQueryHelper.Operators.EQUAL;
import static com.ts.common.entitites.commonEntities.List.Constants.*;
import static com.ts.common.entitites.commonEntities.Task.Constants.CORE;
import static com.ts.common.entitites.commonEntities.Udfs.UdfSd.*;
import static com.ts.common.enums.Operations.*;
import static com.ts.common.enums.TaskStatuses.STATUS_SLAHELP_NEW;
import static com.ts.common.utils.InitEntities.*;
import static com.ts.common.utils.RandomUtils.*;

public class SlaHelpBaseStaticTest extends BaseIntegrationTest {

    public SlaHelpController slaHelpController;
    private GeneralTask task;
    private Parent parent;
    private User CLIENT;
    private User SUPPORT_MANAGER;
    private User SUPPORT_MEMBER;
    private User ROLE_CONTRACT_EMP;
    private User RANDOM_WATCHER;
    private User RANDOM_WATCHER2;
    private User RANDOM_TRUST_WATCHER;
    private Task[] BDKU_CONFIGURATION;
    private com.ts.common.entitites.commonEntities.List[] SD_RELATED_TASK_CODES;
    private List<UserRole> USER_ROLES;
    private String patchNumber;


    @BeforeClass(alwaysRun = true)
    public void beforeClass() {
        slaHelpController = apiController.getSlaHelpController();
        userController = apiController.getUserController();
        GrTaskTable grTaskTable = dbHelper.getGrTaskTable();
        GrTaskDbEntity parentTaskFromDb = (GrTaskDbEntity) grTaskTable.receiveByTaskNumber("928684");
        parent = InitEntities.generateParent(parentTaskFromDb.getTask_id(), parentTaskFromDb.getTask_number());
        task = InitEntities.getGeneralTask(TaskType.SLA_HElP, Operations.CAT);

        var parentPayloadResponse = slaHelpController.getParentPayload(parent.getNumber(), "CAT_SLAHELP");
        ApiAsserts.assertThat(parentPayloadResponse).isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK);
        var parentPayload = JsonUtils.removeExtraCharacters(parentPayloadResponse);
        BDKU_CONFIGURATION = slaHelpController.getParent_UDF_BDKU_CONFIGURATION(parentPayload);

        SD_RELATED_TASK_CODES = slaHelpController.getParent_UDF_SD_RELATED_TASK_CODES(parentPayload);

        USER_ROLES = userController.receiveUserByTask(parent.getNumber());
        CLIENT = userController.receiveUserByRole(USER_ROLES, "Клиент", "root").getForUser();
        SUPPORT_MANAGER = userController.receiveUserByRole(USER_ROLES, "Менеджер клиента", "ovoronov").getForUser();
        SUPPORT_MEMBER = userController.receiveUserByRole(USER_ROLES, "Участник проекта сопровождения", "root").getForUser();
        ROLE_CONTRACT_EMP = userController.receiveUserByRole(USER_ROLES, "Ведение контрактов", "root").getForUser();
    }

    @Test(groups = {"PROC_SLAHELP", "Regression"}, description = "создание")
    public void createTask() {
        apiController.updateToken(InitEntities.generateAuthToken(CLIENT));
        udf = refreshUdf();
        task.refreshTask();
        task.setParent(parent);
        task.setDescription(generateDescriptionForOperation(Operations.CAT));
        task.setName(generateComment());
        udf.setUdfList(generateUdfList(UDF_SLA_URGANCYHELP, URGANCYHELP_YES));
        udf.setUdfMultiList(generateUdfMultiList(UDF_SD_RELATED_TASK_CODES, SD_RELATED_TASK_CODES[0].getId()));
        udf.setUdfTask(generateUdfTask(UDF_SD_MODULE, CORE));
        udf.setUdfString(generateUdfString(UDF_SD_CLIENTWATCHERS, generateEmail()));
        udf.setSecondUdfTask(generateUdfTask(UDF_BDKU_CONFIGURATION, BDKU_CONFIGURATION[0]));
        udf.setSecondUdfString(generateUdfString(UDF_SD_REMOTEID, generateString()));
        udf.setThirdUdfString(generateUdfString(UDF_SD_INITPERSON, generateString()));
        task.refreshUdf(udf);
        slaHelpController.createTask(task);
        var response = slaHelpController.getResponse();
        ApiAsserts
                .assertThat(response)
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isCorrectStatus(STATUS_SLAHELP_NEW)
                .isEquals(task);

        var taskDetail = apiController.receiveTask(task.getNumber());
        CommonAssert
                .assertThat(taskDetail)
                .isCorrectUdfList(UDF_SD_RESPONSIBLE_PARTY, RESPONSIBLE_PARTY_SUPPLIER);
    }

    @Test(groups = {"PROC_SLAHELP", "Regression"}, description = "Задать вопрос", dependsOnMethods = "createTask")
    public void taskCliComment() {
        apiController.updateToken(generateAuthToken(CLIENT));
        task.refreshTask();
        task.setDescription(generateComment());
        udf = refreshUdf();
        task.refreshUdf(udf);
        slaHelpController.performCommonOperation(task, CLI_COMMENT);
        ApiAsserts
                .assertThat(slaHelpController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK).assertTask();
//        var taskDetail = apiController.receiveTask(task.getNumber());
//        CommonAssert
//                .assertThat(taskDetail)
//                .isCorrectUdfList(UDF_SD_RESPONSIBLE_PARTY, RESPONSIBLE_PARTY_CLIENT.getId());
    }

    @Test(groups = {"PROC_SLAHELP", "Regression"}, description = "Сообщить информацию (Комментарий)", dependsOnMethods = "taskCliComment")
    public void taskOurComment() {
        apiController.updateToken(generateAuthToken(CLIENT));
        task.refreshTask();
        udf = refreshUdf();
        task.setDescription(generateString());
        task.refreshUdf(udf);
        slaHelpController.performCommonOperation(task, OUR_COMMENT);
        ApiAsserts.assertThat(slaHelpController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK);
//        var taskDetail = apiController.receiveTask(task.getNumber());
//        CommonAssert
//                .assertThat(taskDetail)
//                .isCorrectUdfList(UDF_SD_RESPONSIBLE_PARTY, RESPONSIBLE_PARTY_SUPPLIER.getId());
    }

    @Test(groups = {"PROC_SLAHELP", "Regression"}, description = "Изменить атрибуты запроса", dependsOnMethods = "taskOurComment")
    public void taskChangeAttr() {
        apiController.updateToken(generateAuthToken(CLIENT));
        task.refreshTask();
        udf = refreshUdf();
        task.setDescription(generateString());
        var sdRemoteId = generateString();
        var sdInterPerson = generateString();
        udf.setUdfString(generateUdfString(UDF_SD_REMOTEID, sdRemoteId));
        udf.setSecondUdfString(generateUdfString(UDF_SD_INITPERSON, sdInterPerson));
        task.refreshUdf(udf);
        slaHelpController.performCommonOperation(task, CHANGE_ATTR);
        ApiAsserts
                .assertThat(slaHelpController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .assertTask();
        var taskDetail = apiController.receiveTask(task.getNumber());
        CommonAssert
                .assertThat(taskDetail)
                .isCorrectUdfString(UDF_SD_REMOTEID, sdRemoteId)
                .isCorrectUdfString(UDF_SD_INITPERSON, sdInterPerson);
    }

    @Test(groups = {"PROC_SLAHELP", "Regression"}, description = "Назначить наблюдателей клиента", dependsOnMethods = "taskChangeAttr")
    public void taskAppointCliWatchers() {
        apiController.updateToken(generateAuthToken(CLIENT));
        task.refreshTask();
        udf = refreshUdf();
        task.setDescription(generateString());
        var email = generateEmail();
        udf.setUdfString(generateUdfString(UDF_SD_CLIENTWATCHERS, email));
        task.refreshUdf(udf);
        slaHelpController.performCommonOperation(task, ADD_CLIENT_WATCHERS);
        ApiAsserts
                .assertThat(slaHelpController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .assertTask();
        var taskDetail = apiController.receiveTask(task.getNumber());
        CommonAssert
                .assertThat(taskDetail)
                .isCorrectUdfString(UDF_SD_CLIENTWATCHERS, email);
    }

    @Test(groups = {"PROC_SLAHELP", "Regression"}, description = "Изменить автора", dependsOnMethods = "taskAppointCliWatchers")
    public void taskChangeAuthors() {
        var authors = slaHelpController.getAuthors(task.getTaskType(), CHANGE_AUTHOR, task.getNumber());
        if (authors == null) {
            throw new SkipException("Шаг <Изменить автора> пропущен, не найдены авторы.");
        }

        apiController.updateToken(generateAuthToken(SUPPORT_MANAGER));
        task.refreshTask();
        udf = refreshUdf();
        var initPerson = generateString();
        task.setDescription(generateString());
        udf.setUdfString(generateUdfString(UDF_SD_INITPERSON, initPerson));
        udf.setUdfUser(generateUdfUser(UDF_SD_AUTHORCLIENT_MSG, authors[0]));
        task.refreshUdf(udf);
        slaHelpController.performCommonOperation(task, CHANGE_AUTHOR);
        ApiAsserts.assertThat(slaHelpController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .assertTask();
        var taskDetail = apiController.receiveTask(task.getNumber());
        CommonAssert
                .assertThat(taskDetail)
                .isCorrectUdfString(UDF_SD_INITPERSON, initPerson)
                .isCorrectSubmitterUser(authors[0].getLogin());
    }

    @Test(groups = {"PROC_SLAHELP", "Regression"}, description = "Изменить модуль системы", dependsOnMethods = "taskChangeAuthors")
    public void taskChangeModule() {
        apiController.updateToken(generateAuthToken(SUPPORT_MANAGER));
        task.refreshTask();
        udf = refreshUdf();
        task.setDescription(generateString());
        udf.setUdfTask(generateUdfTask(UDF_SD_MODULE, CORE));
        task.refreshUdf(udf);
        slaHelpController.performCommonOperation(task, CHANGE_MODULE);
        ApiAsserts.assertThat(slaHelpController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .assertTask();
        var taskDetail = apiController.receiveTask(task.getNumber());
        CommonAssert
                .assertThat(taskDetail)
                .isCorrectUdfTask(UDF_SD_MODULE, CORE);
    }

    @Test(groups = {"PROC_SLAHELP", "Regression"}, description = "Сообщить информацию (Комментарий)", dependsOnMethods = "taskChangeModule")
    public void taskOurComment2() {
        apiController.updateToken(generateAuthToken(SUPPORT_MANAGER));
        task.refreshTask();
        udf = refreshUdf();
        var description = generateString();
        task.setDescription(description);
        task.refreshUdf(udf);
        slaHelpController.performCommonOperation(task, OUR_COMMENT);
        ApiAsserts.assertThat(slaHelpController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .assertTask();
    }

    @Test(groups = {"PROC_SLAHELP", "Regression"}, description = "Установить дату оказания консультации", dependsOnMethods = "taskOurComment2")
    public void taskSetConsultDate() {
        apiController.updateToken(generateAuthToken(SUPPORT_MANAGER));
        task.refreshTask();
        udf = refreshUdf();
        task.setDescription(generateString());
        udf.setUdfDate(generateUdfDate(UDF_SD_PROVIDEDHELPDEADLINE, DateUtils.getCurrentDate(0)));
        task.refreshUdf(udf);
        slaHelpController.performCommonOperation(task, SETCONSULTDATE);
        ApiAsserts.assertThat(slaHelpController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask();

        var taskDetail = apiController.receiveTask(task.getNumber());
        CommonAssert
                .assertThat(taskDetail)
                .isCorrectUdfDate(UDF_SD_PROVIDEDHELPDEADLINE, DateUtils.getCurrentDate(0));
    }

    @Test(groups = {"PROC_SLAHELP", "Regression"}, description = "Снять запрос", dependsOnMethods = "taskSetConsultDate")
    public void taskRemoveRequest() {
        apiController.updateToken(generateAuthToken(SUPPORT_MANAGER));
        task.refreshTask();
        task.setDescription(generateString());
        udf = refreshUdf();
        task.refreshUdf(udf);
        slaHelpController.performCommonOperation(task, REMOVE_REQUEST);
        ApiAsserts
                .assertThat(slaHelpController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .assertTask();
    }

    @Test(groups = {"PROC_SLAHELP", "Regression"}, description = "Оценить выполнение запроса", dependsOnMethods = "taskRemoveRequest")
    public void taskEvaluateRequest() {
        apiController.updateToken(generateAuthToken(CLIENT));
        task.refreshTask();
        udf = refreshUdf();
        task.setDescription(generateString());
        udf.setUdfList(generateUdfList(UDF_EVALUATING_REQUEST_EXECUTION, FIVE));
        var randomText = generateString();
        udf.setUdfMemo(generateUdfMemo(UDF_EVALUATING_REQUEST_COMMENT, randomText));
        task.refreshUdf(udf);
        slaHelpController.performCommonOperation(task, EVALUATE_REQUEST_EXE);
        ApiAsserts.assertThat(slaHelpController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .assertTask()
                .isCorrectStatus(TaskStatuses.STATUS_SLAHELP_CLOSED);

        var taskDetail = apiController.receiveTask(task.getNumber());
        CommonAssert
                .assertThat(taskDetail)
                .isCorrectUdfList(UDF_EVALUATING_REQUEST_EXECUTION, FIVE.getId())
                .isCorrectUdfMemo(UDF_EVALUATING_REQUEST_COMMENT, randomText);
    }

    @Test(groups = {"PROC_SLAHELP", "Regression"}, description = "Изменить автора", dependsOnMethods = "taskEvaluateRequest")
    public void taskChangeAuthors2() {
        var authors = slaHelpController.getAuthors(task.getTaskType(), CHANGE_AUTHOR, task.getNumber());
        if (authors == null) {
            throw new SkipException("Шаг <Изменить автора> пропущен, не найдены авторы.");
        }

        apiController.updateToken(generateAuthToken(SUPPORT_MANAGER));
        task.refreshTask();
        udf = refreshUdf();
        var initPerson = generateString();
        task.setDescription(generateString());
        udf.setUdfString(generateUdfString(UDF_SD_INITPERSON, initPerson));
        udf.setUdfUser(generateUdfUser(UDF_SD_AUTHORCLIENT_MSG, authors[1]));
        task.refreshUdf(udf);
        slaHelpController.performCommonOperation(task, CHANGE_AUTHOR);
        ApiAsserts.assertThat(slaHelpController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .assertTask();
        var taskDetail = apiController.receiveTask(task.getNumber());
        CommonAssert
                .assertThat(taskDetail)
                .isCorrectUdfString(UDF_SD_INITPERSON, initPerson)
                .isCorrectSubmitterUser(authors[1].getLogin());
    }

    @Test(groups = {"PROC_SLAHELP", "Regression"}, description = "Изменить ответственного", dependsOnMethods = "taskChangeAuthors2")
    public void taskChangeHandler() {
        RANDOM_TRUST_WATCHER = userController.receiveUserByRole(USER_ROLES, "Сотрудник", "root").getForUser();
        RANDOM_WATCHER = userController.receiveUserByRole(USER_ROLES, "root").getForUser();
        apiController.updateToken(generateAuthToken(SUPPORT_MANAGER));
        task.refreshTask();
        task.setDescription(generateString());
        udf = refreshUdf();
        task.setHandlerUser(SUPPORT_MEMBER);
        task.setDescription(generateString());
        udf.setUdfUser(generateUdfUser(STDT_HANDLER, SUPPORT_MEMBER));
        udf.setSecondUdfUser(generateUdfUser(UDF_SD_TRUSTEDWATCHER, RANDOM_TRUST_WATCHER));
        udf.setThirdUdfUser(generateUdfUser(UDF_WATCHER, RANDOM_WATCHER));
        task.refreshUdf(udf);
        slaHelpController.performCommonOperation(task, CHANGE_RES_PERSON);
        ApiAsserts
                .assertThat(slaHelpController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .assertTask()
                .isCorrectHandlerUser(SUPPORT_MEMBER);
    }

    @Test(groups = {"PROC_SLAHELP", "Regression"}, description = "Изменить список связанных задач", dependsOnMethods = "taskChangeHandler")
    public void taskChangeLinkedTask() {
        apiController.updateToken(generateAuthToken(SUPPORT_MANAGER));
        task.refreshTask();
        udf = refreshUdf();
        task.setDescription(generateString());

        GrTaskTable grTaskTable = dbHelper.getGrTaskTable();
        var dbProcWorkTask = (GrTaskDbEntity) grTaskTable.receiveRandomTask("task_category", EQUAL.operator, "CAT_DEVTASK");
        var workTask = new Task(dbProcWorkTask.getTask_id(), dbProcWorkTask.getTask_number());

        udf.setUdfTask(generateUdfTask(UDF_SD_LINKEDREQUEST, workTask));
        task.refreshUdf(udf);
        slaHelpController.performCommonOperation(task, CHANGE_LINKED_TASKS);
        ApiAsserts
                .assertThat(slaHelpController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .assertTask();
        var taskDetail = apiController.receiveTask(task.getNumber());
        CommonAssert
                .assertThat(taskDetail)
                .isCorrectUdfTask(UDF_SD_LINKEDREQUEST, workTask.getNumber());
    }

    @Test(groups = {"PROC_SLAHELP", "Regression"}, description = "Корректировка сроков SLA", dependsOnMethods = "taskChangeLinkedTask")
    public void taskCorrectSlaDates() {
        apiController.updateToken(generateAuthToken(SUPPORT_MANAGER));
        var dates = slaHelpController.getUDF_SLA_CONSULTPROVIDEDATE(task.getTaskType(), CORRECT_SLA_DATES, task.getNumber());
        var formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        var dateTime = OffsetDateTime.parse(dates.get(0), formatter);
        var milliseconds = dateTime.toInstant().toEpochMilli();
        task.refreshTask();
        udf = refreshUdf();
        udf.setUdfString(generateUdfString(UDF_SLA_CONSULTPROVIDEDATE, Long.toString(milliseconds)));
        task.refreshUdf(udf);
        slaHelpController.performCommonOperation(task, CORRECT_SLA_DATES);
        ApiAsserts.assertThat(slaHelpController.getResponse()).
                isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK);

        var taskDetail = apiController.receiveTask(task.getNumber());
        CommonAssert.assertThat(taskDetail)
                .isCorrectUdfString(UDF_SLA_CONSULTPROVIDEDATE, Long.toString(milliseconds));
    }

    @Test(groups = {"PROC_SLAHELP", "Regression"}, description = "Назначить доверенного наблюдателя", dependsOnMethods = "taskCorrectSlaDates")
    public void taskAddTrustedWatcher() {
        RANDOM_TRUST_WATCHER = userController.receiveUserByRole(USER_ROLES, "Сотрудник", "root").getForUser();
        apiController.updateToken(generateAuthToken(SUPPORT_MANAGER));
        task.refreshTask();
        udf = refreshUdf();
        task.setDescription(generateString());
        udf.setUdfUser(generateUdfUser(UDF_SD_TRUSTEDWATCHER, RANDOM_TRUST_WATCHER));
        task.refreshUdf(udf);
        slaHelpController.performCommonOperation(task, ADD_TRUST_WATCHER);
        ApiAsserts.assertThat(slaHelpController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .assertTask();
        var taskDetail = apiController.receiveTask(task.getNumber());
        CommonAssert
                .assertThat(taskDetail)
                .isCorrectUdfUSer(UDF_SD_TRUSTEDWATCHER, RANDOM_TRUST_WATCHER);
    }

    @Test(groups = {"PROC_SLAHELP", "Regression"}, description = "Назначить наблюдателя", dependsOnMethods = "taskAddTrustedWatcher")
    public void taskAddAppointWatcher() {
        RANDOM_WATCHER = userController.receiveUserByRole(USER_ROLES, "root").getForUser();
        apiController.updateToken(generateAuthToken(SUPPORT_MANAGER));
        task.refreshTask();
        udf = refreshUdf();
        task.setDescription(generateString());
        udf.setUdfUser(generateUdfUser(UDF_WATCHER, RANDOM_WATCHER));
        task.refreshUdf(udf);
        slaHelpController.performCommonOperation(task, ADD_WATCHERS);
        ApiAsserts
                .assertThat(slaHelpController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .assertTask();
        var taskDetail = apiController.receiveTask(task.getNumber());
        CommonAssert.assertThat(taskDetail).isCorrectUdfUSer(UDF_WATCHER, RANDOM_WATCHER);
    }

    @Test(groups = {"PROC_SLAHELP", "Regression"}, description = "Приватный комментарий", dependsOnMethods = "taskAddAppointWatcher")
    public void taskPrivateComment() {
        apiController.updateToken(generateAuthToken(SUPPORT_MANAGER));
        task.refreshTask();
        udf = refreshUdf();
        task.setDescription(generateString());
        task.refreshUdf(udf);
        slaHelpController.performCommonOperation(task, PRIVATECOMMENT);
        ApiAsserts
                .assertThat(slaHelpController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .assertTask();
        var messages = slaHelpController.getTaskMessages(task.getNumber());
        CommonAssert
                .assertThat(messages)
                .fieldFromListIsNotEmpty("под 'Менеджер клиента' проверить что доступно сообщение", "Приватный комментарий", "mstatusName");

        apiController.updateToken(generateAuthToken(CLIENT));
        var messagesForClient = slaHelpController.getTaskMessages(task.getNumber());
        CommonAssert
                .assertThat(messagesForClient)
                .fieldFromListIsEmpty("под 'Клиент' проверить что НЕ  доступно сообщение", "Приватный комментарий", "mstatusName");
    }
//    @Test(groups = {"SlaHelp", "Regression"}, description = "Снять запрос", dependsOnMethods = "taskPrivateComment")
//    public void taskRemoveRequest2() {
//        apiController.updateToken(generateAuthToken(SUPPORT_MANAGER));
//        task.refreshTask();
//        task.setDescription(generateString());
//        udf = refreshUdf();
//        task.refreshUdf(udf);
//        slaHelpController.performCommonOperation(task, REMOVE_REQUEST);
//        ApiAsserts
//                .assertThat(slaHelpController.getResponse())
//                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
//                .assertTask();
//    }
}
