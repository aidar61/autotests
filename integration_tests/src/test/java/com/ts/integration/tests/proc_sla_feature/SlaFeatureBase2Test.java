package com.ts.integration.tests.proc_sla_feature;

import com.ts.common.asserts.ApiAsserts;
import com.ts.common.asserts.CommonAssert;
import com.ts.common.controllers.TaskResponseBody;
import com.ts.common.controllers.sla.SlaFeatureController;
import com.ts.common.entitites.commonEntities.CostString;
import com.ts.common.entitites.commonEntities.Parent;
import com.ts.common.entitites.commonEntities.User;
import com.ts.common.entitites.tasks.GeneralTask;
import com.ts.common.enums.Tables;
import com.ts.common.utils.DateUtils;
import com.ts.common.utils.InitEntities;
import com.ts.common.utils.WaitManager;
import com.ts.integration.tests.BaseIntegrationTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static com.ts.common.application.controllers.TrackStudioHttpStatusCodes.HTTP_OK;
import static com.ts.common.config.AppConfigProvider.getUserConfig;
import static com.ts.common.entitites.commonEntities.List.Constants.*;
import static com.ts.common.entitites.commonEntities.Role.RoleConstants.ROLE_CLIENT;
import static com.ts.common.entitites.commonEntities.Role.RoleConstants.ROLE_WORKER;
import static com.ts.common.entitites.commonEntities.Task.Constants.MTBANK;
import static com.ts.common.entitites.commonEntities.Task.Constants.NOTIFICATION_SERVICE2;
import static com.ts.common.entitites.commonEntities.Udfs.UdfSd.*;
import static com.ts.common.enums.MemoPlan.*;
import static com.ts.common.enums.Operations.*;
import static com.ts.common.enums.TaskStatuses.*;
import static com.ts.common.enums.TaskType.SLA_FEATURE;
import static com.ts.common.utils.InitEntities.*;
import static com.ts.common.utils.RandomUtils.*;

public class SlaFeatureBase2Test extends BaseIntegrationTest {
    private SlaFeatureController slaFeatureController;
    private GeneralTask task;
    private Parent parent;
    private User role_client;
    private User at_task_analitic;
    private User at_support_costmanager;
    private User at_sdfeature_analysis_manager;
    private User at_sdfeature_impl_manager;
    private User at_support_manager;

    @BeforeClass(alwaysRun = true)
    public void beforeCLass() {
        slaFeatureController = apiController.getSlaFeatureController();
        userController = apiController.getUserController();
        baseController = apiController.getBaseController();

        parent = taskGenerator.getAt_sdproject().toParent();

        role_client = userController.getUserBy(userRoles, ROLE_CLIENT, getUserConfig().role_client());
        at_task_analitic = userController.getUserBy(userRoles, ROLE_WORKER, getUserConfig().at_task_analitic());
        at_support_costmanager = userController.getUserBy(userRoles, ROLE_WORKER, getUserConfig().at_support_costmanager());
        at_sdfeature_analysis_manager = userController.getUserBy(userRoles, ROLE_WORKER, getUserConfig().at_sdfeature_analysis_manager());
        at_sdfeature_impl_manager = userController.getUserBy(userRoles, ROLE_WORKER, getUserConfig().at_sdfeature_impl_manager());
        at_support_manager = userController.getUserBy(userRoles, ROLE_WORKER, getUserConfig().at_support_manager());

        task = InitEntities.getGeneralTask(SLA_FEATURE, CAT);
    }

    @BeforeMethod(alwaysRun = true)
    public void beforeMethod() {
        WaitManager.pause(5);
    }

    @Test(groups = {"PROC_SLAFEATURE", "Regression"}
            , description = "Создать Запрос на доработку ЛПО (new) (КЛИЕНТ)")
    void cat() {
        apiController.updateToken(generateAuthToken(role_client));
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

    @Test(groups = {"PROC_SLAFEATURE", "Regression"}
            , description = "Начать предварительную оценку (ACCOUNT-MANAGER)"
            , dependsOnMethods = "cat")
    void toprecost() {
        apiController.updateToken(at_support_costmanager);
        udf = refreshUdf();
        task.refreshTask();

        task.setHandlerUser(at_task_analitic);
        udf.setUdfString(generateUdfString(UDF_SDFEATURE_OVERLIMITREASON, generateString()));
        udf.setUdfUser(generateUdfUser(STDT_HANDLER, at_task_analitic));
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

    @Test(groups = {"PROC_SLAFEATURE", "Regression"}
            , description = "Передать на предварительное согласование менеджеру по анализу доработок (АНАЛИТИК)"
            , dependsOnMethods = "toprecost")
    void toAnlsmgprlmapr() {
        apiController.updateToken(at_task_analitic);
        udf = refreshUdf();
        task.refreshTask();

        task.setHandlerUser(at_sdfeature_analysis_manager);
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

        udf.setUdfUser(generateUdfUser(STDT_HANDLER, at_sdfeature_analysis_manager));

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
                .isCorrectUdfUSer(UDF_ROLE_WORKER, at_sdfeature_analysis_manager);
    }

    @Test(groups = {"PROC_SLAFEATURE", "Regression"}
            , description = "Передать на предварительное планирование менеджеру запроса (МЕНЕДЖЕР ПО АНАЛИЗУ ДОРАБОТОК)"
            , dependsOnMethods = "toAnlsmgprlmapr")
    void toImplmgrprlmapr() {
        apiController.updateToken(at_sdfeature_analysis_manager);
        udf = refreshUdf();
        task.refreshTask();

        task.setHandlerUser(at_sdfeature_impl_manager);
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
        udf.setUdfUser(generateUdfUser(STDT_HANDLER, at_sdfeature_impl_manager));

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
                .isCorrectUdfUSer(UDF_ROLE_WORKER, at_sdfeature_impl_manager);
    }

    @Test(groups = {"PROC_SLAFEATURE", "Regression"}
            , description = "Передать на предварительную оценку аккаунт менеджеру (МЕНЕДЖЕР ЗАПРОСА)"
            , dependsOnMethods = "toImplmgrprlmapr")
    void beginCostPre() {
        apiController.updateToken(generateAuthToken(at_sdfeature_impl_manager));
        udf = refreshUdf();
        task.refreshTask();

        task.setHandlerUser(at_support_costmanager);

        udf.setUdfDouble(generateUdfDouble(UDF_ACTUAL_PRELIM_BUDGET, 115));
        udf.setSecondUdfDouble(generateUdfDouble(UDF_BUDGET_FORMANAGE, 115));

        udf.setUdfMemo(generateUdfMemo(UDF_FINAL_ASSMT_DAY_STEPPLAN, ASSMT_DAY_STEP_PLAN_FINAL.getValue()));
        udf.setSecondUdfMemo(generateUdfMemo(UDF_IMPL_BUDGET, UDF_IMPL_BUDGET_MEMO.getValue()));
        udf.setThirdUdfMemo(generateUdfMemo(UDF_ACCEPT_BUDGET, UDF_ACCEPT_BUDGET_FINAL.getValue()));

        udf.setThirdUdfDouble(generateUdfDouble(UDF_TOTAL_PRELIM_OPTIMK_BUDGET, 42.5));
        udf.setFourthUdfDouble(generateUdfDouble(UDF_TOTAL_PRELIM_PESM_BUDGET, 42.5));
        udf.setFifthUdfDouble(generateUdfDouble(UDF_TOTAL_PRELIM_AVG_BUDGET, 42.5));

        udf.setUdfMultiList(generateUdfMultiList(UDF_LIST_AFFCTD_SYS, AFS, COLVIR_V4));

        udf.setFourthUdfMemo(generateUdfMemo(UDF_IMPL_SUCCESS_PLAN_LIMITATION, generateString()));

        udf.setUdfDate(generateUdfDate(UDF_POTENTIAL_DAY_FINALASSMT, 9));
        udf.setSecondUdfDate(generateUdfDate(UDF_POTENTIAL_PRELIM_IMPL_DATE, 9));
        udf.setThirdUdfDate(generateUdfDate(UDF_DELIVERY_PATH_DATE, 9));

        udf.setFifthUdfMemo(generateUdfMemo(UDF_WORK_PLAN, generateString()));
        udf.setSixthUdfMemo(generateUdfMemo(UDF_REALIZATION_DECISION, generateString()));
        udf.setSeventhUdfMemo(generateUdfMemo(UDF_SDFEATURE_IMPLSTATEMENT, generateString()));

        udf.setUdfList(generateUdfList(UDF_SDFEATURE_DOCREVISION, DOC_REVISION_YES));
        udf.setSecondUdfList(generateUdfList(UDF_SDFEATURE_TYPE, ADDREQ));

        udf.setUdfString(generateUdfString(UDF_SDFEATURE_LEGALREQ_DOC, generateString()));

        udf.setEighthUdfMemo(generateUdfMemo(UDF_SDFEATURE_TYPEOTHER, generateString()));

        udf.setThirdUdfList(generateUdfList(UDF_SDFEATURE_GENUSE, GENERAL, "{\"username\":\"vvolskiy\",\"name\":\"Вольский Валерий\"}"));

        udf.setNinethUdfMemo(generateUdfMemo(UDF_SDFEATURE_GENUSEOTHER, generateString()));
        udf.setTenthUdfMemo(generateUdfMemo(UDF_SDFEATURE_AGREEDDECISION, generateString()));
        udf.setEleventhUdfMemo(generateUdfMemo(UDF_SDFEATURE_NOTE, generateString()));

        udf.setUdfUser(generateUdfUser(STDT_HANDLER, at_support_costmanager));

        task.refreshUdf(udf);

        slaFeatureController.performCommonOperation(task, BEGINCOST_PRE);
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
                .isCorrectUdfList(UDF_ROLE_CURRENT, ACCOUNT_MANAGER_LIST)
                .isCorrectUdfUSer(UDF_ROLE_WORKER, at_support_costmanager);
    }

    @Test(groups = {"PROC_SLAFEATURE", "Regression"}
            , description = "Сообщить предварительные условия реализации (ACCOUNT-MANAGER)"
            , dependsOnMethods = "beginCostPre")
    void sendCostPre() {
        apiController.updateToken(at_support_costmanager);
        udf = refreshUdf();
        task.refreshTask();

        udf.setUdfList(generateUdfList(UDF_SDFEATURE_PAYDCS, PAID));

        udf.setUdfUser(generateUdfUser(UDF_SDFEATURE_PAYLPR, at_support_costmanager));

        udf.setUdfMemo(generateUdfMemo(UDF_SDFEATURE_PAYDCSREASON, generateString()));

        udf.setSecondUdfList(generateUdfList(UDF_SLA_CLIENTGENUSE, NOTCUSTOM));

        udf.setSecondUdfMemo(generateUdfMemo(UDF_SDFEATURE_AGREEDDECISION, generateString()));

        CostString cost = InitEntities.generateCostString(5000, 5, 5, 5);

        udf.setUdfString(generateUdfString(UDF_SLA_AWAITCOST, cost.toJson()));
        udf.setSecondUdfString(generateUdfString(UDF_SLA_PRECOSTBUDGET, cost.toJson()));
        udf.setThirdUdfString(generateUdfString(UDF_SLA_AWAITENDCOSTBUDGET, cost.toJson()));
        udf.setFourthUdfString(generateUdfString(UDF_SLA_IMPLPLANTD_PRE, cost.toJson()));

        udf.setUdfDate(generateUdfDate(UDF_SLA_FINALESTIMATIONDATE, DateUtils.getCurrentDate(9)));

        task.refreshUdf(udf);

        slaFeatureController.performCommonOperation(task, SENDCOST_PRE);

        ApiAsserts.assertThat(slaFeatureController.getResponse())
                .isCorrectResponseCode(HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isCorrectStatus(STATUS_SLAFEATURE_WAITPRECOSTACP)
                .isEquals(task);

        apiController.receiveTask(task.getNumber());
        CommonAssert.assertThat(apiController.getResponse())
                .isCorrectUdfList(UDF_SDFEATURE_STAGE, STAGE_PRE_COST)
                .isCorrectUdfList(UDF_SD_RESPONSIBLE_PARTY, RESPONSIBLE_PARTY_CLIENT)
                .isCorrectUdfList(UDF_ROLE_CURRENT, CLIENT_ROLE_CURRENT);
    }

    @Test(groups = {"PROC_SLAFEATURE", "Regression"}
            , description = "Принять предварительные условия реализации (CLIENT)"
            , dependsOnMethods = "sendCostPre")
    void acceptPreCost() {
        apiController.updateToken(role_client);
        udf = refreshUdf();
        task.refreshTask();

        slaFeatureController.performCommonOperation(task, ACCEPTPRECOST);
        ApiAsserts.assertThat(slaFeatureController.getResponse())
                .isCorrectResponseCode(HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isCorrectStatus(STATUS_SLAFEATURE_CALCCOST)
                .isEquals(task);

        apiController.receiveTask(task.getNumber());
        CommonAssert.assertThat(apiController.getResponse())
                .isCorrectUdfList(UDF_SDFEATURE_STAGE, STAGE_COST)
                .isCorrectUdfList(UDF_SD_RESPONSIBLE_PARTY, RESPONSIBLE_PARTY_SUPPLIER)
                .isCorrectUdfList(UDF_ROLE_CURRENT, ACCOUNT_MANAGER_LIST);
    }

    @Test(groups = {"PROC_SLAFEATURE", "Regression"}
            , description = "Передать аналитику (ACCOUNT_MANAGER)"
            , dependsOnMethods = "acceptPreCost")
    void returnToAnal() {
        apiController.updateToken(at_support_costmanager);
        udf = refreshUdf();
        task.refreshTask();

        udf.setUdfUser(generateUdfUser(STDT_HANDLER, at_task_analitic));

        task.refreshUdf(udf);
        task.setHandlerUser(at_task_analitic);

        slaFeatureController.performCommonOperation(task, RETURN_TO_ANAL);
        ApiAsserts.assertThat(slaFeatureController.getResponse())
                .isCorrectResponseCode(HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isEquals(task);

        apiController.receiveTask(task.getNumber());
        CommonAssert.assertThat(apiController.getResponse())
                .isCorrectUdfList(UDF_ROLE_CURRENT, ANALYST)
                .isCorrectUdfUSer(UDF_ROLE_WORKER, at_task_analitic);
    }

    @Test(groups = {"PROC_SLAFEATURE", "Regression"}
            , description = "Передать на окончательное согласование менеджеру по анализу доработок (АНАЛИТИК)"
            , dependsOnMethods = "returnToAnal")
    void submitToAgrAnls() {
        apiController.updateToken(at_task_analitic);
        udf = refreshUdf();
        task.refreshTask();

        udf.setUdfDouble(generateUdfDouble(UDF_ACTUAL_PRELIM_BUDGET, 115));
        udf.setSecondUdfDouble(generateUdfDouble(UDF_BUDGET_FORMANAGE, 115));

        udf.setUdfMemo(generateUdfMemo(UDF_FINAL_ASSMT_DAY_STEPPLAN, UDF_ASSMT_DAY_STEPPLAN_FINAL.getValue()));
        udf.setSecondUdfMemo(generateUdfMemo(UDF_IMPL_BUDGET, UDF_IMPL_BUDGET_FINAL.getValue()));
        udf.setThirdUdfMemo(generateUdfMemo(UDF_ACCEPT_BUDGET, UDF_ACCEPT_BUDGET_FINAL_FINAL.getValue()));

        udf.setThirdUdfDouble(generateUdfDouble(UDF_TOTAL_PRELIM_OPTIMK_BUDGET, 42.5));
        udf.setFourthUdfDouble(generateUdfDouble(UDF_TOTAL_PRELIM_PESM_BUDGET, 42.5));
        udf.setFifthUdfDouble(generateUdfDouble(UDF_TOTAL_PRELIM_AVG_BUDGET, 42.5));
        udf.setSixthUdfDouble(generateUdfDouble(UDF_TOTAL_FINAL_OPT_BUDGET, 42.5));
        udf.setSeventhUdfDouble(generateUdfDouble(UDF_TOTAL_FINAL_PESIM_BUDGET, 42.5));
        udf.setEighthUdfDouble(generateUdfDouble(UDF_TOTAL_FINAL_AVG_BUDGET, 42.5));

        udf.setUdfMultiList(generateUdfMultiList(UDF_LIST_AFFCTD_SYS, COLVIR_V4));

        udf.setFourthUdfMemo(generateUdfMemo(UDF_IMPL_SUCCESS_PLAN_LIMITATION, generateString()));

        udf.setUdfDate(generateUdfDate(UDF_POTENTIAL_FINAL_IMP_DATE, DateUtils.getCurrentDate(10)));
        udf.setSecondUdfDate(generateUdfDate(UDF_DELIVERY_PATH_DATE, DateUtils.getCurrentDate(10)));

        udf.setSixthUdfMemo(generateUdfMemo(UDF_WORK_PLAN, generateString()));
        final String expectedDecision = generateString();
        udf.setSeventhUdfMemo(generateUdfMemo(UDF_REALIZATION_DECISION, expectedDecision));
        final String expectedImplstatement = generateString();
        udf.setEighthUdfMemo(generateUdfMemo(UDF_SDFEATURE_IMPLSTATEMENT, expectedImplstatement));

        udf.setUdfList(generateUdfList(UDF_SDFEATURE_DOCREVISION, DOC_REVISION_YES));
        udf.setSecondUdfList(generateUdfList(UDF_SDFEATURE_TYPE, ADDREQ));

        udf.setUdfString(generateUdfString(UDF_SDFEATURE_LEGALREQ_DOC, generateString()));

        udf.setThirdUdfList(generateUdfList(UDF_SDFEATURE_GENUSE, GENERAL));

        udf.setNinethUdfMemo(generateUdfMemo(UDF_SDFEATURE_GENUSEOTHER, generateString()));
        udf.setTenthUdfMemo(generateUdfMemo(UDF_SDFEATURE_AGREEDDECISION, generateString()));
        udf.setEleventhUdfMemo(generateUdfMemo(UDF_SDFEATURE_TESTCASE, generateString()));
        udf.setTwelvethUdfMemo(generateUdfMemo(UDF_SDFEATURE_NOTE, generateString()));

        udf.setUdfUser(generateUdfUser(STDT_HANDLER, at_sdfeature_analysis_manager));

        task.refreshUdf(udf);
        task.setHandlerUser(at_sdfeature_analysis_manager);

        slaFeatureController.performCommonOperation(task, SUBMIT_TO_AGR_ANLS);
        ApiAsserts.assertThat(slaFeatureController.getResponse())
                .isCorrectResponseCode(HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isCorrectStatus(STATUS_SLAFEATURE_CALCCOST)
                .isEquals(task);

        apiController.receiveTask(task.getNumber());
        CommonAssert.assertThat(apiController.getResponse())
                .isCorrectUdfList(UDF_SDFEATURE_STAGE, STAGE_COST)
                .isCorrectUdfList(UDF_SD_RESPONSIBLE_PARTY, RESPONSIBLE_PARTY_SUPPLIER)
                .isCorrectUdfList(UDF_ROLE_CURRENT, ANALYSIS_MANAGER)
                .isCorrectUdfUSer(UDF_ROLE_WORKER, at_sdfeature_analysis_manager)
                .isCorrectUdfString(UDF_REALIZATION_DECISION, expectedDecision)
                .isCorrectUdfString(UDF_SDFEATURE_IMPLSTATEMENT, expectedImplstatement);
    }

    @Test(groups = {"PROC_SLAFEATURE", "Regression"}
            , description = "Передать на окончательное планирование менеджеру запроса (МЕНЕДЖЕР ПО АНАЛИЗУ ДОРАБОТОК)"
            , dependsOnMethods = "submitToAgrAnls")
    void assignFinPlnimp() {
        apiController.updateToken(at_sdfeature_analysis_manager);
        udf = refreshUdf();
        task.refreshTask();

        udf.setUdfDouble(generateUdfDouble(UDF_ACTUAL_PRELIM_BUDGET, 120));
        udf.setSecondUdfDouble(generateUdfDouble(UDF_BUDGET_FORMANAGE, 120));

        udf.setUdfMemo(generateUdfMemo(UDF_FINAL_ASSMT_DAY_STEPPLAN, UDF_FINAL_ASSMT_DAY_STEPPLAN_MANAGER_REQUEST.getValue()));
        udf.setSecondUdfMemo(generateUdfMemo(UDF_IMPL_BUDGET, UDF_IMPL_BUDGET_MANAGER_REQUEST.getValue()));
        udf.setThirdUdfMemo(generateUdfMemo(UDF_ACCEPT_BUDGET, UDF_ACCEPT_BUDGET_MANAGER_REQUEST.getValue()));

        udf.setThirdUdfDouble(generateUdfDouble(UDF_TOTAL_PRELIM_OPTIMK_BUDGET, 43.75));
        udf.setFourthUdfDouble(generateUdfDouble(UDF_TOTAL_PRELIM_PESM_BUDGET, 43.75));
        udf.setFifthUdfDouble(generateUdfDouble(UDF_TOTAL_PRELIM_AVG_BUDGET, 43.75));
        udf.setSixthUdfDouble(generateUdfDouble(UDF_TOTAL_FINAL_OPT_BUDGET, 44.5));
        udf.setSeventhUdfDouble(generateUdfDouble(UDF_TOTAL_FINAL_PESIM_BUDGET, 44.5));
        udf.setEighthUdfDouble(generateUdfDouble(UDF_TOTAL_FINAL_AVG_BUDGET, 44.5));

        udf.setUdfList(generateUdfList(UDF_LIST_AFFCTD_SYS, COLVIR_V4));

        udf.setFourthUdfMemo(generateUdfMemo(UDF_IMPL_SUCCESS_PLAN_LIMITATION, generateString()));

        udf.setUdfDate(generateUdfDate(UDF_POTENTIAL_FINAL_IMP_DATE, DateUtils.getCurrentDate(10)));
        udf.setSecondUdfDate(generateUdfDate(UDF_DELIVERY_PATH_DATE, DateUtils.getCurrentDate(10)));

        udf.setFifthUdfMemo(generateUdfMemo(UDF_WORK_PLAN, generateString()));
        udf.setSixthUdfMemo(generateUdfMemo(UDF_REALIZATION_DECISION, generateString()));
        udf.setSeventhUdfMemo(generateUdfMemo(UDF_SDFEATURE_IMPLSTATEMENT, generateString()));

        udf.setSecondUdfList(generateUdfList(UDF_SDFEATURE_DOCREVISION, DOC_REVISION_YES));
        udf.setThirdUdfList(generateUdfList(UDF_SDFEATURE_TYPE, ADDREQ));

        udf.setUdfString(generateUdfString(UDF_SDFEATURE_LEGALREQ_DOC, generateString()));

        udf.setFourthUdfList(generateUdfList(UDF_SDFEATURE_GENUSE, GENERAL));

        udf.setEighthUdfMemo(generateUdfMemo(UDF_SDFEATURE_GENUSEOTHER, generateString()));
        udf.setNinethUdfMemo(generateUdfMemo(UDF_SDFEATURE_AGREEDDECISION, generateString()));
        udf.setTenthUdfMemo(generateUdfMemo(UDF_SDFEATURE_TESTCASE, generateString()));
        udf.setEleventhUdfMemo(generateUdfMemo(UDF_SDFEATURE_NOTE, generateString()));

        udf.setUdfUser(generateUdfUser(STDT_HANDLER, at_sdfeature_impl_manager));

        task.refreshUdf(udf);
        task.setHandlerUser(at_sdfeature_impl_manager);

        slaFeatureController.performCommonOperation(task, ASSIGN_FIN_PLNIMP);
        ApiAsserts.assertThat(slaFeatureController.getResponse())
                .isCorrectResponseCode(HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isCorrectStatus(STATUS_SLAFEATURE_CALCCOST)
                .isCorrectMStatusName("Передать на окончательное планирование менеджеру запроса")
                .isEquals(task);

        apiController.receiveTask(task.getNumber());
        CommonAssert.assertThat(apiController.getResponse())
                .isCorrectUdfList(UDF_SDFEATURE_STAGE, STAGE_COST)
                .isCorrectUdfList(UDF_SD_RESPONSIBLE_PARTY, RESPONSIBLE_PARTY_SUPPLIER)
                .isCorrectUdfList(UDF_ROLE_CURRENT, IMPLMANAGER)
                .isCorrectUdfUSer(UDF_ROLE_WORKER, at_sdfeature_impl_manager);
    }

    @Test(groups = {"PROC_SLAFEATURE", "Regression"}
            , description = "Передать на окончательную оценку аккаунт-менеджеру (МЕНЕДЖЕР ЗАПРОСА)"
            , dependsOnMethods = "assignFinPlnimp")
    void beginCostFinal() {
        apiController.updateToken(at_sdfeature_impl_manager);
        udf = refreshUdf();
        task.refreshTask();

        udf.setUdfDouble(generateUdfDouble(UDF_ACTUAL_PRELIM_BUDGET, 120));
        udf.setSecondUdfDouble(generateUdfDouble(UDF_BUDGET_FORMANAGE, 120));

        udf.setUdfMemo(generateUdfMemo(UDF_FINAL_ASSMT_DAY_STEPPLAN, ASSMT_DAY_STEPPLAN_ACCOUNT_MANAGER.getValue()));
        udf.setSecondUdfMemo(generateUdfMemo(UDF_IMPL_BUDGET, IMPL_BUDGET_ACCOUNT_MANAGER.getValue()));
        udf.setThirdUdfMemo(generateUdfMemo(UDF_ACCEPT_BUDGET, ACCEPT_BUDGET_ACCOUNT_MANAGER.getValue()));

        udf.setThirdUdfDouble(generateUdfDouble(UDF_TOTAL_PRELIM_OPTIMK_BUDGET, 43.75));
        udf.setFourthUdfDouble(generateUdfDouble(UDF_TOTAL_PRELIM_PESM_BUDGET, 43.75));
        udf.setFifthUdfDouble(generateUdfDouble(UDF_TOTAL_PRELIM_AVG_BUDGET, 43.75));
        udf.setSixthUdfDouble(generateUdfDouble(UDF_TOTAL_FINAL_OPT_BUDGET, 44.5));
        udf.setSeventhUdfDouble(generateUdfDouble(UDF_TOTAL_FINAL_PESIM_BUDGET, 44.5));
        udf.setEighthUdfDouble(generateUdfDouble(UDF_TOTAL_FINAL_AVG_BUDGET, 44.5));

        udf.setUdfMultiList(generateUdfMultiList(UDF_LIST_AFFCTD_SYS, COLVIR_V4));

        udf.setFourthUdfMemo(generateUdfMemo(UDF_IMPL_SUCCESS_PLAN_LIMITATION, generateString()));

        udf.setUdfDate(generateUdfDate(UDF_POTENTIAL_FINAL_IMP_DATE, DateUtils.getCurrentDate(10)));
        udf.setSecondUdfDate(generateUdfDate(UDF_DELIVERY_PATH_DATE, DateUtils.getCurrentDate(10)));

        udf.setFifthUdfMemo(generateUdfMemo(UDF_WORK_PLAN, generateString()));
        udf.setSixthUdfMemo(generateUdfMemo(UDF_REALIZATION_DECISION, generateString()));
        udf.setSeventhUdfMemo(generateUdfMemo(UDF_SDFEATURE_IMPLSTATEMENT, generateString()));

        udf.setUdfList(generateUdfList(UDF_SDFEATURE_DOCREVISION, DOC_REVISION_YES));
        udf.setSecondUdfList(generateUdfList(UDF_SDFEATURE_TYPE, ADDREQ));

        udf.setUdfString(generateUdfString(UDF_SDFEATURE_LEGALREQ_DOC, generateString()));

        udf.setThirdUdfList(generateUdfList(UDF_SDFEATURE_GENUSE, GENERAL, "{\"username\":\"vvolskiy\",\"name\":\"Вольский Валерий\"}"));


        udf.setEighthUdfMemo(generateUdfMemo(UDF_SDFEATURE_GENUSEOTHER, generateString()));
        udf.setNinethUdfMemo(generateUdfMemo(UDF_SDFEATURE_AGREEDDECISION, generateString()));
        udf.setTenthUdfMemo(generateUdfMemo(UDF_SDFEATURE_TESTCASE, generateString()));
        udf.setEleventhUdfMemo(generateUdfMemo(UDF_SDFEATURE_NOTE, generateString()));

        udf.setUdfUser(generateUdfUser(STDT_HANDLER, at_support_costmanager));

        task.setHandlerUser(at_support_costmanager);
        task.refreshUdf(udf);

        slaFeatureController.performCommonOperation(task, BEGINCOST_FINAL);
        ApiAsserts.assertThat(slaFeatureController.getResponse())
                .isCorrectResponseCode(HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isCorrectStatus(STATUS_SLAFEATURE_CALCCOST)
                .isCorrectMStatusName("Передать на окончательную оценку аккаунт-менеджеру")
                .isEquals(task);

        apiController.receiveTask(task.getNumber());
        CommonAssert.assertThat(apiController.getResponse())
                .isCorrectUdfList(UDF_SDFEATURE_STAGE, STAGE_COST)
                .isCorrectUdfList(UDF_SD_RESPONSIBLE_PARTY, RESPONSIBLE_PARTY_SUPPLIER)
                .isCorrectUdfList(UDF_ROLE_CURRENT, ACCOUNT_MANAGER_LIST)
                .isCorrectUdfUSer(UDF_ROLE_WORKER, at_support_costmanager);
    }

    @Test(groups = {"PROC_SLAFEATURE", "Regression"}
            , description = "Сообщить окончательные условия реализации (ACCOUNT-MANAGER)"
            , dependsOnMethods = "beginCostFinal")
    void sendCostFinal() {
        apiController.updateToken(at_support_costmanager);
        udf = refreshUdf();
        task.refreshTask();

        udf.setUdfDate(generateUdfDate(UDF_SDFEATUREPLANTD, DateUtils.getCurrentDate(10)));

        udf.setUdfList(generateUdfList(UDF_SDFEATURE_PAYDCS, PAID));

        udf.setUdfUser(generateUdfUser(UDF_SDFEATURE_PAYLPR, at_support_costmanager));

        udf.setUdfMemo(generateUdfMemo(UDF_SDFEATURE_PAYDCSREASON, generateString()));

        CostString costString = generateCostString(5000, 5, 5, 5);
        udf.setUdfString(generateUdfString(UDF_SLA_ENDCOSTBUDGET, costString.toJson()));

        udf.setSecondUdfList(generateUdfList(UDF_SLA_CLIENTGENUSE, NOTCUSTOM));

        udf.setSecondUdfString(generateUdfString(UDF_SLA_IMPLDEADLINE, "100"));
        udf.setThirdUdfString(generateUdfString(UDF_SLA_RESULTCOST, costString.toJson()));

        task.refreshUdf(udf);

        slaFeatureController.performCommonOperation(task, SENDCOST_FINAL);
        ApiAsserts.assertThat(slaFeatureController.getResponse())
                .isCorrectResponseCode(HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isCorrectStatus(STATUS_SLAFEATURE_WAITACCEPT)
                .isEquals(task);

        apiController.receiveTask(task.getNumber());
        CommonAssert.assertThat(apiController.getResponse())
                .isCorrectUdfList(UDF_SDFEATURE_STAGE, STAGE_COST)
                .isCorrectUdfList(UDF_SD_RESPONSIBLE_PARTY, RESPONSIBLE_PARTY_CLIENT)
                .isCorrectUdfList(UDF_ROLE_CURRENT, CLIENT_ROLE_CURRENT)
                .isCorrectUdfList(UDF_REQ_APRV_ANLYSMANAGER, APPRVD)
                .isCorrectUdfList(UDF_REQ_APRV_IMPLSMANAGER, APPRVD_REQUEST);
    }

    @Test(groups = {"PROC_SLAFEATURE", "Regression"}
            , description = "Принять на окончательные условия реализации (КЛИЕНТ)"
            , dependsOnMethods = "sendCostFinal")
    void acceptConditions() {
        apiController.updateToken(role_client);
        udf = refreshUdf();
        task.refreshTask();

        slaFeatureController.performCommonOperation(task, ACCEPTCONDITIONS);
        ApiAsserts.assertThat(slaFeatureController.getResponse())
                .isCorrectResponseCode(HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isCorrectStatus(STATUS_SLAFEATURE_ACCEPTED)
                .isEquals(task);

        apiController.receiveTask(task.getNumber());
        CommonAssert.assertThat(apiController.getResponse())
                .isCorrectUdfList(UDF_SDFEATURE_STAGE, STAGE_IMPL)
                .isCorrectUdfList(UDF_SD_RESPONSIBLE_PARTY, RESPONSIBLE_PARTY_SUPPLIER)
                .isCorrectUdfList(UDF_ROLE_CURRENT, ACCOUNT_MANAGER_LIST);
    }

    @Test(groups = {"PROC_SLAFEATURE", "Regression"}
            , description = "Передать в разработку (ACCOUNT-MANAGER)"
            , dependsOnMethods = "acceptConditions")
    void start() {
        apiController.updateToken(at_support_costmanager);
        udf = refreshUdf();
        task.refreshTask();

        udf.setUdfDate(generateUdfDate(UDF_SDFEATUREPLANTD, DateUtils.getCurrentDate(10)));

        udf.setUdfList(generateUdfList(UDF_SDFEATURE_LEGALREQ, YES_LEGAL));
        udf.setSecondUdfList(generateUdfList(UDF_SDFEATURE_CUSTOMDEV, YES_DEV));

        udf.setUdfUser(generateUdfUser(STDT_HANDLER, at_support_costmanager));

        task.refreshUdf(udf);
        task.setHandlerUser(at_support_costmanager);

        slaFeatureController.performCommonOperation(task, START);
        ApiAsserts.assertThat(slaFeatureController.getResponse())
                .isCorrectResponseCode(HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isCorrectStatus(STATUS_SLAFEATURE_INWORK)
                .isEquals(task);

        apiController.receiveTask(task.getNumber());
        CommonAssert.assertThat(apiController.getResponse())
                .isCorrectUdfList(UDF_SDFEATURE_STAGE, STAGE_IMPL)
                .isCorrectUdfList(UDF_SD_RESPONSIBLE_PARTY, RESPONSIBLE_PARTY_SUPPLIER)
                .isCorrectUdfList(UDF_ROLE_CURRENT, ACCOUNT_MANAGER_LIST)
                .isCorrectUdfUSer(UDF_ROLE_WORKER, at_support_costmanager);
    }

    @Test(groups = {"PROC_SLAFEATURE", "Regression"}
            , description = "Завершить выполнение работы (ACCOUNT-MANAGER)"
            , dependsOnMethods = "start")
    void finish() {
        apiController.updateToken(at_support_costmanager);
        udf = refreshUdf();
        task.refreshTask();

        slaFeatureController.performCommonOperation(task, FINISH);
        ApiAsserts.assertThat(slaFeatureController.getResponse())
                .isCorrectResponseCode(HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isCorrectStatus(STATUS_SLAFEATURE_COMPLETEDWORK)
                .isEquals(task);

        apiController.receiveTask(task.getNumber());
        CommonAssert.assertThat(apiController.getResponse())
                .isCorrectUdfList(UDF_SDFEATURE_STAGE, STAGE_PRESHIP)
                .isCorrectUdfList(UDF_SD_RESPONSIBLE_PARTY, RESPONSIBLE_PARTY_SUPPLIER)
                .isCorrectUdfList(UDF_ROLE_CURRENT, MANAGER_CLIENT_ROLE_CURRENT);
    }

    @Test(groups = {"PROC_SLAFEATURE", "Regression"}
            , description = "Передать на проверку клиенту (Менеджер клиента)"
            , dependsOnMethods = "finish")
    void toClientTest() {
        apiController.updateToken(at_support_manager);
        udf = refreshUdf();
        task.refreshTask();

        udf.setUdfList(generateUdfList(UDF_SD_MUSTSETTINGINSTAL, NO_MUST));

        task.refreshUdf(udf);

        slaFeatureController.performCommonOperation(task, TOCLIENTTEST);
        ApiAsserts.assertThat(slaFeatureController.getResponse())
                .isCorrectResponseCode(HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isCorrectStatus(STATUS_SLAFEATURE_PREACCEPT)
                .isEquals(task);
    }

    @Test(groups = {"PROC_SLAFEATURE", "Regression"}
            , description = "Утвердить доработку (КЛИЕНТ)"
            , dependsOnMethods = "toClientTest")
    void acceptFeature() {
        apiController.updateToken(role_client);
        udf = refreshUdf();
        task.refreshTask();

        slaFeatureController.performCommonOperation(task, ACCEPTFEATURE);
        ApiAsserts.assertThat(slaFeatureController.getResponse())
                .isCorrectResponseCode(HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isCorrectStatus(STATUS_SLAFEATURE_COMPLETEDWORK)
                .isEquals(task);

        apiController.receiveTask(task.getNumber());
        CommonAssert.assertThat(apiController.getResponse())
                .isCorrectUdfList(UDF_SDFEATURE_STAGE, STAGE_PRESHIP)
                .isCorrectUdfList(UDF_SD_RESPONSIBLE_PARTY, RESPONSIBLE_PARTY_SUPPLIER)
                .isCorrectUdfList(UDF_ROLE_CURRENT, MANAGER_CLIENT_ROLE_CURRENT);
    }

    @Test(groups = {"PROC_SLAFEATURE", "Regression"}
            , description = "Отправить патч (Менеджер клиента)"
            , dependsOnMethods = "acceptFeature")
    void send() {
        apiController.updateToken(at_support_manager);
        udf = refreshUdf();
        task.refreshTask();

        udf.setUdfList(generateUdfList(UDF_SDFEATURE_DOCREVISION, DOC_REVISION_YES));
        udf.setSecondUdfList(generateUdfList(UDF_SD_MUSTSETTINGINSTAL, NO_MUST));

        task.refreshUdf(udf);

        slaFeatureController.performCommonOperation(task, SEND);
        ApiAsserts.assertThat(slaFeatureController.getResponse())
                .isCorrectResponseCode(HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isCorrectStatus(STATUS_SLAFEATURE_ACCEPT)
                .isEquals(task);

        apiController.receiveTask(task.getNumber());
        CommonAssert.assertThat(apiController.getResponse())
                .isCorrectUdfList(UDF_SDFEATURE_STAGE, STAGE_ACCEPT)
                .isCorrectUdfList(UDF_SD_RESPONSIBLE_PARTY, RESPONSIBLE_PARTY_CLIENT)
                .isCorrectUdfList(UDF_ROLE_CURRENT, CLIENT_ROLE_CURRENT);
    }

    @Test(groups = {"PROC_SLAFEATURE", "Regression"}
            , description = "Установить в производственную среду (КЛИЕНТ)"
            , dependsOnMethods = "send")
    void install() {
        apiController.updateToken(role_client);
        udf = refreshUdf();
        task.refreshTask();

        udf.setUdfList(generateUdfList(UDF_EVALUATING_REQUEST_EXECUTION, FIVE));

        udf.setUdfString(generateUdfString(UDF_EVALUATING_REQUEST_COMMENT, generateString()));

        task.refreshUdf(udf);

        slaFeatureController.performCommonOperation(task, INSTALL);
        ApiAsserts.assertThat(slaFeatureController.getResponse())
                .isCorrectResponseCode(HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isCorrectStatus(STATUS_SLAFEATURE_INSTALLED)
                .isEquals(task);

        apiController.receiveTask(task.getNumber());
        CommonAssert.assertThat(apiController.getResponse())
                .isCorrectUdfList(UDF_SD_RESPONSIBLE_PARTY, RESPONSIBLE_PARTY_SUPPLIER)
                .isCorrectUdfList(UDF_ROLE_CURRENT, ACCOUNT_MANAGER_LIST)
                .isCorrectUdfList(UDF_SDFEATURE_STAGE, STAGE_PAY);
    }

    @Test(groups = {"PROC_SLAFEATURE", "Regression"}
            , description = "Закрыть (ACCOUNT-MANAGER)"
            , dependsOnMethods = "install")
    void close() {
        apiController.updateToken(at_support_costmanager);
        udf = refreshUdf();
        task.refreshTask();

        udf.setUdfList(generateUdfList(UDF_SDFEATURE_DOCREVISION, DOC_REVISION_YES));
        udf.setSecondUdfList(generateUdfList(UDF_SDFEATURE_ANALYSISSPEED, FAST));
        udf.setThirdUdfList(generateUdfList(UDF_SDFEATURE_WORKSPEED, FAST_WORK));
        udf.setFourthUdfList(generateUdfList(UDF_SDFEATURE_WORKQUALITY, GOOD));

        task.refreshUdf(udf);

        slaFeatureController.performCommonOperation(task, CLOSE);
        ApiAsserts.assertThat(slaFeatureController.getResponse())
                .isCorrectResponseCode(HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isCorrectStatus(STATUS_SLAFEATURE_CLOSED)
                .isEquals(task);

        apiController.receiveTask(task.getNumber());
        CommonAssert.assertThat(apiController.getResponse())
                .isCorrectUdfList(UDF_SDFEATURE_STAGE, STAGE_FINISH)
                .isCorrectUdfList(UDF_SD_RESPONSIBLE_PARTY, RESPONSIBLE_PARTY_NOBODY);
    }
}
