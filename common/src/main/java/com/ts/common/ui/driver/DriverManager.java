package com.ts.common.ui.driver;

import org.openqa.selenium.WebDriver;

public class DriverManager {
    private static WebDriver driver;

    private DriverManager() {
    }

    public static WebDriver getDriver() {
        if (driver == null) {
            driver = ChromeWebDriver.loadChromeDriver();
        }
        return driver;
    }

    public void closeDriver() {
        getDriver().quit();
    }
}
