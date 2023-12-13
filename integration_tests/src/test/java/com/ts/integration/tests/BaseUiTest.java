package com.ts.integration.tests;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.testng.SoftAsserts;
import com.codeborne.selenide.testng.TextReport;
import com.ts.common.application.Pages;
import com.ts.common.tests.AbstractBaseTest;
import com.ts.common.ui.driver.Driver;
import lombok.extern.slf4j.Slf4j;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Listeners;

@Slf4j
@Listeners({SoftAsserts.class, TextReport.class})
public class BaseUiTest extends AbstractBaseTest {
    @BeforeTest(alwaysRun = true)
    public void setupUi() {
        Configuration.browserCapabilities = Driver.initBrowserCapabilities();
        trackStudioPages = new Pages();
        log.warn("=====================API TESTS IS STARTED=====================");
    }
}
