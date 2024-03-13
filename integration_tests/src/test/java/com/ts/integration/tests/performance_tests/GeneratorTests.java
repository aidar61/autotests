package com.ts.integration.tests.performance_tests;

import com.ts.common.application.controllers.TrackStudioHttpStatusCodes;
import com.ts.common.asserts.ApiAsserts;
import com.ts.common.controllers.ProjectController;
import com.ts.common.controllers.TaskResponseBody;
import com.ts.common.controllers.sdFeature.SdFeatureController;
import com.ts.common.controllers.user.UserController;
import com.ts.common.controllers.workTask.DevTaskController;
import com.ts.common.entitites.commonEntities.*;
import com.ts.common.entitites.tasks.GeneralTask;
import com.ts.common.enums.Operations;
import com.ts.common.enums.Tables;
import com.ts.common.utils.DateUtils;
import com.ts.common.utils.InitEntities;
import com.ts.integration.tests.BaseIntegrationTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static com.ts.common.entitites.commonEntities.List.Constants.*;
import static com.ts.common.entitites.commonEntities.Role.Constants.*;
import static com.ts.common.entitites.commonEntities.Status.Priority.NORMAL;
import static com.ts.common.entitites.commonEntities.Status.Priority.NORMAL_PRIORITY;
import static com.ts.common.entitites.commonEntities.Task.Constants.PRODUCT;
import static com.ts.common.entitites.commonEntities.Udfs.UdfSd.*;
import static com.ts.common.enums.Operations.CAT;
import static com.ts.common.enums.Operations.TOPRECOST;
import static com.ts.common.enums.TaskType.*;
import static com.ts.common.utils.InitEntities.*;

public class GeneratorTests extends BaseIntegrationTest {
    User jmeter_user;
    String username = "jmeter-user";
    String project = "company.100.07.01";
    Parent SDPROJECT_PARENT;
    Parent GROUPTASK_PARENT;
    Parent REGFOLDER_PARENT;
    SdFeatureController sdFeatureController;
    DevTaskController devTaskController;
    ProjectController projectController;
    UserController userController;
    GeneralTask task;
    GeneralTask JMETER_SDPROJECTGROUP;
    GeneralTask JMETER_SDPROJECT;
    GeneralTask JMETER_GROUPTASKS;
    GeneralTask JMETER_GENPLAN;
    GeneralTask JMETER_REGFOLDER;
    GeneralTask JMETER_REGPROJECT;

    @BeforeClass(alwaysRun = true)
    public void beforeClass() {
        userController = apiController.getUserController();
        projectController = apiController.getProjectController();
        devTaskController = apiController.getDevTaskController();
        sdFeatureController = apiController.getSdFeatureController();
        SDPROJECT_PARENT = InitEntities.generateParent("818181df62d0f2270162d34a073258ec", "758008");
        GROUPTASK_PARENT = InitEntities.generateParent("818181df62d0f2270162d34a07fd58f2", "758009");
        REGFOLDER_PARENT = InitEntities.generateParent("818181df62d0f2270162d34a060558e6", "758007");
    }

    private void createJmeterUser() {
        jmeter_user = userController.createUser(username, project);
        if (userController.getResponse().getStatusCode() == 400) {
            jmeter_user = userController.getUserBy(username);
        }
    }

    private void CAT_SDPROJECTGROUP() {
        task = InitEntities.getGeneralTask(SDPROJECTGROUP, Operations.CAT);
        udf = refreshUdf();
        task.refreshTask();

        task.setName("JMETER_SDPROJECTGROUP");
        task.setParent(SDPROJECT_PARENT);
        task.setPriority(InitEntities.generatePriority(2));

        udf.setUdfDouble(generateUdfDouble(UDF_SD_COST1CAT, 100));
        udf.setSecondUdfDouble(generateUdfDouble(UDF_SD_COST2CAT, 200));
        udf.setThirdUdfDouble(generateUdfDouble(UDF_SD_COST3CAT, 300));

        task.refreshUdf(udf);
        JMETER_SDPROJECTGROUP = projectController.createProject(task);
        ApiAsserts.assertThat(projectController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class);
    }

    private void CAT_SDPROJECT() {
        Parent sdProjectParent = InitEntities.generateParent(JMETER_SDPROJECTGROUP);
        task = InitEntities.getGeneralTask(SDPROJECT, Operations.CAT);
        udf = refreshUdf();
        task.refreshTask();

        task.setName("JMETER_SDPROJECT");
        task.setParent(sdProjectParent);

        udf.setUdfList(generateUdfList(UDF_SDPROJECT_SUPPORTTYPE, EXTENDED));
        udf.setUdfInteger(generateUdfInteger(UDF_SD_COST1CAT, 100));
        udf.setSecondUdfInteger(generateUdfInteger(UDF_SD_COST2CAT, 200));
        udf.setThirdUdfInteger(generateUdfInteger(UDF_SD_COST3CAT, 300));

        task.refreshUdf(udf);
        JMETER_SDPROJECT = projectController.createProject(task);
        ApiAsserts.assertThat(projectController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class);
    }

    private void CAT_GROUPTASKS() {
        task = InitEntities.getGeneralTask(GROUPTASKS, Operations.CAT);
        udf = refreshUdf();
        task.refreshTask();

        task.setName("JMETER_GROUPTASKS");
        task.setParent(GROUPTASK_PARENT);
        task.setPriority(InitEntities.generatePriority(NORMAL_PRIORITY));

        task.refreshUdf(udf);

        JMETER_GROUPTASKS = projectController.createProject(task);
        ApiAsserts.assertThat(projectController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class);
    }

    private void CAT_GENPLAN() {
        Parent genPlanParent = InitEntities.generateParent(JMETER_GROUPTASKS);
        task = InitEntities.getGeneralTask(GENPLAN, Operations.CAT);
        udf = refreshUdf();
        task.refreshTask();

        task.setName("JMETER_GENPLAN");
        task.setParent(genPlanParent);

        udf.setUdfList(generateUdfList(UDF_PROJECT_MANAGING, YES_NEED));
        udf.setSecondUdfList(generateUdfList(UDF_PROJECT_HANDLE_OWN_TASKS, YES_V3));
        udf.setThirdUdfList(generateUdfList(UDF_PROJECT_MEMBERCODEREVIEW, ALL_REVIEWERS));
        task.refreshUdf(udf);

        JMETER_GENPLAN = projectController.createProject(task);
        ApiAsserts.assertThat(projectController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class);
    }

    private void CAT_REGFOLDER() {
        task = InitEntities.getGeneralTask(REGFOLDER, Operations.CAT);
        udf = refreshUdf();
        task.refreshTask();

        task.setName("JMETER_REGFOLDER");
        task.setParent(REGFOLDER_PARENT);
        task.setUdfs(udf);

        JMETER_REGFOLDER = projectController.createProject(task);
        ApiAsserts.assertThat(projectController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class);
    }

    private void CAT_REGPROJECT() {
        Parent regFolderParent = InitEntities.generateParent(JMETER_REGFOLDER);
        task = InitEntities.getGeneralTask(REGPROJECT, Operations.CAT);
        udf = refreshUdf();
        task.refreshTask();

        task.setName("JMETER_REGPROJECT");
        task.setParent(regFolderParent);

        udf.setUdfMultiList(generateUdfMultiList(UDF_REGISTRY_LOB, BANKING_BASE_WORK));
        udf.setUdfList(generateUdfList(UDF_CDP_CUSTOMER, KAZAH_NBP));
        udf.setUdfTask(generateUdfTask(UDF_REGISTRY_FOLDER, JMETER_GENPLAN.mapToTask()));
        udf.setSecondUdfTask(generateUdfTask(UDF_REGISTRY_SUPPORT, JMETER_SDPROJECT.mapToTask()));
        udf.setSecondUdfList(generateUdfList(UDF_MIS_TPRJ, PROC_COLVIR_02));

        task.refreshUdf(udf);
        JMETER_REGPROJECT = projectController.createProject(task);
        ApiAsserts.assertThat(projectController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class);
    }

    private void assignRoles() {
        userController.assignRoleToTask(JMETER_SDPROJECTGROUP, jmeter_user, ROLE_SUPPORT_COSTMANAGER);
        userController.assignRoleToTask(JMETER_SDPROJECTGROUP, jmeter_user, ROLE_SUPPORT_MANAGER);
        userController.assignRoleToTask(JMETER_GROUPTASKS, jmeter_user, ROLE_TASK_MANAGER);
    }

    @Test(groups = "Generator", description = "Task generator")
    public void taskGeneratorTest() {
        // Создать пользователя в узле company.100.07.01 с параметрами:
        createJmeterUser();
        // Создать CAT_SDPROJECTGROUP в узле 758008 с параметрами
        CAT_SDPROJECTGROUP();
        // Создать CAT_SDPROJECT в узле JMETER_SDPROJECTGROUP  с параметрами
        CAT_SDPROJECT();
        // Создать CAT_GROUPTASKS в узле 758009 с параметрами:
        CAT_GROUPTASKS();
        // Создать CAT_GENPLAN в узле JMETER_GROUPTASKS с параметрами:
        CAT_GENPLAN();
        // Создать CAT_REGFOLDER в узле 758007 с параметрами:
        CAT_REGFOLDER();
        // Создать CAT_REGPROJECT в узле JMETER_REGFOLDER с параметрами:
        CAT_REGPROJECT();
        // Установить права для  jmeter-user
        assignRoles();

        // Создать в JMETER_SDPROJECT 100 запросов CAT_SDFEAURE
        apiController.updateToken(generateAuthToken(jmeter_user));
        Parent sdFeatureParent = InitEntities.generateParent(JMETER_SDPROJECT);

        for (int i = 0; i < 100; i++) {
            task = InitEntities.getGeneralTask(SD_FEATURE, Operations.CAT);
            udf = refreshUdf();

            udf.setUdfList(generateUdfList(UDF_SDFEATURE_TYPE, OWN));

            task.refreshUdf(udf);
            task.setParent(sdFeatureParent);
            task.setDescription(Tables.SD_FEATURE.getTable());
            sdFeatureController.createTask(task);
            ApiAsserts.assertThat(sdFeatureController.getResponse())
                    .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                    .isParseableBody(TaskResponseBody.class);
            //  Перевести все запросы CAT_SDFEATURE в состояние анализа с помощью операции "Начать предварительную оценку".
            udf = refreshUdf();
            task.refreshTask();

            udf.setUdfUser(generateUdfUser(STDT_HANDLER, jmeter_user));
            task.refreshUdf(udf);
            task.setHandlerUser(jmeter_user);

            sdFeatureController.performCommonOperation(task, TOPRECOST);
            ApiAsserts.assertThat(sdFeatureController.getResponse())
                    .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                    .isParseableBody(TaskResponseBody.class);
        }

        // Создать JMETER_GENPLAN 100 задач CAT_DEVTASK указав ответственного - jmeter-user
        Parent jmeterGenPlan = InitEntities.generateParent(JMETER_GENPLAN);
        task = InitEntities.getGeneralTask(DEV_TASK, CAT);
        udf = refreshUdf();

        udf.setUdfList(generateUdfList(UDF_CDP_BL, BANKING_BASE_WORK_V2));
        udf.setUdfTask(generateUdfTask(UDF_SD_MODULE, Task.Constants.AKKREDITIVES));
        udf.setSecondUdfList(generateUdfList(UDF_CDP_ACCEPTANCE, UDF_CDP_ACCEPTANCE_NO));
        udf.setUdfDate(generateUdfDate(UDF_WORKTASK_ANALYSISFD, DateUtils.getCurrentDate(0)));
        udf.setSecondUdfTask(generateUdfTask(UDF_PRODUCT, PRODUCT));
        udf.setThirdUdfList(generateUdfList(UDF_SDFEATURE_GENUSE, GENERAL, "{\"username\":\"aziskakov\",\"name\":\"Искаков Азамат\"}"));
        udf.setFourthUdfList(generateUdfList(UDF_WORKTASK_CHANGEWORKERINRQST, YES_AND_LEAVE_THIS_ROLE_TO_THE_CURRENT_PERFORMER));

        task.refreshUdf(udf);
        task.setParent(jmeterGenPlan);
        task.setPriority(InitEntities.generatePriority(NORMAL));
        task.setHandlerUser(jmeter_user);

        for (int i = 0; i < 100; i++) {
            devTaskController.createDevTask(task);
            ApiAsserts.assertThat(devTaskController.getResponse())
                    .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                    .isParseableBody(TaskResponseBody.class);
        }
    }
}
