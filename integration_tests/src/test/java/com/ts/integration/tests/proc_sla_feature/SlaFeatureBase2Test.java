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
import com.ts.common.utils.DateUtils;
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
import static com.ts.common.enums.MemoPlan.*;
import static com.ts.common.enums.Operations.*;
import static com.ts.common.enums.TaskStatuses.STATUS_SLAFEATURE_NEW;
import static com.ts.common.enums.TaskStatuses.STATUS_SLAFEATURE_PRECOST;
import static com.ts.common.enums.TaskType.SD_FEATURE;
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

        task = InitEntities.getGeneralTask(SD_FEATURE, CAT);
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
                .isCorrectStatus(STATUS_SLAFEATURE_PRECOST)
                .isEquals(task);

        apiController.receiveTask(task.getNumber());
        CommonAssert.assertThat(apiController.getResponse())
                .isCorrectUdfList(UDF_SDFEATURE_STAGE, STAGE_PRE_COST)
                .isCorrectUdfList(UDF_SD_RESPONSIBLE_PARTY, RESPONSIBLE_PARTY_SUPPLIER);
    }

    @Test(groups = {"SlaFeature", "Regression"}
            , description = "Передать на предварительное согласование менеджеру по анализу доработок (АНАЛИТИК)"
            , dependsOnMethods = "toprecost")
    void toAnlsmgprlmapr() {
        apiController.updateToken(ANALYTIC);
        udf = refreshUdf();
        task.refreshTask();

        task.setHandlerUser(MANAGER_ANALYZE_FEATURE);
        udf.setUdfDouble(generateUdfDouble(UDF_ACTUAL_PRELIM_BUDGET, 100));
        udf.setSecondUdfDouble(generateUdfDouble(UDF_BUDGET_FORMANAGE, 100));

        udf.setUdfMemo(generateUdfMemo(UDF_FINAL_ASSMT_DAY_STEPPLAN, DAY_STEP_PLAN.getValue()));
        udf.setSecondUdfMemo(generateUdfMemo(UDF_IMPL_BUDGET, IMPL_BUDGET.getValue()));
        udf.setThirdUdfMemo(generateUdfMemo(UDF_ACCEPT_BUDGET, ACCEPT_BUDGET.getValue()));

        udf.setThirdUdfDouble(generateUdfDouble(UDF_TOTAL_PRELIM_OPTIMK_BUDGET, 36.25));
        udf.setFourthUdfDouble(generateUdfDouble(UDF_TOTAL_PRELIM_PESM_BUDGET, 36.25));
        udf.setFifthUdfDouble(generateUdfDouble(UDF_TOTAL_PRELIM_AVG_BUDGET, 36.25));

        udf.setUdfMultiList(generateUdfMultiList(UDF_LIST_AFFCTD_SYS, COLVIR_V4));

        udf.setFourthUdfMemo(generateUdfMemo(UDF_IMPL_SUCCESS_PLAN_LIMITATION, generateString()));

        udf.setUdfDate(generateUdfDate(UDF_POTENTIAL_DAY_FINALASSMT, DateUtils.getCurrentDate(7)));
        udf.setSecondUdfDate(generateUdfDate(UDF_POTENTIAL_PRELIM_IMPL_DATE, DateUtils.getCurrentDate(7)));
        udf.setThirdUdfDate(generateUdfDate(UDF_DELIVERY_PATH_DATE, DateUtils.getCurrentDate(7)));

        udf.setFifthUdfMemo(generateUdfMemo(UDF_WORK_PLAN, generateString()));
        udf.setSixthUdfMemo(generateUdfMemo(UDF_SDFEATURE_IMPLSTATEMENT, generateString()));

        udf.setUdfList(generateUdfList(UDF_SDFEATURE_DOCREVISION, DOC_REVISION_YES));
        udf.setSecondUdfList(generateUdfList(UDF_SDFEATURE_TYPE, ADDREQ));

        udf.setUdfString(generateUdfString(UDF_SDFEATURE_LEGALREQ_DOC, generateString()));

        udf.setSeventhUdfMemo(generateUdfMemo(UDF_SDFEATURE_TYPEOTHER, generateString()));

        udf.setThirdUdfList(generateUdfList(UDF_SDFEATURE_GENUSE, GENERAL));

        udf.setEighthUdfMemo(generateUdfMemo(UDF_SDFEATURE_GENUSEOTHER, generateString()));
        udf.setNinethUdfMemo(generateUdfMemo(UDF_SDFEATURE_AGREEDDECISION, generateString()));
        udf.setTenthUdfMemo(generateUdfMemo(UDF_SDFEATURE_NOTE, generateString()));

        udf.setUdfUser(generateUdfUser(STDT_HANDLER, MANAGER_ANALYZE_FEATURE));

        task.refreshUdf(udf);

        slaFeatureController.performCommonOperation(task, TO_ANLSMGRPRLMAPR);
        ApiAsserts.assertThat(slaFeatureController.getResponse())
                .isCorrectResponseCode(HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isCorrectStatus(STATUS_SLAFEATURE_PRECOST)
                .isEquals(task);

        apiController.receiveTask(task.getNumber());
        CommonAssert.assertThat(apiController.getResponse())
                .isCorrectUdfList(UDF_SDFEATURE_STAGE, STAGE_PRE_COST)
                .isCorrectUdfList(UDF_SD_RESPONSIBLE_PARTY, RESPONSIBLE_PARTY_SUPPLIER)
                .isCorrectUdfList(UDF_ROLE_CURRENT, ANALYSIS_MANAGER)
                .isCorrectUdfUSer(UDF_ROLE_WORKER, MANAGER_ANALYZE_FEATURE);
    }

    @Test(groups = {"SlaFeature", "Regression"}
            , description = "Передать на предварительное планирование менеджеру запроса (МЕНЕДЖЕР ПО АНАЛИЗУ ДОРАБОТОК)"
            , dependsOnMethods = "toAnlsmgprlmapr")
    void toImplmgrprlmapr() {
        apiController.updateToken(MANAGER_ANALYZE_FEATURE);
        udf = refreshUdf();
        task.refreshTask();

        task.setHandlerUser(MANAGER_REQUEST);
        udf.setUdfDouble(generateUdfDouble(UDF_ACTUAL_PRELIM_BUDGET, 110));
        udf.setSecondUdfDouble(generateUdfDouble(UDF_BUDGET_FORMANAGE, 110));

        udf.setUdfMemo(generateUdfMemo(UDF_FINAL_ASSMT_DAY_STEPPLAN, DAY_STEP_PLAN_FINAL.getValue()));
        udf.setSecondUdfMemo(generateUdfMemo(UDF_IMPL_BUDGET, IMPL_BUDGET_FINAL.getValue()));
        udf.setThirdUdfMemo(generateUdfMemo(UDF_ACCEPT_BUDGET, ACCEPT_BUDGET_FINAL.getValue()));

        udf.setThirdUdfDouble(generateUdfDouble(UDF_TOTAL_PRELIM_OPTIMK_BUDGET, 40.13));
        udf.setFourthUdfDouble(generateUdfDouble(UDF_TOTAL_PRELIM_PESM_BUDGET, 40.13));
        udf.setFifthUdfDouble(generateUdfDouble(UDF_TOTAL_PRELIM_AVG_BUDGET, 40.13));

        udf.setUdfMultiList(generateUdfMultiList(UDF_LIST_AFFCTD_SYS, COLVIR_V4, AFS));

        udf.setFourthUdfMemo(generateUdfMemo(UDF_IMPL_SUCCESS_PLAN_LIMITATION, generateString()));

        udf.setUdfDate(generateUdfDate(UDF_POTENTIAL_DAY_FINALASSMT, DateUtils.getCurrentDate(9)));
        udf.setSecondUdfDate(generateUdfDate(UDF_POTENTIAL_PRELIM_IMPL_DATE, DateUtils.getCurrentDate(9)));
        udf.setThirdUdfDate(generateUdfDate(UDF_DELIVERY_PATH_DATE, DateUtils.getCurrentDate(9)));

        udf.setFifthUdfMemo(generateUdfMemo(UDF_WORK_PLAN, generateString()));
        udf.setSixthUdfMemo(generateUdfMemo(UDF_REALIZATION_DECISION, generateString()));
        udf.setSeventhUdfMemo(generateUdfMemo(UDF_SDFEATURE_IMPLSTATEMENT, generateString()));

        udf.setUdfList(generateUdfList(UDF_SDFEATURE_DOCREVISION, DOC_REVISION_YES));
        udf.setSecondUdfList(generateUdfList(UDF_SDFEATURE_TYPE, ADDREQ));

        udf.setUdfString(generateUdfString(UDF_SDFEATURE_LEGALREQ_DOC, generateString()));
        udf.setEighthUdfMemo(generateUdfMemo(UDF_SDFEATURE_TYPEOTHER, generateString()));

        udf.setThirdUdfList(generateUdfList(UDF_SDFEATURE_GENUSE, GENERAL));

        udf.setNinethUdfMemo(generateUdfMemo(UDF_SDFEATURE_GENUSEOTHER, generateString()));
        udf.setTenthUdfMemo(generateUdfMemo(UDF_SDFEATURE_AGREEDDECISION, generateString()));
        udf.setEleventhUdfMemo(generateUdfMemo(UDF_SDFEATURE_NOTE, generateString()));
        udf.setUdfUser(generateUdfUser(STDT_HANDLER, MANAGER_REQUEST));

        task.refreshUdf(udf);
        slaFeatureController.performCommonOperation(task, TO_IMPLMGRPRLMAPR);
        ApiAsserts.assertThat(slaFeatureController.getResponse())
                .isCorrectResponseCode(HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isCorrectStatus(STATUS_SLAFEATURE_PRECOST)
                .isEquals(task);

        apiController.receiveTask(task.getNumber());
        CommonAssert.assertThat(apiController.getResponse())
                .isCorrectUdfList(UDF_SDFEATURE_STAGE, STAGE_PRE_COST)
                .isCorrectUdfList(UDF_SD_RESPONSIBLE_PARTY, RESPONSIBLE_PARTY_SUPPLIER)
                .isCorrectUdfList(UDF_ROLE_CURRENT, IMPLMANAGER)
                .isCorrectUdfUSer(UDF_ROLE_WORKER, MANAGER_REQUEST);
    }
}
