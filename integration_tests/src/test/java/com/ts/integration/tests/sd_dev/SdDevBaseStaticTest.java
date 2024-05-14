package com.ts.integration.tests.sd_dev;

import com.ts.common.application.controllers.TrackStudioHttpStatusCodes;
import com.ts.common.application.database.dbEntities.GrTaskDbEntity;
import com.ts.common.application.database.dbTables.GrTaskTable;
import com.ts.common.asserts.ApiAsserts;
import com.ts.common.asserts.CommonAssert;
import com.ts.common.controllers.TaskResponseBody;
import com.ts.common.controllers.sddev.SdDevController;
import com.ts.common.entitites.commonEntities.Parent;
import com.ts.common.entitites.commonEntities.Task;
import com.ts.common.entitites.commonEntities.User;
import com.ts.common.entitites.commonEntities.UserRole;
import com.ts.common.entitites.tasks.GeneralTask;
import com.ts.common.enums.Operations;
import com.ts.common.enums.TaskType;
import com.ts.common.utils.DateUtils;
import com.ts.common.utils.InitEntities;
import com.ts.common.utils.JsonUtils;
import com.ts.integration.tests.BaseIntegrationTest;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.List;

import static com.ts.common.application.database.DbQueryHelper.Operators.EQUAL;
import static com.ts.common.entitites.commonEntities.List.Constants.*;
import static com.ts.common.entitites.commonEntities.Task.Constants.CORE;
import static com.ts.common.entitites.commonEntities.Udfs.UdfSd.*;
import static com.ts.common.enums.Operations.*;
import static com.ts.common.enums.TaskStatuses.STATUS_SDDEV_ANALIZING;
import static com.ts.common.enums.TaskStatuses.STATUS_SDDEV_NEW;
import static com.ts.common.utils.InitEntities.*;
import static com.ts.common.utils.RandomUtils.*;

public class SdDevBaseStaticTest extends BaseIntegrationTest {

    public SdDevController sdDevController;
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
        sdDevController = apiController.getSdDevController();
        userController = apiController.getUserController();
        GrTaskTable grTaskTable = dbHelper.getGrTaskTable();
        GrTaskDbEntity parentTaskFromDb = (GrTaskDbEntity) grTaskTable.receiveByTaskNumber("928666");
        parent = InitEntities.generateParent(parentTaskFromDb.getTask_id(), parentTaskFromDb.getTask_number());
        task = InitEntities.getGeneralTask(TaskType.SD_DEV, Operations.CAT);

        var parentPayloadResponse = sdDevController.getParentPayload(parent.getNumber(), "CAT_SDDEV");
        ApiAsserts.assertThat(parentPayloadResponse).isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK);
        var parentPayload = JsonUtils.removeExtraCharacters(parentPayloadResponse);
        BDKU_CONFIGURATION = sdDevController.getParent_UDF_BDKU_CONFIGURATION(parentPayload);

        SD_RELATED_TASK_CODES = sdDevController.getParent_UDF_SD_RELATED_TASK_CODES(parentPayload);

        USER_ROLES = userController.receiveUserByTask(parent.getNumber());
        CLIENT = userController.receiveUserByRole(USER_ROLES, "Клиент", "root").getForUser();
        SUPPORT_MANAGER = userController.receiveUserByRole(USER_ROLES, "Менеджер клиента", "@").getForUser();
        SUPPORT_MEMBER = userController.receiveUserByRole(USER_ROLES, "Участник проекта сопровождения", "root").getForUser();
//        ROLE_CONTRACT_EMP = userController.receiveUserByRole(USER_ROLES, "Ведение контрактов", "root").getForUser();
        ROLE_CONTRACT_EMP = userController.receiveUserByLogin(USER_ROLES, "eschepovsky");
        RANDOM_WATCHER = userController.receiveUserByRole(USER_ROLES, "Сотрудник", "@").getForUser();
        RANDOM_WATCHER2 = userController.receiveUserByRole(USER_ROLES, "Сотрудник", "@").getForUser();
    }

    @Test(groups = {"ProcSdDev", "Regression"}, description = "создание")
    public void createTask() {
        apiController.updateToken(InitEntities.generateAuthToken(CLIENT));
        udf = refreshUdf();
        task.refreshTask();
        task.setParent(parent);
        task.setDescription(generateDescriptionForOperation(Operations.CAT));
        task.setName("CAT_SDDEV: " + generateComment());
        udf.setUdfMultiList(generateUdfMultiList(UDF_SD_RELATED_TASK_CODES, SD_RELATED_TASK_CODES[0].getId()));
        udf.setUdfTask(generateUdfTask(UDF_SD_MODULE, CORE));
        udf.setUdfString(generateUdfString(UDF_SD_CLIENTWATCHERS, generateEmail()));
        udf.setUdfList(generateUdfList(UDF_SDDEV_CUSTOMTYPE, UDF_SDDEV_CUSTOMTYPE_ADD));
        udf.setUdfList(generateUdfList(UDF_SDDEV_DEVDECISION, UDF_SDDEV_DEVDECISION_ALTERNATE));
        udf.setSecondUdfTask(generateUdfTask(UDF_BDKU_CONFIGURATION, BDKU_CONFIGURATION[0]));
        udf.setSecondUdfString(generateUdfString(UDF_SD_INITPERSON, generateString()));
        task.refreshUdf(udf);
        sdDevController.create(task);
        var response = sdDevController.getResponse();
        ApiAsserts.assertThat(response).isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK).isParseableBody(TaskResponseBody.class).assertTask().isCorrectStatus(STATUS_SDDEV_NEW).isEquals(task);

        var taskDetail = apiController.receiveTask(task.getNumber());
        CommonAssert
                .assertThat(taskDetail)
//                .isCorrectUdfList(UDF_SD_RESPONSIBLE_PARTY, RESPONSIBLE_PARTY_SUPPLIER.getId())
                .isCorrectUdfList(UDF_SDDEV_CONDITIONSACCEPTED, CONDITIONS_ACCEPTED_NO.getId());
    }

    @Test(groups = {"ProcSdDev", "Regression"}, description = "Принять на анализ", dependsOnMethods = "createTask")
    public void taskAnalyze() {
        RANDOM_WATCHER = userController.receiveUserByRole(USER_ROLES, "root").getForUser();
        RANDOM_TRUST_WATCHER = userController.receiveUserByRole(USER_ROLES, "Сотрудник", "root").getForUser();
        apiController.updateToken(generateAuthToken(SUPPORT_MANAGER));
        task.refreshTask();
        udf = refreshUdf();
        task.setHandlerUser(SUPPORT_MANAGER);
        task.setDescription(generateString());
        udf.setUdfUser(generateUdfUser(STDT_HANDLER, SUPPORT_MANAGER));
        udf.setSecondUdfUser(generateUdfUser(UDF_SD_TRUSTEDWATCHER, RANDOM_TRUST_WATCHER));
        udf.setThirdUdfUser(generateUdfUser(UDF_WATCHER, RANDOM_WATCHER));
        task.refreshUdf(udf);
        sdDevController.performCommonOperation(task, ANALIZE);
        ApiAsserts.assertThat(sdDevController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isCorrectStatus(STATUS_SDDEV_ANALIZING);

        var taskDetail = apiController.receiveTask(task.getNumber());
        CommonAssert
                .assertThat(taskDetail)
                .isCorrectUdfUSer(UDF_SD_TRUSTEDWATCHER, RANDOM_TRUST_WATCHER)
                .isCorrectUdfUSer(UDF_WATCHER, RANDOM_WATCHER)
                .isCorrectUdfList(UDF_SD_RESPONSIBLE_PARTY, RESPONSIBLE_PARTY_SUPPLIER.getId());
    }

    @Test(groups = {"ProcSdDev", "Regression"}, description = "Задать вопрос", dependsOnMethods = "taskAnalyze")
    public void taskCliComment() {
        apiController.updateToken(generateAuthToken(CLIENT));
        task.refreshTask();
        task.setDescription(generateComment());
        udf = refreshUdf();
        task.refreshUdf(udf);
        sdDevController.performCommonOperation(task, CLI_COMMENT);
        ApiAsserts.assertThat(sdDevController.getResponse()).isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK).assertTask().isCorrectStatus(STATUS_SDDEV_ANALIZING);
//        var taskDetail = apiController.receiveTask(task.getNumber());
//        CommonAssert
//                .assertThat(taskDetail)
//                .isCorrectUdfList(UDF_SD_RESPONSIBLE_PARTY, RESPONSIBLE_PARTY_CLIENT.getId());
    }

    @Test(groups = {"ProcSdDev", "Regression"}, description = "Изменить атрибуты запроса", dependsOnMethods = "taskCliComment")
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
        sdDevController.performCommonOperation(task, CHANGE_ATTR);
        ApiAsserts
                .assertThat(sdDevController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .assertTask()
                .isCorrectStatus(STATUS_SDDEV_ANALIZING);
        var taskDetail = apiController.receiveTask(task.getNumber());
        CommonAssert
                .assertThat(taskDetail)
                .isCorrectUdfString(UDF_SD_REMOTEID, sdRemoteId)
                .isCorrectUdfString(UDF_SD_INITPERSON, sdInterPerson);
    }

    @Test(groups = {"ProcSdDev", "Regression"}, description = "Назначить наблюдателей клиента", dependsOnMethods = "taskChangeAttr")
    public void taskAppointCliWatchers() {
        apiController.updateToken(generateAuthToken(CLIENT));
        task.refreshTask();
        udf = refreshUdf();
        task.setDescription(generateString());
        var email = generateEmail();
        udf.setUdfString(generateUdfString(UDF_SD_CLIENTWATCHERS, email));
        task.refreshUdf(udf);
        sdDevController.performCommonOperation(task, ADD_CLIENT_WATCHERS);
        ApiAsserts
                .assertThat(sdDevController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .assertTask()
                .isCorrectStatus(STATUS_SDDEV_ANALIZING);
        var taskDetail = apiController.receiveTask(task.getNumber());
        CommonAssert
                .assertThat(taskDetail)
                .isCorrectUdfString(UDF_SD_CLIENTWATCHERS, email);
    }


    @Test(groups = {"ProcSdDev", "Regression"}, description = "Сообщить информацию (Комментарий)", dependsOnMethods = "taskAppointCliWatchers")
    public void taskOurComment() {
        apiController.updateToken(generateAuthToken(CLIENT));
        task.refreshTask();
        udf = refreshUdf();
        task.setDescription(generateString());
        task.refreshUdf(udf);
        sdDevController.performCommonOperation(task, OUR_COMMENT);
        ApiAsserts.assertThat(sdDevController.getResponse()).isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK).assertTask().isCorrectStatus(STATUS_SDDEV_ANALIZING);
//        var taskDetail = apiController.receiveTask(task.getNumber());
//        CommonAssert
//                .assertThat(taskDetail)
//                .isCorrectUdfList(UDF_SD_RESPONSIBLE_PARTY, RESPONSIBLE_PARTY_SUPPLIER.getId());
    }

    @Test(groups = {"ProcSdDev", "Regression"}, description = "Изменить модуль системы", dependsOnMethods = "taskOurComment")
    public void taskChangeModule() {
        apiController.updateToken(generateAuthToken(SUPPORT_MANAGER));
        task.refreshTask();
        udf = refreshUdf();
        task.setDescription(generateString());
        udf.setUdfTask(generateUdfTask(UDF_SD_MODULE, CORE));
        task.refreshUdf(udf);
        sdDevController.performCommonOperation(task, CHANGE_MODULE);
        ApiAsserts.assertThat(sdDevController.getResponse()).isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK).assertTask().isCorrectStatus(STATUS_SDDEV_ANALIZING);
        var taskDetail = apiController.receiveTask(task.getNumber());
        CommonAssert
                .assertThat(taskDetail)
                .isCorrectUdfTask(UDF_SD_MODULE, CORE);
    }

    @Test(groups = {"ProcSdDev", "Regression"}, description = "Сообщить срок предоставления решения", dependsOnMethods = "taskChangeModule")
    public void taskProvideFixDeadline() {
        apiController.updateToken(generateAuthToken(SUPPORT_MANAGER));
        task.refreshTask();
        udf = refreshUdf();
        task.setDescription(generateString());
        udf.setUdfDate(generateUdfDate(UDF_SD_DEADLINETEMPFIX, DateUtils.getCurrentDate(0)));
        udf.setSecondUdfDate(generateUdfDate(UDF_SD_DEADLINEFIX, DateUtils.getCurrentDate(0)));
        task.refreshUdf(udf);
        sdDevController.performCommonOperation(task, PROVIDE_FIX_DEADLINE);
        ApiAsserts.assertThat(sdDevController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .assertTask()
                .isCorrectStatus(STATUS_SDDEV_ANALIZING);
    }

    @Test(groups = {"ProcSdDev", "Regression"}, description = "Изменить автора", dependsOnMethods = "taskProvideFixDeadline")
    public void taskChangeAuthors() {
        var authors = sdDevController.getAuthors(task.getTaskType(), CHANGE_AUTHOR, task.getNumber());
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
        sdDevController.performCommonOperation(task, CHANGE_AUTHOR);
        ApiAsserts.assertThat(sdDevController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .assertTask()
                .isCorrectStatus(STATUS_SDDEV_ANALIZING);
        var taskDetail = apiController.receiveTask(task.getNumber());
        CommonAssert
                .assertThat(taskDetail)
                .isCorrectUdfString(UDF_SD_INITPERSON, initPerson)
                .isCorrectSubmitterUser(authors[0].getLogin());
    }

    @Test(groups = {"ProcSdDev", "Regression"}, description = "Изменить список связанных задач", dependsOnMethods = "taskChangeAuthors")
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
        sdDevController.performCommonOperation(task, CHANGE_LINKED_TASKS);
        ApiAsserts
                .assertThat(sdDevController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .assertTask()
                .isCorrectStatus(STATUS_SDDEV_ANALIZING);
        var taskDetail = apiController.receiveTask(task.getNumber());
        CommonAssert
                .assertThat(taskDetail)
                .isCorrectUdfTask(UDF_SD_LINKEDREQUEST, workTask.getNumber());
    }

    @Test(groups = {"ProcSdDev", "Regression"}, description = "Назначить доверенного наблюдателя", dependsOnMethods = "taskChangeLinkedTask")
    public void taskAddTrustedWatcher() {
        RANDOM_WATCHER = userController.receiveUserByRole(USER_ROLES, "Сотрудник", "@").getForUser();
        apiController.updateToken(generateAuthToken(SUPPORT_MANAGER));
        task.refreshTask();
        udf = refreshUdf();
        task.setDescription(generateString());
        udf.setUdfUser(generateUdfUser(UDF_SD_TRUSTEDWATCHER, RANDOM_WATCHER));
        task.refreshUdf(udf);
        sdDevController.performCommonOperation(task, ADD_TRUST_WATCHER);
        ApiAsserts.assertThat(sdDevController.getResponse()).isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK).assertTask().isCorrectStatus(STATUS_SDDEV_ANALIZING);
        var taskDetail = apiController.receiveTask(task.getNumber());
        CommonAssert
                .assertThat(taskDetail)
                .isCorrectUdfUSer(UDF_SD_TRUSTEDWATCHER, RANDOM_WATCHER);
    }

    @Test(groups = {"ProcSdDev", "Regression"}, description = "Назначить наблюдателя", dependsOnMethods = "taskAddTrustedWatcher")
    public void taskAddAppointWatcher() {
        apiController.updateToken(generateAuthToken(SUPPORT_MANAGER));
        task.refreshTask();
        udf = refreshUdf();
        task.setDescription(generateString());
        udf.setUdfUser(generateUdfUser(UDF_WATCHER, RANDOM_WATCHER2));
        task.refreshUdf(udf);
        sdDevController.performCommonOperation(task, ADD_WATCHERS);
        ApiAsserts.assertThat(sdDevController.getResponse()).isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK).assertTask().isCorrectStatus(STATUS_SDDEV_ANALIZING);
        var taskDetail = apiController.receiveTask(task.getNumber());
        CommonAssert.assertThat(taskDetail).isCorrectUdfUSer(UDF_WATCHER, RANDOM_WATCHER2);
    }

    @Test(groups = {"ProcSdDev", "Regression"}, description = "Приватный комментарий", dependsOnMethods = "taskAddAppointWatcher")
    public void taskPrivateComment() {
        apiController.updateToken(generateAuthToken(SUPPORT_MANAGER));
        task.refreshTask();
        udf = refreshUdf();
        task.setDescription(generateString());
        task.refreshUdf(udf);
        sdDevController.performCommonOperation(task, PRIVATECOMMENT);
        ApiAsserts
                .assertThat(sdDevController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .assertTask()
                .isCorrectStatus(STATUS_SDDEV_ANALIZING);
        var messages = sdDevController.getTaskMessages(task.getNumber());
        CommonAssert
                .assertThat(messages)
                .fieldFromListIsNotEmpty("под 'Менеджер клиента' проверить что доступно сообщение", "Приватный комментарий", "mstatusName");

        apiController.updateToken(generateAuthToken(CLIENT));
        var messagesForClient = sdDevController.getTaskMessages(task.getNumber());
        CommonAssert
                .assertThat(messagesForClient)
                .fieldFromListIsEmpty("под 'Клиент' проверить что НЕ  доступно сообщение", "Приватный комментарий", "mstatusName");
    }

    @Test(groups = {"ProcSdDev", "Regression"}, description = "Сообщить информацию (Комментарий)", dependsOnMethods = "taskPrivateComment")
    public void taskOurComment2() {
        apiController.updateToken(generateAuthToken(SUPPORT_MANAGER));
        task.refreshTask();
        udf = refreshUdf();
        var description = generateString();
        task.setDescription(description);
        task.refreshUdf(udf);
        sdDevController.performCommonOperation(task, OUR_COMMENT);
        ApiAsserts.assertThat(sdDevController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .assertTask()
                .isCorrectStatus(STATUS_SDDEV_ANALIZING);
    }

    @Test(groups = {"ProcSdDev", "Regression"}, description = "Учесть в лицензионной стоимости", dependsOnMethods = "taskOurComment2")
    public void taskAccountInLicence() {
        apiController.updateToken(generateAuthToken(ROLE_CONTRACT_EMP));
        task.refreshTask();
        udf = refreshUdf();
        var randomDate = DateUtils.getCurrentDate(0);
        udf.setUdfDate(generateUdfDate(UDF_SDDEV_LCOSTCHGDATE, randomDate));
        udf.setUdfList(generateUdfList(UDF_SD_INLICENSE, SD_INLICENSE_YES));
        task.refreshUdf(udf);
        sdDevController.performCommonOperation(task, ACCOUNT_IN_LICENSE);
        ApiAsserts.assertThat(sdDevController.getResponse()).isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK).assertTask().isCorrectStatus(STATUS_SDDEV_ANALIZING);

        var taskDetail = apiController.receiveTask(task.getNumber());
        CommonAssert.assertThat(taskDetail)
                .isCorrectUdfList(UDF_SD_INLICENSE, SD_INLICENSE_YES.getId());
    }

    @Test(groups = {"ProcSdDev", "Regression"}, description = "Указать трудоемкость лимитированных работ", dependsOnMethods = "taskAccountInLicence")
    public void taskCorrectBudget() {
        apiController.updateToken(generateAuthToken(ROLE_CONTRACT_EMP));
        task.refreshTask();
        task.setDescription(generateString());
        udf = refreshUdf();
        task.refreshUdf(udf);
        sdDevController.performCommonOperation(task, CORRECT_BUDGET);
        ApiAsserts
                .assertThat(sdDevController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .assertTask()
                .isCorrectStatus(STATUS_SDDEV_ANALIZING);
    }

    @Test(groups = {"ProcSdDev", "Regression"}, description = "Изменить ответственного", dependsOnMethods = "taskCorrectBudget")
    public void taskChangeHandler() {
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
        sdDevController.performCommonOperation(task, CHANGE_RES_PERSON);
        ApiAsserts
                .assertThat(sdDevController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .assertTask().isCorrectStatus(STATUS_SDDEV_ANALIZING)
                .isCorrectHandlerUser(SUPPORT_MEMBER);
    }
}
