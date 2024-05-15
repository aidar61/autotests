package com.ts.integration.tests.proc_sla_bug;

import com.ts.common.application.controllers.TrackStudioHttpStatusCodes;
import com.ts.common.application.database.dbEntities.GrTaskDbEntity;
import com.ts.common.application.database.dbTables.GrTaskTable;
import com.ts.common.asserts.ApiAsserts;
import com.ts.common.asserts.CommonAssert;
import com.ts.common.controllers.TaskResponseBody;
import com.ts.common.controllers.sla.SlaBugController;
import com.ts.common.entitites.commonEntities.Parent;
import com.ts.common.entitites.commonEntities.Role;
import com.ts.common.entitites.commonEntities.User;
import com.ts.common.entitites.commonEntities.UserRole;
import com.ts.common.entitites.tasks.GeneralTask;
import com.ts.common.enums.Operations;
import com.ts.common.enums.Tables;
import com.ts.common.enums.TaskType;
import com.ts.common.enums.Users;
import com.ts.common.utils.DateUtils;
import com.ts.common.utils.InitEntities;
import com.ts.common.utils.WaitManager;
import com.ts.integration.tests.BaseIntegrationTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;

import static com.ts.common.entitites.commonEntities.List.Constants.*;
import static com.ts.common.entitites.commonEntities.Task.Constants.AKKREDITIVES;
import static com.ts.common.entitites.commonEntities.Task.Constants.MTBANK;
import static com.ts.common.entitites.commonEntities.Udfs.UdfSd.*;
import static com.ts.common.enums.Operations.*;
import static com.ts.common.enums.TaskStatuses.*;
import static com.ts.common.utils.InitEntities.*;
import static com.ts.common.utils.InitEntities.generateUdfMemo;
import static com.ts.common.utils.RandomUtils.*;
import static com.ts.common.utils.RandomUtils.generateString;

public class SlaBugNoTempSolutionTest extends BaseIntegrationTest {
    private SlaBugController slaBugController;
    private GeneralTask task;
    private Parent parent;
    private User CLIENT;
    private User CLIENT_MANAGER;
    private User EMPLOYEE;
    private User EMPLOYEE_WATCHER;
    private List<UserRole> USER_ROLES;
    private final String parentTaskNumber = "928684";

    @BeforeClass(alwaysRun = true)
    public void beforeClass() {
        apiController.updateToken(InitEntities.generateAuthToken(Users.ROOT));
        slaBugController = apiController.getSlaBugController();
        userController = apiController.getUserController();
        baseController = apiController.getBaseController();

        GrTaskTable grTaskTable = dbHelper.getGrTaskTable();
        GrTaskDbEntity grTaskDbEntity = (GrTaskDbEntity) grTaskTable.receiveByTaskNumber(parentTaskNumber);
        parent = InitEntities.generateParent(grTaskDbEntity.getTask_id(), grTaskDbEntity.getTask_number());

        USER_ROLES = userController.receiveUserByTask(parentTaskNumber);

        CLIENT = userController.receiveUserByRole(USER_ROLES, Role.Constants.CLIENT, "root").getForUser();
//        CLIENT = InitEntities.generateUser("818181df7b322025017b33fa24720f8a", "edyro@mtbank.by", "Дыро Елизавета Игоревна");
        CLIENT_MANAGER = userController.receiveUserByRole(USER_ROLES, Role.Constants.CLIENT_MANAGER, "root").getForUser();
//        CLIENT_MANAGER = InitEntities.generateUser("818181b03c3a3f18013c3d38d5190138", "vvolskiy", "Вольский Валерий");
        EMPLOYEE = userController.receiveUserByRole(USER_ROLES, Role.Constants.EMPLOYEE, "root").getForUser();
//        EMPLOYEE = InitEntities.generateUser("818181df795d7d1b01796b30780247fa", "mefimov", "Ефимов Михаил");
        EMPLOYEE_WATCHER = userController.receiveUserByRole(USER_ROLES, Role.Constants.EMPLOYEE, EMPLOYEE.getLogin()).getForUser();
//        EMPLOYEE_WATCHER = InitEntities.generateUser("8a8181df75b956e50175d653f6693b37", "aizotov", "Изотов Алексей");
        task = InitEntities.getGeneralTask(TaskType.SLA_BUG, Operations.CAT);
    }

    @BeforeMethod(alwaysRun = true)
    public void beforeMethod() {
        WaitManager.pause(5);
    }

    @Test(groups = {"PROC_SLABUG", "Regression"}, description = "Создание CAT_SLABUG")
    public void slaBugCat() {
        apiController.updateToken(InitEntities.generateAuthToken(CLIENT));
        udf = refreshUdf();
        task.refreshTask();

        task.setName(generateName());
        task.setParent(parent);
        task.setDescription(Tables.SLA_BUG.getTable());

        udf.setUdfMultiList(generateUdfMultiList(UDF_SD_RELATED_TASK_CODES, ABNATTR));
        udf.setUdfTask(generateUdfTask(UDF_SD_MODULE, AKKREDITIVES));
        udf.setUdfList(generateUdfList(UDF_SDBUG_PRIORITYBUG, SDBUG_PRIORITYBUG_CRITICAL_AFTER_UPDATE));
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
    @Test(groups = {"PROC_SLABUG", "Regression"}, description = "Запросить информацию", dependsOnMethods = "msgAnalyze")
    public void requestInfo() {
        apiController.updateToken(generateAuthToken(CLIENT_MANAGER));
        udf = refreshUdf();
        task.refreshTask();

        slaBugController.performCommonOperation(task, REQUESTINFO);
        ApiAsserts.assertThat(slaBugController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isCorrectStatus(STATUS_SLABUG_WAITING);

        apiController.receiveTask(task.getNumber());
        CommonAssert.assertThat(apiController.getResponse())
                .isCorrectUdfList(UDF_SD_RESPONSIBLE_PARTY, RESPONSIBLE_PARTY_CLIENT)
                .isCorrectUdfList(UDF_ROLE_CURRENT, CLIENT_ROLE_CURRENT);
    }
    @Test(groups = {"PROC_SLABUG", "Regression"}, description = "Предоставить решение", dependsOnMethods = "requestInfo")
    public void hotFix() {
        apiController.updateToken(generateAuthToken(CLIENT_MANAGER));
        udf = refreshUdf();
        task.refreshTask();

        String expectedTempProvideDate = DateUtils.getCurrentDateTimeStamp();

        udf.setUdfList(generateUdfList(UDF_SDFEATURE_DOCREVISION, DOC_REVISION_NO));
        udf.setUdfString(generateUdfString(UDF_SLABUG_PERMPROVIDEDATE, expectedTempProvideDate));
        task.refreshUdf(udf);

        slaBugController.performCommonOperation(task, HOTFIX);
        ApiAsserts.assertThat(slaBugController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isCorrectStatus(STATUS_SLABUG_FIXED);

        apiController.receiveTask(task.getNumber());
        CommonAssert.assertThat(apiController.getResponse())
                .isCorrectUdfList(UDF_SD_RESPONSIBLE_PARTY, RESPONSIBLE_PARTY_CLIENT);
//                .isCorrectUdfString(UDF_SLABUG_PERMPROVIDEDATE, expectedTempProvideDate);
    }
    @Test(groups = {"PROC_SLABUG", "Regression"}, description = "Ошибка устранена", dependsOnMethods = "hotFix")
    public void acceptSolution() {
        apiController.updateToken(generateAuthToken(CLIENT));
        udf = refreshUdf();
        task.refreshTask();

        udf.setUdfList(generateUdfList(UDF_EVALUATING_REQUEST_EXECUTION, FIVE));
        udf.setUdfString(generateUdfString(UDF_EVALUATING_REQUEST_COMMENT, generateString()));
        task.refreshUdf(udf);

        slaBugController.performCommonOperation(task, ACCEPTSOLUTION);
        ApiAsserts.assertThat(slaBugController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isCorrectStatus(STATUS_SLABUG_CLOSED);

        apiController.receiveTask(task.getNumber());
        CommonAssert.assertThat(apiController.getResponse())
                .isCorrectUdfList(UDF_SD_RESPONSIBLE_PARTY, RESPONSIBLE_PARTY_SUPPLIER);
    }
}
