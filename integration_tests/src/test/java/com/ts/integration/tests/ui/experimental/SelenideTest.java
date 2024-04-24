package com.ts.integration.tests.ui.experimental;

import com.codeborne.selenide.Configuration;
import com.ts.common.ui.driver.Driver;
import org.testng.annotations.Test;

import static com.codeborne.selenide.Selenide.*;

public class SelenideTest {
    @Test
    public void authTest(){
        //Configuration.browserCapabilities  = Driver.initBrowserCapabilities();
        open("http://tsdev7.dev.colvir.ru/TrackStudio/app");
        $("#login").click();
        $("#login").setValue("vkhudoshin");
        $("#password").click();
        $("#password").setValue("password");
        $x("//span[text()='Войти']").click();

    }
}
