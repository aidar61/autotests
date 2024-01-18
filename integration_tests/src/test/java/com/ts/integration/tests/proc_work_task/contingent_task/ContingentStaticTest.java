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
import static com.ts.common.enums.TaskStatuses.STATUS_PROJECT_PLANNED;
import static com.ts.common.enums.TaskStatuses.STATUS_WORKTASK_NEW;
import static com.ts.common.utils.InitEntities.*;
import static com.ts.common.utils.RandomUtils.*;

public class ContingentStaticTest extends BaseIntegrationTest {
    public ContingentTaskController contingentTaskController;
    private GeneralTask task;
    private Parent parent;
    private Task taskRegProject;
    private User creator;
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
        creator = creators.get(0).getForUser();
    }


    @Test(groups = {"WorkTask", "Regression"}, description = "Заявка на подбор персонала")
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
        ApiAsserts.assertThat(response)
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isCorrectStatus(STATUS_WORKTASK_NEW).isEquals(task);
    }

    @Test(groups = {"WorkTask", "Regression"}, description = "Изменить участников", dependsOnMethods = "workTask")
    public void changeMembers() {
        udf = refreshUdf();
        task.refreshTask();
        udf.setUdfUser(generateUdfUser(UDF_PARTICIPANTS, ABDULLAEV_BAHODIR));
        task.refreshUdf(udf);
        contingentTaskController.performCommonOperation(task, CHANGE_MEMBERS);
        ApiAsserts.assertThat(contingentTaskController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask();

        var taskDetail = apiController.receiveTask(task.getNumber());
        CommonAssert
                .assertThat(taskDetail)
                .isCorrectUdfUSer(UDF_PARTICIPANTS, ABDULLAEV_BAHODIR.login);
    }

    @Test(groups = {"BugTask", "Regression"}, description = "Комментарий", dependsOnMethods = "changeMembers")
    public void taskComment() {
        var comment = generateString();
        task.setDescription(comment);
        udf = refreshUdf();
        task.refreshUdf(udf);
        contingentTaskController.performCommonOperation(task, COMMENT);
        var updateResponse = contingentTaskController.getResponse();
        ApiAsserts.assertThat(updateResponse)
                .checkingResponseMessageField("description", comment)
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isCorrectStatus(STATUS_WORKTASK_NEW);
    }

    @Test(groups = {"WorkTask", "Regression"}, description = "Изменить планируемую дату завершения", dependsOnMethods = "taskComment")
    public void changePlanTD() {
        udf = refreshUdf();
        task.refreshTask();
        task.setDescription(generateString());
        task.refreshUdf(udf);
        var pannedStartDate = DateUtils.getCurrentDate(6);
        udf.setUdfDate(generateUdfDate(UDF_WORKTASK_PLANTD, pannedStartDate));
        contingentTaskController.performCommonOperation(task, CHANGE_PLAN_TD);
        ApiAsserts.assertThat(contingentTaskController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask();
        var taskDetail = apiController.receiveTask(task.getNumber());
        CommonAssert.assertThat(taskDetail)
                .isCorrectUdfDate(UDF_WORKTASK_PLANTD, pannedStartDate);
    }

    @Test(groups = {"WorkTask", "Regression"}, description = "Изменить услугу", dependsOnMethods = "changePlanTD")
    public void taskChangeService() {
        task.setDescription(generateString());
        udf = refreshUdf();
        task.refreshTask();
        udf.setUdfList(generateUdfList(UDF_MIS_SERVICE, MIS_SERVICE2));
        task.refreshUdf(udf);
        contingentTaskController.performCommonOperation(task, CHANGE_SERVICE);
        var response = contingentTaskController.getResponse();
        ApiAsserts.assertThat(response)
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK);

        var taskDetail = apiController.receiveTask(task.getNumber());
        CommonAssert
                .assertThat(taskDetail)
                .isCorrectUdfList(UDF_MIS_SERVICE, MIS_SERVICE2);
    }

    @Test(groups = {"WorkTask", "Regression"}, description = "Назначить контролёра", dependsOnMethods = "taskChangeService")
    public void setSupervise() {
        udf = refreshUdf();
        task.refreshTask();
        task.setDescription(generateString());
        task.refreshUdf(udf);
        udf.setUdfUser(generateUdfUser(UDF_WORKTASK_SUPERVISER, ARUTYANIN_YURIY));
        contingentTaskController.performCommonOperation(task, SUPERVISE);
        ApiAsserts.assertThat(contingentTaskController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask();
        var taskDetail = apiController.receiveTask(task.getNumber());
        CommonAssert.assertThat(taskDetail)
                .isCorrectUdfUSer(UDF_WORKTASK_SUPERVISER, ARUTYANIN_YURIY.login);
    }

    @Test(groups = {"WorkTask", "Regression"}, description = "Назначить наблюдателя", dependsOnMethods = "setSupervise")
    public void setWatcher() {
        udf = refreshUdf();
        task.refreshTask();
        task.setDescription(generateString());
        task.refreshUdf(udf);
        udf.setUdfUser(generateUdfUser(UDF_WATCHER, ARTEMEVA_MARINA));
        contingentTaskController.performCommonOperation(task, WATCH);
        ApiAsserts.assertThat(contingentTaskController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask();
        var taskDetail = apiController.receiveTask(task.getNumber());
        CommonAssert.assertThat(taskDetail)
                .isCorrectUdfUSer(UDF_WATCHER, ARTEMEVA_MARINA.login);
    }
}
