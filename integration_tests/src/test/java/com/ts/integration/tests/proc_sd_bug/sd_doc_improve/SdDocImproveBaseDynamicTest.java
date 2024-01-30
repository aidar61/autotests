package com.ts.integration.tests.proc_sd_bug.sd_doc_improve;

import com.ts.common.application.controllers.TrackStudioHttpStatusCodes;
import com.ts.common.application.database.dbEntities.GrTaskDbEntity;
import com.ts.common.application.database.dbTables.GrTaskTable;
import com.ts.common.asserts.ApiAsserts;
import com.ts.common.asserts.CommonAssert;
import com.ts.common.controllers.TaskResponseBody;
import com.ts.common.controllers.sdbug.SdDocImproveController;
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
import static com.ts.common.entitites.commonEntities.Task.Constants.SERVICE_DESK;
import static com.ts.common.entitites.commonEntities.Udfs.UdfSd.*;
import static com.ts.common.enums.Operations.*;
import static com.ts.common.enums.TaskStatuses.*;
import static com.ts.common.enums.TaskStatuses.STATUS_SDBUG_INWORK;
import static com.ts.common.utils.InitEntities.*;
import static com.ts.common.utils.RandomUtils.*;

public class SdDocImproveBaseDynamicTest extends BaseIntegrationTest {
    public SdDocImproveController sdDocImproveController;
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
        sdDocImproveController = apiController.getSdDocImproveController();
        userController = apiController.getUserController();
        GrTaskTable grTaskTable = dbHelper.getGrTaskTable();
        GrTaskDbEntity parentTaskFromDb = (GrTaskDbEntity) grTaskTable.receiveByTaskNumber("1328786");
        parent = InitEntities.generateParent(parentTaskFromDb.getTask_id(), parentTaskFromDb.getTask_number());
        task = InitEntities.getGeneralTask(TaskType.SD_DOC_IMPROVE, Operations.CAT);

        var parentPayloadResponse = sdDocImproveController.getParentPayload(parent.getNumber(), "CAT_SDDOCIMPROVE");
        ApiAsserts.assertThat(parentPayloadResponse).isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK);
        var parentPayload = JsonUtils.removeExtraCharacters(parentPayloadResponse);
        BDKU_CONFIGURATION = sdDocImproveController.getParent_UDF_BDKU_CONFIGURATION(parentPayload);

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
        sdDocImproveController.create(task);
        var response = sdDocImproveController.getResponse();
        ApiAsserts
                .assertThat(response)
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isCorrectStatus(STATUS_SDBUG_NEW)
                .isEquals(task);
        var taskDetail = apiController.receiveTask(task.getNumber());
        CommonAssert
                .assertThat(taskDetail)
                .isCorrectUdfList(UDF_SD_RESPONSIBLE_PARTY, RESPONSIBLE_PARTY_SUPPLIER.getId());
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
        sdDocImproveController.performCommonOperation(task, ANALIZE);
        ApiAsserts
                .assertThat(sdDocImproveController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isCorrectStatus(STATUS_SDBUG_ANALIZING);
        var taskDetail = apiController.receiveTask(task.getNumber());
        CommonAssert
                .assertThat(taskDetail)
                .isCorrectUdfList(UDF_SD_RESPONSIBLE_PARTY, RESPONSIBLE_PARTY_SUPPLIER.getId());
    }

    @Test(groups = {"PROC_SDBUG", "Regression"}, description = "Отклонить", dependsOnMethods = "taskAnalyze")
    public void taskDecline() {
        apiController.updateToken(generateAuthToken(SUPPORT_MEMBER));
        task.refreshTask();
        udf = refreshUdf();
        task.refreshUdf(udf);
        sdDocImproveController.performCommonOperation(task, DECLINE);
        ApiAsserts
                .assertThat(sdDocImproveController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .assertTask()
                .isCorrectStatus(STATUS_SDBUG_DECLINED);
        var taskDetail = apiController.receiveTask(task.getNumber());
        CommonAssert
                .assertThat(taskDetail)
                .isCorrectUdfList(UDF_SD_RESPONSIBLE_PARTY, RESPONSIBLE_PARTY_CLIENT.getId());
    }

    @Test(groups = {"PROC_SDBUG", "Regression"}, description = "Вернуть в работу", dependsOnMethods = "taskDecline")
    public void taskReturn() {
        apiController.updateToken(generateAuthToken(CLIENT));
        task.refreshTask();
        udf = refreshUdf();
        task.setDescription(generateString());
        task.setHandlerUser(SUPPORT_MEMBER);
        udf.setUdfUser(generateUdfUser(STDT_HANDLER, SUPPORT_MEMBER));
        task.refreshUdf(udf);
        sdDocImproveController.performCommonOperation(task, RETURN);
        ApiAsserts
                .assertThat(sdDocImproveController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .assertTask()
                .isCorrectStatus(STATUS_SDBUG_ANALIZING);
        var taskDetail = apiController.receiveTask(task.getNumber());
        CommonAssert
                .assertThat(taskDetail)
                .isCorrectUdfList(UDF_SD_RESPONSIBLE_PARTY, RESPONSIBLE_PARTY_SUPPLIER.getId());
    }

    @Test(groups = {"PROC_SDBUG", "Regression"}, description = "Начать работу", dependsOnMethods = "taskReturn")
    public void taskStart() {
        apiController.updateToken(generateAuthToken(SUPPORT_MEMBER));
        task.refreshTask();
        udf = refreshUdf();
        task.refreshUdf(udf);
        sdDocImproveController.performCommonOperation(task, START);
        ApiAsserts
                .assertThat(sdDocImproveController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .assertTask()
                .isCorrectStatus(STATUS_SDBUG_INWORK);
        var taskDetail = apiController.receiveTask(task.getNumber());
        CommonAssert
                .assertThat(taskDetail)
                .isCorrectUdfList(UDF_SD_RESPONSIBLE_PARTY, RESPONSIBLE_PARTY_SUPPLIER.getId());
    }

    @Test(groups = {"PROC_SDBUG", "Regression"}, description = "Закрыть без патча", dependsOnMethods = "taskStart")
    public void taskCloseWoPatch() {
        apiController.updateToken(generateAuthToken(SUPPORT_MEMBER));
        task.refreshTask();
        udf = refreshUdf();
        task.refreshUdf(udf);
        sdDocImproveController.performCommonOperation(task, CLOSE_WO_PATCH);
        ApiAsserts
                .assertThat(sdDocImproveController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .assertTask()
                .isCorrectStatus(STATUS_SDBUG_CLOSED);
        var taskDetail = apiController.receiveTask(task.getNumber());
        CommonAssert
                .assertThat(taskDetail)
                .isCorrectUdfList(UDF_SD_RESPONSIBLE_PARTY, RESPONSIBLE_PARTY_NOBODY.getId());
    }

    @Test(groups = {"PROC_SDBUG", "Regression"}, description = "Отменить закрытие", dependsOnMethods = "taskCloseWoPatch")
    public void taskUndoClose() {
        apiController.updateToken(generateAuthToken(SUPPORT_MEMBER));
        task.refreshTask();
        udf = refreshUdf();

        task.setHandlerUser(SUPPORT_MEMBER);
        udf.setUdfUser(generateUdfUser(STDT_HANDLER, SUPPORT_MEMBER));

        task.refreshUdf(udf);
        sdDocImproveController.performCommonOperation(task, UNDO_CLOSE);
        ApiAsserts
                .assertThat(sdDocImproveController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .assertTask()
                .isCorrectStatus(STATUS_SDBUG_ANALIZING);
        var taskDetail = apiController.receiveTask(task.getNumber());
        CommonAssert
                .assertThat(taskDetail)
                .isCorrectUdfList(UDF_SD_RESPONSIBLE_PARTY, RESPONSIBLE_PARTY_SUPPLIER.getId());
    }

    @Test(groups = {"PROC_SDBUG", "Regression"}, description = "Начать работу", dependsOnMethods = "taskUndoClose")
    public void taskStart1() {
        apiController.updateToken(generateAuthToken(SUPPORT_MEMBER));
        task.refreshTask();
        udf = refreshUdf();
        task.refreshUdf(udf);
        sdDocImproveController.performCommonOperation(task, START);
        ApiAsserts
                .assertThat(sdDocImproveController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .assertTask()
                .isCorrectStatus(STATUS_SDBUG_INWORK);
        var taskDetail = apiController.receiveTask(task.getNumber());
        CommonAssert
                .assertThat(taskDetail)
                .isCorrectUdfList(UDF_SD_RESPONSIBLE_PARTY, RESPONSIBLE_PARTY_SUPPLIER.getId());
    }

    @Test(groups = {"PROC_SDBUG", "Regression"}, description = "Вернуть на анализ", dependsOnMethods = "taskStart1")
    public void taskUndoStart() {
        apiController.updateToken(generateAuthToken(SUPPORT_MEMBER));
        task.refreshTask();
        udf = refreshUdf();

        task.setHandlerUser(SUPPORT_MEMBER);
        udf.setUdfUser(generateUdfUser(STDT_HANDLER, SUPPORT_MEMBER));

        task.refreshUdf(udf);
        sdDocImproveController.performCommonOperation(task, UNDOSTART);
        ApiAsserts
                .assertThat(sdDocImproveController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .assertTask()
                .isCorrectStatus(STATUS_SDBUG_ANALIZING);
        var taskDetail = apiController.receiveTask(task.getNumber());
        CommonAssert
                .assertThat(taskDetail)
                .isCorrectUdfList(UDF_SD_RESPONSIBLE_PARTY, RESPONSIBLE_PARTY_SUPPLIER.getId());
    }

    @Test(groups = {"PROC_SDBUG", "Regression"}, description = "Начать работу", dependsOnMethods = "taskUndoStart")
    public void taskStart2() {
        apiController.updateToken(generateAuthToken(SUPPORT_MEMBER));
        task.refreshTask();
        udf = refreshUdf();
        task.refreshUdf(udf);
        sdDocImproveController.performCommonOperation(task, START);
        ApiAsserts
                .assertThat(sdDocImproveController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .assertTask()
                .isCorrectStatus(STATUS_SDBUG_INWORK);
        var taskDetail = apiController.receiveTask(task.getNumber());
        CommonAssert
                .assertThat(taskDetail)
                .isCorrectUdfList(UDF_SD_RESPONSIBLE_PARTY, RESPONSIBLE_PARTY_SUPPLIER.getId());
    }

    @Test(groups = {"PROC_SDBUG", "Regression"}, description = "Запросить информацию", dependsOnMethods = "taskStart2")
    public void taskRequestInfo() {
        apiController.updateToken(generateAuthToken(SUPPORT_MEMBER));
        task.refreshTask();
        udf = refreshUdf();
        task.refreshUdf(udf);
        sdDocImproveController.performCommonOperation(task, REQUESTINFO);
        ApiAsserts
                .assertThat(sdDocImproveController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .assertTask()
                .isCorrectStatus(STATUS_SDBUG_WAITINWORK);
        var taskDetail = apiController.receiveTask(task.getNumber());
        CommonAssert
                .assertThat(taskDetail)
                .isCorrectUdfList(UDF_SD_RESPONSIBLE_PARTY, RESPONSIBLE_PARTY_CLIENT.getId());
    }

    @Test(groups = {"PROC_SDBUG", "Regression"}, description = "Предоставить информацию", dependsOnMethods = "taskRequestInfo")
    public void taskProvideInfo() {
        apiController.updateToken(generateAuthToken(CLIENT));
        task.refreshTask();
        udf = refreshUdf();
        task.refreshUdf(udf);
        sdDocImproveController.performCommonOperation(task, PROVIDEINFO);
        ApiAsserts
                .assertThat(sdDocImproveController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .assertTask()
                .isCorrectStatus(STATUS_SDBUG_INWORK);
        var taskDetail = apiController.receiveTask(task.getNumber());
        CommonAssert
                .assertThat(taskDetail)
                .isCorrectUdfList(UDF_SD_RESPONSIBLE_PARTY, RESPONSIBLE_PARTY_SUPPLIER.getId());
    }

    @Test(groups = {"PROC_SDBUG", "Regression"}, description = "Предоставить решение", dependsOnMethods = "taskProvideInfo")
    public void taskHotFix() {
        apiController.updateToken(generateAuthToken(SUPPORT_MEMBER));
        task.refreshTask();
        udf = refreshUdf();
        udf.setUdfList(generateUdfList(UDF_SDFEATURE_DOCREVISION, DOC_REVISION_YES));
        task.refreshUdf(udf);
        sdDocImproveController.performCommonOperation(task, HOTFIX);
        ApiAsserts
                .assertThat(sdDocImproveController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .assertTask()
                .isCorrectStatus(STATUS_SDBUG_FIXED);
        var taskDetail = apiController.receiveTask(task.getNumber());
        CommonAssert
                .assertThat(taskDetail)
                .isCorrectUdfList(UDF_SD_RESPONSIBLE_PARTY, RESPONSIBLE_PARTY_CLIENT.getId());
    }

    @Test(groups = {"PROC_SDBUG", "Regression"}, description = "Подтвердить исправление", dependsOnMethods = "taskHotFix")
    public void taskAcceptHotfix() {
        apiController.updateToken(generateAuthToken(CLIENT));
        task.refreshTask();
        udf = refreshUdf();
        task.refreshUdf(udf);
        sdDocImproveController.performCommonOperation(task, ACCEPTHOTFIX);
        ApiAsserts
                .assertThat(sdDocImproveController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .assertTask()
                .isCorrectStatus(STATUS_SDBUG_INWORK);
        var taskDetail = apiController.receiveTask(task.getNumber());
        CommonAssert
                .assertThat(taskDetail)
                .isCorrectUdfList(UDF_SD_RESPONSIBLE_PARTY, RESPONSIBLE_PARTY_SUPPLIER.getId());
    }

    @Test(groups = {"PROC_SDBUG", "Regression"}, description = "Включить в патч", dependsOnMethods = "taskAcceptHotfix")
    public void taskInPatch() {
        apiController.updateToken(generateAuthToken(SUPPORT_MANAGER));
        task.refreshTask();
        udf = refreshUdf();
        udf.setUdfString(generateUdfString(UDF_SD_PATCHNUMBER, generateComment()));
        task.refreshUdf(udf);
        sdDocImproveController.performCommonOperation(task, INPATCH);
        ApiAsserts
                .assertThat(sdDocImproveController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .assertTask()
                .isCorrectStatus(STATUS_SDBUG_INPATCH);
    }

    @Test(groups = {"PROC_SDBUG", "Regression"}, description = "Отменить включение в патч", dependsOnMethods = "taskInPatch")
    public void taskUndoInPatch() {
        apiController.updateToken(generateAuthToken(SUPPORT_MANAGER));
        task.refreshTask();
        udf = refreshUdf();
        task.refreshUdf(udf);
        sdDocImproveController.performCommonOperation(task, UNDOINPATCH);
        ApiAsserts
                .assertThat(sdDocImproveController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .assertTask()
                .isCorrectStatus(STATUS_SDBUG_INWORK);
    }

    @Test(groups = {"PROC_SDBUG", "Regression"}, description = "Включить в патч", dependsOnMethods = "taskUndoInPatch")
    public void taskInPatch2() {
        apiController.updateToken(generateAuthToken(SUPPORT_MANAGER));
        task.refreshTask();
        udf = refreshUdf();
        udf.setUdfString(generateUdfString(UDF_SD_PATCHNUMBER, generateComment()));
        task.refreshUdf(udf);
        sdDocImproveController.performCommonOperation(task, INPATCH);
        ApiAsserts
                .assertThat(sdDocImproveController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .assertTask()
                .isCorrectStatus(STATUS_SDBUG_INPATCH);
    }

    @Test(groups = {"PROC_SDBUG", "Regression"}, description = "Отправить патч", dependsOnMethods = "taskInPatch2")
    public void taskSendPatch() {
        apiController.updateToken(generateAuthToken(SUPPORT_MANAGER));
        task.refreshTask();
        udf = refreshUdf();
        udf.setUdfList(generateUdfList(UDF_SDFEATURE_DOCREVISION, DOC_REVISION_YES));
        task.refreshUdf(udf);
        sdDocImproveController.performCommonOperation(task, SENDPATCH);
        ApiAsserts
                .assertThat(sdDocImproveController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .assertTask()
                .isCorrectStatus(STATUS_SDBUG_PATCHSEND);
    }

    @Test(groups = {"PROC_SDBUG", "Regression"}, description = "Отменить отправку патча", dependsOnMethods = "taskSendPatch")
    public void taskUndoSendPatch() {
        apiController.updateToken(generateAuthToken(SUPPORT_MANAGER));
        task.refreshTask();
        udf = refreshUdf();
        task.refreshUdf(udf);
        sdDocImproveController.performCommonOperation(task, UNDOSENDPATCH);
        ApiAsserts
                .assertThat(sdDocImproveController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .assertTask()
                .isCorrectStatus(STATUS_SDBUG_INPATCH);
    }

    @Test(groups = {"PROC_SDBUG", "Regression"}, description = "Отправить патч", dependsOnMethods = "taskUndoSendPatch")
    public void taskSendPatch2() {
        apiController.updateToken(generateAuthToken(SUPPORT_MANAGER));
        task.refreshTask();
        udf = refreshUdf();
        udf.setUdfList(generateUdfList(UDF_SDFEATURE_DOCREVISION, DOC_REVISION_YES));
        task.refreshUdf(udf);
        sdDocImproveController.performCommonOperation(task, SENDPATCH);
        ApiAsserts
                .assertThat(sdDocImproveController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .assertTask()
                .isCorrectStatus(STATUS_SDBUG_PATCHSEND);
    }

    @Test(groups = {"PROC_SDBUG", "Regression"}, description = "Закрыть", dependsOnMethods = "taskSendPatch2")
    public void taskClose() {
        apiController.updateToken(generateAuthToken(CLIENT));
        task.refreshTask();
        task.setResolution(generateResolution(Resolutions.REQUEST_RESOLVED));
        udf = refreshUdf();
        task.refreshUdf(udf);
        sdDocImproveController.performCommonOperation(task, CLOSE);
        ApiAsserts
                .assertThat(sdDocImproveController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .assertTask()
                .isCorrectStatus(STATUS_SDBUG_CLOSED);
        var taskDetail = apiController.receiveTask(task.getNumber());
        CommonAssert
                .assertThat(taskDetail)
                .isCorrectUdfList(UDF_SD_RESPONSIBLE_PARTY, RESPONSIBLE_PARTY_NOBODY.getId());
    }
}
