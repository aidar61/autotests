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

public class SlaHelpBaseDynamicTest extends BaseIntegrationTest {

    public SlaHelpController slaHelpController;
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

    @Test(groups = {"ProcSdDev", "Regression"}, description = "создание")
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

//        var taskDetail = apiController.receiveTask(task.getNumber());
//        CommonAssert
//                .assertThat(taskDetail)
//                .isCorrectUdfList(UDF_SDDEV_CONDITIONSACCEPTED, CONDITIONS_ACCEPTED_NO.getId())
//                .isCorrectUdfList(UDF_SD_RESPONSIBLE_PARTY, RESPONSIBLE_PARTY_SUPPLIER.getId());
    }

    @Test(groups = {"ProcSdDev", "Regression"}, description = "Принять на анализ", dependsOnMethods = "createTask")
    public void taskAnalyze() {
        apiController.updateToken(generateAuthToken(SUPPORT_MANAGER));
        task.refreshTask();
        udf = refreshUdf();
        task.setHandlerUser(SUPPORT_MANAGER);
        task.setDescription(generateString());
        udf.setUdfUser(generateUdfUser(STDT_HANDLER, SUPPORT_MANAGER));
        task.refreshUdf(udf);
        slaHelpController.performCommonOperation(task, ANALIZE);
        ApiAsserts
                .assertThat(slaHelpController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isCorrectStatus(STATUS_SLAHELP_ANALIZING);

//        var taskDetail = apiController.receiveTask(task.getNumber());
//        CommonAssert
//                .assertThat(taskDetail)
//                .isCorrectUdfList(UDF_SD_RESPONSIBLE_PARTY, RESPONSIBLE_PARTY_SUPPLIER.getId());
    }

    @Test(groups = {"ProcSdDev", "Regression"}, description = "Запросить информацию", dependsOnMethods = "taskAnalyze")
    public void taskRequestInfo() {
        apiController.updateToken(generateAuthToken(SUPPORT_MANAGER));
        task.refreshTask();
        udf = refreshUdf();
        task.refreshUdf(udf);
        slaHelpController.performCommonOperation(task, REQUESTINFO);
        ApiAsserts
                .assertThat(slaHelpController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .assertTask()
                .isCorrectStatus(STATUS_SLAHELP_WAITANALIZING);
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
        slaHelpController.performCommonOperation(task, UNDOREQUESTINFO);
        ApiAsserts
                .assertThat(slaHelpController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .assertTask()
                .isCorrectStatus(STATUS_SLAHELP_ANALIZING);
//        var taskDetail = apiController.receiveTask(task.getNumber());
//        CommonAssert
//                .assertThat(taskDetail)
//                .isCorrectUdfList(UDF_SD_RESPONSIBLE_PARTY, RESPONSIBLE_PARTY_SUPPLIER.getId());
    }

    @Test(groups = {"ProcSdDev", "Regression"}, description = "Запросить информацию", dependsOnMethods = "taskUndoRequestInfo")
    public void taskRequestInfo1() {
        apiController.updateToken(generateAuthToken(SUPPORT_MANAGER));
        task.refreshTask();
        udf = refreshUdf();
        task.refreshUdf(udf);
        slaHelpController.performCommonOperation(task, REQUESTINFO);
        ApiAsserts
                .assertThat(slaHelpController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .assertTask()
                .isCorrectStatus(STATUS_SLAHELP_WAITANALIZING);
//        var taskDetail = apiController.receiveTask(task.getNumber());
//        CommonAssert
//                .assertThat(taskDetail)
//                .isCorrectUdfList(UDF_SD_RESPONSIBLE_PARTY, RESPONSIBLE_PARTY_CLIENT.getId());
    }

    @Test(groups = {"ProcSdDev", "Regression"}, description = "Предоставить информацию", dependsOnMethods = "taskRequestInfo1")
    public void taskProvideInfo() {
        apiController.updateToken(generateAuthToken(CLIENT));
        task.refreshTask();
        udf = refreshUdf();
        task.refreshUdf(udf);
        slaHelpController.performCommonOperation(task, PROVIDEINFO);
        ApiAsserts
                .assertThat(slaHelpController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .assertTask()
                .isCorrectStatus(STATUS_SLAHELP_ANALIZING);
//        var taskDetail = apiController.receiveTask(task.getNumber());
//        CommonAssert
//                .assertThat(taskDetail)
//                .isCorrectUdfList(UDF_SD_RESPONSIBLE_PARTY, RESPONSIBLE_PARTY_SUPPLIER.getId());
    }

    @Test(groups = {"ProcSdDev", "Regression"}, description = "Оказать консультацию", dependsOnMethods = "taskProvideInfo")
    public void taskConsult() {
        apiController.updateToken(generateAuthToken(SUPPORT_MANAGER));
        task.refreshTask();
        udf = refreshUdf();
        task.refreshUdf(udf);
        slaHelpController.performCommonOperation(task, CONSULT);
        ApiAsserts
                .assertThat(slaHelpController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .assertTask()
                .isCorrectStatus(STATUS_SLAHELP_CONSULTED);
//        var taskDetail = apiController.receiveTask(task.getNumber());
//        CommonAssert
//                .assertThat(taskDetail)
//                .isCorrectUdfList(UDF_SD_RESPONSIBLE_PARTY, RESPONSIBLE_PARTY_CLIENT.getId());
    }

    @Test(groups = {"ProcSdDev", "Regression"}, description = "Вернуть в работу", dependsOnMethods = "taskConsult")
    public void taskReturn() {
        apiController.updateToken(generateAuthToken(CLIENT));
        task.refreshTask();
        udf = refreshUdf();
        task.setDescription(generateString());
        task.refreshUdf(udf);
        slaHelpController.performCommonOperation(task, RETURN);
        ApiAsserts
                .assertThat(slaHelpController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .assertTask()
                .isCorrectStatus(STATUS_SLAHELP_ANALIZING);
//        var taskDetail = apiController.receiveTask(task.getNumber());
//        CommonAssert
//                .assertThat(taskDetail)
//                .isCorrectUdfList(UDF_SD_RESPONSIBLE_PARTY, RESPONSIBLE_PARTY_SUPPLIER.getId());
    }

    @Test(groups = {"ProcSdDev", "Regression"}, description = "Оказать консультацию", dependsOnMethods = "taskReturn")
    public void taskConsult2() {
        apiController.updateToken(generateAuthToken(SUPPORT_MANAGER));
        task.refreshTask();
        udf = refreshUdf();
        task.refreshUdf(udf);
        slaHelpController.performCommonOperation(task, CONSULT);
        ApiAsserts
                .assertThat(slaHelpController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .assertTask()
                .isCorrectStatus(STATUS_SLAHELP_CONSULTED);
//        var taskDetail = apiController.receiveTask(task.getNumber());
//        CommonAssert
//                .assertThat(taskDetail)
//                .isCorrectUdfList(UDF_SD_RESPONSIBLE_PARTY, RESPONSIBLE_PARTY_CLIENT.getId());
    }

    @Test(groups = {"ProcSdDev", "Regression"}, description = "Закрыть", dependsOnMethods = "taskConsult2")
    public void taskClose() {
        apiController.updateToken(generateAuthToken(CLIENT));
        task.refreshTask();
        udf = refreshUdf();

        udf.setUdfList(generateUdfList(UDF_EVALUATING_REQUEST_EXECUTION, FIVE));
        udf.setUdfMemo(generateUdfMemo(UDF_EVALUATING_REQUEST_COMMENT, generateString()));
        task.refreshUdf(udf);
        slaHelpController.performCommonOperation(task, CLOSE);
        ApiAsserts
                .assertThat(slaHelpController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .assertTask()
                .isCorrectStatus(STATUS_SLAHELP_CLOSED);
//        var taskDetail = apiController.receiveTask(task.getNumber());
//        CommonAssert
//                .assertThat(taskDetail)
//                .isCorrectUdfList(UDF_SD_RESPONSIBLE_PARTY, RESPONSIBLE_PARTY_SUPPLIER.getId());
    }

//    @Test(groups = {"ProcSdDev", "Regression"}, description = "Отменить закрытие", dependsOnMethods = "taskClose")
//    public void taskUndoClose() {
//        apiController.updateToken(generateAuthToken(SUPPORT_MANAGER));
//        task.refreshTask();
//        udf = refreshUdf();
//        udf.setUdfUser(generateEmptyUdfUser(STDT_HANDLER));
//        task.refreshUdf(udf);
//        slaHelpController.performCommonOperation(task, UNDO_CLOSE);
//        ApiAsserts
//                .assertThat(slaHelpController.getResponse())
//                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
//                .assertTask()
//                .isCorrectStatus(STATUS_SLAHELP_NEW);
//        var taskDetail = apiController.receiveTask(task.getNumber());
//        CommonAssert
//                .assertThat(taskDetail)
//                .isCorrectField("Резолюция", "Запрос решён", "resolutionName");
//    }
}
