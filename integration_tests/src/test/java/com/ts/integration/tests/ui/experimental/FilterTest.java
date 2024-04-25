package com.ts.integration.tests.ui.experimental;

import com.codeborne.selenide.Condition;
import com.ts.common.asserts.UiAsserts;
import com.ts.common.ui.pages.FilterPage;
import com.ts.common.ui.pages.HomePage;
import com.ts.common.ui.pages.LoginPage;
import com.ts.integration.tests.BaseUiTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static com.codeborne.selenide.Selenide.open;

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
        //TODO здесь вместо статичной ссылки лучше использовать динамичную "STAND_URL" используя метод getEndpoint();
        open("http://tsdev8.dev.colvir.ru/TrackStudio/app");
        loginPage.loginNoToken("vkhudoshin");
        homePage.openFilterSetting();

        FilterPage filterPage;//TODO page хранить лучше как поле класса и производить инициализацию в BeforeClass()
        filterPage = trackStudioPages.getFilterPage();

        filterPage.setFilterName("Selenide_test")
                .setGroupName("Selenide_test")
                .clearSearchParams()
                .saveAsNewForm();

        UiAsserts.assertThat(filterPage.getSuccessMsg())
                .isElementAccordCondition(Condition.visible)
                .isElementNotAccordCondition(Condition.visible);
    }
}
