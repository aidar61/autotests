package com.ts.integration.tests;

import com.codeborne.selenide.Configuration;

import com.codeborne.selenide.testng.SoftAsserts;
import com.codeborne.selenide.testng.TextReport;
import com.ts.common.application.Pages;
import com.ts.common.application.controllers.TrackStudioApiControllers;
import com.ts.common.application.database.DbHelper;
import com.ts.common.enums.Users;
import com.ts.common.tests.AbstractBaseTest;
import com.ts.common.ui.driver.Driver;
import com.ts.common.ui.pages.FormPage;
import com.ts.common.ui.pages.LoginPage;
import com.ts.common.utils.InitEntities;
import lombok.extern.slf4j.Slf4j;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Listeners;

import static com.codeborne.selenide.Selenide.open;

@Slf4j
@Listeners({SoftAsserts.class, TextReport.class})
public class BaseUiTest extends AbstractBaseTest {


    @BeforeTest(alwaysRun = true)
    public void setupUi() {
        Configuration.browserCapabilities = Driver.initBrowserCapabilities();
        apiController = new TrackStudioApiControllers(InitEntities.generateAuthToken(Users.ROOT));
        trackStudioPages = new Pages();
        dbHelper = new DbHelper();
        log.warn("=====================UI TESTS IS STARTED=====================");
    }

    public static void auth(String url, String userLogin) {
        LoginPage loginPage = trackStudioPages.getLoginPage(); //TODO использовать LoginPage как поле и добавить инициализацию в setupUi()
        open(url);
        loginPage.loginNoToken(userLogin);
    }
}
