package com.ts.integration.tests.proc_sd_bug.sd_improve;

import com.ts.common.application.controllers.TrackStudioHttpStatusCodes;
import com.ts.common.application.database.dbEntities.GrTaskDbEntity;
import com.ts.common.application.database.dbTables.GrTaskTable;
import com.ts.common.asserts.ApiAsserts;
import com.ts.common.controllers.TaskResponseBody;
import com.ts.common.controllers.sdbug.SdImproveController;
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

import static com.ts.common.entitites.commonEntities.List.Constants.DOC_REVISION_YES;
import static com.ts.common.entitites.commonEntities.List.Constants.SDBUG_PRIORITYBUG_CRITICAL;
import static com.ts.common.entitites.commonEntities.Task.Constants.CORE;
import static com.ts.common.entitites.commonEntities.Udfs.UdfSd.*;
import static com.ts.common.enums.Operations.*;
import static com.ts.common.enums.TaskStatuses.*;
import static com.ts.common.utils.InitEntities.*;
import static com.ts.common.utils.RandomUtils.*;

public class SdImporveBaseDynamicTest extends BaseIntegrationTest {

    public SdImproveController sdBugController;
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
        sdBugController = apiController.getSdImproveController();
        userController = apiController.getUserController();
        GrTaskTable grTaskTable = dbHelper.getGrTaskTable();
        GrTaskDbEntity parentTaskFromDb = (GrTaskDbEntity) grTaskTable.receiveByTaskNumber("1328786");
        parent = InitEntities.generateParent(parentTaskFromDb.getTask_id(), parentTaskFromDb.getTask_number());
        task = InitEntities.getGeneralTask(TaskType.SD_IMPROVE, Operations.CAT);

        var parentPayloadResponse = sdBugController.getParentPayload(parent.getNumber(), "CAT_SDIMPROVE");
        ApiAsserts.assertThat(parentPayloadResponse).isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK);
        var parentPayload = JsonUtils.removeExtraCharacters(parentPayloadResponse);
        BDKU_CONFIGURATION = sdBugController.getParent_UDF_BDKU_CONFIGURATION(parentPayload);

        USER_ROLES = userController.receiveUserByTask(parent.getNumber());
        CLIENT = userController.receiveUserByRole(USER_ROLES, "Клиент", "root").getForUser();
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
        ApiAsserts.assertThat(response).isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK).isParseableBody(TaskResponseBody.class).assertTask().isCorrectStatus(STATUS_SDBUG_NEW).isEquals(task);
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
        ApiAsserts.assertThat(sdBugController.getResponse()).isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK).isParseableBody(TaskResponseBody.class).assertTask().isCorrectStatus(STATUS_SDBUG_ANALIZING);
    }

    @Test(groups = {"PROC_SDBUG", "Regression"}, description = "Отклонить", dependsOnMethods = "taskAnalyze")
    public void taskDecline() {
        apiController.updateToken(generateAuthToken(SUPPORT_MEMBER));
        task.refreshTask();
        udf = refreshUdf();
        task.refreshUdf(udf);
        sdBugController.performCommonOperation(task, DECLINE);
        ApiAsserts.assertThat(sdBugController.getResponse()).isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK).assertTask().isCorrectStatus(STATUS_SDBUG_DECLINED);
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
        sdBugController.performCommonOperation(task, RETURN);
        ApiAsserts.assertThat(sdBugController.getResponse()).isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK).assertTask().isCorrectStatus(STATUS_SDBUG_ANALIZING);
        ;
    }

    @Test(groups = {"PROC_SDBUG", "Regression"}, description = "Начать работу", dependsOnMethods = "taskReturn")
    public void taskStart() {
        apiController.updateToken(generateAuthToken(SUPPORT_MEMBER));
        task.refreshTask();
        udf = refreshUdf();
        task.refreshUdf(udf);
        sdBugController.performCommonOperation(task, START);
        ApiAsserts.assertThat(sdBugController.getResponse()).isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK).assertTask().isCorrectStatus(STATUS_SDBUG_INWORK);

    }

    @Test(groups = {"PROC_SDBUG", "Regression"}, description = "Закрыть без патча", dependsOnMethods = "taskStart")
    public void taskCloseWoPatch() {
        apiController.updateToken(generateAuthToken(SUPPORT_MEMBER));
        task.refreshTask();
        udf = refreshUdf();
        task.refreshUdf(udf);
        sdBugController.performCommonOperation(task, CLOSE_WO_PATCH);
        ApiAsserts.assertThat(sdBugController.getResponse()).isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK).assertTask().isCorrectStatus(STATUS_SDBUG_CLOSED);
    }

    @Test(groups = {"PROC_SDBUG", "Regression"}, description = "Отменить закрытие", dependsOnMethods = "taskCloseWoPatch")
    public void taskUndoClose() {
        apiController.updateToken(generateAuthToken(SUPPORT_MEMBER));
        task.refreshTask();
        udf = refreshUdf();

        task.setHandlerUser(SUPPORT_MEMBER);
        udf.setUdfUser(generateUdfUser(STDT_HANDLER, SUPPORT_MEMBER));

        task.refreshUdf(udf);
        sdBugController.performCommonOperation(task, UNDO_CLOSE);
        ApiAsserts.assertThat(sdBugController.getResponse()).isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK).assertTask().isCorrectStatus(STATUS_SDBUG_ANALIZING);
    }

    @Test(groups = {"PROC_SDBUG", "Regression"}, description = "Начать работу", dependsOnMethods = "taskUndoClose")
    public void taskStart1() {
        apiController.updateToken(generateAuthToken(SUPPORT_MEMBER));
        task.refreshTask();
        udf = refreshUdf();
        task.refreshUdf(udf);
        sdBugController.performCommonOperation(task, START);
        ApiAsserts.assertThat(sdBugController.getResponse()).isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK).assertTask().isCorrectStatus(STATUS_SDBUG_INWORK);
    }

    @Test(groups = {"PROC_SDBUG", "Regression"}, description = "Вернуть на анализ", dependsOnMethods = "taskStart1")
    public void taskUndoStart() {
        apiController.updateToken(generateAuthToken(SUPPORT_MEMBER));
        task.refreshTask();
        udf = refreshUdf();

        task.setHandlerUser(SUPPORT_MEMBER);
        udf.setUdfUser(generateUdfUser(STDT_HANDLER, SUPPORT_MEMBER));

        task.refreshUdf(udf);
        sdBugController.performCommonOperation(task, UNDOSTART);
        ApiAsserts.assertThat(sdBugController.getResponse()).isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK).assertTask().isCorrectStatus(STATUS_SDBUG_ANALIZING);
        ;
    }

    @Test(groups = {"PROC_SDBUG", "Regression"}, description = "Начать работу", dependsOnMethods = "taskUndoStart")
    public void taskStart2() {
        apiController.updateToken(generateAuthToken(SUPPORT_MEMBER));
        task.refreshTask();
        udf = refreshUdf();
        task.refreshUdf(udf);
        sdBugController.performCommonOperation(task, START);
        ApiAsserts.assertThat(sdBugController.getResponse()).isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK).assertTask().isCorrectStatus(STATUS_SDBUG_INWORK);
        ;
    }

    @Test(groups = {"PROC_SDBUG", "Regression"}, description = "Запросить информацию", dependsOnMethods = "taskStart2")
    public void taskRequestInfo() {
        apiController.updateToken(generateAuthToken(SUPPORT_MEMBER));
        task.refreshTask();
        udf = refreshUdf();
        task.refreshUdf(udf);
        sdBugController.performCommonOperation(task, REQUESTINFO);
        ApiAsserts.assertThat(sdBugController.getResponse()).isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK).assertTask().isCorrectStatus(STATUS_SDBUG_WAITINWORK);
        ;
    }

    @Test(groups = {"PROC_SDBUG", "Regression"}, description = "Предоставить информацию", dependsOnMethods = "taskRequestInfo")
    public void taskProvideInfo() {
        apiController.updateToken(generateAuthToken(CLIENT));
        task.refreshTask();
        udf = refreshUdf();
        task.refreshUdf(udf);
        sdBugController.performCommonOperation(task, PROVIDEINFO);
        ApiAsserts.assertThat(sdBugController.getResponse()).isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK).assertTask().isCorrectStatus(STATUS_SDBUG_INWORK);
    }


    @Test(groups = {"PROC_SDBUG", "Regression"}, description = "Предоставить решение", dependsOnMethods = "taskProvideInfo")
    public void taskHotFix() {
        apiController.updateToken(generateAuthToken(SUPPORT_MEMBER));
        task.refreshTask();
        udf = refreshUdf();
        udf.setUdfList(generateUdfList(UDF_SDFEATURE_DOCREVISION, DOC_REVISION_YES));
        task.refreshUdf(udf);
        sdBugController.performCommonOperation(task, HOTFIX);
        ApiAsserts
                .assertThat(sdBugController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .assertTask()
                .isCorrectStatus(STATUS_SDBUG_FIXED);
    }

    @Test(groups = {"PROC_SDBUG", "Regression"}, description = "Подтвердить исправление", dependsOnMethods = "taskHotFix")
    public void taskAcceptHotfix() {
        apiController.updateToken(generateAuthToken(CLIENT));
        task.refreshTask();
        udf = refreshUdf();
        task.refreshUdf(udf);
        sdBugController.performCommonOperation(task, ACCEPTHOTFIX);
        ApiAsserts.assertThat(sdBugController.getResponse()).isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK).assertTask().isCorrectStatus(STATUS_SDBUG_INWORK);
    }


    @Test(groups = {"PROC_SDBUG", "Regression"}, description = "Включить в патч", dependsOnMethods = "taskAcceptHotfix")
    public void taskInPatch() {
        apiController.updateToken(generateAuthToken(SUPPORT_MANAGER));
        task.refreshTask();
        udf = refreshUdf();
        udf.setUdfString(generateUdfString(UDF_SD_PATCHNUMBER, generateComment()));
        task.refreshUdf(udf);
        sdBugController.performCommonOperation(task, INPATCH);
        ApiAsserts.assertThat(sdBugController.getResponse()).isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK).assertTask().isCorrectStatus(STATUS_SDBUG_INPATCH);
    }

    @Test(groups = {"PROC_SDBUG", "Regression"}, description = "Отменить включение в патч", dependsOnMethods = "taskInPatch")
    public void taskUndoInPatch() {
        apiController.updateToken(generateAuthToken(SUPPORT_MANAGER));
        task.refreshTask();
        udf = refreshUdf();
        task.refreshUdf(udf);
        sdBugController.performCommonOperation(task, UNDOINPATCH);
        ApiAsserts.assertThat(sdBugController.getResponse()).isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK).assertTask().isCorrectStatus(STATUS_SDBUG_INWORK);
    }

    @Test(groups = {"PROC_SDBUG", "Regression"}, description = "Отправить патч", dependsOnMethods = "taskUndoInPatch")
    public void taskSendPatch() {
        apiController.updateToken(generateAuthToken(SUPPORT_MANAGER));
        task.refreshTask();
        udf = refreshUdf();
        udf.setUdfList(generateUdfList(UDF_SDFEATURE_DOCREVISION, DOC_REVISION_YES));
        task.refreshUdf(udf);
        sdBugController.performCommonOperation(task, SENDPATCH);
        ApiAsserts.assertThat(sdBugController.getResponse()).isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK).assertTask().isCorrectStatus(STATUS_SDBUG_PATCHSEND);
    }

    @Test(groups = {"PROC_SDBUG", "Regression"}, description = "Закрыть", dependsOnMethods = "taskSendPatch")
    public void taskClose() {
        apiController.updateToken(generateAuthToken(CLIENT));
        task.refreshTask();
        task.setResolution(generateResolution(Resolutions.REQUEST_RESOLVED));
        udf = refreshUdf();
        task.refreshUdf(udf);
        sdBugController.performCommonOperation(task, CLOSE);
        ApiAsserts
                .assertThat(sdBugController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .assertTask()
                .isCorrectStatus(STATUS_SDBUG_CLOSED);
    }
}
