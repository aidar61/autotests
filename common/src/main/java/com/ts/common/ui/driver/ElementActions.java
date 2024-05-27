package com.ts.common.ui.driver;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideElement;

import static com.codeborne.selenide.Condition.visible;

public class ElementActions {

    public ElementActions click(SelenideElement element) { //TODO сюда добавить нужно hover()
        element.shouldBe(visible).click();
        return this;
    }

    public ElementActions input(SelenideElement element, String text) {
        element.shouldBe(visible).sendKeys(text);
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
