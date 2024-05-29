package com.ts.integration.tests;

import com.ts.common.application.ui.Pages;
import com.ts.common.application.controllers.AuthToken;
import com.ts.common.application.controllers.TrackStudioApiControllers;
import com.ts.common.application.database.DbHelper;
import com.ts.common.asserts.ApiAsserts;
import com.ts.common.controllers.BaseController;
import com.ts.common.controllers.user.UserController;
import com.ts.common.entitites.commonEntities.Role;
import com.ts.common.entitites.commonEntities.Udfs;
import com.ts.common.entitites.commonEntities.User;
import com.ts.common.enums.Users;
import com.ts.common.listeners.TestListener;
import com.ts.common.tests.AbstractBaseTest;
import com.ts.common.utils.InitEntities;
import lombok.extern.slf4j.Slf4j;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Listeners;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.ts.common.application.controllers.TrackStudioHttpStatusCodes.HTTP_OK;
import static com.ts.common.entitites.commonEntities.Role.RoleConstants.*;
import static com.ts.common.entitites.commonEntities.Task.Constants.COLVIR;
import static com.ts.common.entitites.commonEntities.Task.Constants.SERVICE_DESK;
import static com.ts.common.entitites.commonEntities.User.Constants.CLI_ROOT;
import static com.ts.common.entitites.commonEntities.User.Constants.COMPANY_100_100;
import static com.ts.common.utils.InitEntities.*;

@Slf4j
@Listeners({TestListener.class})
public class BaseIntegrationTest extends AbstractBaseTest {
    protected AuthToken authToken;
    protected BaseController baseController;
    protected UserController userController;
    protected Udfs udf;
    protected Map<Role.RoleConstants, List<User>> userMap;
    private User user;
    private User cliRoot;
    private User company100100;
    private List<User> organizations;
    private List<User> clients;
    private List<User> deps;
    private List<User> workers;

    @BeforeSuite(alwaysRun = true)
    public void setUp() {
        this.authToken = generateAuthToken(Users.ROOT);
        apiController = new TrackStudioApiControllers(authToken);
        dbHelper = new DbHelper();
        trackStudioPages = new Pages();
        userMap = new HashMap<>();
        log.warn("=====================API TESTS IS STARTED=====================");
    }

    @BeforeTest(alwaysRun = true)
    public void init() {
        log.warn("=====================BEFORE TEST INITIALIZING=====================");
        baseController = apiController.getBaseController();
        userController = apiController.getUserController();
        cliRoot = userController.getUserBy(CLI_ROOT.getLogin());
        company100100 = userController.getUserBy(COMPANY_100_100.getLogin());

        //generate users
        userWithRoleGenerator();
    }

    private void userWithRoleGenerator() {
        ROLE_ORGANIZATION();
        userMap.put(ROLE_ORGANIZATION, organizations);

        ROLE_CLIENT();
        userMap.put(ROLE_CLIENT, clients);

        ROLE_DEP();
        userMap.put(ROLE_DEP, deps);

        createUserRoleWorker("AT_TASK_MANAGER");
        createUserRoleWorker("AT_TASK_PARTICIPANT");
        createUserRoleWorker("AT_SUPPORT_MANAGER");
        createUserRoleWorker("AT_SUPPLIERMANAGER");
        createUserRoleWorker("AT_TASK_ANALITIC");
        createUserRoleWorker("AT_SUPPORT_COSTMANAGER");
        createUserRoleWorker("AT_SDFEATURE_ANALYSIS_MANAGER");
        createUserRoleWorker("AT_SDFEATURE_IMPL_MANAGER");
        createUserRoleWorker("AT_CONTRACT_EMP");
        userMap.put(ROLE_WORKER, workers);
    }

    private void ROLE_ORGANIZATION() {
        String username = "ROLE_ORGANIZATION";
        if (organizations == null || organizations.isEmpty()) {
            organizations = new ArrayList<>();
        }
        if (isUserNotExist(username)) {
            user = getUser(username, ROLE_ORGANIZATION);
            user.defaultUser();
            user.setDefaultTask(generateTask(SERVICE_DESK));
            user.setParent(generateUser(cliRoot));
            userController.createUser(user);
            ApiAsserts.assertThat(userController.getResponse())
                    .isCorrectResponseCode(HTTP_OK);
        }
        organizations.add(user);
    }

    private void ROLE_CLIENT() {
        String username = "ROLE_CLIENT";
        if (clients == null || clients.isEmpty()) {
            clients = new ArrayList<>();
        }
        User organizationParent = userController.getUserBy(userMap, "ROLE_ORGANIZATION", ROLE_ORGANIZATION);
        User parent = generateUser(organizationParent.getId(), organizationParent.getLogin(), organizationParent.getName());
        if (isUserNotExist(username)) {
            user = getUser(username, ROLE_CLIENT);
            user.defaultUser();
            user.setDefaultTask(generateTask(COLVIR));
            user.setParent(parent);
            userController.createUser(user);
            ApiAsserts.assertThat(userController.getResponse())
                    .isCorrectResponseCode(HTTP_OK);
        }
        clients.add(user);
    }

    private void ROLE_DEP() {
        String username = "ROLE_DEP";
        if (deps == null || deps.isEmpty()) {
            deps = new ArrayList<>();
        }
        if (isUserNotExist(username)) {
            user = getUser(username, ROLE_DEP);
            user.defaultUser();
            user.setCompany("Colvir");
            user.setDefaultTask(generateTask(COLVIR));
            user.setParent(InitEntities.generateUser(company100100));
            userController.createUser(user);
            ApiAsserts.assertThat(userController.getResponse())
                    .isCorrectResponseCode(HTTP_OK);
        }
        deps.add(user);
    }

    private void createUserRoleWorker(String username) {
        if (workers == null || workers.isEmpty()) {
            workers = new ArrayList<>();
        }
        User depParent = userController.getUserBy(userMap, "ROLE_DEP", ROLE_DEP);
        User parent = generateUser(depParent.getId(), depParent.getLogin(), depParent.getName());
        if (isUserNotExist(username)) {
            user = getUser(username, ROLE_WORKER);
            user.defaultUser();
            user.setDefaultTask(generateTask(COLVIR));
            user.setParent(parent);
            userController.createUser(user);
            ApiAsserts.assertThat(userController.getResponse())
                    .isCorrectResponseCode(HTTP_OK);
        }
        workers.add(user);
    }

    private boolean isUserNotExist(String username) {
        user = userController.getUserBy(username);
        return user.getId() == null;
    }

}
