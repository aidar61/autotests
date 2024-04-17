package com.ts.common.ui.pages;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;

import static com.codeborne.selenide.Selenide.$$x;
import static com.codeborne.selenide.Selenide.$x;

public class FilterPage extends BasePage {
    SelenideElement filterNameInput = $x("//input[contains(@ng-model, \"filter.name\")]");
    SelenideElement groupNameInput = $x("//input[contains(@ng-model, \"filter.group\")]");
    ElementsCollection deleteParamsBtns = $$x("//*[contains(@ng-click, \"deleteParamField(param)\")]");
    SelenideElement applyBtn = $x("//button[contains(@ng-click, \"applyFilter()\")]");
    SelenideElement saveAsNewBtn = $x("//button[contains(@ng-click, \"createFilter\")]");
    SelenideElement successMsg = $x("//*[contains(text(), \"Filter created\")]");


    public FilterPage saveNewFilter() {
        elActions.click(saveAsNewBtn);
        return this;
    }

    public FilterPage setFilterName(String text) {
        elActions.inputWithClear(filterNameInput, text);
        return this;
    }

    public FilterPage setGroupName(String text) {
        elActions.inputWithClear(groupNameInput, text);
        return this;
    }

    public FilterPage clearSearchParams() {
        for (SelenideElement element : deleteParamsBtns) {
            elActions.click(element);
        }
        return this;
    }

    public FilterPage checkSaveIsSuccess() {
        successMsg.shouldBe(Condition.visible);
        return this;
    }


}
