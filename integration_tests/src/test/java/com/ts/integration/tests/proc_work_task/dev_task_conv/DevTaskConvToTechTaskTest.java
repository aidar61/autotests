package com.ts.integration.tests.proc_work_task.dev_task_conv;

import com.ts.common.application.controllers.TrackStudioHttpStatusCodes;
import com.ts.common.application.database.dbEntities.GrTaskDbEntity;
import com.ts.common.application.database.dbTables.GrTaskTable;
import com.ts.common.asserts.ApiAsserts;
import com.ts.common.asserts.CommonAssert;
import com.ts.common.controllers.TaskResponseBody;
import com.ts.common.controllers.workTask.DevTaskController;
import com.ts.common.entitites.commonEntities.Parent;
import com.ts.common.entitites.commonEntities.Task;
import com.ts.common.entitites.commonEntities.User;
import com.ts.common.entitites.commonEntities.udf.UdfTask;
import com.ts.common.entitites.tasks.GeneralTask;
import com.ts.common.enums.Operations;
import com.ts.common.enums.TaskType;
import com.ts.common.utils.DateUtils;
import com.ts.common.utils.InitEntities;
import com.ts.integration.tests.BaseIntegrationTest;
import io.restassured.path.json.JsonPath;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static com.ts.common.application.database.DbQueryHelper.Operators.*;
import static com.ts.common.entitites.commonEntities.List.Constants.*;
import static com.ts.common.entitites.commonEntities.Task.Constants.AKKREDITIVES;
import static com.ts.common.entitites.commonEntities.Task.Constants.CORE;
import static com.ts.common.entitites.commonEntities.Udfs.UdfSd.*;
import static com.ts.common.entitites.commonEntities.User.Constants.ABDULLAEV_BAHODIR;
import static com.ts.common.entitites.commonEntities.User.Constants.BABUSHKIN_IVAN;
import static com.ts.common.enums.Operations.CHANGE_CAT_TO_BUG_TASK;
import static com.ts.common.enums.Operations.CHANGE_CAT_TO_TECH_TASK;
import static com.ts.common.enums.TaskStatuses.STATUS_PROJECT_PLANNED;
import static com.ts.common.enums.TaskStatuses.STATUS_WORKTASK_ONANALYSIS;
import static com.ts.common.utils.InitEntities.*;
import static com.ts.common.utils.RandomUtils.generateComment;
import static com.ts.common.utils.RandomUtils.generateString;

public class DevTaskConvToTechTaskTest extends BaseIntegrationTest {
    public DevTaskController devTaskController;
    private GeneralTask task;
    private String misService;
    private String cdpBl;
    private UdfTask productTask;
    private UdfTask bdkuTask;
    private Parent parent;
    private GrTaskTable grTaskTable;
    private GrTaskDbEntity parentTaskFromDb;
    private UdfTask customerRequest;
    private User creator;
    private User handlerUser;
    private String moduleReason;
    private String workTaskAnnotation;
    java.util.List<String> branches;


    @BeforeClass(alwaysRun = true)
    public void beforeClass() {
        devTaskController = apiController.getDevTaskController();
        userController = apiController.getUserController();
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
        var taskSlaBug = (GrTaskDbEntity) grTaskTable.receiveByCategory("CAT_SLABUG");
        customerRequest = InitEntities.generateUdfTask(UDF_WORKTASK_SDREQUEST, new Task(taskSlaBug.getTask_id(), taskSlaBug.getTask_number()));
        misService = devTaskController.getMisService().get(0);
        cdpBl = devTaskController.getCdpBl();
        var employees = userController.receiveUserByTask(parent.getNumber());
        creator = employees.stream().filter(f -> f.getAssignedRole().getName().equals("Менеджер проекта")).findFirst().get().getForUser();
        handlerUser = employees.stream().filter(f -> f.getAssignedRole().getName().equals("Участник проекта") && !f.getForUser().getLogin().equals(creator.getLogin())).findFirst().get().getForUser();
        var parentTaskPayload = apiController.receiveParentTaskPayload(parent.getNumber(), "CAT_DEVTASK").asString().replace("\\&", "\\\\&");
        branches = new JsonPath(parentTaskPayload).getList("udfs.UDF_WORKTASK_BRANCH.stringValueSelector", String.class);
    }


    @Test(groups = {"DevTask", "Regression"}, description = "Создание запроса на разработку")
    public void devTask() {
        udf = refreshUdf();
        task.refreshTask();
        apiController.updateToken(InitEntities.generateAuthToken(creator));
        task.setParent(parent);
        task.setName("Создание запроса на разработку: " + generateComment());
        task.setDescription(task.getDescription() + generateString());
        task.setHandlerUser(handlerUser);
        udf.setEighthUdfList(generateUdfList(UDF_WORKTASK_COMPLEXITYLEVEL, TASK_LEVEL_7));
        udf.setUdfUser(generateUdfUser(UDF_WORKTASK_SUPERVISER, ABDULLAEV_BAHODIR));
        udf.setSecondUdfUser(generateUdfUser(UDF_WATCHER, BABUSHKIN_IVAN));
        if (cdpBl != null) {
            udf.setUdfList(generateUdfList(UDF_CDP_BL, cdpBl));
        }
        moduleReason = generateString();
        udf.setUdfTask(generateUdfTask(UDF_SD_MODULE, CORE));
        udf.setUdfString(generateUdfString(UDF_SD_NOMODULE_REASON, moduleReason));
        udf.setSecondUdfList(generateUdfList(UDF_CDP_ACCEPTANCE, UDF_CDP_ACCEPTANCE_NO));
        udf.setUdfDate(generateUdfDate(UDF_WORKTASK_ANALYSISFD, 0));
        udf.setSecondUdfTask(generateUdfTask(UDF_PRODUCT, productTask.getTaskValue()[0]));
        udf.setThirdUdfList(generateUdfList(UDF_WORKTASK_WAYCODEREVIEW, WAY_CODE_REVIEW_OFF));
        udf.setFourthUdfList(generateUdfList(UDF_SDFEATURE_GENUSE, GENERAL, "{\"username\":\"babdullayev\",\"name\":\"Абдуллаев Баходир\"}"));
        workTaskAnnotation = generateString();
        udf.setThirdUdfString(generateUdfString(UDF_WORKTASK_ANNOTATION, workTaskAnnotation));
        udf.setFifthUdfList(generateUdfList(UDF_WORKTASK_CHANGEWORKERINRQST, YES_AND_LEAVE_THIS_ROLE_TO_THE_CURRENT_PERFORMER));
        udf.setSixthUdfList(generateUdfList(UDF_WORKTASK_ADDWATCHERINREQST, UDF_WORKTASK_ADDWATCHERINREQST_YES));
        udf.setSeventhUdfList(generateUdfList(UDF_WORKTASK_ADDTRSTWATCHINREQST, UDF_WORKTASK_ADDTRSTWATCHINREQST_YES));
        udf.setFourthUdfTask(generateUdfTask(UDF_BDKU_CONFIGURATION, bdkuTask.getTaskValueSelector()[0]));
        udf.setSeventhUdfTask(generateUdfTask(UDF_SD_LINKEDREQUEST, AKKREDITIVES));
        udf.setFifthUdfTask(customerRequest);
        if (misService != null)
            udf.setNinethUdfList(generateUdfList(UDF_MIS_SERVICE, misService));
        task.refreshUdf(udf);
        devTaskController.createDevTask(task);
        var response = devTaskController.getResponse();
        ApiAsserts.assertThat(response)
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isCorrectStatus(STATUS_WORKTASK_ONANALYSIS)
                .isEquals(task);
    }

    @Test(groups = {"DevTask", "Regression"}, description = "Изменить категорию на технологическую работу", dependsOnMethods = "devTask")
    public void changeConv() {
        udf = refreshUdf();
        task.refreshTask();
        apiController.updateToken(generateAuthToken(creator));
        task.setDescription(generateString());
        udf.setUdfList(generateUdfList(UDF_CDP_ACCEPTANCE, UDF_CDP_ACCEPTANCE_NO));
        udf.setUdfString(generateUdfString(UDF_WORKTASK_BRANCH, ""));
        task.refreshUdf(udf);
        devTaskController.performCommonOperation(task, CHANGE_CAT_TO_TECH_TASK);
        ApiAsserts.assertThat(devTaskController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isCorrectStatus(STATUS_WORKTASK_ONANALYSIS);

        var response = apiController.receiveTask(task.getNumber());
        CommonAssert
                .assertThat(response)
                .isCorrectTaskCategory("CAT_TECHTASK")
                .isCorrectUdfList(UDF_WORKTASK_COMPLEXITYLEVEL, TASK_LEVEL_7.getId())
                .isCorrectSubmitterUser(creator.getLogin())
                .isCorrectHandlerUser(handlerUser.getLogin())
                .isCorrectUdfUSer(UDF_WORKTASK_SUPERVISER, ABDULLAEV_BAHODIR.login)
                .isCorrectUdfUSer(UDF_WATCHER, BABUSHKIN_IVAN.login)
                .isCorrectUdfList(UDF_CDP_BL, cdpBl)
                .isCorrectUdfString(UDF_SD_NOMODULE_REASON, moduleReason)
                .isCorrectUdfTask(UDF_SD_LINKEDREQUEST, AKKREDITIVES)
                .isCorrectUdfList(UDF_MIS_SERVICE, misService)
                .isCorrectUdfList(UDF_CDP_ACCEPTANCE, UDF_CDP_ACCEPTANCE_NO.getId())
                .isCorrectUdfList(UDF_WORKTASK_ANALYSIS, UDF_WORKTASK_ANALYSIS_YES.getId())
                .isCorrectUdfDate(UDF_WORKTASK_ANALYSISFD, DateUtils.getCurrentDate(0))
                .isCorrectUdfList(UDF_SDFEATURE_GENUSE, GENERAL.getId())
                .isCorrectUdfMemo(UDF_WORKTASK_ANNOTATION, workTaskAnnotation)
                .isCorrectUdfTask(UDF_BDKU_CONFIGURATION, bdkuTask.getTaskValueSelector()[0].getNumber());
    }
}

