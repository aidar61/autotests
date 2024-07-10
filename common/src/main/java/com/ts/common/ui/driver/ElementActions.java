package com.ts.common.ui.driver;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideElement;
import org.openqa.selenium.JavascriptExecutor;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.webdriver;

public class ElementActions {

    public ElementActions click(SelenideElement element) {
        element.shouldBe(visible).click();
        return this;
    }

    public ElementActions setInputTextUsingJS(SelenideElement element, String text) {
        // Получаем экземпляр JavaScriptExecutor
        JavascriptExecutor js = (JavascriptExecutor) webdriver().object();
        // Выполняем JavaScript для установки значения input
        js.executeScript("arguments[0].value='" + text + "';", element);
        return this;
    }

    public ElementActions input(SelenideElement element, String text) {
        element.shouldBe(visible).sendKeys(text);
        return this;
    }

    public ElementActions enter(SelenideElement element) {
        element.shouldBe(visible).pressEnter();
        return this;
    }

    public ElementActions inputWithClear(SelenideElement element, String text) {
        element.shouldBe(visible).click();
        element.shouldBe(visible).clear();
        element.shouldBe(visible).sendKeys(text);
        return this;
    }

    public ElementActions hover(SelenideElement element) {
        element.shouldBe(visible).hover();
        return this;
    }
}
