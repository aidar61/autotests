package com.ts.integration.tests.proc_sla_feature;

import com.ts.common.application.database.dbEntities.GrTaskDbEntity;
import com.ts.common.application.database.dbTables.GrTaskTable;
import com.ts.common.asserts.ApiAsserts;
import com.ts.common.asserts.CommonAssert;
import com.ts.common.controllers.TaskResponseBody;
import com.ts.common.controllers.sla.SlaFeatureController;
import com.ts.common.entitites.commonEntities.Parent;
import com.ts.common.entitites.commonEntities.User;
import com.ts.common.entitites.commonEntities.UserRole;
import com.ts.common.entitites.tasks.GeneralTask;
import com.ts.common.enums.Tables;
import com.ts.common.utils.InitEntities;
import com.ts.integration.tests.BaseIntegrationTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.List;

import static com.ts.common.application.controllers.TrackStudioHttpStatusCodes.HTTP_OK;
import static com.ts.common.entitites.commonEntities.List.Constants.*;
import static com.ts.common.entitites.commonEntities.Task.Constants.MTBANK;
import static com.ts.common.entitites.commonEntities.Task.Constants.NOTIFICATION_SERVICE2;
import static com.ts.common.entitites.commonEntities.Udfs.UdfSd.*;
import static com.ts.common.enums.Operations.CAT;
import static com.ts.common.enums.Operations.TOPRECOST;
import static com.ts.common.enums.TaskStatuses.STATUS_SLAFEATURE_NEW;
import static com.ts.common.enums.TaskStatuses.STATUS_SLAFEATURE_PRECOST;
import static com.ts.common.enums.TaskType.SLA_FEATURE;
import static com.ts.common.utils.InitEntities.*;
import static com.ts.common.utils.RandomUtils.*;

public class SlaFeatureBase2Test extends BaseIntegrationTest {
    private SlaFeatureController slaFeatureController;
    private GeneralTask task;
    private Parent parent;
    private User CLIENT;
    private User ANALYTIC;
    private User ACCOUNT_MANAGER;
    private User MANAGER_ANALYZE_FEATURE;
    private User MANAGER_REQUEST;
    private User MANAGER_CLIENT;
    private List<UserRole> USER_ROLES;
    private final String parentTaskNumber = "928666";

    @BeforeClass(alwaysRun = true)
    public void beforeCLass() {
        slaFeatureController = apiController.getSlaFeatureController();
        userController = apiController.getUserController();
        baseController = apiController.getBaseController();

        GrTaskTable grTaskTable = dbHelper.getGrTaskTable();
        GrTaskDbEntity grTaskDbEntity = (GrTaskDbEntity) grTaskTable.receiveByTaskNumber(parentTaskNumber);
        parent = InitEntities.generateParent(grTaskDbEntity.getTask_id(), grTaskDbEntity.getTask_number());

        USER_ROLES = userController.receiveUserByTask(parentTaskNumber);

        CLIENT = userController.receiveUserByLogin(USER_ROLES, "vvoskobovich@mtb.minsk.by");
        ANALYTIC = userController.receiveUserByLogin(USER_ROLES, "azubov");
        ACCOUNT_MANAGER = userController.receiveUserByLogin(USER_ROLES, "vvolskiy");
        MANAGER_ANALYZE_FEATURE = userController.receiveUserByLogin(USER_ROLES, "nsolovey");
        MANAGER_REQUEST = userController.receiveUserByLogin(USER_ROLES, "ibabushkin");
        MANAGER_CLIENT = userController.receiveUserByLogin(USER_ROLES, "lkorennaya");

        task = InitEntities.getGeneralTask(SLA_FEATURE, CAT);
    }

    @Test(groups = {"SlaFeature", "Regression"}
            , description = "Создать Запрос на доработку ЛПО (new) (КЛИЕНТ)")
    void cat() {
        apiController.updateToken(generateAuthToken(CLIENT));
        udf = refreshUdf();
        task.refreshTask();

        task.setName(generateName());
        task.setParent(parent);
        task.setDescription(Tables.SLA_FEATURE.getTable());

        udf.setUdfList(generateUdfList(UDF_SD_TASK_CODE, ABNATTR));
        udf.setUdfMultiList(generateUdfMultiList(UDF_SD_RELATED_TASK_CODES, ABNATTR));
        udf.setUdfTask(generateUdfTask(UDF_SD_MODULE, NOTIFICATION_SERVICE2));
        udf.setSecondUdfList(generateUdfList(UDF_SDFEATURE_TYPE, OWN));
        udf.setUdfString(generateUdfString(UDF_SDFEATURE_LEGALREQ_DOC, generateString()));
        udf.setSecondUdfString(generateUdfString(UDF_SD_CLIENTWATCHERS, generateEmail()));
        udf.setSecondUdfTask(generateUdfTask(UDF_BDKU_CONFIGURATION, MTBANK));
        udf.setThirdUdfString(generateUdfString(UDF_SD_REMOTEID, generateString()));
        udf.setFourthUdfString(generateUdfString(UDF_SD_INITPERSON, generateString()));
        task.refreshUdf(udf);

        slaFeatureController.createSlaFeatureTask(task);
        ApiAsserts.assertThat(slaFeatureController.getResponse())
                .isCorrectResponseCode(HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isCorrectStatus(STATUS_SLAFEATURE_NEW);

        apiController.receiveTask(task.getNumber());
        CommonAssert.assertThat(apiController.getResponse())
                .isCorrectUdfList(UDF_SDFEATURE_STAGE, STAGE_AWAIT)
                .isCorrectUdfList(UDF_SD_RESPONSIBLE_PARTY, RESPONSIBLE_PARTY_NOBODY);
    }

    @Test(groups = {"SlaFeature", "Regression"}
            , description = "Начать предварительную оценку (ACCOUNT-MANAGER)"
            , dependsOnMethods = "cat")
    void toprecost() {
        apiController.updateToken(ACCOUNT_MANAGER);
        udf = refreshUdf();
        task.refreshTask();

        task.setHandlerUser(ANALYTIC);
        udf.setUdfString(generateUdfString(UDF_SDFEATURE_OVERLIMITREASON, generateString()));
        udf.setUdfUser(generateUdfUser(STDT_HANDLER, ANALYTIC));
        task.refreshUdf(udf);

        slaFeatureController.performCommonOperation(task, TOPRECOST);
        ApiAsserts.assertThat(slaFeatureController.getResponse())
                .isCorrectResponseCode(HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isCorrectStatus(STATUS_SLAFEATURE_PRECOST);
        apiController.receiveTask(task.getNumber());
        CommonAssert.assertThat(apiController.getResponse())
                .isCorrectUdfList(UDF_SDFEATURE_STAGE, STAGE_PRE_COST)
                .isCorrectUdfList(UDF_SD_RESPONSIBLE_PARTY, RESPONSIBLE_PARTY_SUPPLIER);
    }
}
