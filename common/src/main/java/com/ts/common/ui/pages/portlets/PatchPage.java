package com.ts.common.ui.pages.portlets;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;
import com.ts.common.ui.pages.BasePage;

import static com.codeborne.selenide.Selenide.$$x;
import static com.codeborne.selenide.Selenide.$x;

public class PatchPage extends BasePage {
    SelenideElement selectConfigBtn = $x("//clv-select[contains(@ng-model, \"configuration\")]//button");
    SelenideElement confgListItem = $x("//*[contains(text(), \"AT_INSTALLATION\")]");
    SelenideElement createPatchBtn = $x("//*[contains(@ng-click, \"createNewPatch\")]");
    SelenideElement udfPatchOwnerBtn = $x("//*[contains(text(), \"Владелец патча\")]" +
            "/ancestor::div[contains(@class, \"form-group\")]" +
            "//button[not(contains(@class, \"clv-select-toggle\"))]");
    SelenideElement saveBtn = $x("//*[contains(@ng-click, \"createNewPatch89\")]");

    public PatchPage setConfiguration(){
        //elActions.hover(selectConfigBtn);
        elActions.click(selectConfigBtn);
        elActions.click(confgListItem);
        return this;
    }

    public PatchPage openTaskForm(){
        elActions.click(createPatchBtn);
        return this;
    }

    public PatchPage saveTaskForm(){
        elActions.click(saveBtn);
        return this;
    }

    public PatchPage udfPatchOwnerIsPresent (){
        udfPatchOwnerBtn.shouldNot(Condition.exist);
        return this;
    }
}
