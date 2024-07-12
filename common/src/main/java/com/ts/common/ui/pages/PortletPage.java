package com.ts.common.ui.pages;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;
import lombok.Getter;


import static com.codeborne.selenide.Selenide.$$x;
import static com.codeborne.selenide.Selenide.$x;

public class PortletPage extends BasePage {
    @Getter
    private ElementsCollection deleteButton = $$x("//span[@aria-label='delete']");
    @Getter
    private SelenideElement saveButton = $x("//span[@aria-label='save']");

    public PortletPage chooseUser(String login) {
        elActions.click($x("//span[text()='Выберите сотрудника']/.."))
                .input($x("(//input[@type='search'])[3]"), login)
                .click($x("//div[text()='AT_SUPPORT_MANAGER_1 (AT_SUPPORT_MANAGER_1)']"));
        return this;
    }

    public PortletPage refresh() {
        elActions.click($x("//span[text()='Обновить']"));
        return this;
    }

    public PortletPage edit() {
        elActions.click($x("//button[@class='ant-btn css-m4timi ant-btn-text ant-btn-sm ant-btn-icon-only']"));
        return this;
    }

}
