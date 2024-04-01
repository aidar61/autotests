package com.ts.integration.tests.proc_sd_help.sdBuy;

import com.ts.common.application.controllers.TrackStudioHttpStatusCodes;
import com.ts.common.application.database.dbEntities.GrTaskDbEntity;
import com.ts.common.application.database.dbTables.GrTaskTable;
import com.ts.common.asserts.ApiAsserts;
import com.ts.common.asserts.CommonAssert;
import com.ts.common.controllers.TaskResponseBody;
import com.ts.common.controllers.sdhelp.SdHelpController;
import com.ts.common.entitites.commonEntities.Parent;
import com.ts.common.entitites.commonEntities.Role;
import com.ts.common.entitites.commonEntities.User;
import com.ts.common.entitites.commonEntities.UserRole;
import com.ts.common.entitites.tasks.GeneralTask;
import com.ts.common.enums.Operations;
import com.ts.common.enums.Resolutions;
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
import static com.ts.common.entitites.commonEntities.Udfs.UdfSd.UDF_EVALUATING_REQUEST_COMMENT;
import static com.ts.common.enums.Operations.*;
import static com.ts.common.enums.Operations.EVALUATE_REQUEST_EXE;
import static com.ts.common.enums.TaskStatuses.STATUS_SDHELP_CLOSED;
import static com.ts.common.enums.TaskStatuses.STATUS_SDHELP_NEW;
import static com.ts.common.utils.InitEntities.*;
import static com.ts.common.utils.RandomUtils.*;
import static com.ts.common.utils.RandomUtils.generateString;

public class SdBuyBaseStaticTest extends BaseIntegrationTest {
    private SdHelpController sdHelpController;
    private GeneralTask task;
    private Parent parent;
    private User CLIENT;
    private User CLIENT_MANAGER;
    private User HANDLER_USER;
    private User TRUSTED_WATCHER;
    private User EMPLOYEE;
    private List<UserRole> USER_ROLES;
    private final String parentTaskNumber = "830940";
    private GrTaskTable grTaskTable;

    @BeforeClass(alwaysRun = true)
    public void beforeClass() {
        apiController.updateToken(generateAuthToken(Users.ROOT));
        sdHelpController = apiController.getSdHelpController();
        userController = apiController.getUserController();
        baseController = apiController.getBaseController();

        grTaskTable = dbHelper.getGrTaskTable();
        GrTaskDbEntity grTaskDbEntity = (GrTaskDbEntity) grTaskTable.receiveByTaskNumber(parentTaskNumber);
        parent = InitEntities.generateParent(grTaskDbEntity.getTask_id(), grTaskDbEntity.getTask_number());

        USER_ROLES = userController.receiveUserByTask(parentTaskNumber);

        CLIENT = userController.receiveUserByRole(USER_ROLES, Role.Constants.CLIENT, "root").getForUser();
        CLIENT_MANAGER = userController.receiveUserByRole(USER_ROLES, Role.Constants.CLIENT_MANAGER, "root").getForUser();
        HANDLER_USER = userController.receiveUserByRole(USER_ROLES, Role.Constants.ROLE_SUPPORT_MEMBER, "root").getForUser();
        TRUSTED_WATCHER = userController.receiveUserByRole(USER_ROLES, Role.Constants.ROLE_SUPPORT_MEMBER, HANDLER_USER.getLogin()).getForUser();
        EMPLOYEE = userController.receiveUserByRole(USER_ROLES, Role.Constants.EMPLOYEE, "root").getForUser();
        task = InitEntities.getGeneralTask(TaskType.SD_BUY, Operations.CAT);
    }

    @Test(groups = {"SD_HELP", "Regression"}, description = "Создание CAT_SDBUY (КЛИЕНТ)")
    void catSdBuy() {
        apiController.updateToken(generateAuthToken(CLIENT));
        udf = refreshUdf();

        task.setName(generateName());
        task.setParent(parent);
        task.setDescription(task.getDescription() + generateString());

        udf.setUdfMultiList(generateUdfMultiList(UDF_SD_RELATED_TASK_CODES, ABNATTR));
        udf.setUdfTask(generateUdfTask(UDF_SD_MODULE, AKKREDITIVES));
        udf.setSecondUdfTask(generateUdfTask(UDF_BDKU_CONFIGURATION, SENAGAT_BANK));
        udf.setUdfString(generateUdfString(UDF_SD_CLIENTWATCHERS, generateEmail()));

        task.refreshUdf(udf);
        sdHelpController.createSdHelp(task);
        ApiAsserts.assertThat(sdHelpController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isCorrectStatus(STATUS_SDHELP_NEW)
                .isEquals(task);

        CommonAssert.assertThat(sdHelpController.getResponse())
                .isCorrectUdfList(UDF_SD_RESPONSIBLE_PARTY, RESPONSIBLE_PARTY_SUPPLIER);
    }

    @Test(groups = {"SD_HELP", "Regression"}, description = "Задать вопрос (КЛИЕНТ)", dependsOnMethods = "catSdBuy")
    void cliComment() {
        apiController.updateToken(generateAuthToken(CLIENT));
        udf = refreshUdf();
        task.refreshTask();

        sdHelpController.performCommonOperation(task, CLI_COMMENT);
        ApiAsserts.assertThat(sdHelpController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isEquals(task);

    }

    @Test(groups = {"SD_HELP", "Regression"}, description = "Сообщить информацию (КЛИЕНТ)", dependsOnMethods = "cliComment")
    void ourComment() {
        apiController.updateToken(generateAuthToken(CLIENT));
        udf = refreshUdf();
        task.refreshTask();

        sdHelpController.performCommonOperation(task, OUR_COMMENT);
        ApiAsserts.assertThat(sdHelpController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isEquals(task);
    }

    @Test(groups = {"SD_HELP", "Regression"}, description = "Изменить аттрибуты запроса (КЛИЕНТ)", dependsOnMethods = "ourComment")
    void changeAttrs() {
        apiController.updateToken(generateAuthToken(CLIENT));
        udf = refreshUdf();
        task.refreshTask();

        udf.setUdfString(generateUdfString(UDF_SD_REMOTEID, generateString()));
        udf.setSecondUdfString(generateUdfString(UDF_SD_INITPERSON, generateString()));

        task.refreshUdf(udf);

        sdHelpController.performCommonOperation(task, CHANGE_ATTR);
        ApiAsserts.assertThat(sdHelpController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isEquals(task);
    }

    @Test(groups = {"SD_HELP", "Regression"}, description = "Назначить наблюдателей клиента (КЛИЕНТ)", dependsOnMethods = "changeAttrs")
    void appointClientWatchers() {
        apiController.updateToken(generateAuthToken(CLIENT));
        udf = refreshUdf();
        task.refreshTask();

        udf.setUdfString(generateUdfString(UDF_SD_CLIENTWATCHERS, generateEmail()));

        task.refreshUdf(udf);

        sdHelpController.performCommonOperation(task, ADD_CLIENT_WATCHERS);
        ApiAsserts.assertThat(sdHelpController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isEquals(task);
    }

    @Test(groups = {"SD_HELP", "Regression"}, description = "Изменить автора (КЛИЕНТ)", dependsOnMethods = "appointClientWatchers")
    void changeAuthor() {
        apiController.updateToken(generateAuthToken(CLIENT));
        udf = refreshUdf();
        task.refreshTask();

        CLIENT = userController.receiveUserByRole(USER_ROLES, Role.Constants.CLIENT, CLIENT.getLogin()).getForUser();
        udf.setUdfString(generateUdfString(UDF_SD_INITPERSON, generateString()));
        udf.setUdfUser(generateUdfUser(UDF_SD_AUTHORCLIENT_MSG, CLIENT));
        task.refreshUdf(udf);

        sdHelpController.performCommonOperation(task, CHANGE_AUTHOR);
        ApiAsserts.assertThat(sdHelpController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isEquals(task);
    }

    @Test(groups = {"SD_HELP", "Regression"}, description = "Изменить модуль системы (МЕНЕДЖЕР КЛИЕНТА)", dependsOnMethods = "changeAuthor")
    void changeSdModule() {
        apiController.updateToken(generateAuthToken(CLIENT_MANAGER));
        udf = refreshUdf();
        task.refreshTask();

        udf.setUdfTask(generateUdfTask(UDF_SD_MODULE, CURRENCY_MARKET));
        task.refreshUdf(udf);

        sdHelpController.performCommonOperation(task, CHANGE_ONLY_SD_MODULE);
        ApiAsserts.assertThat(sdHelpController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isEquals(task);
    }

    @Test(groups = {"SD_HELP", "Regression"}, description = "Сообщить информацию (МЕНЕДЖЕР КЛИЕНТА)", dependsOnMethods = "changeSdModule")
    void ourCommentClientManager() {
        apiController.updateToken(generateAuthToken(CLIENT_MANAGER));
        udf = refreshUdf();
        task.refreshTask();

        sdHelpController.performCommonOperation(task, OUR_COMMENT);
        ApiAsserts.assertThat(sdHelpController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isEquals(task);
    }

//    @Test(groups = {"SD_HELP", "Regression"}, description = "Сообщить срок оказания консультации (МЕНЕДЖЕР КЛИЕНТА)", dependsOnMethods = "ourCommentClientManager")
//    void provideDeadline() {
//        apiController.updateToken(generateAuthToken(CLIENT_MANAGER));
//        udf = refreshUdf();
//        task.refreshTask();
//
//        udf.setUdfDate(generateUdfDate(UDF_SD_HELPDEADLINE, DateUtils.getCurrentDate(0)));
//        task.refreshUdf(udf);
//
//        sdHelpController.performCommonOperation(task, PROVIDE_DEADLINE);
//        ApiAsserts.assertThat(sdHelpController.getResponse())
//                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
//                .isParseableBody(TaskResponseBody.class)
//                .assertTask()
//                .isEquals(task);
//    }

    @Test(groups = {"SD_HELP", "Regression"}, description = "Изменить список связанных задач (МЕНЕДЖЕР КЛИЕНТА)"
            , dependsOnMethods = "ourCommentClientManager")
    void changeLinkedTasks() {
        apiController.updateToken(generateAuthToken(CLIENT_MANAGER));
        udf = refreshUdf();
        task.refreshTask();

        GrTaskDbEntity grTaskDbEntity = (GrTaskDbEntity)
                grTaskTable.receiveRandomTask("task_category", EQUAL.operator, "CAT_BUGTASK");

        udf.setUdfTask(generateUdfTask(UDF_SD_LINKEDREQUEST, grTaskDbEntity.mapTo()));
        task.refreshUdf(udf);

        sdHelpController.performCommonOperation(task, CHANGE_LINKED_TASKS);
        ApiAsserts.assertThat(sdHelpController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isEquals(task);
    }

    @Test(groups = {"SD_HELP", "Regression"}, description = "Назначить доверенного наблюдателя (МЕНЕДЖЕР КЛИЕНТА)"
            , dependsOnMethods = "changeLinkedTasks")
    void addTrustedWatcher() {
        apiController.updateToken(generateAuthToken(CLIENT_MANAGER));
        udf = refreshUdf();
        task.refreshTask();

        udf.setUdfUser(generateUdfUser(UDF_SD_TRUSTEDWATCHER, TRUSTED_WATCHER));
        task.refreshUdf(udf);

        sdHelpController.performCommonOperation(task, ADD_TRUST_WATCHER);
        ApiAsserts.assertThat(sdHelpController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isEquals(task);
    }

    @Test(groups = {"SD_HELP", "Regression"}, description = "Назначить наблюдателя (МЕНЕДЖЕР КЛИЕНТА)"
            , dependsOnMethods = "addTrustedWatcher")
    void appointWatcher() {
        apiController.updateToken(generateAuthToken(CLIENT_MANAGER));
        udf = refreshUdf();
        task.refreshTask();

        udf.setUdfUser(generateUdfUser(UDF_WATCHER, EMPLOYEE));
        task.refreshUdf(udf);

        sdHelpController.performCommonOperation(task, ADD_WATCHERS);
        ApiAsserts.assertThat(sdHelpController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isEquals(task);
    }

    @Test(groups = {"SD_HELP", "Regression"}, description = "Приватный комментарий (МЕНЕДЖЕР КЛИЕНТА)"
            , dependsOnMethods = "appointWatcher")
    void privateComment() {
        apiController.updateToken(generateAuthToken(CLIENT_MANAGER));
        udf = refreshUdf();
        task.refreshTask();

        sdHelpController.performCommonOperation(task, PRIVATE_COMMENT);
        ApiAsserts.assertThat(sdHelpController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isEquals(task);
    }

    @Test(groups = {"SD_HELP", "Regression"}, description = "Изменить автора (МЕНЕДЖЕР КЛИЕНТА)"
            , dependsOnMethods = "privateComment")
    void changeAuthorClientManager() {
        apiController.updateToken(generateAuthToken(CLIENT_MANAGER));
        udf = refreshUdf();
        task.refreshTask();

        CLIENT = userController.receiveUserByRole(USER_ROLES, Role.Constants.CLIENT, CLIENT.getLogin()).getForUser();

        udf.setUdfUser(generateUdfUser(UDF_SD_AUTHORCLIENT_MSG, CLIENT));
        udf.setUdfString(generateUdfString(UDF_SD_INITPERSON, generateString()));
        task.refreshUdf(udf);

        sdHelpController.performCommonOperation(task, CHANGE_AUTHOR);
        ApiAsserts.assertThat(sdHelpController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isEquals(task);
    }

//    @Test(groups = {"SD_HELP", "Regression"}, description = "Изменить ответственного (МЕНЕДЖЕР КЛИЕНТА)"
//            , dependsOnMethods = "changeAuthorClientManager")
//    void reassign() {
//        apiController.updateToken(generateAuthToken(CLIENT_MANAGER));
//        udf = refreshUdf();
//        task.refreshTask();
//
//        udf.setUdfUser(generateUdfUser(STDT_HANDLER, HANDLER_USER));
//        udf.setSecondUdfUser(generateUdfUser(UDF_WATCHER, EMPLOYEE));
//        udf.setThirdUdfUser(generateUdfUser(UDF_SD_TRUSTEDWATCHER, TRUSTED_WATCHER));
//        task.refreshUdf(udf);
//        task.setHandlerUser(HANDLER_USER);
//
//        sdHelpController.performCommonOperation(task, CHANGE_RES_PERSON);
//        ApiAsserts.assertThat(sdHelpController.getResponse())
//                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
//                .isParseableBody(TaskResponseBody.class)
//                .assertTask()
//                .isEquals(task);
//    }

    @Test(groups = {"SD_HELP", "Regression"}, description = "Снять запрос (МЕНЕДЖЕР КЛИЕНТА)"
            , dependsOnMethods = "changeAuthorClientManager")
    void removeRequest() {
        apiController.updateToken(generateAuthToken(CLIENT_MANAGER));
        udf = refreshUdf();
        task.refreshTask();

        task.setResolution(generateResolution(Resolutions.REQUEST_NOT_ACTUAL));

        sdHelpController.performCommonOperation(task, REMOVE_REQUEST);
        ApiAsserts.assertThat(sdHelpController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isCorrectStatus(STATUS_SDHELP_CLOSED)
                .isEquals(task);
    }
}
