package com.ts.common.asserts;

import com.codeborne.selenide.CollectionCondition;
import com.codeborne.selenide.Condition;
import com.codeborne.selenide.ElementsCollection;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;

@Slf4j
public class UiAssertsCollection {
    ElementsCollection elements;

    public UiAssertsCollection(ElementsCollection elements) {
        this.elements = elements;
    }

    public static UiAssertsCollection assertThatCollection(ElementsCollection elements) {
        return new UiAssertsCollection(elements);
    }

    public UiAssertsCollection isCorrectSize(Integer size) {
        Assertions.assertThat(this.elements.size())
                .withFailMessage("Size of collection is not correct %s", size)
                .isEqualTo(size);
        return this;
    }

    public UiAssertsCollection isContainText(String... expectedText) {
        this.elements.shouldHave(CollectionCondition.exactTextsCaseSensitiveInAnyOrder(expectedText));
        return this;
    }

    public UiAssertsCollection isElementAccordsCondition(CollectionCondition condition) {
        this.elements.shouldHave(condition);
        return this;
    }
}
