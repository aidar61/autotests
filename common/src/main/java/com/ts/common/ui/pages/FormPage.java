package com.ts.common.ui.pages;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;
import com.ts.common.asserts.UiAsserts;
import com.ts.common.utils.WaitManager;
import org.openqa.selenium.Keys;

import static com.codeborne.selenide.Selenide.$$x;
import static com.codeborne.selenide.Selenide.$x;

public class FormPage extends BasePage {

    //метод устанавливает значение в поле типа "пользователь" с помощью поиска
    public void userSelectorSetValue(String fieldName, String user) {
        SelenideElement nonActiveInput = $x("//*[contains(text(), \"" + fieldName + "\")]" +
                "/ancestor::*[contains(@class, \"form-group\")]//*[contains(@class, \"clv-select-placeholder\")]");
        //SelenideElement activeInput = $x("//input[@type=\"search\" and (not(contains(@class, \"ng-hide\")))]");
        SelenideElement activeInput = $x("//input[contains(@ng-class, 'searchEnabled') " +
                "and (not(contains(@class, 'ng-hide')))]");
        SelenideElement searchResult = $x("//clv-select-choices-row/*[contains(text(), \"" + user + "\")]");

        elActions.click(nonActiveInput);
        elActions.input(activeInput, user);
        elActions.click(searchResult);
    }

//    public void userSelectorSetValue(String fieldName) {
//        SelenideElement dictionaryBtn = $x("//*[contains(text(), \"" + fieldName + "\")]" +
//                "/ancestor::*[contains(@class, \"form-group\")]//button[not(contains(@class, \"clv-select-toggle\"))]");
//        ElementsCollection usersList = $$x("//*[contains(@class, \"ag-center-cols-container\")]//*[@role=\"row\"]");
//        SelenideElement applyBtn = $x("//*[contains(@class, \"modal-dialog\")]//button[contains(@ng-click, \"ok\")]");
//
//        elActions.click(dictionaryBtn);
//        elActions.click(usersList.first());
//        elActions.click(applyBtn);
//    }

    //метод устанавливает значение в поле типа "пользователь" из справочника
    public void userSelectorSetValue(String fieldName, int userCount) {
        SelenideElement dictionaryBtn = $x("//*[contains(text(), \"" + fieldName + "\")]" +
                "/ancestor::*[contains(@class, \"form-group\")]//button[not(contains(@class, \"clv-select-toggle\"))]");
        ElementsCollection usersList = $$x("//*[contains(@class, \"ag-center-cols-container\")]//*[@role=\"row\"]");
        SelenideElement applyBtn = $x("//*[contains(@class, \"modal-dialog\")]//button[contains(@ng-click, \"ok\")]");

        elActions.click(dictionaryBtn);
        if (userCount > 1) {
            for (int i = 0; i < userCount; i++) {
                elActions.click(usersList.get(i + 1));
            }
        } else {
            elActions.click(usersList.first());
        }
        elActions.click(applyBtn);
    }

    public void userSelectorIsMulti(String fieldName, boolean flag) {
        SelenideElement userCount = $x("//*[contains(text(), '" + fieldName + "')]" +
                "/ancestor::*[contains(@class, 'form-group')]//span[contains(@ng-if, 'selectedUsers.length')]");

        if (flag) {
            UiAsserts.assertThat(userCount).isElementAccordCondition(Condition.visible);
        } else {
            UiAsserts.assertThat(userCount).isElementNotAccordCondition(Condition.visible);
        }
    }

    public void setDateValue(String fieldName, String fieldValue) {
        SelenideElement field = $x("//*[contains(text(), '" + fieldName + "')]" +
                "/ancestor::*[contains(@class, 'form-group')]//input");

        elActions.click(field);
        elActions.input(field, fieldValue);
        field.sendKeys(Keys.ENTER);
    }

    public void setRadioValue(String fieldName, int valueNumber) {
        ElementsCollection field = $$x("//*[contains(text(), '" + fieldName + "')]" +
                "/ancestor::*[contains(@class, 'form-group')]//input");

        if (valueNumber == 1) {
            elActions.click(field.first());
        } else {
            elActions.click(field.get(valueNumber - 1));
        }
    }

    public void setListValue(String fieldName, int valueNumber) {
        SelenideElement field = $x("//*[contains(text(), '" + fieldName + "')]" +
                "/ancestor::*[contains(@class, 'form-group')]//button");
        ElementsCollection list = $$x("//*[contains(text(), '" + fieldName + "')]" +
                "/ancestor::*[contains(@class, 'form-group')]//li[not(contains(@class, 'ng-hide'))]");

        elActions.click(field);
        if (valueNumber == 1) {
            elActions.click(list.first());
        } else {
            elActions.click(list.get(valueNumber - 1));
        }
    }


}
