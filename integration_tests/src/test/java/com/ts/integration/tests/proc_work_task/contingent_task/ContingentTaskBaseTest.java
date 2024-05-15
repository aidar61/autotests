package com.ts.integration.tests.proc_work_task.contingent_task;

import com.ts.common.application.controllers.TrackStudioHttpStatusCodes;
import com.ts.common.application.database.dbEntities.GrTaskDbEntity;
import com.ts.common.application.database.dbTables.GrTaskTable;
import com.ts.common.asserts.ApiAsserts;
import com.ts.common.asserts.CommonAssert;
import com.ts.common.controllers.TaskResponseBody;
import com.ts.common.controllers.workTask.ContingentTaskController;
import com.ts.common.entitites.commonEntities.Parent;
import com.ts.common.entitites.commonEntities.Task;
import com.ts.common.entitites.commonEntities.User;
import com.ts.common.entitites.tasks.GeneralTask;
import com.ts.common.enums.Operations;
import com.ts.common.enums.TaskType;
import com.ts.common.utils.DateUtils;
import com.ts.common.utils.InitEntities;
import com.ts.common.utils.JsonUtils;
import com.ts.integration.tests.BaseIntegrationTest;
import io.restassured.path.json.JsonPath;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.stream.Collectors;

import static com.ts.common.application.database.DbQueryHelper.Operators.*;
import static com.ts.common.entitites.commonEntities.List.Constants.*;
import static com.ts.common.entitites.commonEntities.Task.Constants.CORE;
import static com.ts.common.entitites.commonEntities.Udfs.UdfSd.*;
import static com.ts.common.entitites.commonEntities.User.Constants.*;
import static com.ts.common.enums.Operations.*;
import static com.ts.common.enums.Resolutions.CANNOT_BE_COMPLETED_WITHIN_THE_SPECIFIED_TIME_FRAME;
import static com.ts.common.enums.Resolutions.RESOLUTION_WORK_SUSPENDED_INDEFINITELY;
import static com.ts.common.enums.TaskStatuses.*;
import static com.ts.common.utils.InitEntities.*;
import static com.ts.common.utils.RandomUtils.*;

public class ContingentTaskBaseTest extends BaseIntegrationTest {
    public ContingentTaskController contingentTaskController;
    private GeneralTask task;
    private Parent parent;
    private Task taskRegProject;
    private User creator;
    private User handlerUser;
    private User handlerUser2;
    private User handlerUser3;
    private String QUALIFICATION;
    private String MIS_SERVICE;
    private String MIS_SERVICE2;
    private String CDP_BL;


    @BeforeClass(alwaysRun = true)
    public void beforeClass() {
        contingentTaskController = apiController.getContingentTaskController();
        userController = apiController.getUserController();
        GrTaskTable grTaskTable = dbHelper.getGrTaskTable();
        GrTaskDbEntity parentTaskFromDb = (GrTaskDbEntity) grTaskTable.receiveRandomTask("task_category", EQUAL.operator, "CAT_GENPLAN", AND.operator, "task_status", EQUAL.operator, STATUS_PROJECT_PLANNED.name(), AND.operator, "task_path", LIKE.operator, "%/2405/758009%");
        parent = InitEntities.generateParent(parentTaskFromDb.getTask_id(), parentTaskFromDb.getTask_number());
        task = InitEntities.getGeneralTask(TaskType.CONTINGENT, Operations.CAT);
        var taskRegProjectEntity = (GrTaskDbEntity) grTaskTable.receiveRandomTask("task_category", EQUAL.operator, "CAT_REGPROJECT");
        taskRegProject = new Task(taskRegProjectEntity.getTask_id(), taskRegProjectEntity.getTask_number());

        var parentPayloadResponse = contingentTaskController.getParentPayload(parent.getNumber(), "CAT_CONTINGENT");
        ApiAsserts.assertThat(parentPayloadResponse).isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK);
        var parentPayload = JsonUtils.removeExtraCharacters(parentPayloadResponse);
        MIS_SERVICE = contingentTaskController.getParent_UDF_MIS_SERVICE(parentPayload).get(0);
        MIS_SERVICE2 = contingentTaskController.getParent_UDF_MIS_SERVICE(parentPayload).get(1);
        CDP_BL = contingentTaskController.getParent_UDF_CDP_BL(parentPayload).get(0);
        QUALIFICATION = contingentTaskController.getParent_WORKTASK_QUALIFICATION(parentPayload).get(0);

        var employees = userController.receiveUserByTask(parent.getNumber());
        Collections.shuffle(employees);
        var creators = employees.stream().filter(f -> f.getAssignedRole().getName().equals("Менеджер проекта") && f.getForUser().getActive()).collect(Collectors.toList());
        var handlers = employees.stream().filter(f -> f.getAssignedRole().getName().equals("Участник проекта") && f.getForUser().getActive()).collect(Collectors.toList());
        creator = creators.get(0).getForUser();
        handlerUser = handlers.get(0).getForUser();
        handlerUser2 = handlers.get(1).getForUser();
        handlerUser3 = handlers.get(2).getForUser();
    }


    @Test(groups = {"PROC_WORKTASK", "Regression"}, description = "Заявка на подбор персонала")
    public void workTask() {
        udf = refreshUdf();
        task.refreshTask();
        apiController.updateToken(InitEntities.generateAuthToken(creator));
        task.setParent(parent);
        task.setName("Заявка на подбор персонала" + LocalDateTime.now().getNano());
        task.setDescription(task.getDescription() + generateString());
        udf.setUdfList(generateUdfList(UDF_CDP_BL, CDP_BL));
        udf.setUdfTask(generateUdfTask(UDF_SD_MODULE, CORE));
        udf.setUdfString(generateUdfString(UDF_SD_NOMODULE_REASON, generateString()));
        if (MIS_SERVICE != null) udf.setNinethUdfList(generateUdfList(UDF_MIS_SERVICE, MIS_SERVICE));
        udf.setSecondUdfList(generateUdfList(UDF_CDP_ACCEPTANCE, REQBYAUTHOR));
        udf.setUdfDouble(generateUdfDouble(UDF_WORKTASK_PLANBUDGET, generateRandomNumberBetween(1, 10)));
        udf.setUdfUser(generateUdfUser(UDF_WORKTASK_SANCTIONER, ABDULLAEV_BAHODIR));
        udf.setSecondUdfString(generateUdfString(UDF_WORKTASK_POSITION, generatePosition()));
        udf.setUdfMultiList(generateUdfMultiList(UDF_WORKTASK_QUALIFICATION, QUALIFICATION));
        udf.setSecondUdfUser(generateUdfUser(UDF_WORKTASK_DEP, FREELANCERS));
        udf.setThirdUdfString(generateUdfString(UDF_WORKTASK_LINE, generateComment()));
        udf.setThirdUdfUser(generateUdfUser(UDF_WORKTASK_JOBAPPLICANT, creator));
        udf.setFourthUdfUser(generateUdfUser(UDF_WORKTASK_LINEMANAGER, ARTEMEVA_MARINA));
        udf.setUdfMemo(generateUdfMemo(UDF_WORKTASK_MAINDUTIES, generateString()));
        udf.setFourthUdfString(generateUdfString(UDF_WORKTASK_VACANCYREASON, generateString()));
        udf.setSecondUdfMemo(generateUdfMemo(UDF_WORKTASK_RATIONALEVACANCY, generateString()));
        udf.setThirdUdfMemo(generateUdfMemo(UDF_WORKTASK_MAINREQUIREMENTS, generateString()));
        udf.setThirdUdfList(generateUdfList(UDF_WORKTASK_NEEDTECHTESTING, NEED_TECH_TESTING_YES));
        udf.setFifthUdfString(generateUdfString(UDF_WORKTASK_TECHTESTING, generateString()));
        udf.setSecondUdfMultiList(generateUdfMultiList(UDF_WORKTASK_CITY, BREST_CITY));
        udf.setFourthUdfList(generateUdfList(UDF_WORKTASK_LANGUAGES, WORKTASK_LANGUAGES_TECHNICAL));
        udf.setFifthUdfList(generateUdfList(UDF_WORKTASK_COMPLEXITY, WORKTASK_COMPLEXITY_3));
        udf.setSixthUdfList(generateUdfList(UDF_WORKTASK_BUSINESSTRIP, WORKTASK_BUSINESSTRIP_NO));
        udf.setSixthUdfString(generateUdfString(UDF_WORKTASK_JOBAPPLICANTCOMMENT, generateString()));
        udf.setSeventhUdfList(generateUdfList(UDF_WORKTASK_NEEDNEO, WORKTASK_NEEDNEO_YES));
        udf.setEighthUdfList(generateUdfList(UDF_WORKTASK_NEEDWORKSAMPLES, WORKTASK_NEEDWORKSAMPLES_YES));
        udf.setFourthUdfMemo(generateUdfMemo(UDF_WORKTASK_PROJECTDESCRIPTION, generateString()));
        udf.setUdfInteger(generateUdfInteger(UDF_WORKTASK_NUMBERAPPLICANTS, generateRandomNumberBetween(1, 10)));
        udf.setUdfDate(generateUdfDate(UDF_WORKTASK_VACANCYPLANFD, DateUtils.getCurrentDate(0)));
        udf.setSecondUdfDate(generateUdfDate(UDF_WORKTASK_VACANCYSTARTD, DateUtils.getCurrentDate(0)));
        udf.setTenthUdfList(generateUdfList(UDF_WORKTASK_VACANCYPRIORITY, WORKTASK_VACANCYPRIORITY_HIGH));
        udf.setSecondUdfInteger(generateUdfInteger(UDF_TASK_SECONDWARNINGDAYS, 5));
        udf.setThirdUdfInteger(generateUdfInteger(UDF_TASK_ACTUAL_MONTHS, 2));
        udf.setSecondUdfTask(generateUdfTask(UDF_REGPROJECT, taskRegProject));

        task.refreshUdf(udf);
        contingentTaskController.createTask(task);
        var response = contingentTaskController.getResponse();
        ApiAsserts.assertThat(response).isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK).isParseableBody(TaskResponseBody.class).assertTask().isCorrectStatus(STATUS_WORKTASK_NEW).isEquals(task);
    }

    @Test(groups = {"PROC_WORKTASK", "Regression"}, description = "Передать в работу", dependsOnMethods = "workTask")
    public void taskAssign() {
        apiController.updateToken(generateAuthToken(creator));
        task.refreshTask();
        udf = refreshUdf();
        task.setHandlerUser(handlerUser);
        task.setDescription(generateString());
        udf.setUdfDate(generateUdfDate(UDF_WORKTASK_PLANFD, DateUtils.getCurrentDate(0)));
        udf.setSecondUdfDate(generateUdfDate(UDF_WORKTASK_PLANTD, DateUtils.getCurrentDate(0)));
        udf.setUdfDouble(generateUdfDouble(UDF_WORKTASK_PLANBUDGET, generateRandomNumberBetween(1, 5)));
        udf.setUdfUser(generateUdfUser(STDT_HANDLER, handlerUser));
        task.refreshUdf(udf);
        contingentTaskController.performCommonOperation(task, WORKTASK_ASSIGN);
        ApiAsserts.assertThat(contingentTaskController.getResponse()).isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK).isParseableBody(TaskResponseBody.class).assertTask().isCorrectStatus(STATUS_WORKTASK_ASSIGNED);
    }

    @Test(groups = {"PROC_WORKTASK", "Regression"}, description = "Отменить назначение", dependsOnMethods = "taskAssign")
    public void assignCancel() {
        apiController.updateToken(generateAuthToken(creator));
        task.refreshTask();
        udf = refreshUdf();
        contingentTaskController.performCommonOperation(task, ASSIGNCANCEL);
        ApiAsserts.assertThat(contingentTaskController.getResponse()).isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK).isParseableBody(TaskResponseBody.class).assertTask().isCorrectStatus(STATUS_WORKTASK_NEW);
    }

    @Test(groups = {"PROC_WORKTASK", "Regression"}, description = "Запрос санкции", dependsOnMethods = "assignCancel")
    public void requestSanction() {
        apiController.updateToken(generateAuthToken(creator));
        task.refreshTask();
        udf = refreshUdf();
        contingentTaskController.performCommonOperation(task, REQUEST_SANCTION);
        ApiAsserts.assertThat(contingentTaskController.getResponse()).isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK).isParseableBody(TaskResponseBody.class).assertTask().isCorrectStatus(STATUS_WORKTASK_SANCTION);
    }

    @Test(groups = {"PROC_WORKTASK", "Regression"}, description = "Санкционировать заявку", dependsOnMethods = "requestSanction")
    public void sanction() {
        var taskDetail = apiController.receiveTask(task.getNumber());
        var sanctioner = new JsonPath(taskDetail.asString()).getObject("udfs.UDF_WORKTASK_SANCTIONER.userValue[0]", User.class);
        apiController.updateToken(generateAuthToken(sanctioner));
        task.refreshTask();
        udf = refreshUdf();
        udf.setUdfList(generateUdfList(UDF_WORKTASK_SANCTION, WORKTASK_SANCTION_APPROVE));
        task.refreshUdf(udf);
        contingentTaskController.performCommonOperation(task, SANCTION);
        ApiAsserts.assertThat(contingentTaskController.getResponse()).isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK).isParseableBody(TaskResponseBody.class).assertTask();
        var taskMessages = contingentTaskController.receiveTaskMessages(task.getNumber());
        CommonAssert.assertThat(taskMessages).isCorrectField("Санкция = Одобрена", WORKTASK_SANCTION_APPROVE.getId(), "[0].udfs.UDF_WORKTASK_SANCTION.listValue[0].id");
    }

    @Test(groups = {"PROC_WORKTASK", "Regression"}, description = "Переназначить ответственного", dependsOnMethods = "sanction")
    public void reAssign() {
        apiController.updateToken(generateAuthToken(creator));
        task.refreshTask();
        udf = refreshUdf();
        task.setHandlerUser(handlerUser);
        udf.setUdfList(generateUdfList(UDF_WORKTASK_REASSIGNCONTROL_MSG, WORKTASK_REASSIGNCONTROL_MSG_IGNORE));
        udf.setUdfUser(generateUdfUser(STDT_HANDLER, handlerUser));
        task.refreshUdf(udf);
        contingentTaskController.performCommonOperation(task, CHANGE_RES_PERSON);
        ApiAsserts.assertThat(contingentTaskController.getResponse()).isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK).isParseableBody(TaskResponseBody.class).assertTask().isCorrectHandlerUser(handlerUser);
    }

    @Test(groups = {"PROC_WORKTASK", "Regression"}, description = "Передать в работу", dependsOnMethods = "reAssign")
    public void taskAssign2() {
        apiController.updateToken(generateAuthToken(creator));
        task.refreshTask();
        udf = refreshUdf();
        task.setHandlerUser(handlerUser2);
        task.setDescription(generateString());
        udf.setUdfDate(generateUdfDate(UDF_WORKTASK_PLANFD, DateUtils.getCurrentDate(0)));
        udf.setSecondUdfDate(generateUdfDate(UDF_WORKTASK_PLANTD, DateUtils.getCurrentDate(0)));
        udf.setUdfDouble(generateUdfDouble(UDF_WORKTASK_PLANBUDGET, generateRandomNumberBetween(1, 5)));
        udf.setUdfUser(generateUdfUser(STDT_HANDLER, handlerUser2));
        task.refreshUdf(udf);
        contingentTaskController.performCommonOperation(task, WORKTASK_ASSIGN);
        ApiAsserts.assertThat(contingentTaskController.getResponse()).isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK).isParseableBody(TaskResponseBody.class).assertTask().isCorrectStatus(STATUS_WORKTASK_ASSIGNED);
    }

    @Test(groups = {"PROC_WORKTASK", "Regression"}, description = "Принять в работу", dependsOnMethods = "taskAssign2")
    public void acceptInWork() {
        apiController.updateToken(generateAuthToken(handlerUser2));
        task.refreshTask();
        udf = refreshUdf();
        task.setHandlerUser(handlerUser2);
        task.setDescription(generateString());
        udf.setUdfDate(generateUdfDate(UDF_WORKTASK_PLANTD, DateUtils.getCurrentDate(3)));
        udf.setUdfDouble(generateUdfDouble(UDF_WORKTASK_PLANBUDGET, generateRandomNumberBetween(1, 5)));
        udf.setUdfUser(generateUdfUser(STDT_HANDLER, handlerUser2));
        task.refreshUdf(udf);
        contingentTaskController.performCommonOperation(task, ACCEPT_IN_WORK);
        ApiAsserts.assertThat(contingentTaskController.getResponse()).isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK).isParseableBody(TaskResponseBody.class).assertTask().isCorrectStatus(STATUS_WORKTASK_INWORK);
    }

    @Test(groups = {"PROC_WORKTASK", "Regression"}, description = "Отложить", dependsOnMethods = "acceptInWork")
    public void taskPostpone() {
        apiController.updateToken(generateAuthToken(handlerUser2));
        task.setResolution(generateResolution(RESOLUTION_WORK_SUSPENDED_INDEFINITELY));
        udf = refreshUdf();
        udf.setUdfDate(generateUdfDate(UDF_WORKTASK_PLANFD, 0));
        task.refreshUdf(udf);
        contingentTaskController.performCommonOperation(task, Operations.POSTPONE);
        ApiAsserts.assertThat(contingentTaskController.getResponse()).isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK).isParseableBody(TaskResponseBody.class).assertTask().isCorrectResolution(RESOLUTION_WORK_SUSPENDED_INDEFINITELY).isCorrectStatus(STATUS_WORKTASK_POSTPONED);
    }

    @Test(groups = {"PROC_WORKTASK", "Regression"}, description = "Принять в работу", dependsOnMethods = "taskPostpone")
    public void acceptInWork2() {
        apiController.updateToken(generateAuthToken(handlerUser2));
        task.refreshTask();
        udf = refreshUdf();
        task.setHandlerUser(handlerUser);
        task.setDescription(generateString());
        udf.setUdfDate(generateUdfDate(UDF_WORKTASK_PLANTD, DateUtils.getCurrentDate(5)));
        udf.setUdfDouble(generateUdfDouble(UDF_WORKTASK_PLANBUDGET, generateRandomNumberBetween(1, 5)));
        udf.setUdfUser(generateUdfUser(STDT_HANDLER, handlerUser));
        task.refreshUdf(udf);
        contingentTaskController.performCommonOperation(task, ACCEPT_IN_WORK);
        ApiAsserts.assertThat(contingentTaskController.getResponse()).isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK).isParseableBody(TaskResponseBody.class).assertTask().isCorrectStatus(STATUS_WORKTASK_INWORK);
    }

    @Test(groups = {"PROC_WORKTASK", "Regression"}, description = "Отклонить", dependsOnMethods = "acceptInWork2")
    public void taskDecline() {
        apiController.updateToken(generateAuthToken(handlerUser));
        task.setResolution(generateResolution(CANNOT_BE_COMPLETED_WITHIN_THE_SPECIFIED_TIME_FRAME));
        udf = refreshUdf();
        task.refreshUdf(udf);
        contingentTaskController.performCommonOperation(task, DECLINE);
        ApiAsserts.assertThat(contingentTaskController.getResponse()).isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK).isParseableBody(TaskResponseBody.class).assertTask().isCorrectResolution(CANNOT_BE_COMPLETED_WITHIN_THE_SPECIFIED_TIME_FRAME).isCorrectHandlerUser(creator).isCorrectStatus(STATUS_WORKTASK_DECLINED);
    }

    @Test(groups = {"PROC_WORKTASK", "Regression"}, description = "Вернуть в работу", dependsOnMethods = "taskDecline")
    public void taskReturn() {
        apiController.updateToken(generateAuthToken(creator));
        task.refreshTask();
        udf = refreshUdf();
        task.setHandlerUser(handlerUser);
        task.setDescription(generateString());
        udf.setUdfUser(generateUdfUser(STDT_HANDLER, handlerUser));
        task.refreshUdf(udf);
        contingentTaskController.performCommonOperation(task, RETURN);
        var updateResponse = contingentTaskController.getResponse();
        ApiAsserts.assertThat(updateResponse).isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK).isParseableBody(TaskResponseBody.class).assertTask().isCorrectStatus(STATUS_WORKTASK_ASSIGNED);
    }

    @Test(groups = {"PROC_WORKTASK", "Regression"}, description = "Назначить контролёра", dependsOnMethods = "taskReturn")
    public void setSupervise() {
        apiController.updateToken(generateAuthToken(handlerUser));
        udf = refreshUdf();
        task.refreshTask();
        task.setDescription(generateString());
        task.refreshUdf(udf);
        udf.setUdfUser(generateUdfUser(UDF_WORKTASK_SUPERVISER, ARUTYANIN_YURIY));
        contingentTaskController.performCommonOperation(task, SUPERVISE);
        ApiAsserts.assertThat(contingentTaskController.getResponse()).isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK).isParseableBody(TaskResponseBody.class).assertTask();
        var taskDetail = apiController.receiveTask(task.getNumber());
        CommonAssert.assertThat(taskDetail).isCorrectUdfUSer(UDF_WORKTASK_SUPERVISER, ARUTYANIN_YURIY.login);
    }

    @Test(groups = {"PROC_WORKTASK", "Regression"}, description = "Назначить наблюдателя", dependsOnMethods = "setSupervise")
    public void setWatcher() {
        apiController.updateToken(generateAuthToken(handlerUser));
        udf = refreshUdf();
        task.refreshTask();
        task.setDescription(generateString());
        task.refreshUdf(udf);
        udf.setUdfUser(generateUdfUser(UDF_WATCHER, ARTEMEVA_MARINA));
        contingentTaskController.performCommonOperation(task, WATCH);
        ApiAsserts.assertThat(contingentTaskController.getResponse()).isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK).isParseableBody(TaskResponseBody.class).assertTask();
        var taskDetail = apiController.receiveTask(task.getNumber());
        CommonAssert.assertThat(taskDetail).isCorrectUdfUSer(UDF_WATCHER, ARTEMEVA_MARINA.login);
    }

    @Test(groups = {"PROC_WORKTASK", "Regression"}, description = "Изменить услугу", dependsOnMethods = "setWatcher")
    public void taskChangeService() {
        apiController.updateToken(generateAuthToken(handlerUser));
        task.setDescription(generateString());
        udf = refreshUdf();
        task.refreshTask();
        udf.setUdfList(generateUdfList(UDF_MIS_SERVICE, MIS_SERVICE2));
        task.refreshUdf(udf);
        contingentTaskController.performCommonOperation(task, CHANGE_SERVICE);
        var response = contingentTaskController.getResponse();
        ApiAsserts.assertThat(response).isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK);

        var taskDetail = apiController.receiveTask(task.getNumber());
        CommonAssert.assertThat(taskDetail).isCorrectUdfList(UDF_MIS_SERVICE, MIS_SERVICE2);
    }

    @Test(groups = {"PROC_WORKTASK", "Regression"}, description = "Принять в работу", dependsOnMethods = "taskChangeService")
    public void taskStart2() {
        apiController.updateToken(generateAuthToken(handlerUser));
        udf = refreshUdf();
        task.refreshTask();
        task.setHandlerUser(handlerUser);
        udf.setUdfDate(generateUdfDate(UDF_WORKTASK_PLANTD, DateUtils.getCurrentDate(5)));
        udf.setUdfDouble(generateUdfDouble(UDF_WORKTASK_PLANBUDGET, generateRandomNumberBetween(1, 5)));
        udf.setUdfUser(generateUdfUser(STDT_HANDLER, handlerUser));
        task.refreshUdf(udf);
        contingentTaskController.performCommonOperation(task, ACCEPT_IN_WORK);
        ApiAsserts.assertThat(contingentTaskController.getResponse()).isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK).isParseableBody(TaskResponseBody.class).assertTask().isCorrectStatus(STATUS_WORKTASK_INWORK);
    }

    @Test(groups = {"PROC_WORKTASK", "Regression"}, description = "Перепланировать", dependsOnMethods = "taskStart2")
    public void taskChangeTime() {
        apiController.updateToken(generateAuthToken(handlerUser));
        task.setDescription(generateString());
        task.setConfirmed(false);
        udf = refreshUdf();
        udf.setUdfDate(generateUdfDate(UDF_WORKTASK_AWAITTD, DateUtils.getCurrentDate(0)));
        udf.setUdfUser(generateUdfUser(UDF_WORKTASK_RESPFOREPLAN, ALTUNIN_NIKOLAY));
        udf.setUdfList(generateUdfList(UDF_WORKTASK_REASONFOREPLAN, DISTRACTION_TO_OTHER_WORK));
        udf.setUdfDouble(generateUdfDouble(UDF_WORKTASK_AWAITBUDGET, 0));
        udf.setSecondUdfDouble(generateUdfDouble(UDF_CDP_NORMBUDGET, 0));
        task.refreshUdf(udf);
        contingentTaskController.performCommonOperation(task, CHANGE_TIME);
        ApiAsserts.assertThat(contingentTaskController.getResponse()).isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK).isParseableBody(TaskResponseBody.class).assertTask().isCorrectStatus(STATUS_WORKTASK_INWORK);
    }

    @Test(groups = {"PROC_WORKTASK", "Regression"}, description = "Переназначить ответственного", dependsOnMethods = "taskChangeTime")
    public void reAssign2() {
        apiController.updateToken(generateAuthToken(creator));
        udf = refreshUdf();
        task.refreshTask();
        task.setDescription(generateString());
        task.setHandlerUser(handlerUser3);
        task.refreshUdf(udf);
        udf.setUdfUser(generateUdfUser(UDF_WATCHER, ARTEMEVA_MARINA));
        udf.setUdfUser(generateUdfUser(UDF_WORKTASK_SUPERVISER, ARUTYANIN_YURIY));
        udf.setUdfUser(generateUdfUser(STDT_HANDLER, handlerUser2));
        udf.setUdfList(generateUdfList(UDF_WORKTASK_REASSIGNCONTROL_MSG, CONTROL));
        contingentTaskController.performCommonOperation(task, CHANGE_RES_PERSON);
        ApiAsserts.assertThat(contingentTaskController.getResponse()).isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK).isParseableBody(TaskResponseBody.class).assertTask();
        var taskDetail = apiController.receiveTask(task.getNumber());
        CommonAssert.assertThat(taskDetail).isCorrectHandlerUser(handlerUser3.getLogin()).isCorrectUdfUSer(UDF_WORKTASK_SUPERVISER, creator.getLogin());
    }

    @Test(groups = {"PROC_WORKTASK", "Regression"}, description = "Передать на приёмку", dependsOnMethods = "reAssign2")
    public void taskAcceptance2() {
        apiController.updateToken(generateAuthToken(handlerUser3));
        udf = refreshUdf();
        task.setHandlerUser(generateUser(creator));
        udf.setUdfList(generateUdfList(UDF_SDFEATURE_DOCREVISION, DOC_REVISION_NO));
        udf.setUdfUser(generateUdfUser(STDT_HANDLER, creator));
        task.refreshUdf(udf);
        contingentTaskController.performCommonOperation(task, WORKTASK_TO_ACCEPTANCE);
        ApiAsserts.assertThat(contingentTaskController.getResponse()).isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK);

        var response = apiController.receiveSubTask(task.getNumber());
        CommonAssert.assertThat(response).isCorrectSubTaskStatus("CAT_ACCEPTTASK", STATUS_WORKTASK_ASSIGNED).isTaskNotCreate("CAT_DOCTASK");
    }
}
