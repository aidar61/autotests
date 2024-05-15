package com.ts.integration.tests.proc_advice.cat_sanction;

import com.ts.common.application.controllers.TrackStudioHttpStatusCodes;
import com.ts.common.application.database.dbEntities.GrTaskDbEntity;
import com.ts.common.application.database.dbTables.GrTaskTable;
import com.ts.common.asserts.ApiAsserts;
import com.ts.common.asserts.TaskAsserts;
import com.ts.common.controllers.TaskResponseBody;
import com.ts.common.controllers.advice.SanctionController;
import com.ts.common.entitites.commonEntities.Parent;
import com.ts.common.entitites.commonEntities.Task;
import com.ts.common.entitites.commonEntities.User;
import com.ts.common.entitites.tasks.GeneralTask;
import com.ts.common.enums.Operations;
import com.ts.common.enums.Resolutions;
import com.ts.common.enums.TaskType;
import com.ts.common.utils.InitEntities;
import com.ts.integration.tests.BaseIntegrationTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static com.ts.common.entitites.commonEntities.List.Constants.ANALITIK_PLATFORM;
import static com.ts.common.entitites.commonEntities.List.Constants.B;
import static com.ts.common.entitites.commonEntities.Task.Constants.AKKREDITIVES;
import static com.ts.common.entitites.commonEntities.Udfs.UdfSd.*;
import static com.ts.common.entitites.commonEntities.Udfs.UdfSd.UDF_WORKTASK_WORK;
import static com.ts.common.entitites.commonEntities.User.Constants.ABDULLAEV_BAHODIR;
import static com.ts.common.enums.TaskStatuses.*;
import static com.ts.common.utils.InitEntities.*;
import static com.ts.common.utils.InitEntities.generateUdfTask;
import static com.ts.common.utils.RandomUtils.generateString;

public class CatSanction1Test extends BaseIntegrationTest {
    public SanctionController sanctionController;
    private GeneralTask task;
    private Parent parent;
    private GrTaskTable grTaskTable;
    private GrTaskDbEntity parentTaskFromDb;
    private Task parenOfParentTaskFromDb;
    private User HANDLER_USER_FROM_PARENT;

    @BeforeClass(alwaysRun = true)
    public void beforeClass() {
        sanctionController = apiController.getSanctionController();
        grTaskTable = dbHelper.getGrTaskTable();
        parentTaskFromDb = (GrTaskDbEntity) grTaskTable.receiveByCategoryAndTaskStatus("CAT_BUGTASK", STATUS_WORKTASK_INWORK);
        parent = InitEntities.generateParent(parentTaskFromDb.getTask_id(), parentTaskFromDb.getTask_number());
        parenOfParentTaskFromDb = parentTaskFromDb.receiveParentTask();
        HANDLER_USER_FROM_PARENT = apiController.receiveGeneralTask(parent.getNumber()).getHandlerUser();
        task = InitEntities.getGeneralTask(TaskType.SANCTION, Operations.CAT);
    }

    @Test(groups = {"PROC_ADVICE", "Regression"}, description = "Создание запроса на подтверждение КПО")
    public void catSanction() {
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
        udf.setSecondUdfTask(generateUdfTask(UDF_WORKTASK_SDREQUEST, parenOfParentTaskFromDb));
        udf.setThirdUdfTask(generateUdfTask(UDF_WORKTASK_WORK, parentTaskFromDb.mapTo()));
        task.refreshUdf(udf);
        sanctionController.createSanction(task);
        ApiAsserts.assertThat(sanctionController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isEquals(task);
    }

    @Test(groups = {"PROC_ADVICE", "Regression"}, description = "Комментарий", dependsOnMethods = "catSanction")
    public void comment() {
        apiController.updateToken(generateAuthToken(HANDLER_USER_FROM_PARENT));
        udf = refreshUdf();
        task.refreshUdf();
        sanctionController.performCommonOperation(task, Operations.COMMENT);
        ApiAsserts.assertThat(sanctionController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class);
        GeneralTask generalTask = apiController.receiveGeneralTask(task.getNumber());
        TaskAsserts.assertThat(generalTask)
                .isNotEmpty(generalTask.getDescription());
    }

    @Test(groups = {"PROC_ADVICE", "Regression"}, description = "Отклонить запрос", dependsOnMethods = "comment")
    public void reject() {
        apiController.updateToken(generateAuthToken(HANDLER_USER_FROM_PARENT));
        udf = refreshUdf();
        task.refreshUdf();
        task.setHandlerUser(null);
        task.setResolution(generateResolution(Resolutions.CANNOT_ANSWER));
        task.setFinishStatus(generateStatus(STATUS_ADVICE_CLOSED));
        sanctionController.performCommonOperation(task, Operations.REJECT);
        ApiAsserts.assertThat(sanctionController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isCorrectStatus(STATUS_ADVICE_CLOSED);
    }

    @Test(groups = {"PROC_ADVICE", "Regression"}, description = "Вернуть в работу", dependsOnMethods = "reject")
    public void adviceReturn() {
        apiController.updateToken(generateAuthToken(HANDLER_USER_FROM_PARENT));
        udf = refreshUdf();
        udf.setUdfDate(generateUdfDate(UDF_ADVICE_PLANTD,0));
        udf.setUdfUser(generateUdfUser(STDT_HANDLER, ABDULLAEV_BAHODIR));
        task.refreshUdf(udf);
        task.setHandlerUser(HANDLER_USER_FROM_PARENT);
        sanctionController.performCommonOperation(task, Operations.RETURN);
        ApiAsserts.assertThat(sanctionController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isCorrectStatus(STATUS_ADVICE_AWAIT);
    }

    @Test(groups = {"PROC_ADVICE", "Regression"}, description = "Санкцинировать привязку КПО", dependsOnMethods = "adviceReturn")
    public void allowKPO() {
        apiController.updateToken(generateAuthToken(HANDLER_USER_FROM_PARENT));
        udf = refreshUdf();
        udf.setUdfList(generateUdfList(UDF_PRGAREA, B));
        task.refreshUdf(udf);
        task.setHandlerUser(null);
        sanctionController.performCommonOperation(task, Operations.ALLOW_KPO);
        ApiAsserts.assertThat(sanctionController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isCorrectStatus(STATUS_ADVICE_AWAIT);
    }

    @Test(groups = {"PROC_ADVICE", "Regression"}, description = "Санкцинировать привязку КПО", dependsOnMethods = "allowKPO")
    public void allowKPORetry() {
        apiController.updateToken(generateAuthToken(HANDLER_USER_FROM_PARENT));
        udf = refreshUdf();
        udf.setUdfList(generateUdfList(UDF_PRGAREA, B, "{\"allowfl\":\"1\",\"reviewmode\":\"OFF\"}"));
        task.refreshUdf(udf);
        sanctionController.performCommonOperation(task, Operations.ALLOW_KPO);
        ApiAsserts.assertThat(sanctionController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isCorrectStatus(STATUS_ADVICE_CLOSED);
    }

    @Test(groups = {"PROC_ADVICE", "Regression"}, description = "Вернуть в работу", dependsOnMethods = "allowKPORetry")
    public void returnRetry() {
        apiController.updateToken(generateAuthToken(HANDLER_USER_FROM_PARENT));
        task.setHandlerUser(HANDLER_USER_FROM_PARENT);
        udf = refreshUdf();
        udf.setUdfDate(generateUdfDate(UDF_ADVICE_PLANTD,0));
        udf.setUdfUser(generateUdfUser(STDT_HANDLER, ABDULLAEV_BAHODIR));
        task.refreshUdf(udf);
        task.setDescription(task.getDescription() + generateString());
        sanctionController.performCommonOperation(task, Operations.RETURN);
        ApiAsserts.assertThat(sanctionController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isCorrectStatus(STATUS_ADVICE_AWAIT);
    }

    @Test(groups = {"PROC_ADVICE", "Regression"}, description = "Закрыть вопрос", dependsOnMethods = "returnRetry")
    public void close() {
        apiController.updateToken(generateAuthToken(HANDLER_USER_FROM_PARENT));
        task.refreshUdf();
        task.setHandlerUser(null);
        sanctionController.performCommonOperation(task, Operations.CLOSE);
        ApiAsserts.assertThat(sanctionController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isCorrectStatus(STATUS_ADVICE_CLOSED);
    }
}
