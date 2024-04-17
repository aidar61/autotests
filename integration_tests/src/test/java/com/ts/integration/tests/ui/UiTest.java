package com.ts.integration.tests.ui;

import com.codeborne.selenide.Driver;
import com.codeborne.selenide.Selenide;
import com.ts.common.controllers.sla.SlaFeatureController;
import com.ts.common.controllers.sla.SlaHelpController;
import com.ts.common.controllers.workTask.WorkTaskController;
import com.ts.common.entitites.tasks.GeneralTask;
import com.ts.common.enums.Operations;
import com.ts.common.enums.TaskType;
import com.ts.common.enums.Users;
import com.ts.common.request.ApiRequest;
import com.ts.common.ui.pages.HomePage;
import com.ts.common.ui.pages.LoginPage;
import com.ts.common.utils.InitEntities;
import com.ts.integration.tests.BaseUiTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static com.codeborne.selenide.Configuration.browserVersion;
import static com.codeborne.selenide.Selenide.open;
import static com.ts.common.application.controllers.TrackStudioEndPoints.APP;
import static com.ts.common.config.AppConfigProvider.STAND_URL;
import static com.ts.common.enums.Operations.CAT;
import static com.ts.common.enums.TaskType.SLA_FEATURE;
import static com.ts.common.utils.InitEntities.getGeneralTask;

public class UiTest extends BaseUiTest {
    LoginPage loginPage;
    HomePage homePage;
    SlaFeatureController slaFeatureController;
    private GeneralTask task;

    @BeforeClass(alwaysRun = true)
    public void beforeClass() {
        loginPage = trackStudioPages.getLoginPage();
        homePage = trackStudioPages.getHomePage();
    }

    @Test
    public void uiLoginTest() {
        open(ApiRequest.getEndpoint(STAND_URL, APP));
        loginPage.login(Users.ROOT);
        homePage.openTreeBtn()
                .openPlusAllElements()
                .openAllTreeElements();
    }

    @Test
    public void experemental () {
        slaFeatureController = apiController.getSlaFeatureController();
        GeneralTask generalTask = InitEntities.getGeneralTask(TaskType.SLA_FEATURE, Operations.CAT);
        slaFeatureController.createSlaFeatureTask(generalTask);
        String taskURL = "http://tsdev7.dev.colvir.ru/TrackStudio/app/task/" + generalTask.getNumber();
        open(taskURL);

    }
}
