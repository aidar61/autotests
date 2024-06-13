package com.ts.integration.tests;

import com.ts.common.application.controllers.AuthToken;
import com.ts.common.application.controllers.TrackStudioApiControllers;
import com.ts.common.application.database.DbHelper;
import com.ts.common.application.ui.Pages;
import com.ts.common.controllers.BaseController;
import com.ts.common.controllers.user.UserController;
import com.ts.common.entitites.commonEntities.Role;
import com.ts.common.entitites.commonEntities.Udfs;
import com.ts.common.entitites.commonEntities.User;
import com.ts.common.entitites.tasks.GeneralTask;
import com.ts.common.enums.Users;
import com.ts.common.generators.TaskGenerator;
import com.ts.common.generators.UserGenerator;
import com.ts.common.listeners.TestListener;
import com.ts.common.tests.AbstractBaseTest;
import lombok.extern.slf4j.Slf4j;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Listeners;

import java.util.List;
import java.util.Map;

import static com.ts.common.config.AppConfigProvider.getUserConfig;
import static com.ts.common.entitites.commonEntities.Role.RoleConstants.*;
import static com.ts.common.utils.InitEntities.generateAuthToken;

@Slf4j
@Listeners({TestListener.class})
public class BaseIntegrationTest extends AbstractBaseTest {
    protected AuthToken authToken;
    protected BaseController baseController;
    protected UserController userController;
    protected Udfs udf;
    protected Map<Role.RoleConstants, List<User>> userRoles;
    protected TaskGenerator taskGenerator;

    @BeforeSuite(alwaysRun = true)
    public void setUp() {
        this.authToken = generateAuthToken(Users.ROOT);
        apiController = new TrackStudioApiControllers(authToken);
        dbHelper = new DbHelper();
        trackStudioPages = new Pages();
        log.warn("=====================API TESTS IS STARTED=====================");
    }

    @BeforeTest(alwaysRun = true)
    public void init() {
        log.warn("=====================BEFORE TEST INITIALIZING=====================");
        baseController = apiController.getBaseController();
        userController = apiController.getUserController();

        log.warn("=====================GENERATOR IS STARTING=====================");
        //Generator
        userRoles = UserGenerator.create(userController).generateUsers();
        taskGenerator = TaskGenerator.create(apiController, dbHelper);
        taskGenerator.generateTasks(
                "758009",
                "758008",
                "758007",
                "462311"
        );
        assignRoles();
        log.warn("=====================GENERATOR IS ENDING=====================");
    }

    private void assignRoles() {
        GeneralTask at_sdproject = taskGenerator.getAt_sdproject();

        User at_support_manager = userController.getUserBy(userRoles, ROLE_WORKER, getUserConfig().at_support_manager());
        userController.assignRoleToTask(at_sdproject, at_support_manager, ROLE_SUPPORT_MANAGER, ROLE_SUPPORT_MEMBER);

        User at_task_analitic = userController.getUserBy(userRoles, ROLE_WORKER, getUserConfig().at_task_analitic());
        userController.assignRoleToTask(at_sdproject, at_task_analitic, ROLE_TASK_ANALITIC, ROLE_SUPPORT_MEMBER);

        User at_support_costmanager = userController.getUserBy(userRoles, ROLE_WORKER, getUserConfig().at_support_costmanager());
        userController.assignRoleToTask(at_sdproject, at_support_costmanager, ROLE_SUPPORT_COSTMANAGER, ROLE_SUPPORT_MEMBER);

        User at_sdfeature_analysis_manager = userController.getUserBy(userRoles, ROLE_WORKER, getUserConfig().at_sdfeature_analysis_manager());
        userController.assignRoleToTask(at_sdproject, at_sdfeature_analysis_manager, ROLE_SDFEATURE_ANALYSIS_MANAGER, ROLE_SUPPORT_MEMBER);

        User at_sdfeature_impl_manager = userController.getUserBy(userRoles, ROLE_WORKER, getUserConfig().at_sdfeature_impl_manager());
        userController.assignRoleToTask(at_sdproject, at_sdfeature_impl_manager, ROLE_SDFEATURE_IMPL_MANAGER, ROLE_SUPPORT_MEMBER);

        User at_contract_emp = userController.getUserBy(userRoles, ROLE_WORKER, getUserConfig().at_contract_emp());
        userController.assignRoleToTask(at_sdproject, at_contract_emp, ROLE_CONTRACT_EMP, ROLE_SUPPORT_MEMBER);

        User role_organization = userController.getUserBy(userRoles, ROLE_ORGANIZATION, getUserConfig().role_organization());
        userController.assignRoleToTask(at_sdproject, role_organization, ROLE_CLIENT);

        User role_client = userController.getUserBy(userRoles, ROLE_CLIENT, getUserConfig().role_client());
        userController.assignRoleToTask(at_sdproject, role_client, ROLE_CLIENT);

        User role_client_2 = userController.getUserBy(userRoles, ROLE_CLIENT, getUserConfig().role_client_2());
        userController.assignRoleToTask(at_sdproject, role_client_2, ROLE_CLIENT);

        GeneralTask at_genplan = taskGenerator.getAt_genplan();

        User at_task_manager = userController.getUserBy(userRoles, ROLE_WORKER, getUserConfig().at_task_manager());
        userController.assignRoleToTask(at_genplan, at_task_manager, ROLE_TASK_MANAGER, ROLE_TASK_PARTICIPANT);

        User at_task_manager_2 = userController.getUserBy(userRoles, ROLE_WORKER, getUserConfig().at_task_manager_2());
        userController.assignRoleToTask(at_genplan, at_task_manager_2, ROLE_TASK_MANAGER, ROLE_TASK_PARTICIPANT);

        User at_task_participant = userController.getUserBy(userRoles, ROLE_WORKER, getUserConfig().at_task_participant());
        userController.assignRoleToTask(at_genplan, at_task_participant, ROLE_TASK_PARTICIPANT);

        User at_task_participant_2 = userController.getUserBy(userRoles, ROLE_WORKER, getUserConfig().at_task_participant_2());
        userController.assignRoleToTask(at_genplan, at_task_participant_2, ROLE_TASK_PARTICIPANT);

        GeneralTask at_bdku_installation = taskGenerator.getAt_bdku_installation();

        User at_group_techonologyservice = userController.getUserBy(userRoles, ROLE_WORKER, getUserConfig().at_group_technologyservice());
        userController.assignRoleToTask(at_bdku_installation, at_group_techonologyservice, ROLE_TASK_PARTICIPANT, ROLE_GROUP_TECHNOLOGYSERVICE);
    }

}
