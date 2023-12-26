package com.ts.integration.tests.proc_work_task.conversion.admin_job;

import com.ts.common.application.controllers.TrackStudioHttpStatusCodes;
import com.ts.common.application.database.dbEntities.GrTaskDbEntity;
import com.ts.common.application.database.dbTables.GrTaskTable;
import com.ts.common.asserts.ApiAsserts;
import com.ts.common.asserts.CommonAssert;
import com.ts.common.controllers.TaskResponseBody;
import com.ts.common.controllers.workTask.WorkTaskController;
import com.ts.common.entitites.commonEntities.Parent;
import com.ts.common.entitites.commonEntities.Task;
import com.ts.common.entitites.commonEntities.Udfs;
import com.ts.common.entitites.commonEntities.User;
import com.ts.common.entitites.commonEntities.udf.UdfTask;
import com.ts.common.entitites.tasks.GeneralTask;
import com.ts.common.enums.Operations;
import com.ts.common.enums.TaskType;
import com.ts.common.utils.InitEntities;
import com.ts.integration.tests.BaseIntegrationTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static com.ts.common.application.database.DbQueryHelper.Operators.*;
import static com.ts.common.entitites.commonEntities.List.Constants.*;
import static com.ts.common.entitites.commonEntities.Task.Constants.CORE;
import static com.ts.common.entitites.commonEntities.Udfs.UdfSd.*;
import static com.ts.common.enums.Operations.CHANGE_CAT;
import static com.ts.common.enums.TaskStatuses.STATUS_PROJECT_PLANNED;
import static com.ts.common.enums.TaskStatuses.STATUS_WORKTASK_ONANALYSIS;
import static com.ts.common.utils.InitEntities.*;
import static com.ts.common.utils.RandomUtils.generateComment;

public class AdminJobConvertToChangeCatTest extends BaseIntegrationTest {
    public WorkTaskController workTaskController;
    private GeneralTask task;
    private String MIS_SERVICE;
    private String CDP_BL;
    private Parent parent;
    private GrTaskTable grTaskTable;
    private GrTaskDbEntity parentTaskFromDb;
    private UdfTask UDF_WORKTASK_SDREQUEST;
    private User creator;
    private User handlerUser;
    private Task udf_product;

    @BeforeClass(alwaysRun = true)
    public void beforeClass() {
        workTaskController = apiController.getWorkTaskController();
        userController = apiController.getUserController();
        grTaskTable = dbHelper.getGrTaskTable();
        parentTaskFromDb =
                (GrTaskDbEntity) grTaskTable.receiveRandomTask("task_category", EQUAL.operator, "CAT_GENPLAN", AND.operator, "task_status", EQUAL.operator, STATUS_PROJECT_PLANNED.name(), AND.operator, "task_path", LIKE.operator, "%/2405/758009%");
        parent = InitEntities.generateParent(parentTaskFromDb.getTask_id(), parentTaskFromDb.getTask_number());
        var parentPayload = workTaskController.getParentPayload(parent.getNumber(), "CAT_ADMINJOB");
        task = InitEntities.getGeneralTask(TaskType.ADMINJOB, Operations.CAT);
        var taskSlaBug = (GrTaskDbEntity) grTaskTable.receiveByCategory("CAT_SLABUG");
        var product = (GrTaskDbEntity) grTaskTable.receiveRandomTask("task_category", EQUAL.operator, "CAT_PRODUCT", AND.operator, "task_status", EQUAL.operator, "STATUS_WORK_ACTIVE");
        udf_product = new Task(product.getTask_id(), product.getTask_number());
        UDF_WORKTASK_SDREQUEST = InitEntities.generateUdfTask(Udfs.UdfSd.UDF_WORKTASK_SDREQUEST, new Task(taskSlaBug.getTask_id(), taskSlaBug.getTask_number()));
        MIS_SERVICE = workTaskController.getParent_UDF_MIS_SERVICE(parentPayload).get(0);
        CDP_BL = workTaskController.getParent_UDF_CDP_BL(parentPayload).get(0);
        var employees = userController.receiveUserByTask(parent.getNumber());
        creator = employees.stream().filter(f -> f.getAssignedRole().getName().equals("Менеджер проекта")).findFirst().get().getForUser();
        handlerUser = employees.stream().filter(f -> f.getAssignedRole().getName().equals("Участник проекта") && !f.getForUser().getLogin().equals(creator.getLogin())).findFirst().get().getForUser();
    }


    @Test(groups = {"WorkTask", "Regression"}, description = "создание Административная задача")
    public void createTask() {
        apiController.updateToken(InitEntities.generateAuthToken(creator));
        udf = refreshUdf();
        task.refreshTask();
        task.setParent(parent);
        task.setName("Административная задача: " + generateComment());
        task.setHandlerUser(handlerUser);
        if (CDP_BL != null) {
            udf.setSecondUdfList(generateUdfList(UDF_CDP_BL, CDP_BL));
        }
        udf.setUdfTask(generateUdfTask(UDF_SD_MODULE, CORE));
        if (MIS_SERVICE != null) udf.setThirdUdfList(generateUdfList(UDF_MIS_SERVICE, MIS_SERVICE));
        udf.setFourthUdfList(generateUdfList(UDF_CDP_ACCEPTANCE, REQBYAUTHOR));
        udf.setFifthUdfList(generateUdfList(UDF_WORKTASK_ANALYSIS, YES_V2));
        udf.setUdfDate(generateUdfDate(UDF_WORKTASK_ANALYSISFD, 0));
        udf.setSecondUdfTask(UDF_WORKTASK_SDREQUEST);
        udf.setSixthUdfList(generateUdfList(UDF_WORKTASK_CHANGEWORKERINRQST, CHANGEWORKERINRQST_NO));
        task.refreshUdf(udf);
        workTaskController.createAbstractWorkTask(task);
        var response = workTaskController.getResponse();
        ApiAsserts.assertThat(response).isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK).isParseableBody(TaskResponseBody.class).assertTask().isCorrectStatus(STATUS_WORKTASK_ONANALYSIS).isEquals(task);
    }

    @Test(groups = {"WorkTask", "Regression"}, description = "Изменить категорию на доработку", dependsOnMethods = "createTask")
    public void changeCategory() {
        udf = refreshUdf();
        task.refreshTask();
        apiController.updateToken(generateAuthToken(creator));

        task.refreshUdf(udf);
        udf.setUdfDate(generateUdfDate(UDF_WORKTASK_ANALYSISFD, 7));
        udf.setUdfTask(generateUdfTask(UDF_PRODUCT, udf_product));
        udf.setUdfList(generateUdfList(UDF_SDFEATURE_GENUSE, GENERAL, "{\"username\":\"" + handlerUser.getLogin() + "\",\"name\":\"" + handlerUser.getName() + "\"}"));
        udf.setUdfMemo(generateUdfMemo(UDF_CDP_STEPPROGRESS, "[{\"id\":\"818181698c3fa358018c9fcfbddf1c01\",\"name\":\"Предварительный анализ\",\"order\":0,\"taskId\":\"" + task.getId() +
                "\",\"weight\":0,\"budget\":7200,\"progress\":0,\"description\":\"\",\"status\":\"ACTUAL\",\"workTypeId\":\"402881c2516c220101516c711ff80024\",\"workTypeNorm\":2.0,\"hrs\":0,\"deletable\":true,\"actualBudget\":0,\"planby\":\"budget\",\"draftChanged\":true,\"workTypeAsString\":\"[0701] Иное\",\"workTypeDraftAsString\":\"-\"}]"));

        workTaskController.performCommonOperation(task, CHANGE_CAT);
        ApiAsserts.assertThat(workTaskController.getResponse()).isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK).isParseableBody(TaskResponseBody.class).assertTask();

        var taskDetail = apiController.receiveTask(task.getNumber());
        CommonAssert
                .assertThat(taskDetail)
                .isCorrectTaskCategory("CAT_DEVTASK")
                .isCorrectUdfTask(UDF_PRODUCT, udf_product.getNumber())
                .isCorrectUdfList(UDF_SDFEATURE_GENUSE, GENERAL.getId());
    }
}

