package com.ts.integration.tests.proc_gap.TP_1541680.TM_01;

import com.ts.common.application.database.dbEntities.GrTaskDbEntity;
import com.ts.common.application.database.dbTables.GrTaskTable;
import com.ts.common.asserts.ApiAsserts;
import com.ts.common.asserts.CommonAssert;
import com.ts.common.controllers.TaskResponseBody;
import com.ts.common.controllers.gap.GapSolutionController;
import com.ts.common.controllers.gap.PotentialGapController;
import com.ts.common.controllers.sdquestion.SdQuestionController;
import com.ts.common.entitites.commonEntities.Parent;
import com.ts.common.entitites.commonEntities.User;
import com.ts.common.entitites.commonEntities.UserRole;
import com.ts.common.entitites.tasks.GeneralTask;
import com.ts.common.enums.TaskType;
import com.ts.common.utils.InitEntities;
import com.ts.integration.tests.BaseIntegrationTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.ts.common.application.controllers.TrackStudioHttpStatusCodes.HTTP_OK;
import static com.ts.common.entitites.commonEntities.Task.Constants.AKKREDITIVES;
import static com.ts.common.entitites.commonEntities.Task.Constants.ALOQA_BANK;
import static com.ts.common.entitites.commonEntities.Udfs.UdfSd.*;
import static com.ts.common.entitites.commonEntities.Udfs.UdfSd.UDF_WORKTASK_SDREQUEST;
import static com.ts.common.enums.Operations.*;
import static com.ts.common.enums.TaskStatuses.*;
import static com.ts.common.enums.TaskType.POTENTIAL_GAP;
import static com.ts.common.enums.TaskType.SD_QUESTION;
import static com.ts.common.utils.InitEntities.*;
import static com.ts.common.utils.RandomUtils.generateName;
import static com.ts.common.utils.RandomUtils.generateString;

public class GapPrivateQuestion1Test extends BaseIntegrationTest {
    GapSolutionController gapSolutionController;
    PotentialGapController potentialGapController;
    SdQuestionController sdQuestionController;
    GeneralTask task;
    Map<TaskType, GeneralTask> tasks;
    Map<User, GeneralTask> taskByHandlerUser;
    Parent parent;
    User ANALYTIC;
    User MANAGER_CLIENT;
    User MANAGER_REQUEST;
    User SUPPORT_MEMBER;
    User AUTHOR;
    List<UserRole> USER_ROLES;
    final String parentTaskNumber = "999317";

    @DataProvider(name = "users")
    public Object[][] users() {
        Object[][] users = new Object[4][];
        users[0] = new Object[]{ANALYTIC}; // аналитик
        users[1] = new Object[]{MANAGER_CLIENT}; // менеджер клиента
        users[2] = new Object[]{MANAGER_REQUEST}; // менеджер запроса
        users[3] = new Object[]{SUPPORT_MEMBER}; // участник сопровождения проекта
        return users;
    }

    @BeforeClass(alwaysRun = true)
    public void beforeClass() {
        tasks = new HashMap<>();
        gapSolutionController = apiController.getGapSolutionController();
        potentialGapController = apiController.getPotentialGapController();
        sdQuestionController = apiController.getSdQuestionController();
        userController = apiController.getUserController();
        baseController = apiController.getBaseController();
        GrTaskTable grTaskTable = dbHelper.getGrTaskTable();
        GrTaskDbEntity grTaskDbEntity = (GrTaskDbEntity) grTaskTable.receiveByTaskNumber(parentTaskNumber);

        parent = InitEntities.generateParent(grTaskDbEntity.getTask_id(), grTaskDbEntity.getTask_number());
        USER_ROLES = userController.receiveUserByTask(parentTaskNumber);

        AUTHOR = userController.receiveUserByLogin(USER_ROLES, "episkunova");
        ANALYTIC = userController.receiveUserByLogin(USER_ROLES, "episkunova");
        MANAGER_CLIENT = userController.receiveUserByLogin(USER_ROLES, "lkorennaya");
        MANAGER_REQUEST = userController.receiveUserByLogin(USER_ROLES, "jmankevich");
        SUPPORT_MEMBER = userController.receiveUserByLogin(USER_ROLES, "nselezneva");


        taskByHandlerUser = new HashMap<>();
    }

    @Test(groups = {"PROC_GAP", "Regression"}
            , description = "Создание потенциального GAP (СОТРУДНИК)"
            , dataProvider = "users")
    void cat(User handlerUser) {
        apiController.updateToken(AUTHOR);
        task = getGeneralTask(POTENTIAL_GAP, CAT);
        task.setName(generateName());
        task.setParent(parent);

        udf = refreshUdf();
        udf.setUdfTask(generateUdfTask(UDF_SD_MODULE, AKKREDITIVES));

        task.refreshUdf(udf);
        task.setHandlerUser(handlerUser);

        gapSolutionController.createGapSolution(task);

        ApiAsserts.assertThat(gapSolutionController.getResponse())
                .isCorrectResponseCode(HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isCorrectStatus(STATUS_GAP_ANALIZING)
                .isEquals(task);

        taskByHandlerUser.put(handlerUser, task);
    }

    @Test(groups = {"PROC_GAP", "Regression"}, description = "Передать на согласование (СОТРУДНИК)", dependsOnMethods = "cat"
            , dataProvider = "users")
    void passForApproval(User handlerUser) {
        task = taskByHandlerUser.get(handlerUser);

        apiController.updateToken(AUTHOR);
        udf = refreshUdf();
        task.refreshTask();

        udf.setUdfUser(generateUdfUser(STDT_HANDLER, handlerUser));
        task.refreshUdf(udf);
        task.setHandlerUser(handlerUser);

        potentialGapController.performCommonOperation(task, PASS_FOR_APPROVAL);
        ApiAsserts.assertThat(potentialGapController.getResponse())
                .isCorrectResponseCode(HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isCorrectStatus(STATUS_GAP_APPROVAL)
                .isEquals(task);
    }

    @Test(groups = {"PROC_GAP", "Regression"}, description = "Подтвердить скрытый Gap (СОТРУДНИК)", dependsOnMethods = "passForApproval"
            , dataProvider = "users")
    void confirmHidden(User handlerUser) {
        task = taskByHandlerUser.get(handlerUser);

        apiController.updateToken(AUTHOR);
        udf = refreshUdf();
        task.refreshTask();

        udf.setUdfUser(generateUdfUser(STDT_HANDLER, handlerUser));
        task.refreshUdf(udf);
        task.setHandlerUser(handlerUser);
        potentialGapController.performCommonOperation(task, CONFIRM_HIDDEN);
        ApiAsserts.assertThat(potentialGapController.getResponse())
                .isCorrectResponseCode(HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isCorrectStatus(STATUS_GAP_CONFIRMED)
                .isEquals(task);
    }

    @Test(groups = {"PROC_GAP", "Regression"}, description = "Создание категории \"Вопрос Сотруднику\"(СОТРУДНИК)", dependsOnMethods = "confirmHidden"
            , dataProvider = "users")
    void catSdQuestion(User handlerUser) {
        task = taskByHandlerUser.get(handlerUser);

        tasks.put(task.getTaskType(), task);
        parent = InitEntities.generateParent(task.getId(), task.getNumber());

        apiController.updateToken(handlerUser);

        task = getGeneralTask(SD_QUESTION, CAT);
        udf = refreshUdf();
        task.refreshTask();

        udf.setUdfTask(generateUdfTask(UDF_SD_MODULE, AKKREDITIVES));
        udf.setSecondUdfTask(generateUdfTask(UDF_BDKU_CONFIGURATION, ALOQA_BANK));
        udf.setThirdUdfTask(generateUdfTask(UDF_WORKTASK_SDREQUEST, tasks.get(POTENTIAL_GAP).to()));

        task.setName(generateName());
        task.setParent(parent);
        task.refreshUdf(udf);
        task.setDescription(generateString());

        sdQuestionController.createSdQuestion(task);
        ApiAsserts.assertThat(sdQuestionController.getResponse())
                .isCorrectResponseCode(HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isCorrectStatus(STATUS_SDQUESTION_NEW)
                .isEquals(task);

        tasks.put(task.getTaskType(), task);

        apiController.receiveTask(tasks.get(POTENTIAL_GAP).getNumber());
        CommonAssert.assertThat(apiController.getResponse())
                .isCorrectTaskStatus(STATUS_GAP_WAITANALIZING);

    }
}
