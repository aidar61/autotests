package com.ts.integration.tests.proc_sla_bug;

import com.ts.common.application.controllers.TrackStudioHttpStatusCodes;
import com.ts.common.application.database.dbEntities.GrTaskDbEntity;
import com.ts.common.application.database.dbTables.GrTaskTable;
import com.ts.common.asserts.ApiAsserts;
import com.ts.common.asserts.CommonAssert;
import com.ts.common.controllers.TaskResponseBody;
import com.ts.common.controllers.sla.SlaBugController;
import com.ts.common.entitites.commonEntities.*;
import com.ts.common.entitites.commonEntities.udf.UdfString;
import com.ts.common.entitites.tasks.GeneralTask;
import com.ts.common.enums.Operations;
import com.ts.common.enums.Tables;
import com.ts.common.enums.TaskType;
import com.ts.common.enums.Users;
import com.ts.common.utils.InitEntities;
import com.ts.common.utils.RandomUtils;
import com.ts.integration.tests.BaseIntegrationTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.List;

import static com.ts.common.entitites.commonEntities.List.Constants.*;
import static com.ts.common.entitites.commonEntities.Task.Constants.AKKREDITIVES;
import static com.ts.common.entitites.commonEntities.Task.Constants.MTBANK;
import static com.ts.common.entitites.commonEntities.Udfs.UdfSd.*;
import static com.ts.common.enums.Operations.ANALIZE;
import static com.ts.common.enums.TaskStatuses.STATUS_SLABUG_NEW;
import static com.ts.common.utils.InitEntities.*;
import static com.ts.common.utils.RandomUtils.*;

public class SlaBugBaseDynamicTest extends BaseIntegrationTest {
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
        GrTaskTable grTaskTable = dbHelper.getGrTaskTable();
        GrTaskDbEntity grTaskDbEntity = (GrTaskDbEntity) grTaskTable.receiveByTaskNumber(parentTaskNumber);
        parent = InitEntities.generateParent(grTaskDbEntity.getTask_id(), grTaskDbEntity.getTask_number());
        USER_ROLES = userController.receiveUserByTask(parentTaskNumber);

        CLIENT = userController.receiveUserByRole(USER_ROLES, Role.Constants.CLIENT, "root").getForUser();
        CLIENT_MANAGER = userController.receiveUserByRole(USER_ROLES, Role.Constants.CLIENT_MANAGER, "root").getForUser();
        EMPLOYEE = userController.receiveUserByRole(USER_ROLES, Role.Constants.EMPLOYEE, "root").getForUser();
        EMPLOYEE_WATCHER = userController.receiveUserByRole(USER_ROLES, Role.Constants.EMPLOYEE, EMPLOYEE.getLogin()).getForUser();

        task = InitEntities.getGeneralTask(TaskType.SLA_BUG, Operations.CAT);
    }

    @Test(groups = {"SlaBug", "Regression"}, description = "создание")
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
        udf.setSecondUdfList(generateUdfList(UDF_SD_REMOTEACCESS, REMOTE_ACCESS));
        udf.setUdfMemo(generateUdfMemo(UDF_SD_REMOTEACCESSINFO, generateString()));

        task.refreshUdf(udf);
        slaBugController.createSlaBugTask(task);
        ApiAsserts.assertThat(slaBugController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isCorrectStatus(STATUS_SLABUG_NEW);
    }

    @Test(groups = {"SlaBug", "Regression"}, description = "принять на анализ")
    public void msgAnalyze() {
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
                .isParseableBody(TaskResponseBody.class);
    }

}
