package com.ts.integration.tests.ui;


import com.codeborne.selenide.Selenide;
import com.ts.common.enums.Users;
import com.ts.common.request.ApiRequest;
import com.ts.common.ui.pages.HomePage;
import com.ts.common.ui.pages.LoginPage;
import com.ts.integration.tests.BaseUiTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static com.ts.common.application.controllers.TrackStudioEndPoints.APP;
import static com.ts.common.config.AppConfigProvider.STAND_URL;
public class UiTest extends BaseUiTest {

    LoginPage loginPage;
    HomePage homePage;

    @BeforeClass(alwaysRun = true)
    public void beforeClass() {
        loginPage = trackStudioPages.getLoginPage();
        homePage = trackStudioPages.getHomePage();
    }

    @Test
    public void uiLoginTest() {
        Selenide.open(ApiRequest.getEndpoint(STAND_URL, APP));
        loginPage.login(Users.ROOT);
        homePage.openTreeBtn()
                .openPlusAllElements()
                .openAllTreeElements();
    }
}
