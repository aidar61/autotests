package com.ts.integration.tests;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.testng.SoftAsserts;
import com.codeborne.selenide.testng.TextReport;
import com.ts.common.ui.driver.Driver;
import lombok.extern.slf4j.Slf4j;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Listeners;

@Slf4j
@Listeners({SoftAsserts.class, TextReport.class})
public class BaseUiTest extends BaseIntegrationTest {
    @BeforeTest(alwaysRun = true)
    public void setupUi() {
        Configuration.browserCapabilities = Driver.initBrowserCapabilities();
        log.warn("=====================UI TESTS IS STARTED=====================");
    }

    @AfterTest(alwaysRun = true)
    public void tearDownUi() {
        Selenide.clearBrowserCookies();
        Selenide.clearBrowserLocalStorage();
        Selenide.closeWebDriver();
        log.warn("=====================UI TESTS IS STOPPED=====================");
    }
}
