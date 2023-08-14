package com.ts.integration.tests.proc_work_task.dev_task_sanction;

import com.ts.common.application.controllers.TrackStudioHttpStatusCodes;
import com.ts.common.application.database.DbQueryHelper;
import com.ts.common.application.database.dbEntities.GrTaskDbEntity;
import com.ts.common.application.database.dbTables.GrTaskTable;
import com.ts.common.asserts.ApiAsserts;
import com.ts.common.asserts.CommonAssert;
import com.ts.common.controllers.TaskResponseBody;
import com.ts.common.controllers.advice.AdviceController;
import com.ts.common.controllers.dev.DevTaskController;
import com.ts.common.entitites.commonEntities.*;
import com.ts.common.entitites.commonEntities.udf.UdfTask;
import com.ts.common.entitites.tasks.GeneralTask;
import com.ts.common.enums.Operations;
import com.ts.common.enums.TaskType;
import com.ts.common.utils.DateUtils;
import com.ts.common.utils.InitEntities;
import com.ts.integration.tests.BaseIntegrationTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import static com.ts.common.application.database.DbQueryHelper.Operators.*;
import static com.ts.common.entitites.commonEntities.List.Constants.*;
import static com.ts.common.entitites.commonEntities.Task.Constants.*;
import static com.ts.common.entitites.commonEntities.Udfs.UdfSd.*;
import static com.ts.common.entitites.commonEntities.User.Constants.ABDULLAEV_BAHODIR;
import static com.ts.common.entitites.commonEntities.User.Constants.BABUSHKIN_IVAN;
import static com.ts.common.enums.Operations.*;
import static com.ts.common.enums.TaskStatuses.*;
import static com.ts.common.utils.InitEntities.*;
import static com.ts.common.utils.RandomUtils.generateString;

public class DevTaskSanctionTest extends BaseIntegrationTest {
    public DevTaskController devTaskController;
    private GeneralTask task;
    private String misService;
    private String cdpBl;
    private UdfTask productTask;
    private UdfTask bdkuTask;
    private Parent parent;
    private GrTaskTable grTaskTable;
    private GrTaskDbEntity parentTaskFromDb;
    private String expectedCompletionDate;
    private String expectedCompletionDate2;
    private String expectedCompletionDate3;
    private String plannedStartDate;

    private UdfTask customerRequest;
    private User creator;
    private User handlerUser;
    private Integer estimationLaborInput;
    private Integer initialAssessmentLaborIntensity;

    private com.ts.common.entitites.tasks.Task catSanctionTask;
    private Map<List.Constants, String> prgAreas;


    @BeforeClass(alwaysRun = true)
    public void beforeClass() {
        prgAreas = new HashMap<>();
        devTaskController = apiController.getDevTaskController();
        grTaskTable = dbHelper.getGrTaskTable();
        parentTaskFromDb = (GrTaskDbEntity) grTaskTable.receiveRandomTask(
                "task_category", EQUAL.operator, "CAT_GENPLAN",
                AND.operator,
                "task_status", EQUAL.operator, STATUS_PROJECT_PLANNED.name(),
                AND.operator,
                "task_path", LIKE.operator, "%/2405/758009%");

        parent = InitEntities.generateParent(parentTaskFromDb.getTask_id(), parentTaskFromDb.getTask_number());
        task = InitEntities.getGeneralTask(TaskType.DEV_TASK, Operations.CAT);
        var tasks = devTaskController.getTaskForSDRequest(parent.getNumber());
        productTask = tasks.get("UDF_PRODUCT");
        bdkuTask = tasks.get("UDF_BDKU_CONFIGURATION");
        cdpBl = devTaskController.getCdpBl();
        var taskSlaBug = (GrTaskDbEntity) grTaskTable.receiveByCategory("CAT_SLABUG");
        customerRequest = InitEntities.generateUdfTask(UDF_WORKTASK_SDREQUEST, new Task(taskSlaBug.getTask_id(), taskSlaBug.getTask_number()));
        misService = devTaskController.getMisService();

        var taskEmployees = userController.receiveUserByTask(parent.getNumber());
        System.out.println("********* " + taskEmployees.size());
        creator = userController.receiveUserByRole(taskEmployees, "Менеджер проекта", "root").getForUser();
        handlerUser = userController.receiveUserByRole(taskEmployees, "Участник проекта", creator.getLogin()).getForUser();
    }


    @Test(groups = {"DevTask", "Regression"}, description = "Создание запроса на разработку")
    public void devTask() {
        apiController.updateToken(InitEntities.generateAuthToken(creator));
        task.setParent(parent);
        task.setName("Создание запроса на разработку");
        task.setDescription(task.getDescription() + generateString());
        task.setHandlerUser(handlerUser);

        udf = refreshUdf();
        udf.setEighthUdfList(generateUdfList(UDF_WORKTASK_COMPLEXITYLEVEL, TASK_LEVEL_7));
        udf.setUdfUser(generateUdfUser(UDF_WORKTASK_SUPERVISER, ABDULLAEV_BAHODIR));
        udf.setSecondUdfUser(generateUdfUser(UDF_WATCHER, BABUSHKIN_IVAN));
        if (cdpBl != null) {
            udf.setUdfList(generateUdfList(UDF_CDP_BL, cdpBl));
        }
        udf.setUdfTask(generateUdfTask(UDF_SD_MODULE, CORE));
        udf.setUdfString(generateUdfString(UDF_SD_NOMODULE_REASON, generateString()));
        udf.setSecondUdfList(generateUdfList(UDF_CDP_ACCEPTANCE, REQBYCONRTOLLER));
        udf.setUdfDate(generateUdfDate(UDF_WORKTASK_ANALYSISFD, 1));
        udf.setSecondUdfString(generateUdfString(UDF_WORKTASK_BRANCH, "hg:apng:default [Аnalitic platform new generation]"));
        udf.setSecondUdfTask(generateUdfTask(UDF_PRODUCT, productTask.getTaskValue()[0]));
        udf.setThirdUdfList(generateUdfList(UDF_WORKTASK_WAYCODEREVIEW, WAY_CODE_REVIEW_NO));
        udf.setFourthUdfList(generateUdfList(UDF_SDFEATURE_GENUSE, GENERAL, "{\"username\":\"babdullayev\",\"name\":\"Абдуллаев Баходир\"}"));
        udf.setThirdUdfString(generateUdfString(UDF_WORKTASK_ANNOTATION, generateString()));
        udf.setFifthUdfList(generateUdfList(UDF_WORKTASK_CHANGEWORKERINRQST, YES_AND_LEAVE_THIS_ROLE_TO_THE_CURRENT_PERFORMER));
        udf.setSixthUdfList(generateUdfList(UDF_WORKTASK_ADDWATCHERINREQST, UDF_WORKTASK_ADDWATCHERINREQST_YES));
        udf.setSeventhUdfList(generateUdfList(UDF_WORKTASK_ADDTRSTWATCHINREQST, UDF_WORKTASK_ADDTRSTWATCHINREQST_YES));
        udf.setFourthUdfTask(generateUdfTask(UDF_BDKU_CONFIGURATION, bdkuTask.getTaskValueSelector()[0]));
        udf.setSixthUdfTask(generateUdfTask(UDF_WORKTASK_DEPENDBF, MTBANK));
        udf.setSeventhUdfTask(generateUdfTask(UDF_SD_LINKEDREQUEST, AKKREDITIVES));
        System.out.println("**** MIS_SERVICE: " + misService);
        if (misService != null)
            udf.setNinethUdfList(generateUdfList(UDF_MIS_SERVICE, misService));
        task.refreshUdf(udf);
        devTaskController.createDevTask(task);
        ApiAsserts.assertThat(devTaskController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isCorrectStatus(STATUS_WORKTASK_ONANALYSIS)
                .isEquals(task);
    }

    @Test(groups = {"DevTask", "Regression"}, description = "Коррекция плана", dependsOnMethods = "devTask")
    public void changePlan() {
        apiController.updateToken(generateAuthToken(handlerUser));
        udf = refreshUdf();

        expectedCompletionDate = DateUtils.getCurrentDate(0);
        udf.setUdfDate(generateUdfDate(UDF_WORKTASK_AWAITTD, expectedCompletionDate));
        udf.setUdfDouble(generateUdfDouble(UDF_WORKTASK_AWAITBUDGET, 2));
        udf.setSecondUdfDouble(generateUdfDouble(UDF_CDP_NORMBUDGET, 2));
        udf.setUdfMemo(generateUdfMemo(UDF_CDP_STEPPLAN,
                "[{\"id\":\"8181816c88683b1801894f980bef6f6a\",\"name\":\"Предварительный анализ\",\"order\":0,\"taskId\":\"8181816c88683b1801894f97fa9a6f43\",\"weight\":0,\"budget\":7200,\"progress\":0,\"description\":\"\",\"status\":\"ACTUAL\",\"workTypeId\":\"402881c2516c220101516c711ff80024\",\"workTypeNorm\":2,\"hrs\":0,\"deletable\":true,\"actualBudget\":0,\"planby\":\"budget\",\"workTypeAsString\":\"[0701] Иное\",\"workTypeDraftAsString\":\"-\",\"draftChanged\":true,\"orderDraft\":0,\"nameDraft\":\"Предварительный анализ\",\"descriptionDraft\":\"\",\"statusDraft\":\"ACTUAL\",\"weightDraft\":0,\"budgetDraft\":7200,\"budgetHrs\":2,\"budgetMinutes\":0,\"workTypeIdDraft\":\"402881c2516c220101516c711ff80024\",\"workTypeNormDraft\":2}]",
                "{\"autoFinishAnalysis\": \"true\"}"));
        udf.setSecondUdfMemo(generateUdfMemo(UDF_CDP_STEPPROGRESS, "[{}]"));
        udf.setUdfList(generateUdfList(UDF_CDP_WT, SOFTWARE_DESIGN));
        task.refreshUdf(udf);
        task.setConfirmed(true);
        devTaskController.performCommonOperation(task, CHANGE_PLAN);
        ApiAsserts.assertThat(devTaskController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isCorrectStatus(STATUS_WORKTASK_ASSIGNED);

        var response = apiController.receiveTask(task.getNumber());
        CommonAssert
                .assertThat(response)
                .isCorrectUdfDouble(UDF_WORKTASK_PLANBUDGET, 2.0)
                .isCorrectUdfDouble(UDF_CDP_NORMBUDGET, 2.0)
                .isCorrectUdfDate(UDF_WORKTASK_PLANTD, expectedCompletionDate);
    }

    @Test(groups = {"DevTask", "Regression"}, description = "Принять в работу", dependsOnMethods = "changePlan")
    public void taskStart() {
        apiController.updateToken(generateAuthToken(handlerUser));
        task.setHandlerUser(handlerUser);
        task.refreshUdf();
        udf.setUdfMemo(generateUdfMemo(UDF_CDP_STEPPROGRESS, "[{}]"));
        udf.setUdfUser(generateUdfUser(STDT_HANDLER, handlerUser));
        devTaskController.performCommonOperation(task, ACCEPT_IN_WORK);
        ApiAsserts.assertThat(devTaskController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isCorrectStatus(STATUS_WORKTASK_INWORK);
    }

    @Test(groups = {"DevTask", "Regression"}, description = "Связь с ККПО", dependsOnMethods = "taskStart")
    public void changePrgArea() {
        apiController.updateToken(generateAuthToken(handlerUser));
        task.refreshUdf();
        udf.setUdfList(generateUdfList(UDF_PRGAREA, UDF_PRGAREA_BNK));
        task.refreshUdf(udf);
        devTaskController.performCommonOperation(task, WORKTASK_CHANGEPRGAREA);
        ApiAsserts.assertThat(devTaskController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isCorrectStatus(STATUS_WORKTASK_INWORK);

        catSanctionTask = apiController.receiveSubTaskByCategory(task.getNumber(), "CAT_SANCTION");

        var response = apiController.receiveTask(catSanctionTask.getNumber());
        CommonAssert
                .assertThat(response)
                .isCorrectTaskStatus(STATUS_ADVICE_AWAIT)
                .isCorrectSubmitterUser(handlerUser.getLogin())
                .isCorrectUdfList(UDF_PRGAREA, UDF_PRGAREA_BNK.id)
                .isCorrectTaskDescription("Запрос на санкционирование КПО BNK по задаче", task.getNumber());
    }

    @Test(groups = {"DevTask", "Regression"}, description = "Санкционировать привязку КПО в CAT_SANCTION", dependsOnMethods = "changePrgArea")
    public void allowKPO() {
        apiController.updateToken(generateAuthToken(catSanctionTask.getHandlerUser()));
        var subTask = new GeneralTask();
        udf = refreshUdf();
        prgAreas.put(UDF_PRGAREA_BNK, "{\"prgguid\":\"" + UDF_PRGAREA_BNK.id +
                "\",\"prgcode\":\"BNK\",\"reviewfl\":false,\"reviewmode\":\"NBL\",\"construser\":[],\"subconstruser\":[],\"nearestConstruser\":[],\"nearestSubconstruser\":[],\"allowfl\":\"1\"}");
        udf.setUdfMultiList(generateUdfMultiList(UDF_PRGAREA, prgAreas));

        subTask.refreshUdf(udf);
        subTask.setId(catSanctionTask.getId());
        subTask.setNumber(catSanctionTask.getNumber());
        subTask.setAttachments(new String[0]);
        var adviceController = apiController.getAdviceController();
        adviceController.performCommonOperation(subTask, ADVICE_ALLOWKPO);
        ApiAsserts.assertThat(adviceController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isCorrectStatus(STATUS_ADVICE_CLOSED);
    }

    @Test(groups = {"DevTask", "Regression"}, description = "Связь с ККПО", dependsOnMethods = "allowKPO")
    public void changePrgArea1() {
        apiController.updateToken(generateAuthToken(handlerUser));
        task.refreshUdf();
        prgAreas.put(UDF_PRGAREA_CDW, "{\"longname\":\"Хранилище данных Colvir\",\"parent\":\"\",\"stdFl\":false,\"isdata\":false,\"stdt\":\"0\",\"selectable\":true,\"archive\":false,\"inheritFl\":true,\"reviewfl\":true,\"reviewFl\":false,\"groupfl\":false}");
        prgAreas.put(UDF_PRGAREA_ISB, "{\"longname\":\"Исламский банкинг\",\"parent\":\"\",\"stdFl\":false,\"isdata\":false,\"stdt\":\"0\",\"selectable\":true,\"archive\":false,\"inheritFl\":true,\"reviewfl\":true,\"reviewFl\":false,\"groupfl\":false}");
        udf.setUdfMultiList(generateUdfMultiList(UDF_PRGAREA, prgAreas));
        task.refreshUdf(udf);
        devTaskController.performCommonOperation(task, WORKTASK_CHANGEPRGAREA);
        ApiAsserts.assertThat(devTaskController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isCorrectStatus(STATUS_WORKTASK_INWORK);

        catSanctionTask = apiController.receiveSubTaskByCategory(task.getNumber(), "CAT_SANCTION");

        var response = apiController.receiveTask(catSanctionTask.getNumber());
        CommonAssert
                .assertThat(response)
                .isCorrectTaskStatus(STATUS_ADVICE_AWAIT)
                .isCorrectSubmitterUser(handlerUser.getLogin())
                .isCorrectUdfList(UDF_PRGAREA, UDF_PRGAREA_BNK.id)
                .isCorrectTaskDescription("Запрос на санкционирование КПО BNK по задаче", task.getNumber());
    }

    @Test(groups = {"DevTask", "Regression"}, description = "Связь с ККПО", dependsOnMethods = "changePrgArea1")
    public void changePrgArea2() {
        apiController.updateToken(generateAuthToken(handlerUser));
        task.refreshUdf();
        prgAreas.remove(UDF_PRGAREA_ISB);
        udf.setUdfMultiList(generateUdfMultiList(UDF_PRGAREA, prgAreas));
        task.refreshUdf(udf);
        devTaskController.performCommonOperation(task, WORKTASK_CHANGEPRGAREA);
        ApiAsserts.assertThat(devTaskController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isCorrectStatus(STATUS_WORKTASK_INWORK);

        catSanctionTask = apiController.receiveSubTaskByCategory(task.getNumber(), "CAT_SANCTION");

        var response = apiController.receiveTask(catSanctionTask.getNumber());
        CommonAssert
                .assertThat(response)
                .isCorrectTaskStatus(STATUS_ADVICE_AWAIT)
                .isCorrectSubmitterUser(handlerUser.getLogin())
                .isCorrectUdfList(UDF_PRGAREA, UDF_PRGAREA_BNK.id)
                .isCorrectTaskDescription("Запрос на санкционирование КПО BNK по задаче", task.getNumber());
    }
}
