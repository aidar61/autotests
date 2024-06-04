package com.ts.integration.tests;

import com.ts.common.application.ui.Pages;
import com.ts.common.application.controllers.AuthToken;
import com.ts.common.application.controllers.TrackStudioApiControllers;
import com.ts.common.application.database.DbHelper;
import com.ts.common.controllers.BaseController;
import com.ts.common.controllers.user.UserController;
import com.ts.common.entitites.commonEntities.*;
import com.ts.common.enums.Users;
import com.ts.common.generators.TaskGenerator;
import com.ts.common.generators.UserGenerator;
import com.ts.common.listeners.TestListener;
import com.ts.common.tests.AbstractBaseTest;
import lombok.extern.slf4j.Slf4j;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;

import static com.ts.common.utils.InitEntities.*;

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
        taskGenerator.generateTasks("758009", "758008", "758007");
        log.info("Users created\n{}", userRoles.values());
        log.info("Task structure created\n{}", taskGenerator.toString());
        log.warn("=====================GENERATOR IS ENDING=====================");
    }

    @Test(groups = {"GENERATOR"}, description = "Generating tasks and user for precondition")
    void generator() {

    }

}
