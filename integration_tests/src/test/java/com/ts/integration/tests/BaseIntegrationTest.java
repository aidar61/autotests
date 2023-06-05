package com.ts.integration.tests;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.testng.SoftAsserts;
import com.codeborne.selenide.testng.TextReport;
import com.ts.common.application.Pages;
import com.ts.common.application.controllers.AuthToken;
import com.ts.common.application.controllers.TrackStudioApiControllers;
import com.ts.common.application.database.DbHelper;
import com.ts.common.controllers.BaseController;
import com.ts.common.controllers.UserController;
import com.ts.common.entitites.commonEntities.Udfs;
import com.ts.common.entitites.commonEntities.User;
import com.ts.common.enums.Users;
import com.ts.common.listeners.TestListener;
import com.ts.common.tests.AbstractBaseTest;
import com.ts.common.ui.driver.Driver;
import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Listeners;

import static com.ts.common.utils.InitEntities.generateAuthToken;

@Slf4j
@Listeners({TestListener.class, SoftAsserts.class, TextReport.class})
public class BaseIntegrationTest extends AbstractBaseTest {
    protected Response response;
    protected AuthToken authToken;
    protected BaseController slaController;
    protected UserController userController;
    protected Udfs udf;

    @BeforeSuite(alwaysRun = true)
    public void setUp() {
        log.info("test");
        Configuration.browserCapabilities = Driver.initBrowserCapabilities();
        this.authToken = generateAuthToken(Users.CLIENT);
        apiController = new TrackStudioApiControllers(authToken);
        trackStudioPages = new Pages();
        slaController = apiController.getSlaController();
        userController = apiController.getUserController();
        dbHelper = new DbHelper();
        log.warn("=====================API TESTS IS STARTED=====================");
    }

}
