package com.ts.integration.tests.ui.experimental;

import com.ts.common.enums.Users;
import com.ts.common.request.ApiRequest;
import com.ts.common.ui.pages.FilterPage;
import com.ts.common.ui.pages.HomePage;
import com.ts.common.ui.pages.LoginPage;
import com.ts.integration.tests.BaseUiTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static com.codeborne.selenide.Selenide.open;
import static com.ts.common.application.controllers.TrackStudioEndPoints.APP;
import static com.ts.common.config.AppConfigProvider.STAND_URL;

public class FilterTest extends BaseUiTest {
    LoginPage loginPage;
    HomePage homePage;


    @BeforeClass(alwaysRun = true)
    public void beforeClass() {
        loginPage = trackStudioPages.getLoginPage();
        homePage = trackStudioPages.getHomePage();
    }

    @Test
    public void editFilter() {
        open("http://tsdev8.dev.colvir.ru/TrackStudio/app");
        loginPage.loginNoToken("vkhudoshin");
        homePage.openFilterSetting();

        FilterPage filterPage;
        filterPage = trackStudioPages.getFilterPage();

        filterPage.setFilterName("Selenide_test")
                .setGroupName("Selenide_test")
                .clearSearchParams()
                .saveAsNewForm()
                .checkSaveIsSuccess();
    }
}
