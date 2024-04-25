package com.ts.common.ui.pages.portlets;

import com.codeborne.selenide.SelenideElement;
import com.ts.common.ui.pages.BasePage;

import static com.codeborne.selenide.Selenide.$x;

public class UserSelector extends BasePage {
    public UserSelector setFromSearch(String fieldName, String user){
        SelenideElement nonActiveInput = $x("//*[contains(text(), \"" + fieldName + "\")]" +
                "/ancestor::*[contains(@class, \"form-group\")]//*[contains(@class, \"clv-select-placeholder\")]");

        SelenideElement activeInput = $x("//input[@type=\"search\" and (not(contains(@class, \"ng-hide\")))]");

        SelenideElement searchResult = $x("//clv-select-choices-row/*[contains(text(), \"" + user + "\")]");

        nonActiveInput.click();
        activeInput.setValue(user);
        searchResult.click();
        return this;
    }

}
