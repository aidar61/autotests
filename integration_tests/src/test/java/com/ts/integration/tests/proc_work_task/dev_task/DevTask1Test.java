package com.ts.integration.tests.proc_work_task.dev_task;

import com.ts.common.application.controllers.TrackStudioHttpStatusCodes;
import com.ts.common.application.database.dbEntities.GrTaskDbEntity;
import com.ts.common.application.database.dbTables.GrTaskTable;
import com.ts.common.asserts.ApiAsserts;
import com.ts.common.controllers.TaskResponseBody;
import com.ts.common.controllers.dev.DevTaskController;
import com.ts.common.entitites.commonEntities.Parent;
import com.ts.common.entitites.commonEntities.Task;
import com.ts.common.entitites.commonEntities.User;
import com.ts.common.entitites.tasks.GeneralTask;
import com.ts.common.enums.Operations;
import com.ts.common.enums.TaskType;
import com.ts.common.utils.InitEntities;
import com.ts.integration.tests.BaseIntegrationTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static com.ts.common.entitites.commonEntities.List.Constants.ANALITIK_PLATFORM;
import static com.ts.common.entitites.commonEntities.Task.Constants.AKKREDITIVES;
import static com.ts.common.entitites.commonEntities.Udfs.UdfSd.*;
import static com.ts.common.entitites.commonEntities.Udfs.UdfSd.UDF_WORKTASK_WORK;
import static com.ts.common.entitites.commonEntities.User.Constants.ABDULLAEV_BAHODIR;
import static com.ts.common.enums.TaskStatuses.STATUS_PROJECT_PLANNED;
import static com.ts.common.utils.InitEntities.*;
import static com.ts.common.utils.InitEntities.generateUdfTask;
import static com.ts.common.utils.RandomUtils.generateString;

public class DevTask1Test extends BaseIntegrationTest {
    public DevTaskController devTaskController;
    private GeneralTask task;
    private Parent parent;
    private GrTaskTable grTaskTable;
    private GrTaskDbEntity parentTaskFromDb;
    private Task parenOfParentTaskFromDb;
    private User HANDLER_USER_FROM_PARENT;

    @BeforeClass(alwaysRun = true)
    public void beforeClass() {
        devTaskController = apiController.getDevTaskController();
        grTaskTable = dbHelper.getGrTaskTable();
        parentTaskFromDb = (GrTaskDbEntity) grTaskTable.receiveByCategoryAndTaskStatus("CAT_GENPLAN", STATUS_PROJECT_PLANNED);
        parent = InitEntities.generateParent(parentTaskFromDb.getTask_id(), parentTaskFromDb.getTask_number());
        parenOfParentTaskFromDb = parentTaskFromDb.receiveParentTask();
        HANDLER_USER_FROM_PARENT = apiController.receiveGeneralTask(parent.getNumber()).getHandlerUser();
        task = InitEntities.getGeneralTask(TaskType.DEV_TASK, Operations.CAT);
    }

    @Test(groups = {"DevTask", "Regression"}, description = "Создание запроса на разработку")
    public void devTask() {
        apiController.updateToken(InitEntities.generateAuthToken(HANDLER_USER_FROM_PARENT));
        task.setParent(parent);
        task.setHandlerUser(HANDLER_USER_FROM_PARENT);
        task.setDescription(task.getDescription() + generateString());
        udf = refreshUdf();
        udf.setUdfUser(generateUdfUser(UDF_WORKTASK_SUPERVISER, ABDULLAEV_BAHODIR));
        udf.setSecondUdfUser(generateUdfUser(UDF_WATCHER, ABDULLAEV_BAHODIR));
        udf.setUdfList(generateUdfList(UDF_CDP_BL, ANALITIK_PLATFORM));
        udf.setUdfTask(generateUdfTask(UDF_SD_MODULE, AKKREDITIVES));
        udf.setUdfString(generateUdfString(UDF_SD_NOMODULE_REASON, generateString()));
        udf.setUdfDate(generateUdfDate(UDF_ADVICE_PLANTD));
        udf.setSecondUdfTask(generateUdfTask(UDF_WORKTASK_SDREQUEST, parenOfParentTaskFromDb));
        udf.setThirdUdfTask(generateUdfTask(UDF_WORKTASK_WORK, parentTaskFromDb.mapTo()));
        task.refreshUdf(udf);
        devTaskController.createDevTask(task);
        ApiAsserts.assertThat(devTaskController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isEquals(task);
    }
}
