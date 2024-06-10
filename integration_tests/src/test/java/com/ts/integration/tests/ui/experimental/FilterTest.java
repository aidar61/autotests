package com.ts.integration.tests.ui.experimental;

import com.ts.common.config.AppConfigProvider;
import com.ts.common.ui.pages.FilterPage;
import com.ts.common.ui.pages.HomePage;
import com.ts.common.ui.pages.LoginPage;
import com.ts.integration.tests.BaseUiTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.UUID;

import static com.codeborne.selenide.Selenide.open;

import static com.ts.common.config.AppConfigProvider.STAND_URL;
import static com.ts.common.config.AppConfigProvider.getUi;
import static com.ts.common.request.ApiRequest.getEndpoint;

public class FilterTest extends BaseUiTest {
    LoginPage loginPage;
    HomePage homePage;
    FilterPage filterPage;

    String uuid = UUID.randomUUID().toString();
    String filterGroupName = "Selenide_" + "_" + uuid;
    String filterpName = "Selenide_" + "_" + uuid;


    @BeforeClass(alwaysRun = true)
    public void beforeClass() {
        loginPage = trackStudioPages.getLoginPage();
        homePage = trackStudioPages.getHomePage();
        filterPage = trackStudioPages.getFilterPage();
    }

    @Test
    public void editFilter() {
        open(getEndpoint(STAND_URL, "app"));
        loginPage.loginNoToken(getUi().baseUser());
        homePage.openFilterSetting();

        filterPage.setFilterName(filterpName)
                .setGroupName(filterGroupName)
                .clearSearchParams()
                .saveNewFilter()
                .checkSaveIsSuccess();
    }
}
