package com.ts.integration.tests.proc_release.cat_release;

import com.ts.common.application.controllers.TrackStudioHttpStatusCodes;
import com.ts.common.application.database.dbEntities.GrTaskDbEntity;
import com.ts.common.application.database.dbTables.GrTaskTable;
import com.ts.common.asserts.ApiAsserts;
import com.ts.common.asserts.CommonAssert;
import com.ts.common.controllers.TaskResponseBody;
import com.ts.common.controllers.release.ReleaseController;
import com.ts.common.entitites.commonEntities.Parent;
import com.ts.common.entitites.commonEntities.Task;
import com.ts.common.entitites.commonEntities.User;
import com.ts.common.entitites.commonEntities.UserRole;
import com.ts.common.entitites.commonEntities.udf.UdfTask;
import com.ts.common.entitites.tasks.GeneralTask;
import com.ts.common.enums.Operations;
import com.ts.common.enums.TaskStatuses;
import com.ts.common.enums.TaskType;
import com.ts.common.utils.DateUtils;
import com.ts.common.utils.InitEntities;
import com.ts.common.utils.JsonUtils;
import com.ts.integration.tests.BaseIntegrationTest;
import io.restassured.path.json.JsonPath;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.List;
import java.util.stream.Collectors;

import static com.ts.common.application.database.DbQueryHelper.Operators.AND;
import static com.ts.common.application.database.DbQueryHelper.Operators.EQUAL;
import static com.ts.common.entitites.commonEntities.Udfs.UdfSd.*;
import static com.ts.common.enums.TaskStatuses.STATUS_RELEASE_PLANNING;
import static com.ts.common.utils.InitEntities.*;
import static com.ts.common.utils.RandomUtils.generateString;

public class CatReleaseTest extends BaseIntegrationTest {

    public ReleaseController releaseController;
    private GeneralTask task;
    private Parent parent;
    private User MANAGER;
    private User HANDLER;
    private User CREATOR;
    private List<TaskResponseBody> UDF_RELEASE_BRANCHES_LIST;
    private List<TaskResponseBody> CAT_STANDS;
    private List<UserRole> USER_ROLES;

    private UdfTask RELEASE_INSTALLATION;

    private GrTaskTable grTaskTable;

    private String patchTaskNumber;
    private String clientTaskNumber;


    @BeforeClass(alwaysRun = true)
    public void beforeClass() {
        releaseController = apiController.getReleaseController();
        userController = apiController.getUserController();
        grTaskTable = dbHelper.getGrTaskTable();
        GrTaskDbEntity releaseInstallation = (GrTaskDbEntity) grTaskTable.receiveRandomTask("task_category", EQUAL.operator, "CAT_BDKU_INSTALLATION", AND.operator, "task_status", EQUAL.operator, "STATUS_INSTALLATION_ACTIVE");
        RELEASE_INSTALLATION = InitEntities.generateUdfTask(UDF_RELEASE_INSTALLATION, new Task(releaseInstallation.getTask_id(), releaseInstallation.getTask_number()));

        GrTaskDbEntity parentTaskFromDb = (GrTaskDbEntity) grTaskTable.receiveRandomTask("task_category", EQUAL.operator, "CAT_RELEASEMODULE", AND.operator, "task_status", EQUAL.operator, "STATUS_FOLDER_ACTIVE");
        parent = InitEntities.generateParent(parentTaskFromDb.getTask_id(), parentTaskFromDb.getTask_number());
        task = InitEntities.getGeneralTask(TaskType.RELEASE, Operations.CAT);

        var parentPayloadResponse = releaseController.getParentPayload(parent.getNumber(), "CAT_RELEASE");
        ApiAsserts.assertThat(parentPayloadResponse).isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK);
        var parentPayload = JsonUtils.removeExtraCharacters(parentPayloadResponse);

        UDF_RELEASE_BRANCHES_LIST = new JsonPath(parentPayload).getList("udfs.UDF_RELEASE_BRANCHES.taskValueSelector", TaskResponseBody.class);
        CAT_STANDS = new JsonPath(parentPayload).getList("udfs.UDF_RELEASE_STANDS.taskValueSelector", TaskResponseBody.class);


        USER_ROLES = userController.receiveUserByTask(parent.getNumber());
        MANAGER = userController.receiveUserByRole(USER_ROLES, "Менеджер проекта", "root").getForUser();
        HANDLER = userController.receiveUserByRole(USER_ROLES, "Менеджер проекта", "root").getForUser();
        CREATOR = userController.receiveUserByRole(USER_ROLES, "Менеджер проекта", "root").getForUser();
    }

    @Test(groups = {"ProcRelease", "Regression"}, description = "создание")
    public void createTask() {
        apiController.updateToken(InitEntities.generateAuthToken(MANAGER));
        udf = refreshUdf();
        task.refreshTask();
        task.setName("hg:csb/base-^(?P<major>csb-base-v\\\\d+\\\\.\\\\d+)\\\\.?(?P<minor>(\\\\d+\\\\.?)+)?$-1.0.0");
        task.setShortName("^(?P<major>csb-base-v\\\\d+\\\\.\\\\d+)\\\\.?(?P<minor>(\\\\d+\\\\.?)+)?$-1.0.0");
        task.setParent(parent);
        task.setHandlerUser(HANDLER);

        var RELEASE_BRANCH = UDF_RELEASE_BRANCHES_LIST.stream().filter(x -> x.getCategory().getId().equals("CAT_VERSION") && x.getFinishStatus().getId().equals("STATUS_VERSION_ACTIVE")).collect(Collectors.toList());
        udf.setUdfTask(generateUdfTask(UDF_RELEASE_BRANCHES, new Task(RELEASE_BRANCH.get(0).getId(), RELEASE_BRANCH.get(0).getNumber())));
        var CAT_STAND = CAT_STANDS.stream().filter(x -> x.getCategory().getId().equals("CAT_STAND") && x.getFinishStatus().getId().equals("STATUS_STAND_OPENED")).collect(Collectors.toList());
        udf.setSecondUdfTask(generateUdfTask(UDF_RELEASE_STANDS, new Task(CAT_STAND.get(0).getId(), CAT_STAND.get(0).getNumber())));
        udf.setThirdUdfTask(RELEASE_INSTALLATION);
        udf.setUdfString(generateUdfString(UDF_RELEASE_NUMBER, "^(?P<major>csb-base-v\\\\d+\\\\.\\\\d+)\\\\.?(?P<minor>(\\\\d+\\\\.?)+)?$-1.0.0"));
        udf.setUdfDate(generateUdfDate(UDF_RELEASE_ACTUAL_USAGE, DateUtils.getCurrentDate(0)));

        task.refreshUdf(udf);

        releaseController.create(task);
        var response = releaseController.getResponse();
        ApiAsserts
                .assertThat(response)
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isCorrectStatus(STATUS_RELEASE_PLANNING);
    }

    @Test(groups = {"ProcRelease", "Regression"}, description = "Добавить задачу в патч", dependsOnMethods = "createTask")
    public void addPatchTask() {
        apiController.updateToken(generateAuthToken(MANAGER));
        task.refreshTask();

        var patchTask = (GrTaskDbEntity) grTaskTable.receiveRandomTask("task_category", EQUAL.operator, "CAT_DEVTASK", AND.operator, "task_status", EQUAL.operator, "STATUS_WORKTASK_CLOSED");
        patchTaskNumber = patchTask.getTask_number();
        String[] taskToPatch = new String[]{
                patchTaskNumber
        };
        var response = releaseController.addTask(taskToPatch, task.getNumber());
        ApiAsserts
                .assertThat(response)
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK);

        var addedPatchTask = releaseController.getTasks(task.getNumber());
        CommonAssert
                .assertThat(addedPatchTask)
                .fieldFromListIsNotEmpty("Проверить добавление задачи", patchTask.getTask_number(), "number");
    }

    @Test(groups = {"ProcRelease", "Regression"}, description = "Удалить задачу", dependsOnMethods = "addPatchTask")
    public void deletePatchTask() {
        apiController.updateToken(generateAuthToken(MANAGER));
        task.refreshTask();

        String[] taskToPatch = new String[]{
                patchTaskNumber
        };
        var response = releaseController.removeTask(taskToPatch, task.getNumber());
        ApiAsserts
                .assertThat(response)
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK);

        var addedPatchTaskIsEmpty = releaseController.getTasks(task.getNumber());
        CommonAssert
                .assertThat(addedPatchTaskIsEmpty)
                .isEmptyBody("Проверить удаление задачи", "number", patchTaskNumber);
    }

    @Test(groups = {"ProcRelease", "Regression"}, description = "Добавить задачу в патч", dependsOnMethods = "deletePatchTask")
    public void addPatchTask2() {
        apiController.updateToken(generateAuthToken(MANAGER));
        task.refreshTask();

        var patchTask = (GrTaskDbEntity) grTaskTable.receiveRandomTask("task_category", EQUAL.operator, "CAT_DEVTASK", AND.operator, "task_status", EQUAL.operator, "STATUS_WORKTASK_CLOSED");
        patchTaskNumber = patchTask.getTask_number();
        String[] taskToPatch = new String[]{
                patchTaskNumber
        };
        var response = releaseController.addTask(taskToPatch, task.getNumber());
        ApiAsserts
                .assertThat(response)
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK);

        var addedPatchTask = releaseController.getTasks(task.getNumber());

        CommonAssert
                .assertThat(addedPatchTask)
                .fieldFromListIsNotEmpty("Проверить добавление задачи", patchTask.getTask_number(), "number");
    }

    @Test(groups = {"ProcRelease", "Regression"}, description = "Добавить инсталяцию", dependsOnMethods = "addPatchTask2")
    public void addClients() {
        apiController.updateToken(generateAuthToken(MANAGER));
        task.refreshTask();

        var clientTask = (GrTaskDbEntity) grTaskTable.receiveRandomTask("task_category", EQUAL.operator, "CAT_BDKU_INSTALLATION", AND.operator, "task_status", EQUAL.operator, "STATUS_INSTALLATION_ACTIVE");
        clientTaskNumber = clientTask.getTask_number();
        String[] taskToClient = new String[]{
                clientTaskNumber
        };
        var response = releaseController.addClient(taskToClient, task.getNumber());
        ApiAsserts
                .assertThat(response)
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK);

        var addedPatchTask = releaseController.getClients(task.getNumber());

        CommonAssert
                .assertThat(addedPatchTask)
                .fieldFromListIsNotEmpty("Проверить добавление инсталляции", clientTask.getTask_number(), "taskValue.number");
    }

    @Test(groups = {"ProcRelease", "Regression"}, description = "Удалить инсталляцию", dependsOnMethods = "addClients")
    public void deleteClientTask() {
        apiController.updateToken(generateAuthToken(MANAGER));
        task.refreshTask();
        String[] taskToClient = new String[]{
                clientTaskNumber
        };
        var response = releaseController.removeClient(taskToClient, task.getNumber());
        ApiAsserts
                .assertThat(response)
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK);

        var addedPatchTaskIsEmpty = releaseController.getClients(task.getNumber());
        CommonAssert
                .assertThat(addedPatchTaskIsEmpty)
                .isEmptyBody("Проверить удаление инсталляции", "taskValue.number", clientTaskNumber);
    }

    @Test(groups = {"ProcRelease", "Regression"}, description = "Сформировать описание", dependsOnMethods = "deleteClientTask")
    public void generateDescription() {
        apiController.updateToken(generateAuthToken(MANAGER));

        var response = releaseController.generateDescription(task.getNumber(), "data", "data");
        ApiAsserts
                .assertThat(response)
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_NO_CONTENT);
    }

    @Test(groups = {"ProcRelease", "Regression"}, description = "Задать совместимость", dependsOnMethods = "generateDescription")
    public void compactTask() {
        apiController.updateToken(InitEntities.generateAuthToken(MANAGER));
        udf = refreshUdf();
        task.refreshTask();
        var releaseTask = (GrTaskDbEntity) grTaskTable.receiveRandomTask("task_category", EQUAL.operator, "CAT_RELEASE");
        udf.setUdfTask(generateUdfTask(UDF_RELEASE_COMPAT, new Task(releaseTask.getTask_id(), releaseTask.getTask_number())));
        task.refreshUdf(udf);
        releaseController.performCommonOperation(task, Operations.SETCOMPAT);
        ApiAsserts.assertThat(releaseController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isCorrectStatus(TaskStatuses.STATUS_RELEASE_PLANNING);

        var compat = releaseController.getCompat(task.getNumber());
        CommonAssert
                .assertThat(compat)
                .isNotEmptyBody("проверить наличие совместимостей");
        var backLinkCompat = releaseController.getBackLinkCompat(task.getNumber());
        CommonAssert
                .assertThat(backLinkCompat)
                .isNotEmptyBody("проверить наличие совместимостей");
    }

    @Test(groups = {"ProcRelease", "Regression"}, description = "Изменить автора", dependsOnMethods = "compactTask")
    public void changeAuthor() {
        apiController.updateToken(InitEntities.generateAuthToken(MANAGER));
        udf = refreshUdf();
        task.refreshTask();
        udf.setUdfUser(generateUdfUser(UDF_WORKTASK_NEWAUTHOR_MSG, CREATOR));
        task.refreshUdf(udf);
        releaseController.performCommonOperation(task, Operations.CHANGE_AUTHOR);
        ApiAsserts.assertThat(releaseController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK);

        var taskDetail = apiController.receiveTask(task.getNumber());
        CommonAssert.assertThat(taskDetail)
                .isCorrectSubmitterUser(CREATOR.getLogin());
    }

    @Test(groups = {"ProcRelease", "Regression"}, description = "Завершить планирование состава работ", dependsOnMethods = "changeAuthor")
    public void changeFinishPlaning() {
        apiController.updateToken(InitEntities.generateAuthToken(MANAGER));
        udf = refreshUdf();
        task.refreshTask();
        task.refreshUdf(udf);
        releaseController.performCommonOperation(task, Operations.FINISHPLANNING);
        ApiAsserts.assertThat(releaseController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isCorrectStatus(TaskStatuses.STATUS_RELEASE_PREPARATION);
    }

    @Test(groups = {"ProcRelease", "Regression"}, description = "Начать тестирование", dependsOnMethods = "changeFinishPlaning")
    public void changeStartTesting() {
        apiController.updateToken(InitEntities.generateAuthToken(MANAGER));
        udf = refreshUdf();
        task.refreshTask();

        var RELEASE_BRANCH = UDF_RELEASE_BRANCHES_LIST.stream().filter(x -> x.getCategory().getId().equals("CAT_VERSION") && x.getFinishStatus().getId().equals("STATUS_VERSION_ACTIVE")).collect(Collectors.toList());
        udf.setUdfTask(generateUdfTask(UDF_RELEASE_BRANCHES, new Task(RELEASE_BRANCH.get(1).getId(), RELEASE_BRANCH.get(1).getNumber())));
        var CAT_STAND = CAT_STANDS.stream().filter(x -> x.getCategory().getId().equals("CAT_STAND") && x.getFinishStatus().getId().equals("STATUS_STAND_OPENED")).collect(Collectors.toList());
        udf.setSecondUdfTask(generateUdfTask(UDF_RELEASE_STANDS, new Task(CAT_STAND.get(1).getId(), CAT_STAND.get(1).getNumber())));

        task.refreshUdf(udf);
        releaseController.performCommonOperation(task, Operations.STARTTESTING);
        ApiAsserts.assertThat(releaseController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isCorrectStatus(TaskStatuses.STATUS_RELEASE_INTEST);

        var taskDetail = apiController.receiveTask(task.getNumber());
        CommonAssert.assertThat(taskDetail)
                .isCorrectUdfTask(UDF_RELEASE_BRANCHES, RELEASE_BRANCH.get(1).getNumber())
                .isCorrectUdfTask(UDF_RELEASE_STANDS, CAT_STAND.get(1).getNumber());
    }

    @Test(groups = {"ProcRelease", "Regression"}, description = "Завершить тестирование", dependsOnMethods = "changeStartTesting")
    public void changeFinishTesting() {
        apiController.updateToken(InitEntities.generateAuthToken(MANAGER));
        udf = refreshUdf();
        task.refreshTask();

        var RELEASE_BRANCH = UDF_RELEASE_BRANCHES_LIST.stream().filter(x -> x.getCategory().getId().equals("CAT_VERSION") && x.getFinishStatus().getId().equals("STATUS_VERSION_ACTIVE")).collect(Collectors.toList());
        udf.setUdfTask(generateUdfTask(UDF_RELEASE_BRANCHES, new Task(RELEASE_BRANCH.get(2).getId(), RELEASE_BRANCH.get(2).getNumber())));
        var CAT_STAND = CAT_STANDS.stream().filter(x -> x.getCategory().getId().equals("CAT_STAND") && x.getFinishStatus().getId().equals("STATUS_STAND_OPENED")).collect(Collectors.toList());
        udf.setSecondUdfTask(generateUdfTask(UDF_RELEASE_STANDS, new Task(CAT_STAND.get(2).getId(), CAT_STAND.get(2).getNumber())));

        task.refreshUdf(udf);
        releaseController.performCommonOperation(task, Operations.FINISHTESTING);
        ApiAsserts.assertThat(releaseController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isCorrectStatus(TaskStatuses.STATUS_RELEASE_ISSUE);

        var taskDetail = apiController.receiveTask(task.getNumber());
        CommonAssert.assertThat(taskDetail)
                .isCorrectUdfTask(UDF_RELEASE_BRANCHES, RELEASE_BRANCH.get(2).getNumber())
                .isCorrectUdfTask(UDF_RELEASE_STANDS, CAT_STAND.get(2).getNumber());
    }

    @Test(groups = {"ProcRelease", "Regression"}, description = "Вернуть на тестирование", dependsOnMethods = "changeFinishTesting")
    public void changeReturnTesting() {
        apiController.updateToken(InitEntities.generateAuthToken(MANAGER));
        udf = refreshUdf();
        task.refreshTask();
        releaseController.performCommonOperation(task, Operations.RETURNTESTING);
        ApiAsserts.assertThat(releaseController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isCorrectStatus(TaskStatuses.STATUS_RELEASE_INTEST);
    }

    @Test(groups = {"ProcRelease", "Regression"}, description = "Вернуться на подготовку", dependsOnMethods = "changeReturnTesting")
    public void changeReturnPreparation() {
        apiController.updateToken(InitEntities.generateAuthToken(MANAGER));
        udf = refreshUdf();
        task.refreshTask();
        releaseController.performCommonOperation(task, Operations.RETURNPREPARATION);
        ApiAsserts.assertThat(releaseController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isCorrectStatus(TaskStatuses.STATUS_RELEASE_PREPARATION);
    }

    @Test(groups = {"ProcRelease", "Regression"}, description = "Вернуться на планирование состава работ", dependsOnMethods = "changeReturnPreparation")
    public void changeReturnPlaning() {
        apiController.updateToken(InitEntities.generateAuthToken(MANAGER));
        udf = refreshUdf();
        task.refreshTask();
        releaseController.performCommonOperation(task, Operations.RETURNPLANNING);
        ApiAsserts.assertThat(releaseController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isCorrectStatus(TaskStatuses.STATUS_RELEASE_PLANNING);
    }

    @Test(groups = {"ProcRelease", "Regression"}, description = "Завершить планирование состава работ", dependsOnMethods = "changeReturnPlaning")
    public void changeFinishPlaning2() {
        apiController.updateToken(InitEntities.generateAuthToken(MANAGER));
        udf = refreshUdf();
        task.refreshTask();
        task.refreshUdf(udf);
        releaseController.performCommonOperation(task, Operations.FINISHPLANNING);
        ApiAsserts.assertThat(releaseController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isCorrectStatus(TaskStatuses.STATUS_RELEASE_PREPARATION);
    }

    @Test(groups = {"ProcRelease", "Regression"}, description = "Начать выпуск", dependsOnMethods = "changeFinishPlaning2")
    public void changeStartIssue() {
        apiController.updateToken(InitEntities.generateAuthToken(MANAGER));
        udf = refreshUdf();
        task.refreshTask();

        var RELEASE_BRANCH = UDF_RELEASE_BRANCHES_LIST.stream().filter(x -> x.getCategory().getId().equals("CAT_VERSION") && x.getFinishStatus().getId().equals("STATUS_VERSION_ACTIVE")).collect(Collectors.toList());
        udf.setUdfTask(generateUdfTask(UDF_RELEASE_BRANCHES, new Task(RELEASE_BRANCH.get(3).getId(), RELEASE_BRANCH.get(3).getNumber())));
        var CAT_STAND = CAT_STANDS.stream().filter(x -> x.getCategory().getId().equals("CAT_STAND") && x.getFinishStatus().getId().equals("STATUS_STAND_OPENED")).collect(Collectors.toList());
        udf.setSecondUdfTask(generateUdfTask(UDF_RELEASE_STANDS, new Task(CAT_STAND.get(3).getId(), CAT_STAND.get(3).getNumber())));

        task.refreshUdf(udf);
        releaseController.performCommonOperation(task, Operations.STARTISSUE);
        ApiAsserts.assertThat(releaseController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isCorrectStatus(TaskStatuses.STATUS_RELEASE_ISSUE);

        var taskDetail = apiController.receiveTask(task.getNumber());
        CommonAssert.assertThat(taskDetail)
                .isCorrectUdfTask(UDF_RELEASE_BRANCHES, RELEASE_BRANCH.get(3).getNumber())
                .isCorrectUdfTask(UDF_RELEASE_STANDS, CAT_STAND.get(3).getNumber());
    }

    @Test(groups = {"ProcRelease", "Regression"}, description = "Завершить выпуск", dependsOnMethods = "changeStartIssue")
    public void changeFinishIssue() {
        apiController.updateToken(InitEntities.generateAuthToken(MANAGER));
        udf = refreshUdf();
        task.refreshTask();
        task.setDescription(generateString());
        udf.setUdfDate(generateUdfDate(UDF_RELEASE_ACTUAL_USAGE, DateUtils.getCurrentDate(1)));

        task.refreshUdf(udf);
        releaseController.performCommonOperation(task, Operations.FINISHISSUE);
        ApiAsserts.assertThat(releaseController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isCorrectStatus(TaskStatuses.STATUS_RELEASE_USING);
    }

    @Test(groups = {"ProcRelease", "Regression"}, description = "Передать в архив", dependsOnMethods = "changeFinishIssue")
    public void changeToArchive() {
        apiController.updateToken(InitEntities.generateAuthToken(MANAGER));
        udf = refreshUdf();
        task.refreshTask();
        releaseController.performCommonOperation(task, Operations.TOARCHIVE);
        ApiAsserts.assertThat(releaseController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isCorrectStatus(TaskStatuses.STATUS_RELEASE_CLOSED);
    }
}
