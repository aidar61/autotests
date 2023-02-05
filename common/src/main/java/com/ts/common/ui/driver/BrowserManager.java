package com.ts.common.ui.driver;

import org.openqa.selenium.Cookie;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;

import java.util.ArrayList;
import java.util.Set;

public class BrowserManager {

    protected WebDriver driver;

    public BrowserManager() {
        this.driver = DriverManager.getDriver();
    }

    public void closeTab() {
        driver.close();
    }

    public void reloadPage() {
        driver.navigate().refresh();
    }

    public void navigateTo(String urlPart) {

        driver.navigate().to(urlPart);
    }

    public Set<Cookie> getAllCookies() {
        return driver.manage().getCookies();
    }

    public void clearCookies() {
        driver.manage().deleteAllCookies();
    }

    public String getTitle() {
        return driver.getTitle();
    }


    public void openNewTab(String url) {
        driver.getWindowHandle();
        ((JavascriptExecutor) driver).executeScript(String.format("window.open(\"%s\");return true;", url));
    }

    public void switchToAnotherTab(int tabNumber) {
        ArrayList<String> tabs = new ArrayList<>(driver.getWindowHandles());
        driver.switchTo().window(tabs.get(tabNumber));
    }

    public void switchToSecondTab() {
        ArrayList<String> tabs = new ArrayList<>(driver.getWindowHandles());
        driver.switchTo().window(tabs.get(1));
    }

    public void switchToDefaultTab() {
        ArrayList<String> tabs = new ArrayList<>(driver.getWindowHandles());
        driver.switchTo().window(tabs.get(0));
    }

    public void switchToAnotherTab(String handle) {
        driver.switchTo().window(handle);
    }


    public void closeExtraTabs() {
        String currentHandle = driver.getWindowHandle();
        ArrayList<String> handles = new ArrayList<>(driver.getWindowHandles());
        handles.stream()
                .filter(s -> !s.equals(currentHandle))
                .forEach(s -> {
                    switchToAnotherTab(s);
                    closeTab();
                    switchToAnotherTab(currentHandle);
                });
    }

    public int getNumberOfTabs() {
        return driver.getWindowHandles().size();
    }
}
