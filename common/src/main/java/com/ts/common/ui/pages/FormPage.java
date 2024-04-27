package com.ts.common.ui.pages;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;

import static com.codeborne.selenide.Selenide.$$x;
import static com.codeborne.selenide.Selenide.$x;

public class FormPage extends BasePage{
    public void userSelectorSetValue(String fieldName, String user){
        SelenideElement nonActiveInput = $x("//*[contains(text(), \"" + fieldName + "\")]" +
                "/ancestor::*[contains(@class, \"form-group\")]//*[contains(@class, \"clv-select-placeholder\")]");
        SelenideElement activeInput = $x("//input[@type=\"search\" and (not(contains(@class, \"ng-hide\")))]");
        SelenideElement searchResult = $x("//clv-select-choices-row/*[contains(text(), \"" + user + "\")]");

        elActions.click(nonActiveInput);
        elActions.input(activeInput, user);
        elActions.click(searchResult);
    }

    public void userSelectorSetValue(String fieldName){
        SelenideElement dictionaryBtn = $x("//*[contains(text(), \"" + fieldName + "\")]" +
                "/ancestor::*[contains(@class, \"form-group\")]//button[not(contains(@class, \"clv-select-toggle\"))]");
        ElementsCollection usersList  = $$x("//*[contains(@class, \"ag-center-cols-container\")]//*[@role=\"row\"]");
        SelenideElement applyBtn = $x("//*[contains(@class, \"modal-dialog\")]//button[contains(@ng-click, \"ok\")]");

        elActions.click(dictionaryBtn);
        elActions.click(usersList.first());
        elActions.click(applyBtn);
    }
}
