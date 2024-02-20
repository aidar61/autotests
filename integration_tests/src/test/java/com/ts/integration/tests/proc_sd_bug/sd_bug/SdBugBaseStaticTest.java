package com.ts.integration.tests.proc_sd_bug.sd_bug;

import com.ts.common.application.controllers.TrackStudioHttpStatusCodes;
import com.ts.common.application.database.dbEntities.GrTaskDbEntity;
import com.ts.common.application.database.dbTables.GrTaskTable;
import com.ts.common.asserts.ApiAsserts;
import com.ts.common.controllers.TaskResponseBody;
import com.ts.common.controllers.sdbug.SdBugController;
import com.ts.common.entitites.commonEntities.Parent;
import com.ts.common.entitites.commonEntities.Task;
import com.ts.common.entitites.commonEntities.User;
import com.ts.common.entitites.commonEntities.UserRole;
import com.ts.common.entitites.tasks.GeneralTask;
import com.ts.common.enums.Operations;
import com.ts.common.enums.TaskType;
import com.ts.common.utils.InitEntities;
import com.ts.common.utils.JsonUtils;
import com.ts.integration.tests.BaseIntegrationTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.List;

import static com.ts.common.entitites.commonEntities.List.Constants.SDBUG_PRIORITYBUG_CRITICAL;
import static com.ts.common.entitites.commonEntities.List.Constants.SDBUG_PRIORITYBUG_CRITICAL_AFTER_UPDATE;
import static com.ts.common.entitites.commonEntities.Task.Constants.CORE;
import static com.ts.common.entitites.commonEntities.Task.Constants.SERVICE_DESK;
import static com.ts.common.entitites.commonEntities.Udfs.UdfSd.*;
import static com.ts.common.enums.Operations.*;
import static com.ts.common.enums.TaskStatuses.STATUS_SDBUG_ANALIZING;
import static com.ts.common.enums.TaskStatuses.STATUS_SDBUG_NEW;
import static com.ts.common.utils.InitEntities.*;
import static com.ts.common.utils.RandomUtils.*;

public class SdBugBaseStaticTest extends BaseIntegrationTest {

    public SdBugController sdBugController;
    private GeneralTask task;
    private Parent parent;
    private User CLIENT;
    private User SUPPORT_MANAGER;
    private User SUPPORT_MEMBER;
    private User SUPPORT_MEMBER2;
    private User RANDOM_WATCHER;
    private User RANDOM_TRUST_WATCHER;
    private Task[] BDKU_CONFIGURATION;
    private List<UserRole> USER_ROLES;


    @BeforeClass(alwaysRun = true)
    public void beforeClass() {
        sdBugController = apiController.getSdBugController();
        userController = apiController.getUserController();
        GrTaskTable grTaskTable = dbHelper.getGrTaskTable();
        GrTaskDbEntity parentTaskFromDb = (GrTaskDbEntity) grTaskTable.receiveByTaskNumber("1328786");
        parent = InitEntities.generateParent(parentTaskFromDb.getTask_id(), parentTaskFromDb.getTask_number());
        task = InitEntities.getGeneralTask(TaskType.SD_BUG, Operations.CAT);

        var parentPayloadResponse = sdBugController.getParentPayload(parent.getNumber(), "CAT_SDBUG");
        ApiAsserts.assertThat(parentPayloadResponse).isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK);
        var parentPayload = JsonUtils.removeExtraCharacters(parentPayloadResponse);
        BDKU_CONFIGURATION = sdBugController.getParent_UDF_BDKU_CONFIGURATION(parentPayload);

        USER_ROLES = userController.receiveUserByTask(parent.getNumber());
        CLIENT = userController.receiveUserByRole(USER_ROLES, "Клиент", "rysgalbank.tm").getForUser();
        SUPPORT_MANAGER = userController.receiveUserByRole(USER_ROLES, "Менеджер клиента", CLIENT.getLogin()).getForUser();
        SUPPORT_MEMBER = userController.receiveUserByRole(USER_ROLES, "Участник проекта сопровождения", SUPPORT_MANAGER.getLogin()).getForUser();
        SUPPORT_MEMBER2 = userController.receiveUserByRole(USER_ROLES, "Участник проекта сопровождения", SUPPORT_MEMBER.getLogin()).getForUser();
    }

    @Test(groups = {"PROC_SDBUG", "Regression"}, description = "создание CAT_SDBUG")
    public void createTask() {
        apiController.updateToken(InitEntities.generateAuthToken(CLIENT));
        udf = refreshUdf();
        task.refreshTask();
        task.setParent(parent);
        task.setDescription(generateDescriptionForOperation(Operations.CAT));
        task.setName("CAT_SDBUG: " + generateComment());
        udf.setUdfTask(generateUdfTask(UDF_SD_MODULE, CORE));
        udf.setUdfList(generateUdfList(UDF_SDBUG_PRIORITYBUG, SDBUG_PRIORITYBUG_CRITICAL));
        udf.setUdfMemo(generateUdfMemo(UDF_SDBUG_CONSEQ, generateString()));
        udf.setSecondUdfTask(generateUdfTask(UDF_BDKU_CONFIGURATION, BDKU_CONFIGURATION[0]));
        udf.setUdfString(generateUdfString(UDF_SD_REMOTEID, generateString()));
        udf.setSecondUdfString(generateUdfString(UDF_SD_INITPERSON, generateString()));
        task.refreshUdf(udf);
        sdBugController.create(task);
        var response = sdBugController.getResponse();
        ApiAsserts
                .assertThat(response)
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isCorrectStatus(STATUS_SDBUG_NEW)
                .isEquals(task);
    }

    @Test(groups = {"PROC_SDBUG", "Regression"}, description = "Принять на анализ", dependsOnMethods = "createTask")
    public void taskAnalyze() {
        RANDOM_WATCHER = userController.receiveUserByRole(USER_ROLES, "root").getForUser();
        RANDOM_TRUST_WATCHER = userController.receiveUserByRole(USER_ROLES, "Сотрудник", "root").getForUser();
        apiController.updateToken(generateAuthToken(SUPPORT_MANAGER));
        task.refreshTask();
        udf = refreshUdf();
        task.setHandlerUser(SUPPORT_MEMBER);
        task.setDescription(generateString());
        udf.setUdfUser(generateUdfUser(STDT_HANDLER, SUPPORT_MEMBER));
        udf.setSecondUdfUser(generateUdfUser(UDF_SD_TRUSTEDWATCHER, RANDOM_TRUST_WATCHER));
        udf.setThirdUdfUser(generateUdfUser(UDF_WATCHER, RANDOM_WATCHER));
        task.refreshUdf(udf);
        sdBugController.performCommonOperation(task, ANALIZE);
        ApiAsserts
                .assertThat(sdBugController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isCorrectStatus(STATUS_SDBUG_ANALIZING);
    }

    @Test(groups = {"PROC_SDBUG", "Regression"}, description = "Задать вопрос", dependsOnMethods = "taskAnalyze")
    public void taskAsk() {
        apiController.updateToken(generateAuthToken(CLIENT));
        task.refreshTask();
        udf = refreshUdf();
        task.refreshUdf(udf);
        sdBugController.performCommonOperation(task, CLI_COMMENT);
        ApiAsserts
                .assertThat(sdBugController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK);
    }

    @Test(groups = {"PROC_SDBUG", "Regression"}, description = "Изменить приоритет", dependsOnMethods = "taskAsk")
    public void taskChangePriority() {
        apiController.updateToken(generateAuthToken(CLIENT));
        task.refreshTask();
        udf = refreshUdf();
        udf.setUdfList(generateUdfList(UDF_SDBUG_PRIORITYBUG, SDBUG_PRIORITYBUG_CRITICAL_AFTER_UPDATE));
        task.refreshUdf(udf);
        sdBugController.performCommonOperation(task, CHANGE_PRIORITY);
        ApiAsserts
                .assertThat(sdBugController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK);
    }

    @Test(groups = {"PROC_SDBUG", "Regression"}, description = "Сообщить информацию (Комментарий)", dependsOnMethods = "taskChangePriority")
    public void taskOurComment() {
        apiController.updateToken(generateAuthToken(CLIENT));
        task.refreshTask();
        udf = refreshUdf();
        task.setDescription(generateDescriptionForOperation(OUR_COMMENT));
        task.refreshUdf(udf);
        sdBugController.performCommonOperation(task, OUR_COMMENT);
        ApiAsserts
                .assertThat(sdBugController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK);
    }

    @Test(groups = {"PROC_SDBUG", "Regression"}, description = "Изменить атрибуты запроса", dependsOnMethods = "taskOurComment")
    public void taskChangeAttrs() {
        apiController.updateToken(generateAuthToken(CLIENT));
        task.refreshTask();
        udf = refreshUdf();
        task.setDescription(generateDescriptionForOperation(CHANGE_ATTR));
        udf.setUdfString(generateUdfString(UDF_SD_REMOTEID, generateString()));
        udf.setSecondUdfString(generateUdfString(UDF_SD_INITPERSON, generateString()));
        task.refreshUdf(udf);
        sdBugController.performCommonOperation(task, CHANGE_ATTR);
        ApiAsserts
                .assertThat(sdBugController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK);
    }

    @Test(groups = {"PROC_SDBUG", "Regression"}, description = "Назначить наблюдателей клиента", dependsOnMethods = "taskChangeAttrs")
    public void taskAppointCliWatchers() {
        apiController.updateToken(generateAuthToken(CLIENT));
        task.refreshTask();
        udf = refreshUdf();
        task.setDescription(generateDescriptionForOperation(ADD_CLIENT_WATCHERS));
        udf.setUdfString(generateUdfString(UDF_SD_CLIENTWATCHERS, generateEmail()));
        task.refreshUdf(udf);
        sdBugController.performCommonOperation(task, ADD_CLIENT_WATCHERS);
        ApiAsserts
                .assertThat(sdBugController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK);
    }

    @Test(groups = {"PROC_SDBUG", "Regression"}, description = "Изменить модуль системы", dependsOnMethods = "taskAppointCliWatchers")
    public void taskChangeSdModule() {
        apiController.updateToken(generateAuthToken(SUPPORT_MANAGER));
        task.refreshTask();
        udf = refreshUdf();
        task.setDescription(generateDescriptionForOperation(CHANGE_MODULE));
        udf.setUdfTask(generateUdfTask(UDF_SD_MODULE, CORE));
        task.refreshUdf(udf);
        sdBugController.performCommonOperation(task, CHANGE_MODULE);
        ApiAsserts
                .assertThat(sdBugController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK);
    }

    @Test(groups = {"PROC_SDBUG", "Regression"}, description = "Изменить список связанных задач", dependsOnMethods = "taskChangeSdModule")
    public void taskChangeLinkedTask() {
        apiController.updateToken(generateAuthToken(SUPPORT_MANAGER));
        task.refreshTask();
        udf = refreshUdf();
        task.setDescription(generateDescriptionForOperation(CHANGE_LINKED_TASKS));
        udf.setUdfTask(generateUdfTask(UDF_SD_LINKEDREQUEST, SERVICE_DESK));
        task.refreshUdf(udf);
        sdBugController.performCommonOperation(task, CHANGE_LINKED_TASKS);
        ApiAsserts
                .assertThat(sdBugController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK);
    }

    @Test(groups = {"PROC_SDBUG", "Regression"}, description = "Назначить доверенного наблюдателя", dependsOnMethods = "taskChangeLinkedTask")
    public void taskAddTrustWatcher() {
        RANDOM_TRUST_WATCHER = userController.receiveUserByRole(USER_ROLES, "Сотрудник", "root").getForUser();
        apiController.updateToken(generateAuthToken(SUPPORT_MANAGER));
        task.refreshTask();
        udf = refreshUdf();
        task.setDescription(generateDescriptionForOperation(ADD_TRUST_WATCHER));
        udf.setUdfUser(generateUdfUser(UDF_SD_TRUSTEDWATCHER, RANDOM_TRUST_WATCHER));
        task.refreshUdf(udf);
        sdBugController.performCommonOperation(task, ADD_TRUST_WATCHER);
        ApiAsserts
                .assertThat(sdBugController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK);
    }

    @Test(groups = {"PROC_SDBUG", "Regression"}, description = "Назначить  наблюдателя", dependsOnMethods = "taskAddTrustWatcher")
    public void taskAddWatcher() {
        RANDOM_WATCHER = userController.receiveUserByRole(USER_ROLES, "root").getForUser();
        apiController.updateToken(generateAuthToken(SUPPORT_MANAGER));
        task.refreshTask();
        udf = refreshUdf();
        task.setDescription(generateDescriptionForOperation(ADD_WATCHERS));
        udf.setUdfUser(generateUdfUser(UDF_WATCHER, RANDOM_WATCHER));
        task.refreshUdf(udf);
        sdBugController.performCommonOperation(task, ADD_WATCHERS);
        ApiAsserts
                .assertThat(sdBugController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK);
    }

    @Test(groups = {"PROC_SDBUG", "Regression"}, description = "Приватный комментарий", dependsOnMethods = "taskAddWatcher")
    public void taskPrivateComment() {
        apiController.updateToken(generateAuthToken(SUPPORT_MANAGER));
        task.refreshTask();
        udf = refreshUdf();
        task.setDescription(generateDescriptionForOperation(PRIVATE_COMMENT));
        task.refreshUdf(udf);
        sdBugController.performCommonOperation(task, PRIVATE_COMMENT);
        ApiAsserts
                .assertThat(sdBugController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK);
    }

    @Test(groups = {"PROC_SDBUG", "Regression"}, description = "Изменить автора", dependsOnMethods = "taskPrivateComment")
    public void taskChangeAuthor() {
        var authors = sdBugController.receiveAuthor(task.getNumber());
        if (authors != null) {
            apiController.updateToken(generateAuthToken(SUPPORT_MANAGER));
            task.refreshTask();
            udf = refreshUdf();
            task.setDescription(generateDescriptionForOperation(CHANGE_AUTHOR));
            udf.setUdfUser(generateUdfUser(UDF_SD_AUTHORCLIENT_MSG, authors.get(0)));
            task.refreshUdf(udf);
            sdBugController.performCommonOperation(task, CHANGE_AUTHOR);
            ApiAsserts
                    .assertThat(sdBugController.getResponse())
                    .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK);
        }
    }

    @Test(groups = {"PROC_SDBUG", "Regression"}, description = "Изменить ответственного", dependsOnMethods = "taskChangeAuthor")
    public void taskReassign() {
        var authors = sdBugController.receiveAuthor(task.getNumber());
        if (authors != null) {
            apiController.updateToken(generateAuthToken(SUPPORT_MANAGER));
            task.refreshTask();
            udf = refreshUdf();
            task.setHandlerUser(SUPPORT_MEMBER2);
            task.setDescription(generateString());
            udf.setUdfUser(generateUdfUser(STDT_HANDLER, SUPPORT_MEMBER2));
            udf.setSecondUdfUser(generateUdfUser(UDF_SD_TRUSTEDWATCHER, RANDOM_TRUST_WATCHER));
            udf.setThirdUdfUser(generateUdfUser(UDF_WATCHER, RANDOM_WATCHER));
            task.refreshUdf(udf);
            sdBugController.performCommonOperation(task, CHANGE_RES_PERSON);
            ApiAsserts
                    .assertThat(sdBugController.getResponse())
                    .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK);
        }
    }
}
