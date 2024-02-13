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
import com.ts.common.enums.Resolutions;
import com.ts.common.enums.TaskType;
import com.ts.common.utils.InitEntities;
import com.ts.common.utils.JsonUtils;
import com.ts.integration.tests.BaseIntegrationTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.List;

import static com.ts.common.entitites.commonEntities.List.Constants.*;
import static com.ts.common.entitites.commonEntities.Task.Constants.CORE;
import static com.ts.common.entitites.commonEntities.Udfs.UdfSd.*;
import static com.ts.common.enums.Operations.*;
import static com.ts.common.enums.TaskStatuses.*;
import static com.ts.common.utils.InitEntities.*;
import static com.ts.common.utils.RandomUtils.*;

public class SdDevBaseDynamicTest extends BaseIntegrationTest {

    public SdDevController sdDevController;
    private GeneralTask task;
    private Parent parent;
    private User CLIENT;
    private User SUPPORT_MANAGER;
    private User SUPPORT_MEMBER;
    private User ROLE_CONTRACT_EMP;
    private User RANDOM_WATCHER;
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
        SUPPORT_MANAGER = userController.receiveUserByRole(USER_ROLES, "Менеджер клиента", "ovoronov").getForUser();
        SUPPORT_MEMBER = userController.receiveUserByRole(USER_ROLES, "Участник проекта сопровождения", "root").getForUser();
        ROLE_CONTRACT_EMP = userController.receiveUserByRole(USER_ROLES, "Ведение контрактов", "root").getForUser();
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
        ApiAsserts
                .assertThat(response)
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isCorrectStatus(STATUS_SDDEV_NEW)
                .isEquals(task);

//        var taskDetail = apiController.receiveTask(task.getNumber());
//        CommonAssert
//                .assertThat(taskDetail)
//                .isCorrectUdfList(UDF_SDDEV_CONDITIONSACCEPTED, CONDITIONS_ACCEPTED_NO.getId())
//                .isCorrectUdfList(UDF_SD_RESPONSIBLE_PARTY, RESPONSIBLE_PARTY_SUPPLIER.getId());
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
        ApiAsserts
                .assertThat(sdDevController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isCorrectStatus(STATUS_SDDEV_ANALIZING);

//        var taskDetail = apiController.receiveTask(task.getNumber());
//        CommonAssert
//                .assertThat(taskDetail)
//                .isCorrectUdfList(UDF_SD_RESPONSIBLE_PARTY, RESPONSIBLE_PARTY_SUPPLIER.getId());
    }

    @Test(groups = {"ProcSdDev", "Regression"}, description = "Отклонить запрос", dependsOnMethods = "taskAnalyze")
    public void taskDecline() {
        apiController.updateToken(generateAuthToken(SUPPORT_MANAGER));
        task.refreshTask();
        udf = refreshUdf();
        task.refreshUdf(udf);
        sdDevController.performCommonOperation(task, DECLINE);
        ApiAsserts
                .assertThat(sdDevController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .assertTask()
                .isCorrectStatus(STATUS_SDDEV_DECLINED);
//        var taskDetail = apiController.receiveTask(task.getNumber());
//        CommonAssert
//                .assertThat(taskDetail)
//                .isCorrectUdfList(UDF_SD_RESPONSIBLE_PARTY, RESPONSIBLE_PARTY_CLIENT.getId());
    }

    @Test(groups = {"ProcSdDev", "Regression"}, description = "Вернуть в работу", dependsOnMethods = "taskDecline")
    public void taskReturn() {
        apiController.updateToken(generateAuthToken(CLIENT));
        task.refreshTask();
        udf = refreshUdf();
        task.setDescription(generateString());
        task.refreshUdf(udf);
        sdDevController.performCommonOperation(task, RETURN);
        ApiAsserts
                .assertThat(sdDevController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .assertTask()
                .isCorrectStatus(STATUS_SDDEV_ANALIZING);
//        var taskDetail = apiController.receiveTask(task.getNumber());
//        CommonAssert
//                .assertThat(taskDetail)
//                .isCorrectUdfList(UDF_SD_RESPONSIBLE_PARTY, RESPONSIBLE_PARTY_SUPPLIER.getId());
    }

    @Test(groups = {"ProcSdDev", "Regression"}, description = "Отклонить запрос", dependsOnMethods = "taskReturn")
    public void taskDecline2() {
        apiController.updateToken(generateAuthToken(SUPPORT_MANAGER));
        task.refreshTask();
        udf = refreshUdf();
        task.refreshUdf(udf);
        sdDevController.performCommonOperation(task, DECLINE);
        ApiAsserts
                .assertThat(sdDevController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .assertTask()
                .isCorrectStatus(STATUS_SDDEV_DECLINED);
//        var taskDetail = apiController.receiveTask(task.getNumber());
//        CommonAssert
//                .assertThat(taskDetail)
//                .isCorrectUdfList(UDF_SD_RESPONSIBLE_PARTY, RESPONSIBLE_PARTY_CLIENT.getId());
    }

    @Test(groups = {"ProcSdDev", "Regression"}, description = "Отменить отказ", dependsOnMethods = "taskDecline2")
    public void taskUndoDecline() {
        apiController.updateToken(generateAuthToken(SUPPORT_MANAGER));
        task.refreshTask();
        udf = refreshUdf();
        task.refreshUdf(udf);
        sdDevController.performCommonOperation(task, UNDODECLINE);
        ApiAsserts
                .assertThat(sdDevController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .assertTask()
                .isCorrectStatus(STATUS_SDDEV_ANALIZING);
//        var taskDetail = apiController.receiveTask(task.getNumber());
//        CommonAssert
//                .assertThat(taskDetail)
//                .isCorrectUdfList(UDF_SD_RESPONSIBLE_PARTY, RESPONSIBLE_PARTY_SUPPLIER.getId());
    }

    @Test(groups = {"ProcSdDev", "Regression"}, description = "Запросить информацию", dependsOnMethods = "taskUndoDecline")
    public void taskRequestInfo() {
        apiController.updateToken(generateAuthToken(SUPPORT_MANAGER));
        task.refreshTask();
        udf = refreshUdf();
        task.refreshUdf(udf);
        sdDevController.performCommonOperation(task, REQUESTINFO);
        ApiAsserts
                .assertThat(sdDevController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .assertTask()
                .isCorrectStatus(STATUS_SDDEV_WAITANALIZING);
//        var taskDetail = apiController.receiveTask(task.getNumber());
//        CommonAssert
//                .assertThat(taskDetail)
//                .isCorrectUdfList(UDF_SD_RESPONSIBLE_PARTY, RESPONSIBLE_PARTY_CLIENT.getId());
    }

    @Test(groups = {"ProcSdDev", "Regression"}, description = "Отменить запрос информации", dependsOnMethods = "taskRequestInfo")
    public void taskUndoRequestInfo() {
        apiController.updateToken(generateAuthToken(SUPPORT_MANAGER));
        task.refreshTask();
        udf = refreshUdf();
        task.refreshUdf(udf);
        sdDevController.performCommonOperation(task, UNDOREQUESTINFO);
        ApiAsserts
                .assertThat(sdDevController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .assertTask()
                .isCorrectStatus(STATUS_SDDEV_ANALIZING);
//        var taskDetail = apiController.receiveTask(task.getNumber());
//        CommonAssert
//                .assertThat(taskDetail)
//                .isCorrectUdfList(UDF_SD_RESPONSIBLE_PARTY, RESPONSIBLE_PARTY_SUPPLIER.getId());
    }

    @Test(groups = {"ProcSdDev", "Regression"}, description = "Запросить информацию", dependsOnMethods = "taskUndoRequestInfo")
    public void taskRequestInfo2() {
        apiController.updateToken(generateAuthToken(SUPPORT_MANAGER));
        task.refreshTask();
        udf = refreshUdf();
        task.refreshUdf(udf);
        sdDevController.performCommonOperation(task, REQUESTINFO);
        ApiAsserts
                .assertThat(sdDevController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .assertTask()
                .isCorrectStatus(STATUS_SDDEV_WAITANALIZING);
//        var taskDetail = apiController.receiveTask(task.getNumber());
//        CommonAssert
//                .assertThat(taskDetail)
//                .isCorrectUdfList(UDF_SD_RESPONSIBLE_PARTY, RESPONSIBLE_PARTY_CLIENT.getId());
    }

    @Test(groups = {"ProcSdDev", "Regression"}, description = "Предоставить информацию", dependsOnMethods = "taskRequestInfo2")
    public void taskProvideInfo() {
        apiController.updateToken(generateAuthToken(CLIENT));
        task.refreshTask();
        udf = refreshUdf();
        task.refreshUdf(udf);
        sdDevController.performCommonOperation(task, PROVIDEINFO);
        ApiAsserts
                .assertThat(sdDevController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .assertTask()
                .isCorrectStatus(STATUS_SDDEV_ANALIZING);
//        var taskDetail = apiController.receiveTask(task.getNumber());
//        CommonAssert
//                .assertThat(taskDetail)
//                .isCorrectUdfList(UDF_SD_RESPONSIBLE_PARTY, RESPONSIBLE_PARTY_SUPPLIER.getId());
    }

    @Test(groups = {"ProcSdDev", "Regression"}, description = "Передать на оценку", dependsOnMethods = "taskProvideInfo")
    public void taskBeginCost() {
        apiController.updateToken(generateAuthToken(SUPPORT_MANAGER));
        task.refreshTask();
        task.setHandlerUser(SUPPORT_MANAGER);
        udf = refreshUdf();
        udf.setUdfList(generateUdfList(UDF_SDDEV_CUSTOMTYPE, UDF_SDDEV_CUSTOMTYPE_REPLACE));
        udf.setUdfDouble(generateUdfDouble(UDF_SDDEV_BUDGET1, generateRandomNumberBetween(1, 100)));
        udf.setSecondUdfDouble(generateUdfDouble(UDF_SDDEV_BUDGET2, generateRandomNumberBetween(1, 100)));
        udf.setUdfUser(generateUdfUser(STDT_HANDLER, SUPPORT_MANAGER));
        task.refreshUdf(udf);
        sdDevController.performCommonOperation(task, BEGINCOST);
        ApiAsserts
                .assertThat(sdDevController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .assertTask()
                .isCorrectStatus(STATUS_SDDEV_CALCCOST);
//        var taskDetail = apiController.receiveTask(task.getNumber());
//        CommonAssert
//                .assertThat(taskDetail)
//                .isCorrectUdfList(UDF_SD_RESPONSIBLE_PARTY, RESPONSIBLE_PARTY_SUPPLIER.getId());
    }

    @Test(groups = {"ProcSdDev", "Regression"}, description = "Вернуть на анализ", dependsOnMethods = "taskBeginCost")
    public void taskReturnToAnal() {
        apiController.updateToken(generateAuthToken(SUPPORT_MANAGER));
        task.refreshTask();
        task.setHandlerUser(SUPPORT_MANAGER);
        udf = refreshUdf();
        udf.setUdfUser(generateUdfUser(STDT_HANDLER, SUPPORT_MANAGER));
        task.refreshUdf(udf);
        sdDevController.performCommonOperation(task, RETURN_TO_ANAL);
        ApiAsserts
                .assertThat(sdDevController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .assertTask()
                .isCorrectStatus(STATUS_SDDEV_ANALIZING);
//        var taskDetail = apiController.receiveTask(task.getNumber());
//        CommonAssert
//                .assertThat(taskDetail)
//                .isCorrectUdfList(UDF_SD_RESPONSIBLE_PARTY, RESPONSIBLE_PARTY_SUPPLIER.getId());
    }

    @Test(groups = {"ProcSdDev", "Regression"}, description = "Передать на оценку", dependsOnMethods = "taskReturnToAnal")
    public void taskBeginCost2() {
        apiController.updateToken(generateAuthToken(SUPPORT_MANAGER));
        task.refreshTask();
        task.setHandlerUser(SUPPORT_MANAGER);
        udf = refreshUdf();
        udf.setUdfList(generateUdfList(UDF_SDDEV_CUSTOMTYPE, UDF_SDDEV_CUSTOMTYPE_ADD));
        udf.setUdfDouble(generateUdfDouble(UDF_SDDEV_BUDGET1, generateRandomNumberBetween(1, 100)));
        udf.setSecondUdfDouble(generateUdfDouble(UDF_SDDEV_BUDGET2, generateRandomNumberBetween(1, 100)));
        udf.setUdfUser(generateUdfUser(STDT_HANDLER, SUPPORT_MANAGER));
        task.refreshUdf(udf);
        sdDevController.performCommonOperation(task, BEGINCOST);
        ApiAsserts
                .assertThat(sdDevController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .assertTask()
                .isCorrectStatus(STATUS_SDDEV_CALCCOST);
//        var taskDetail = apiController.receiveTask(task.getNumber());
//        CommonAssert
//                .assertThat(taskDetail)
//                .isCorrectUdfList(UDF_SD_RESPONSIBLE_PARTY, RESPONSIBLE_PARTY_SUPPLIER.getId());
    }

    @Test(groups = {"ProcSdDev", "Regression"}, description = "Сообщить условия учета кастомизации", dependsOnMethods = "taskBeginCost2")
    public void taskSendCost() {
        apiController.updateToken(generateAuthToken(SUPPORT_MANAGER));
        task.refreshTask();
        udf = refreshUdf();
        udf.setUdfList(generateUdfList(UDF_SDDEV_CUSTOMTYPE, UDF_SDDEV_CUSTOMTYPE_REPLACE));
        udf.setUdfDouble(generateUdfDouble(UDF_SDDEV_BUDGET1, generateRandomNumberBetween(1, 100)));
        udf.setSecondUdfDouble(generateUdfDouble(UDF_SDDEV_BUDGET2, generateRandomNumberBetween(1, 100)));
        udf.setUdfUser(generateUdfUser(STDT_HANDLER, SUPPORT_MANAGER));
        task.refreshUdf(udf);
        sdDevController.performCommonOperation(task, SENDCOST);
        ApiAsserts
                .assertThat(sdDevController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .assertTask()
                .isCorrectStatus(STATUS_SDDEV_WAITACCEPT);
//        var taskDetail = apiController.receiveTask(task.getNumber());
//        CommonAssert
//                .assertThat(taskDetail)
//                .isCorrectUdfList(UDF_SD_RESPONSIBLE_PARTY, RESPONSIBLE_PARTY_CLIENT.getId());
    }

    @Test(groups = {"ProcSdDev", "Regression"}, description = "Отменить сообщение условий", dependsOnMethods = "taskSendCost")
    public void taskUndoCost() {
        apiController.updateToken(generateAuthToken(SUPPORT_MANAGER));
        task.refreshTask();
        udf = refreshUdf();
        udf.setUdfList(generateUdfList(UDF_SDDEV_CUSTOMTYPE, UDF_SDDEV_CUSTOMTYPE_REPLACE));
        udf.setUdfDouble(generateUdfDouble(UDF_SDDEV_BUDGET1, generateRandomNumberBetween(1, 100)));
        udf.setSecondUdfDouble(generateUdfDouble(UDF_SDDEV_BUDGET2, generateRandomNumberBetween(1, 100)));
        udf.setUdfUser(generateUdfUser(STDT_HANDLER, SUPPORT_MANAGER));
        task.refreshUdf(udf);
        sdDevController.performCommonOperation(task, UNDOSENDCOST);
        ApiAsserts
                .assertThat(sdDevController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .assertTask()
                .isCorrectStatus(STATUS_SDDEV_CALCCOST);
        var taskDetail = apiController.receiveTask(task.getNumber());
        CommonAssert
                .assertThat(taskDetail)
                .isCorrectUdfList(UDF_SDDEV_CONDITIONSACCEPTED, CONDITIONS_ACCEPTED_NO.getId());
//                .isCorrectUdfList(UDF_SD_RESPONSIBLE_PARTY, RESPONSIBLE_PARTY_SUPPLIER.getId());
    }

    @Test(groups = {"ProcSdDev", "Regression"}, description = "Сообщить условия учета кастомизации", dependsOnMethods = "taskUndoCost")
    public void taskSendCost2() {
        apiController.updateToken(generateAuthToken(SUPPORT_MANAGER));
        task.refreshTask();
        udf = refreshUdf();
        udf.setUdfList(generateUdfList(UDF_SDDEV_CUSTOMTYPE, UDF_SDDEV_CUSTOMTYPE_ADD));
        udf.setUdfDouble(generateUdfDouble(UDF_SDDEV_BUDGET1, generateRandomNumberBetween(1, 100)));
        udf.setSecondUdfDouble(generateUdfDouble(UDF_SDDEV_BUDGET2, generateRandomNumberBetween(1, 100)));
        udf.setUdfUser(generateUdfUser(STDT_HANDLER, SUPPORT_MANAGER));
        task.refreshUdf(udf);
        sdDevController.performCommonOperation(task, SENDCOST);
        ApiAsserts
                .assertThat(sdDevController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .assertTask()
                .isCorrectStatus(STATUS_SDDEV_WAITACCEPT);
//        var taskDetail = apiController.receiveTask(task.getNumber());
//        CommonAssert
//                .assertThat(taskDetail)
//                .isCorrectUdfList(UDF_SD_RESPONSIBLE_PARTY, RESPONSIBLE_PARTY_CLIENT.getId());
    }

    @Test(groups = {"ProcSdDev", "Regression"}, description = "Предложить альтернативные условия", dependsOnMethods = "taskSendCost2")
    public void taskAlternateCost() {
        apiController.updateToken(generateAuthToken(CLIENT));
        task.refreshTask();
        udf = refreshUdf();
        task.refreshUdf(udf);
        sdDevController.performCommonOperation(task, ALTERNATECOST);
        ApiAsserts
                .assertThat(sdDevController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .assertTask()
                .isCorrectStatus(STATUS_SDDEV_CALCCOST);
//        var taskDetail = apiController.receiveTask(task.getNumber());
//        CommonAssert
//                .assertThat(taskDetail)
//                .isCorrectUdfList(UDF_SD_RESPONSIBLE_PARTY, RESPONSIBLE_PARTY_SUPPLIER.getId());
    }

    @Test(groups = {"ProcSdDev", "Regression"}, description = "Сообщить условия учета кастомизации", dependsOnMethods = "taskAlternateCost")
    public void taskSendCost3() {
        apiController.updateToken(generateAuthToken(SUPPORT_MANAGER));
        task.refreshTask();
        udf = refreshUdf();
        udf.setUdfList(generateUdfList(UDF_SDDEV_CUSTOMTYPE, UDF_SDDEV_CUSTOMTYPE_REPLACE));
        udf.setUdfDouble(generateUdfDouble(UDF_SDDEV_BUDGET1, generateRandomNumberBetween(1, 100)));
        udf.setSecondUdfDouble(generateUdfDouble(UDF_SDDEV_BUDGET2, generateRandomNumberBetween(1, 100)));
        udf.setUdfUser(generateUdfUser(STDT_HANDLER, SUPPORT_MANAGER));
        task.refreshUdf(udf);
        sdDevController.performCommonOperation(task, SENDCOST);
        ApiAsserts
                .assertThat(sdDevController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .assertTask()
                .isCorrectStatus(STATUS_SDDEV_WAITACCEPT);
//        var taskDetail = apiController.receiveTask(task.getNumber());
//        CommonAssert
//                .assertThat(taskDetail)
//                .isCorrectUdfList(UDF_SD_RESPONSIBLE_PARTY, RESPONSIBLE_PARTY_CLIENT.getId());
    }

    @Test(groups = {"ProcSdDev", "Regression"}, description = "Принять условия учета", dependsOnMethods = "taskSendCost3")
    public void taskAcceptCondition() {
        apiController.updateToken(generateAuthToken(CLIENT));
        task.refreshTask();
        udf = refreshUdf();
        task.refreshUdf(udf);
        sdDevController.performCommonOperation(task, ACCEPTCONDITIONS);
        ApiAsserts
                .assertThat(sdDevController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .assertTask()
                .isCorrectStatus(STATUS_SDDEV_ACCEPTED);
        var taskDetail = apiController.receiveTask(task.getNumber());
        CommonAssert
                .assertThat(taskDetail)
//                .isCorrectUdfList(UDF_SD_RESPONSIBLE_PARTY, RESPONSIBLE_PARTY_SUPPLIER.getId())
                .isCorrectUdfList(UDF_SDDEV_CONDITIONSACCEPTED, CONDITIONS_ACCEPTED_YES.getId());
    }

    @Test(groups = {"ProcSdDev", "Regression"}, description = "Начать работу", dependsOnMethods = "taskAcceptCondition")
    public void taskStart() {
        apiController.updateToken(generateAuthToken(SUPPORT_MANAGER));
        task.refreshTask();
        task.setHandlerUser(SUPPORT_MEMBER);
        udf = refreshUdf();
        udf.setUdfList(generateUdfList(UDF_SDDEV_DEVDECISION, UDF_SDDEV_DEVDECISION_GOOD));
        udf.setSecondUdfList(generateUdfList(UDF_SDDEV_GENUSE, SDDEV_GENUSE_GENERAL));
        udf.setUdfUser(generateUdfUser(STDT_HANDLER, SUPPORT_MEMBER));
        task.refreshUdf(udf);
        sdDevController.performCommonOperation(task, START);
        ApiAsserts
                .assertThat(sdDevController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .assertTask()
                .isCorrectStatus(STATUS_SDDEV_INWORK);
        var taskDetail = apiController.receiveTask(task.getNumber());
        CommonAssert
                .assertThat(taskDetail)
                .isCorrectUdfList(UDF_SD_RESPONSIBLE_PARTY, RESPONSIBLE_PARTY_SUPPLIER.getId());
    }

    @Test(groups = {"ProcSdDev", "Regression"}, description = "Запросить информацию", dependsOnMethods = "taskStart")
    public void taskRequestInfo3() {
        apiController.updateToken(generateAuthToken(SUPPORT_MEMBER));
        task.refreshTask();
        udf = refreshUdf();
        task.refreshUdf(udf);
        sdDevController.performCommonOperation(task, REQUESTINFO);
        ApiAsserts
                .assertThat(sdDevController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .assertTask()
                .isCorrectStatus(STATUS_SDDEV_WAITINWORK);
//        var taskDetail = apiController.receiveTask(task.getNumber());
//        CommonAssert
//                .assertThat(taskDetail)
//                .isCorrectUdfList(UDF_SD_RESPONSIBLE_PARTY, RESPONSIBLE_PARTY_CLIENT.getId());
    }

    @Test(groups = {"ProcSdDev", "Regression"}, description = "Предоставить информацию", dependsOnMethods = "taskRequestInfo3")
    public void taskProvideInfo2() {
        apiController.updateToken(generateAuthToken(CLIENT));
        task.refreshTask();
        udf = refreshUdf();
        task.refreshUdf(udf);
        sdDevController.performCommonOperation(task, PROVIDEINFO);
        ApiAsserts
                .assertThat(sdDevController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .assertTask()
                .isCorrectStatus(STATUS_SDDEV_INWORK);
//        var taskDetail = apiController.receiveTask(task.getNumber());
//        CommonAssert
//                .assertThat(taskDetail)
//                .isCorrectUdfList(UDF_SD_RESPONSIBLE_PARTY, RESPONSIBLE_PARTY_SUPPLIER.getId());
    }

    @Test(groups = {"ProcSdDev", "Regression"}, description = "Передать для включения в патч", dependsOnMethods = "taskProvideInfo2")
    public void taskReadyToPatch() {
        apiController.updateToken(generateAuthToken(SUPPORT_MEMBER));
        task.refreshTask();
        udf = refreshUdf();
        task.refreshUdf(udf);
        sdDevController.performCommonOperation(task, READYTOPATCH);
        ApiAsserts
                .assertThat(sdDevController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .assertTask()
                .isCorrectStatus(STATUS_SDDEV_TESTED);
//        var taskDetail = apiController.receiveTask(task.getNumber());
//        CommonAssert
//                .assertThat(taskDetail)
//                .isCorrectUdfList(UDF_SD_RESPONSIBLE_PARTY, RESPONSIBLE_PARTY_SUPPLIER.getId());
    }

    @Test(groups = {"ProcSdDev", "Regression"}, description = "Отменить включение в патч", dependsOnMethods = "taskReadyToPatch")
    public void taskUndoInPatch() {
        apiController.updateToken(generateAuthToken(SUPPORT_MEMBER));
        task.refreshTask();
        udf = refreshUdf();
        task.refreshUdf(udf);
        sdDevController.performCommonOperation(task, UNDOINPATCH);
        ApiAsserts
                .assertThat(sdDevController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .assertTask()
                .isCorrectStatus(STATUS_SDDEV_INWORK);
//        var taskDetail = apiController.receiveTask(task.getNumber());
//        CommonAssert
//                .assertThat(taskDetail)
//                .isCorrectUdfList(UDF_SD_RESPONSIBLE_PARTY, RESPONSIBLE_PARTY_SUPPLIER.getId());
    }

    @Test(groups = {"ProcSdDev", "Regression"}, description = "Передать для включения в патч", dependsOnMethods = "taskUndoInPatch")
    public void taskReadyToPatch2() {
        apiController.updateToken(generateAuthToken(SUPPORT_MEMBER));
        task.refreshTask();
        udf = refreshUdf();
        task.refreshUdf(udf);
        sdDevController.performCommonOperation(task, READYTOPATCH);
        ApiAsserts
                .assertThat(sdDevController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .assertTask()
                .isCorrectStatus(STATUS_SDDEV_TESTED);
//        var taskDetail = apiController.receiveTask(task.getNumber());
//        CommonAssert
//                .assertThat(taskDetail)
//                .isCorrectUdfList(UDF_SD_RESPONSIBLE_PARTY, RESPONSIBLE_PARTY_SUPPLIER.getId());
    }

    @Test(groups = {"ProcSdDev", "Regression"}, description = "Включить в патч", dependsOnMethods = "taskReadyToPatch2")
    public void taskInPatch() {
        apiController.updateToken(generateAuthToken(SUPPORT_MANAGER));
        task.refreshTask();
        udf = refreshUdf();
        patchNumber = generateName();
        udf.setUdfString(generateUdfString(UDF_SD_PATCHNUMBER, patchNumber));
        task.refreshUdf(udf);
        sdDevController.performCommonOperation(task, INPATCH);
        ApiAsserts
                .assertThat(sdDevController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .assertTask()
                .isCorrectStatus(STATUS_SDDEV_INPATCH);
        var taskDetail = apiController.receiveTask(task.getNumber());
        CommonAssert
                .assertThat(taskDetail)
//                .isCorrectUdfList(UDF_SD_RESPONSIBLE_PARTY, RESPONSIBLE_PARTY_SUPPLIER.getId())
                .isCorrectUdfString(UDF_SD_PATCHNUMBER, patchNumber);
    }

    @Test(groups = {"ProcSdDev", "Regression"}, description = "Отменить включение в патч", dependsOnMethods = "taskInPatch")
    public void taskUndoInPatch2() {
        apiController.updateToken(generateAuthToken(SUPPORT_MANAGER));
        task.refreshTask();
        udf = refreshUdf();
        task.refreshUdf(udf);
        sdDevController.performCommonOperation(task, UNDOINPATCH);
        ApiAsserts
                .assertThat(sdDevController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .assertTask()
                .isCorrectStatus(STATUS_SDDEV_INWORK);
//        var taskDetail = apiController.receiveTask(task.getNumber());
//        CommonAssert
//                .assertThat(taskDetail)
//                .isCorrectUdfList(UDF_SD_RESPONSIBLE_PARTY, RESPONSIBLE_PARTY_SUPPLIER.getId());
    }

    @Test(groups = {"ProcSdDev", "Regression"}, description = "Передать для включения в патч", dependsOnMethods = "taskUndoInPatch2")
    public void taskReadyToPatch3() {
        apiController.updateToken(generateAuthToken(SUPPORT_MANAGER));
        task.refreshTask();
        udf = refreshUdf();
        task.refreshUdf(udf);
        sdDevController.performCommonOperation(task, READYTOPATCH);
        ApiAsserts
                .assertThat(sdDevController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .assertTask()
                .isCorrectStatus(STATUS_SDDEV_TESTED);
//        var taskDetail = apiController.receiveTask(task.getNumber());
//        CommonAssert
//                .assertThat(taskDetail)
//                .isCorrectUdfList(UDF_SD_RESPONSIBLE_PARTY, RESPONSIBLE_PARTY_SUPPLIER.getId());
    }

    @Test(groups = {"ProcSdDev", "Regression"}, description = "Включить в патч", dependsOnMethods = "taskReadyToPatch3")
    public void taskInPatch2() {
        apiController.updateToken(generateAuthToken(SUPPORT_MANAGER));
        task.refreshTask();
        udf = refreshUdf();
        patchNumber = generateName();
        udf.setUdfString(generateUdfString(UDF_SD_PATCHNUMBER, patchNumber));
        task.refreshUdf(udf);
        sdDevController.performCommonOperation(task, INPATCH);
        ApiAsserts
                .assertThat(sdDevController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .assertTask()
                .isCorrectStatus(STATUS_SDDEV_INPATCH);
        var taskDetail = apiController.receiveTask(task.getNumber());
        CommonAssert
                .assertThat(taskDetail)
//                .isCorrectUdfList(UDF_SD_RESPONSIBLE_PARTY, RESPONSIBLE_PARTY_SUPPLIER.getId())
                .isCorrectUdfString(UDF_SD_PATCHNUMBER, patchNumber);
    }

    @Test(groups = {"ProcSdDev", "Regression"}, description = "Запросить информацию", dependsOnMethods = "taskInPatch2")
    public void taskRequestInfo4() {
        apiController.updateToken(generateAuthToken(SUPPORT_MANAGER));
        task.refreshTask();
        udf = refreshUdf();
        task.refreshUdf(udf);
        sdDevController.performCommonOperation(task, REQUESTINFO);
        ApiAsserts
                .assertThat(sdDevController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .assertTask()
                .isCorrectStatus(STATUS_SDDEV_WAITINPATCH);
//        var taskDetail = apiController.receiveTask(task.getNumber());
//        CommonAssert
//                .assertThat(taskDetail)
//                .isCorrectUdfList(UDF_SD_RESPONSIBLE_PARTY, RESPONSIBLE_PARTY_CLIENT.getId());
    }

    @Test(groups = {"ProcSdDev", "Regression"}, description = "Предоставить информацию", dependsOnMethods = "taskRequestInfo4")
    public void taskProvideInfo3() {
        apiController.updateToken(generateAuthToken(CLIENT));
        task.refreshTask();
        udf = refreshUdf();
        task.refreshUdf(udf);
        sdDevController.performCommonOperation(task, PROVIDEINFO);
        ApiAsserts
                .assertThat(sdDevController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .assertTask()
                .isCorrectStatus(STATUS_SDDEV_INPATCH);
//        var taskDetail = apiController.receiveTask(task.getNumber());
//        CommonAssert
//                .assertThat(taskDetail)
//                .isCorrectUdfList(UDF_SD_RESPONSIBLE_PARTY, RESPONSIBLE_PARTY_SUPPLIER.getId());
    }

    @Test(groups = {"ProcSdDev", "Regression"}, description = "Отправить патч", dependsOnMethods = "taskProvideInfo3")
    public void taskSendPatch() {
        apiController.updateToken(generateAuthToken(SUPPORT_MANAGER));
        task.refreshTask();
        udf = refreshUdf();
        task.refreshUdf(udf);
        sdDevController.performCommonOperation(task, SENDPATCH);
        ApiAsserts
                .assertThat(sdDevController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .assertTask()
                .isCorrectStatus(STATUS_SDDEV_PATCHSEND);
//        var taskDetail = apiController.receiveTask(task.getNumber());
//        CommonAssert
//                .assertThat(taskDetail)
//                .isCorrectUdfList(UDF_SD_RESPONSIBLE_PARTY, RESPONSIBLE_PARTY_SUPPLIER.getId());
    }

    @Test(groups = {"ProcSdDev", "Regression"}, description = "Отменить включение в патч", dependsOnMethods = "taskSendPatch")
    public void taskUndoInPatch3() {
        apiController.updateToken(generateAuthToken(SUPPORT_MANAGER));
        task.refreshTask();
        udf = refreshUdf();
        task.refreshUdf(udf);
        sdDevController.performCommonOperation(task, UNDOSEND);
        ApiAsserts
                .assertThat(sdDevController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .assertTask()
                .isCorrectStatus(STATUS_SDDEV_INPATCH);
//        var taskDetail = apiController.receiveTask(task.getNumber());
//        CommonAssert
//                .assertThat(taskDetail)
//                .isCorrectUdfList(UDF_SD_RESPONSIBLE_PARTY, RESPONSIBLE_PARTY_SUPPLIER.getId());
    }

    @Test(groups = {"ProcSdDev", "Regression"}, description = "Отправить патч", dependsOnMethods = "taskUndoInPatch3")
    public void taskSendPatch2() {
        apiController.updateToken(generateAuthToken(SUPPORT_MANAGER));
        task.refreshTask();
        udf = refreshUdf();
        task.refreshUdf(udf);
        sdDevController.performCommonOperation(task, SENDPATCH);
        ApiAsserts
                .assertThat(sdDevController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .assertTask()
                .isCorrectStatus(STATUS_SDDEV_PATCHSEND);
//        var taskDetail = apiController.receiveTask(task.getNumber());
//        CommonAssert
//                .assertThat(taskDetail)
//                .isCorrectUdfList(UDF_SD_RESPONSIBLE_PARTY, RESPONSIBLE_PARTY_SUPPLIER.getId());
    }

    @Test(groups = {"ProcSdDev", "Regression"}, description = "Запросить информацию", dependsOnMethods = "taskSendPatch2")
    public void taskRequestInfo5() {
        apiController.updateToken(generateAuthToken(SUPPORT_MANAGER));
        task.refreshTask();
        udf = refreshUdf();
        task.refreshUdf(udf);
        sdDevController.performCommonOperation(task, REQUESTINFO);
        ApiAsserts
                .assertThat(sdDevController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .assertTask()
                .isCorrectStatus(STATUS_SDDEV_WAITPATCHSEND);
//        var taskDetail = apiController.receiveTask(task.getNumber());
//        CommonAssert
//                .assertThat(taskDetail)
//                .isCorrectUdfList(UDF_SD_RESPONSIBLE_PARTY, RESPONSIBLE_PARTY_CLIENT.getId());
    }

    @Test(groups = {"ProcSdDev", "Regression"}, description = "Предоставить информацию", dependsOnMethods = "taskRequestInfo5")
    public void taskProvideInfo4() {
        apiController.updateToken(generateAuthToken(CLIENT));
        task.refreshTask();
        udf = refreshUdf();
        task.refreshUdf(udf);
        sdDevController.performCommonOperation(task, PROVIDEINFO);
        ApiAsserts
                .assertThat(sdDevController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .assertTask()
                .isCorrectStatus(STATUS_SDDEV_PATCHSEND);
//        var taskDetail = apiController.receiveTask(task.getNumber());
//        CommonAssert
//                .assertThat(taskDetail)
//                .isCorrectUdfList(UDF_SD_RESPONSIBLE_PARTY, RESPONSIBLE_PARTY_SUPPLIER.getId());
    }

    @Test(groups = {"ProcSdDev", "Regression"}, description = "Закрыть", dependsOnMethods = "taskProvideInfo4")
    public void taskClose() {
        apiController.updateToken(generateAuthToken(CLIENT));
        task.refreshTask();
        task.setResolution(generateResolution(Resolutions.REQUEST_SOLVED));
        udf = refreshUdf();
        task.refreshUdf(udf);
        sdDevController.performCommonOperation(task, CLOSE);
        ApiAsserts
                .assertThat(sdDevController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .assertTask()
                .isCorrectStatus(STATUS_SDDEV_CLOSED);
//        var taskDetail = apiController.receiveTask(task.getNumber());
//        CommonAssert
//                .assertThat(taskDetail)
//                .isCorrectUdfList(UDF_SD_RESPONSIBLE_PARTY, RESPONSIBLE_PARTY_SUPPLIER.getId());
    }

    @Test(groups = {"ProcSdDev", "Regression"}, description = "Отменить закрытие", dependsOnMethods = "taskClose")
    public void taskUndoClose() {
        apiController.updateToken(generateAuthToken(SUPPORT_MANAGER));
        task.refreshTask();
        udf = refreshUdf();
        udf.setUdfUser(generateEmptyUdfUser(STDT_HANDLER));
        task.refreshUdf(udf);
        sdDevController.performCommonOperation(task, UNDO_CLOSE);
        ApiAsserts
                .assertThat(sdDevController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .assertTask()
                .isCorrectStatus(STATUS_SDDEV_NEW);
        var taskDetail = apiController.receiveTask(task.getNumber());
        CommonAssert
                .assertThat(taskDetail)
                .isCorrectField("Резолюция", "Запрос решён", "resolutionName");
    }

    @Test(groups = {"ProcSdDev", "Regression"}, description = "Снять запрос", dependsOnMethods = "taskUndoClose")
    public void taskRemoveRequest() {
        apiController.updateToken(generateAuthToken(SUPPORT_MANAGER));
        task.refreshTask();
        udf = refreshUdf();
        task.refreshUdf(udf);
        sdDevController.performCommonOperation(task, REMOVE_REQUEST);
        ApiAsserts
                .assertThat(sdDevController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .assertTask()
                .isCorrectStatus(STATUS_SDDEV_CLOSED);
        var taskDetail = apiController.receiveTask(task.getNumber());
        CommonAssert
                .assertThat(taskDetail)
//                .isCorrectUdfList(UDF_SD_RESPONSIBLE_PARTY, RESPONSIBLE_PARTY_SUPPLIER.getId())
                .isCorrectField("Резолюция", "Запрос решён", "resolutionName");
    }
}
