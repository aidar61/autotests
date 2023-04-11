package com.ts.common.ui.driver;

import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.NoSuchElementException;

import static com.ts.common.config.AppConfigProvider.IMPLICITLY_SLEEP_MS;
import static com.ts.common.config.AppConfigProvider.IMPLICITLY_WAIT_SEC;

public class WebElementActions {
    protected WebDriver driver;
    protected WebDriverWait wait;
    protected BrowserManager browse;

    public WebElementActions() {
        this.browse = new BrowserManager();
        this.wait = (WebDriverWait) new WebDriverWait(driver, Duration.ofSeconds(IMPLICITLY_WAIT_SEC), Duration.ofSeconds(IMPLICITLY_SLEEP_MS))
                .ignoring(NoSuchElementException.class)
                .ignoring(StaleElementReferenceException.class)
                .ignoring(org.openqa.selenium.TimeoutException.class)
                .ignoring(org.openqa.selenium.WebDriverException.class);
    }
}
