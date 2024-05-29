package com.ts.integration.tests.proc_sla_bug;

import com.ts.common.application.controllers.TrackStudioHttpStatusCodes;
import com.ts.common.application.database.dbEntities.GrTaskDbEntity;
import com.ts.common.application.database.dbTables.GrTaskTable;
import com.ts.common.asserts.ApiAsserts;
import com.ts.common.asserts.CommonAssert;
import com.ts.common.controllers.TaskResponseBody;
import com.ts.common.controllers.sla.SlaBugController;
import com.ts.common.entitites.commonEntities.*;
import com.ts.common.entitites.tasks.GeneralTask;
import com.ts.common.enums.Operations;
import com.ts.common.enums.Tables;
import com.ts.common.enums.TaskType;
import com.ts.common.enums.Users;
import com.ts.common.utils.DateUtils;
import com.ts.common.utils.InitEntities;
import com.ts.integration.tests.BaseIntegrationTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.List;

import static com.ts.common.application.database.DbQueryHelper.Operators.EQUAL;
import static com.ts.common.entitites.commonEntities.List.Constants.*;
import static com.ts.common.entitites.commonEntities.Task.Constants.*;
import static com.ts.common.entitites.commonEntities.Udfs.UdfSd.*;
import static com.ts.common.enums.Operations.*;
import static com.ts.common.enums.TaskStatuses.*;
import static com.ts.common.utils.InitEntities.*;
import static com.ts.common.utils.InitEntities.generateUdfMemo;
import static com.ts.common.utils.RandomUtils.*;
import static com.ts.common.utils.RandomUtils.generateString;

public class SlaBugBaseStaticTest extends BaseIntegrationTest {
    private SlaBugController slaBugController;
    private GeneralTask task;
    private Parent parent;
    private User CLIENT;
    private User CLIENT_MANAGER;
    private User EMPLOYEE;
    private User EMPLOYEE_WATCHER;
    private List<UserRole> USER_ROLES;
    private GrTaskTable grTaskTable;
    private final String parentTaskNumber = "928684";

    @BeforeClass(alwaysRun = true)
    public void beforeClass() {
        apiController.updateToken(InitEntities.generateAuthToken(Users.ROOT));
        slaBugController = apiController.getSlaBugController();
        userController = apiController.getUserController();
        baseController = apiController.getBaseController();

        grTaskTable = dbHelper.getGrTaskTable();
        GrTaskDbEntity grTaskDbEntity = (GrTaskDbEntity) grTaskTable.receiveByTaskNumber(parentTaskNumber);
        parent = InitEntities.generateParent(grTaskDbEntity.getTask_id(), grTaskDbEntity.getTask_number());

        USER_ROLES = userController.receiveUserByTask(parentTaskNumber);

        CLIENT = userController.receiveUserByRole(USER_ROLES, Role.Constants.CLIENT, "root").getForUser();
        CLIENT_MANAGER = userController.receiveUserByRole(USER_ROLES, Role.Constants.CLIENT_MANAGER, "root").getForUser();
        EMPLOYEE = userController.receiveUserByRole(USER_ROLES, Role.Constants.EMPLOYEE, "root").getForUser();
        EMPLOYEE_WATCHER = userController.receiveUserByRole(USER_ROLES, Role.Constants.EMPLOYEE, EMPLOYEE.getLogin()).getForUser();

        task = InitEntities.getGeneralTask(TaskType.SLA_BUG, Operations.CAT);
    }

    @Test(groups = {"PROC_SLABUG", "Regression"}, description = "Создание CAT_SLABUG (КЛИЕНТ)")
    public void slaBugCat() {
        apiController.updateToken(InitEntities.generateAuthToken(CLIENT));
        udf = refreshUdf();
        task.refreshTask();

        task.setName(generateName());
        task.setParent(parent);
        task.setDescription(Tables.SLA_BUG.getTable());

        udf.setUdfMultiList(generateUdfMultiList(UDF_SD_RELATED_TASK_CODES, ABNATTR));
        udf.setUdfTask(generateUdfTask(UDF_SD_MODULE, AKKREDITIVES));
        udf.setUdfList(generateUdfList(UDF_SDBUG_PRIORITYBUG, CRITICAL));
        udf.setUdfString(generateUdfString(UDF_SD_CLIENTWATCHERS, generateEmail()));
        udf.setSecondUdfTask(generateUdfTask(UDF_BDKU_CONFIGURATION, MTBANK));
        udf.setSecondUdfString(generateUdfString(UDF_SD_REMOTEID, generateString()));
        udf.setThirdUdfString(generateUdfString(UDF_SD_INITPERSON, generateString()));
        udf.setSecondUdfList(generateUdfList(UDF_SD_REMOTEACCESS, NO_REMOTE_ACCESS));
        udf.setUdfMemo(generateUdfMemo(UDF_SD_REMOTEACCESSINFO, generateString()));

        task.refreshUdf(udf);
        slaBugController.createSlaBugTask(task);
        ApiAsserts.assertThat(slaBugController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isCorrectStatus(STATUS_SLABUG_NEW);
    }

    @Test(groups = {"PROC_SLABUG", "Regression"}, description = "Принять на анализ (МЕНЕДЖЕР КЛИЕНТА)"
            , dependsOnMethods = "slaBugCat")
    public void msgAnalyze() {
        apiController.updateToken(generateAuthToken(CLIENT_MANAGER));
        udf = refreshUdf();
        task.refreshTask();

        task.setHandlerUser(CLIENT_MANAGER);

        udf.setUdfUser(generateUdfUser(UDF_SD_TRUSTEDWATCHER, EMPLOYEE));
        udf.setSecondUdfUser(generateUdfUser(UDF_WATCHER, EMPLOYEE_WATCHER));
        udf.setThirdUdfUser(generateUdfUser(STDT_HANDLER, CLIENT_MANAGER));

        task.refreshUdf(udf);

        slaBugController.performCommonOperation(task, ANALIZE);
        ApiAsserts.assertThat(slaBugController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isCorrectStatus(STATUS_SLABUG_ANALIZING);

        apiController.receiveTask(task.getNumber());
        CommonAssert.assertThat(apiController.getResponse())
                .isCorrectUdfList(UDF_SD_RESPONSIBLE_PARTY, RESPONSIBLE_PARTY_SUPPLIER)
                .isCorrectUdfList(UDF_ROLE_CURRENT, FIRST_LINE)
                .isCorrectUdfUSer(UDF_ROLE_FIRST_LINE, CLIENT_MANAGER.getLogin());
    }

    @Test(groups = {"PROC_SLABUG", "Regression"}, description = "Сообщить информацию (КЛИЕНТ)"
            , dependsOnMethods = "msgAnalyze")
    public void ourComment() {
        apiController.updateToken(generateAuthToken(CLIENT));
        udf = refreshUdf();
        task.refreshTask();

        slaBugController.performCommonOperation(task, OUR_COMMENT);
        ApiAsserts.assertThat(slaBugController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isEquals(task);
    }

    @Test(groups = {"PROC_SLABUG", "Regression"}, description = "Изменить наличие удаленного доступа (КЛИЕНТ)"
            , dependsOnMethods = "ourComment")
    public void changeRemoteAccess() {
        apiController.updateToken(generateAuthToken(CLIENT));
        udf = refreshUdf();
        task.refreshTask();

        udf.setUdfList(generateUdfList(UDF_SD_REMOTEACCESS, YES_GIVEN));
        task.refreshUdf(udf);

        slaBugController.performCommonOperation(task, CHANGE_REMOTE_ACCESS);
        ApiAsserts.assertThat(slaBugController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isEquals(task);
    }

    @Test(groups = {"PROC_SLABUG", "Regression"}, description = "Изменить приоритет (КЛИЕНТ)"
            , dependsOnMethods = "changeRemoteAccess")
    public void changePriority() {
        apiController.updateToken(generateAuthToken(CLIENT));
        udf = refreshUdf();
        task.refreshTask();

        udf.setUdfList(generateUdfList(UDF_SDBUG_PRIORITYBUG, SDBUG_PRIORITYBUG_CRITICAL_AFTER_UPDATE));
        task.refreshUdf(udf);

        slaBugController.performCommonOperation(task, CHANGE_PRIORITY);
        ApiAsserts.assertThat(slaBugController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isEquals(task);
    }

    @Test(groups = {"PROC_SLABUG", "Regression"}, description = "Изменить аттрибуты запроса (КЛИЕНТ)"
            , dependsOnMethods = "changePriority")
    public void changeAttrs() {
        apiController.updateToken(generateAuthToken(CLIENT));
        udf = refreshUdf();
        task.refreshTask();

        udf.setUdfString(generateUdfString(UDF_SD_REMOTEID, generateString()));
        udf.setSecondUdfString(generateUdfString(UDF_SD_INITPERSON, generateString()));

        task.refreshUdf(udf);

        slaBugController.performCommonOperation(task, CHANGE_ATTR);
        ApiAsserts.assertThat(slaBugController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isEquals(task);
    }

    @Test(groups = {"PROC_SLABUG", "Regression"}, description = "Назначить наблюдателей клиента (КЛИЕНТ)"
            , dependsOnMethods = "changeAttrs")
    public void appointClientWatchers() {
        apiController.updateToken(generateAuthToken(CLIENT));
        udf = refreshUdf();
        task.refreshTask();

        udf.setUdfString(generateUdfString(UDF_SD_CLIENTWATCHERS, generateEmail()));
        task.refreshUdf(udf);

        slaBugController.performCommonOperation(task, ADD_CLIENT_WATCHERS);
        ApiAsserts.assertThat(slaBugController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isEquals(task);
    }

    @Test(groups = {"PROC_SLABUG", "Regression"}, description = "Изменить автора (КЛИЕНТ)"
            , dependsOnMethods = "appointClientWatchers")
    public void changeAuthor() {
        apiController.updateToken(generateAuthToken(CLIENT));
        udf = refreshUdf();
        task.refreshTask();

        CLIENT = userController.receiveUserByRole(USER_ROLES, Role.Constants.CLIENT, CLIENT.getLogin()).getForUser();
        udf.setUdfUser(generateUdfUser(UDF_SD_AUTHORCLIENT_MSG, CLIENT));
        task.refreshUdf(udf);

        slaBugController.performCommonOperation(task, CHANGE_AUTHOR);
        ApiAsserts.assertThat(slaBugController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isEquals(task);
    }

    @Test(groups = {"PROC_SLABUG", "Regression"}, description = "Изменить модуль системы (МЕНЕДЖЕР КЛИЕНТА)"
            , dependsOnMethods = "changeAuthor")
    public void changeSdModule() {
        apiController.updateToken(generateAuthToken(CLIENT_MANAGER));
        udf = refreshUdf();
        task.refreshTask();

        udf.setUdfTask(generateUdfTask(UDF_SD_MODULE, CURRENCY_MARKET));
        task.refreshUdf(udf);

        slaBugController.performCommonOperation(task, CHANGE_MODULE);
        ApiAsserts.assertThat(slaBugController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isEquals(task);
    }

    @Test(groups = {"PROC_SLABUG", "Regression"}, description = "Сообщить информацию (МЕНЕДЖЕР КЛИЕНТА)"
            , dependsOnMethods = "changeSdModule")
    public void ourCommentManager() {
        apiController.updateToken(generateAuthToken(CLIENT_MANAGER));

        udf = refreshUdf();
        task.refreshTask();

        slaBugController.performCommonOperation(task, OUR_COMMENT);
        ApiAsserts.assertThat(slaBugController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isEquals(task);
    }

    @Test(groups = {"PROC_SLABUG", "Regression"}, description = "Изменить первую линию (МЕНЕДЖЕР КЛИЕНТА)"
            , dependsOnMethods = "ourCommentManager")
    public void changeFirstLine() {
        apiController.updateToken(generateAuthToken(CLIENT_MANAGER));

        udf = refreshUdf();
        task.refreshTask();

        EMPLOYEE = userController.receiveUserByRole(USER_ROLES, Role.Constants.EMPLOYEE, EMPLOYEE.getLogin()).getForUser();
        udf.setUdfUser(generateUdfUser(UDF_ROLE_FIRST_LINE, EMPLOYEE));
        task.refreshUdf(udf);

        slaBugController.performCommonOperation(task, CHANGE_FIRST_LINE);
        ApiAsserts.assertThat(slaBugController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isEquals(task);
    }

    @Test(groups = {"PROC_SLABUG", "Regression"}, description = "Изменить разработчика (МЕНЕДЖЕР КЛИЕНТА)"
            , dependsOnMethods = "changeFirstLine")
    public void changeDeveloper() {
        apiController.updateToken(generateAuthToken(CLIENT_MANAGER));

        udf = refreshUdf();
        task.refreshTask();

        EMPLOYEE = userController.receiveUserByRole(USER_ROLES, Role.Constants.EMPLOYEE, EMPLOYEE.getLogin()).getForUser();
        udf.setUdfUser(generateUdfUser(UDF_ROLE_DEVELOPER, EMPLOYEE));
        task.refreshUdf(udf);

        slaBugController.performCommonOperation(task, CHANGE_DEVELOPER);
        ApiAsserts.assertThat(slaBugController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isEquals(task);
    }

    @Test(groups = {"PROC_SLABUG", "Regression"}, description = "Изменить тестировщика (МЕНЕДЖЕР КЛИЕНТА)"
            , dependsOnMethods = "changeDeveloper")
    public void changeTester() {
        apiController.updateToken(generateAuthToken(CLIENT_MANAGER));

        udf = refreshUdf();
        task.refreshTask();

        EMPLOYEE = userController.receiveUserByRole(USER_ROLES, Role.Constants.EMPLOYEE, EMPLOYEE.getLogin()).getForUser();
        udf.setUdfUser(generateUdfUser(UDF_ROLE_TESTER, EMPLOYEE));
        task.refreshUdf(udf);

        slaBugController.performCommonOperation(task, CHANGE_TESTER);
        ApiAsserts.assertThat(slaBugController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isEquals(task);
    }

    @Test(groups = {"PROC_SLABUG", "Regression"}, description = "Изменить наличие удаленного доступа (МЕНЕДЖЕР КЛИЕНТА)"
            , dependsOnMethods = "changeTester")
    public void changeRemoteAccessManager() {
        apiController.updateToken(generateAuthToken(CLIENT_MANAGER));

        udf = refreshUdf();
        task.refreshTask();

        udf.setUdfList(generateUdfList(UDF_SD_REMOTEACCESS, NO_REMOTE_ACCESS));
        task.refreshUdf(udf);

        slaBugController.performCommonOperation(task, CHANGE_REMOTE_ACCESS);
        ApiAsserts.assertThat(slaBugController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isEquals(task);
    }

    @Test(groups = {"PROC_SLABUG", "Regression"}, description = "Изменить приоритет (МЕНЕДЖЕР КЛИЕНТА)"
            , dependsOnMethods = "changeRemoteAccessManager")
    public void changePriorityManager() {
        apiController.updateToken(generateAuthToken(CLIENT_MANAGER));

        udf = refreshUdf();
        task.refreshTask();

        udf.setUdfList(generateUdfList(UDF_SDBUG_PRIORITYBUG, MINOR));
        task.refreshUdf(udf);

        slaBugController.performCommonOperation(task, CHANGE_PRIORITY);
        ApiAsserts.assertThat(slaBugController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isEquals(task);
    }

    @Test(groups = {"PROC_SLABUG", "Regression"}, description = "Корректировка сроков SLA (МЕНЕДЖЕР КЛИЕНТА)"
            , dependsOnMethods = "changePriorityManager")
    public void correctSlaDates() {
        apiController.updateToken(generateAuthToken(CLIENT_MANAGER));

        udf = refreshUdf();
        task.refreshTask();

        String currentDateTimeStamp = DateUtils.getCurrentDateTimeStamp();
        udf.setUdfString(generateUdfString(UDF_SLABUG_TEMPPROVIDEDATE, currentDateTimeStamp));
        udf.setUdfString(generateUdfString(UDF_SLABUG_PERMPROVIDEDATE, currentDateTimeStamp));
        task.refreshUdf(udf);

        slaBugController.performCommonOperation(task, CORRECT_SLA_DATES);
        ApiAsserts.assertThat(slaBugController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isEquals(task);
    }

    @Test(groups = {"PROC_SLABUG", "Regression"}, description = "Изменить автора (МЕНЕДЖЕР КЛИЕНТА)"
            , dependsOnMethods = "correctSlaDates")
    public void changeAuthorManager() {
        apiController.updateToken(generateAuthToken(CLIENT_MANAGER));

        udf = refreshUdf();
        task.refreshTask();

        CLIENT = userController.receiveUserByRole(USER_ROLES, Role.Constants.CLIENT, CLIENT.getLogin()).getForUser();
        udf.setUdfUser(generateUdfUser(UDF_SD_AUTHORCLIENT_MSG, CLIENT));
        task.refreshUdf(udf);

        slaBugController.performCommonOperation(task, CHANGE_AUTHOR);
        ApiAsserts.assertThat(slaBugController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isEquals(task);
    }

    @Test(groups = {"PROC_SLABUG", "Regression"}, description = "Изменить ответственную роль (МЕНЕДЖЕР КЛИЕНТА)"
            , dependsOnMethods = "changeAuthorManager")
    public void changeCurrentRole() {
        apiController.updateToken(generateAuthToken(CLIENT_MANAGER));

        udf = refreshUdf();
        task.refreshTask();

        EMPLOYEE = userController.receiveUserByRole(USER_ROLES, Role.Constants.CLIENT, CLIENT.getLogin()).getForUser();
        udf.setUdfUser(generateUdfUser(UDF_ROLE_WORKER, EMPLOYEE));
        udf.setUdfList(generateUdfList(UDF_ROLE_CURRENT, DEVELOPER_ROLE_CURRENT));
        task.refreshUdf(udf);

        slaBugController.performCommonOperation(task, CHANGE_CURRENT_ROLE);
        ApiAsserts.assertThat(slaBugController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isEquals(task);
    }

    @Test(groups = {"PROC_SLABUG", "Regression"}, description = "Изменить список связанных задач (МЕНЕДЖЕР КЛИЕНТА)"
            , dependsOnMethods = "changeCurrentRole")
    public void changeLinkedTask() {
        apiController.updateToken(generateAuthToken(CLIENT_MANAGER));

        udf = refreshUdf();
        task.refreshTask();

        GrTaskDbEntity grTaskDbEntity = (GrTaskDbEntity)
                grTaskTable.receiveRandomTask("task_category", EQUAL.operator, "CAT_BUGTASK");

        udf.setUdfTask(generateUdfTask(UDF_SD_LINKEDREQUEST, grTaskDbEntity.mapTo()));
        task.refreshUdf(udf);

        slaBugController.performCommonOperation(task, CHANGE_LINKED_TASKS);
        ApiAsserts.assertThat(slaBugController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isEquals(task);
    }

    @Test(groups = {"PROC_SLABUG", "Regression"}, description = "Назначить доверенного наблюдателя (МЕНЕДЖЕР КЛИЕНТА)"
            , dependsOnMethods = "changeLinkedTask")
    public void addTrustedWatcher() {
        apiController.updateToken(generateAuthToken(CLIENT_MANAGER));

        udf = refreshUdf();
        task.refreshTask();

        EMPLOYEE = userController.receiveUserByRole(USER_ROLES, Role.Constants.EMPLOYEE, EMPLOYEE.getLogin()).getForUser();
        udf.setUdfUser(generateUdfUser(UDF_SD_TRUSTEDWATCHER, EMPLOYEE));
        task.refreshUdf(udf);

        slaBugController.performCommonOperation(task, ADD_TRUST_WATCHER);
        ApiAsserts.assertThat(slaBugController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isEquals(task);
    }

    @Test(groups = {"PROC_SLABUG", "Regression"}, description = "Назначить наблюдателя (МЕНЕДЖЕР КЛИЕНТА)"
            , dependsOnMethods = "addTrustedWatcher")
    public void appointWatcher() {
        apiController.updateToken(generateAuthToken(CLIENT_MANAGER));

        udf = refreshUdf();
        task.refreshTask();

        EMPLOYEE_WATCHER = userController.receiveUserByRole(USER_ROLES, Role.Constants.EMPLOYEE, EMPLOYEE_WATCHER.getLogin()).getForUser();
        udf.setUdfUser(generateUdfUser(UDF_WATCHER, EMPLOYEE_WATCHER));
        task.refreshUdf(udf);

        slaBugController.performCommonOperation(task, ADD_WATCHERS);
        ApiAsserts.assertThat(slaBugController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isEquals(task);
    }

    @Test(groups = {"PROC_SLABUG", "Regression"}, description = "Приватный комментарий (МЕНЕДЖЕР КЛИЕНТА)"
            , dependsOnMethods = "appointWatcher")
    public void privateComment() {
        apiController.updateToken(generateAuthToken(CLIENT_MANAGER));

        udf = refreshUdf();
        task.refreshTask();

        slaBugController.performCommonOperation(task, PRIVATE_COMMENT);

        ApiAsserts.assertThat(slaBugController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isEquals(task);
    }

    @Test(groups = {"PROC_SLABUG", "Regression"}, description = "Приватный комментарий (МЕНЕДЖЕР КЛИЕНТА)"
            , dependsOnMethods = "privateComment")
    public void reassign() {
        apiController.updateToken(generateAuthToken(CLIENT_MANAGER));

        udf = refreshUdf();
        task.refreshTask();

        CLIENT_MANAGER = userController.receiveUserByRole(USER_ROLES, Role.Constants.CLIENT_MANAGER, CLIENT_MANAGER.getLogin()).getForUser();
        udf.setUdfUser(generateUdfUser(STDT_HANDLER, CLIENT_MANAGER));
        udf.setSecondUdfUser(generateUdfUser(UDF_WATCHER, EMPLOYEE_WATCHER));
        udf.setThirdUdfUser(generateUdfUser(UDF_SD_TRUSTEDWATCHER, EMPLOYEE));
        task.setHandlerUser(CLIENT_MANAGER);
        task.refreshUdf(udf);

        slaBugController.performCommonOperation(task, CHANGE_RES_PERSON);
        ApiAsserts.assertThat(slaBugController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isEquals(task);
    }

    @Test(groups = {"PROC_SLABUG", "Regression"}, description = "Снять запрос (МЕНЕДЖЕР КЛИЕНТА)"
            , dependsOnMethods = "reassign")
    public void removeRequest() {
        apiController.updateToken(generateAuthToken(CLIENT_MANAGER));

        udf = refreshUdf();
        task.refreshTask();

        slaBugController.performCommonOperation(task, REMOVE_REQUEST);
        ApiAsserts.assertThat(slaBugController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isEquals(task);
    }

    @Test(groups = {"PROC_SLABUG", "Regression"}, description = "Оценить выполнение запроса (КЛИЕНТА)"
            , dependsOnMethods = "removeRequest")
    public void evaluateRequestExe() {
        apiController.updateToken(generateAuthToken(CLIENT));

        udf = refreshUdf();
        task.refreshTask();

        udf.setUdfList(generateUdfList(UDF_EVALUATING_REQUEST_EXECUTION, FIVE));
        udf.setUdfMemo(generateUdfMemo(UDF_EVALUATING_REQUEST_COMMENT, generateString()));
        task.refreshUdf(udf);

        slaBugController.performCommonOperation(task, EVALUATE_REQUEST_EXE);
        ApiAsserts.assertThat(slaBugController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isEquals(task);
    }
}
