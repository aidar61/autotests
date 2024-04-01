package com.ts.integration.tests.proc_sd_help.sdTechAssist;

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
import com.ts.common.enums.TaskType;
import com.ts.common.enums.Users;
import com.ts.common.utils.DateUtils;
import com.ts.common.utils.InitEntities;
import com.ts.common.utils.RandomUtils;
import com.ts.common.utils.WaitManager;
import com.ts.integration.tests.BaseIntegrationTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;

import static com.ts.common.entitites.commonEntities.List.Constants.*;
import static com.ts.common.entitites.commonEntities.Task.Constants.AKKREDITIVES;
import static com.ts.common.entitites.commonEntities.Task.Constants.SENAGAT_BANK;
import static com.ts.common.entitites.commonEntities.Udfs.UdfSd.*;
import static com.ts.common.entitites.commonEntities.Udfs.UdfSd.UDF_SD_PROVIDEDHELPDEADLINE;
import static com.ts.common.enums.Operations.*;
import static com.ts.common.enums.Resolutions.REQUEST_NOT_ACTUAL;
import static com.ts.common.enums.Resolutions.REQUEST_RESOLVE;
import static com.ts.common.enums.TaskStatuses.*;
import static com.ts.common.enums.TaskStatuses.STATUS_SDHELP_CLOSED;
import static com.ts.common.utils.InitEntities.*;
import static com.ts.common.utils.RandomUtils.*;
import static com.ts.common.utils.RandomUtils.generateString;

public class SdTechAssistBaseDynamicTest extends BaseIntegrationTest {
    private SdHelpController sdHelpController;
    private GeneralTask task;
    private Parent parent;
    private User CLIENT;
    private User CLIENT_MANAGER;
    private User HANDLER_USER;
    private List<UserRole> USER_ROLES;
    private final String parentTaskNumber = "830940";

    @BeforeClass(alwaysRun = true)
    public void beforeClass() {
        apiController.updateToken(generateAuthToken(Users.ROOT));
        sdHelpController = apiController.getSdHelpController();
        userController = apiController.getUserController();
        baseController = apiController.getBaseController();

        GrTaskTable grTaskTable = dbHelper.getGrTaskTable();
        GrTaskDbEntity grTaskDbEntity = (GrTaskDbEntity) grTaskTable.receiveByTaskNumber(parentTaskNumber);
        parent = InitEntities.generateParent(grTaskDbEntity.getTask_id(), grTaskDbEntity.getTask_number());

        USER_ROLES = userController.receiveUserByTask(parentTaskNumber);

        CLIENT = userController.receiveUserByRole(USER_ROLES, Role.Constants.CLIENT, "root").getForUser();
        CLIENT_MANAGER = userController.receiveUserByRole(USER_ROLES, Role.Constants.CLIENT_MANAGER, "root").getForUser();
        HANDLER_USER = userController.receiveUserByRole(USER_ROLES, Role.Constants.ROLE_SUPPORT_MEMBER, "root").getForUser();
        task = InitEntities.getGeneralTask(TaskType.SD_TECH_ASSIST, Operations.CAT);
    }

    @BeforeMethod(alwaysRun = true)
    public void beforeMethod() {
        WaitManager.pause(5);
    }

    @Test(groups = {"SD_HELP", "Regression"}, description = "Создание CAT_SDTECHASSIST (КЛИЕНТ)")
    void catSdTechAssist() {
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

    @Test(groups = {"SD_HELP", "Regression"}, description = "Принять на анализ (МЕНЕДЖЕР КЛИЕНТА)", dependsOnMethods = "catSdTechAssist")
    void analize() {
        apiController.updateToken(generateAuthToken(CLIENT_MANAGER));
        udf = refreshUdf();
        task.refreshTask();

        task.setHandlerUser(HANDLER_USER);

        udf.setUdfUser(generateUdfUser(STDT_HANDLER, HANDLER_USER));
        task.refreshUdf(udf);

        sdHelpController.performCommonOperation(task, ANALIZE);
        ApiAsserts.assertThat(sdHelpController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isCorrectStatus(STATUS_SDHELP_ANALIZING)
                .isEquals(task);
        apiController.receiveTask(task.getNumber());
        CommonAssert.assertThat(apiController.getResponse())
                .isCorrectUdfList(UDF_SD_RESPONSIBLE_PARTY, RESPONSIBLE_PARTY_SUPPLIER)
                .isCorrectUdfTask(UDF_BDKU_CONFIGURATION, SENAGAT_BANK);
    }

    @Test(groups = {"SD_HELP", "Regression"}, description = "Запросить информацию (МЕНЕДЖЕР КЛИЕНТА)", dependsOnMethods = "analize")
    void requestInfo() {
        apiController.updateToken(generateAuthToken(CLIENT_MANAGER));
        udf = refreshUdf();
        task.refreshTask();

        sdHelpController.performCommonOperation(task, REQUESTINFO);
        ApiAsserts.assertThat(sdHelpController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isCorrectStatus(STATUS_SDHELP_WAITANALIZING)
                .isEquals(task);
        apiController.receiveTask(task.getNumber());
        CommonAssert.assertThat(apiController.getResponse())
                .isCorrectUdfList(UDF_SD_RESPONSIBLE_PARTY, RESPONSIBLE_PARTY_CLIENT)
                .isCorrectUdfDate(UDF_SD_CLARIFYINGQUESTIONDATE, DateUtils.getCurrentDate(0));
    }

    @Test(groups = {"SD_HELP", "Regression"}, description = "Предоставить информацию (КЛИЕНТ)", dependsOnMethods = "requestInfo")
    void provideInfo() {
        apiController.updateToken(generateAuthToken(CLIENT));
        udf = refreshUdf();
        task.refreshTask();

        sdHelpController.performCommonOperation(task, PROVIDEINFO);
        ApiAsserts.assertThat(sdHelpController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isCorrectStatus(STATUS_SDHELP_ANALIZING);
        apiController.receiveTask(task.getNumber());
        CommonAssert.assertThat(apiController.getResponse())
                .isCorrectUdfList(UDF_SD_RESPONSIBLE_PARTY, RESPONSIBLE_PARTY_SUPPLIER);
    }

    @Test(groups = {"SD_HELP", "Regression"}, description = "Оказать техническую помощь (МЕНЕДЖЕР КЛИЕНТА)"
            , dependsOnMethods = "provideInfo")
    void techHelp() {
        apiController.updateToken(generateAuthToken(CLIENT_MANAGER));
        udf = refreshUdf();
        task.refreshTask();

        sdHelpController.performCommonOperation(task, TECH_HELP);
        ApiAsserts.assertThat(sdHelpController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isCorrectStatus(STATUS_SDHELP_CONSULTED);
        apiController.receiveTask(task.getNumber());
        CommonAssert.assertThat(apiController.getResponse())
                .isCorrectUdfList(UDF_SD_RESPONSIBLE_PARTY, RESPONSIBLE_PARTY_CLIENT);

    }

    @Test(groups = {"SD_HELP", "Regression"}, description = "Запросить информацию (МЕНЕДЖЕР КЛИЕНТА)"
            , dependsOnMethods = "techHelp")
    void requestInfoReply() {
        apiController.updateToken(generateAuthToken(CLIENT_MANAGER));
        udf = refreshUdf();
        task.refreshTask();

        sdHelpController.performCommonOperation(task, REQUESTINFO);
        ApiAsserts.assertThat(sdHelpController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isCorrectStatus(STATUS_SDHELP_WAITCONSULTED);
        apiController.receiveTask(task.getNumber());
        CommonAssert.assertThat(apiController.getResponse())
                .isCorrectUdfList(UDF_SD_RESPONSIBLE_PARTY, RESPONSIBLE_PARTY_CLIENT)
                .isCorrectUdfDate(UDF_SD_CLARIFYINGQUESTIONDATE, DateUtils.getCurrentDate(0));
    }

    @Test(groups = {"SD_HELP", "Regression"}, description = "Отменить запрос информации (МЕНЕДЖЕР КЛИЕНТА)", dependsOnMethods = "requestInfoReply")
    void undoRequestInfo() {
        apiController.updateToken(generateAuthToken(CLIENT_MANAGER));
        udf = refreshUdf();
        task.refreshTask();

        sdHelpController.performCommonOperation(task, UNDOREQUESTINFO);
        ApiAsserts.assertThat(sdHelpController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isCorrectStatus(STATUS_SDHELP_CONSULTED)
                .isEquals(task);
        apiController.receiveTask(task.getNumber());
        CommonAssert.assertThat(apiController.getResponse())
                .isCorrectUdfList(UDF_SD_RESPONSIBLE_PARTY, RESPONSIBLE_PARTY_CLIENT);
    }

    @Test(groups = {"SD_HELP", "Regression"}, description = "Вернуть в работу (КЛИЕНТ)", dependsOnMethods = "undoRequestInfo")
    void msgReturn() {
        apiController.updateToken(generateAuthToken(CLIENT));
        udf = refreshUdf();
        task.refreshTask();

        task.setDescription(RandomUtils.generateStringWithLength(30));

        sdHelpController.performCommonOperation(task, RETURN);
        ApiAsserts.assertThat(sdHelpController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isCorrectStatus(STATUS_SDHELP_ANALIZING)
                .isEquals(task);
        apiController.receiveTask(task.getNumber());
        CommonAssert.assertThat(apiController.getResponse())
                .isCorrectUdfList(UDF_SD_RESPONSIBLE_PARTY, RESPONSIBLE_PARTY_SUPPLIER);
    }

    @Test(groups = {"SD_HELP", "Regression"}, description = "Оказать техническую помощь (МЕНЕДЖЕР КЛИЕНТА)", dependsOnMethods = "msgReturn")
    void techHelpReply() {
        apiController.updateToken(generateAuthToken(CLIENT_MANAGER));
        udf = refreshUdf();
        task.refreshTask();

        sdHelpController.performCommonOperation(task, TECH_HELP);
        ApiAsserts.assertThat(sdHelpController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isCorrectStatus(STATUS_SDHELP_CONSULTED)
                .isEquals(task);
        apiController.receiveTask(task.getNumber());
        CommonAssert.assertThat(apiController.getResponse())
                .isCorrectUdfList(UDF_SD_RESPONSIBLE_PARTY, RESPONSIBLE_PARTY_CLIENT);
    }

    @Test(groups = {"SD_HELP", "Regression"}, description = "Закрыть (КЛИЕНТ)", dependsOnMethods = "techHelpReply")
    void close() {
        apiController.updateToken(generateAuthToken(CLIENT));
        udf = refreshUdf();
        task.refreshTask();

        udf.setUdfList(generateUdfList(UDF_EVALUATING_REQUEST_EXECUTION, FIVE));
        udf.setUdfString(generateUdfString(UDF_EVALUATING_REQUEST_COMMENT, generateString()));

        task.refreshUdf(udf);
        task.setResolution(generateResolution(REQUEST_RESOLVE));

        sdHelpController.performCommonOperation(task, CLOSE);
        ApiAsserts.assertThat(sdHelpController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isCorrectStatus(STATUS_SDHELP_CLOSED)
                .isEquals(task);

        apiController.receiveTask(task.getNumber());
        CommonAssert.assertThat(apiController.getResponse())
                .isCorrectUdfList(UDF_SD_RESPONSIBLE_PARTY, RESPONSIBLE_PARTY_NOBODY);
    }

    @Test(groups = {"SD_HELP", "Regression"}, description = "Отменить закрытие (МЕНЕДЖЕР КЛИЕНТА)", dependsOnMethods = "close")
    void undoClose() {
        apiController.updateToken(generateAuthToken(CLIENT_MANAGER));
        udf = refreshUdf();
        task.refreshTask();

        udf.setUdfUser(generateUdfUser(STDT_HANDLER, HANDLER_USER));

        task.setHandlerUser(HANDLER_USER);
        task.refreshUdf(udf);

        sdHelpController.performCommonOperation(task, UNDO_CLOSE);
        ApiAsserts.assertThat(sdHelpController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isCorrectStatus(STATUS_SDHELP_ANALIZING)
                .isEquals(task);

        apiController.receiveTask(task.getNumber());
        CommonAssert.assertThat(apiController.getResponse())
                .isCorrectUdfList(UDF_SD_RESPONSIBLE_PARTY, RESPONSIBLE_PARTY_SUPPLIER)
                .isCorrectUdfDate(UDF_SD_PROVIDEDHELPDEADLINE, DateUtils.getCurrentDate(0));
    }

    @Test(groups = {"SD_HELP", "Regression"}, description = "Снять запрос (МЕНЕДЖЕР КЛИЕНТА)", dependsOnMethods = "undoClose")
    void removeRequest() {
        apiController.updateToken(generateAuthToken(CLIENT_MANAGER));
        udf = refreshUdf();
        task.refreshTask();

        task.setResolution(generateResolution(REQUEST_NOT_ACTUAL));

        sdHelpController.performCommonOperation(task, REMOVE_REQUEST);
        ApiAsserts.assertThat(sdHelpController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isCorrectStatus(STATUS_SDHELP_CLOSED)
                .isEquals(task);
    }
}
