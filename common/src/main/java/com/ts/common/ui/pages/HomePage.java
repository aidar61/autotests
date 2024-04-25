package com.ts.common.ui.pages;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import com.ts.common.ui.driver.ElementActions;
import com.ts.common.utils.WaitManager;

import static com.codeborne.selenide.Selenide.$$x;
import static com.codeborne.selenide.Selenide.$x;

public class HomePage extends BasePage {
    SelenideElement treeBtn = $x("//span[@class='glyphicon glyphicon-menu-hamburger']");
    ElementsCollection plusTreeElements = $$x("//ul[@class='a1 ng-scope']/li/i[1]");
    ElementsCollection treeElements = $$x("//ul[@class='a1 ng-scope']/li/div");
    SelenideElement selenideElement = $x("//span[text()='Загрузка...']");
    SelenideElement filterSettingBtn = $x("//label[contains(@ng-click, \"currentFilter\")]");

    public HomePage openPlusAllElements() {
        for (SelenideElement element : plusTreeElements) {
            elActions.click(element);
        }
        return this;
    }

    public HomePage openAllTreeElements() {
        for (SelenideElement treeElement : treeElements) {
            WaitManager.pause(1);
            elActions.click(treeElement);
        }
        return this;
    }

    public HomePage openTreeBtn() {
        elActions.click(treeBtn);
        return this;
    }

    public HomePage openFilterSetting() {
        elActions.click(filterSettingBtn);
        return this;
    }

}
