package com.ts.common.asserts;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;
import lombok.extern.slf4j.Slf4j;


import static org.testng.AssertJUnit.assertTrue;

@Slf4j
public class UiAsserts {
    SelenideElement element;
    ElementsCollection elements;

    public UiAsserts(SelenideElement element) {
        this.element = element;
    }

    public UiAsserts(ElementsCollection elements) {
        this.elements = elements;
    }

    @Step("[ASSERT] Selenide element: {element.describe()}")
    public static UiAsserts assertThat(SelenideElement element) {
        if (element == null) assertTrue(false);
        log.info("Asserting element {}", element.describe());
        return new UiAsserts(element);
    }

    public static UiAsserts assertThat(ElementsCollection elements) {
        return new UiAsserts(elements);
    }

    public UiAsserts isTextCorrect(String expectedText) {
        this.element.shouldBe(Condition.text(expectedText));
        return this;
    }

    public UiAsserts isValueCorrect(String expectedText) {
        this.element.shouldBe(Condition.value(expectedText));
        return this;
    }

    public UiAsserts isTextContains(String expectedText) {
        this.element.shouldBe(Condition.innerText(expectedText));
        return this;
    }

    @Step("Element is according condition {0}")
    public UiAsserts isElementAccordCondition(Condition condition) {
        this.element.shouldHave(condition);
        log.info("Selenide element is visible {}", this.element.describe());
        return this;
    }

    public UiAsserts isElementNotAccordCondition(Condition condition) {
        this.element.shouldNotHave(condition);
        return this;
    }

    public UiAssertsCollection uiAssertsCollection(ElementsCollection collection) {
        return UiAssertsCollection.assertThatCollection(collection);
    }
}
