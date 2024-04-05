package com.ts.common.ui.pages;

import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import com.ts.common.application.controllers.AuthToken;
import com.ts.common.enums.Users;
import lombok.extern.java.Log;

import static com.codeborne.selenide.Selenide.$x;

public class LoginPage extends BasePage {

    public SelenideElement loginInput = $x("//input[@id='login']");
    public SelenideElement passwordInput = $x("//input[@id='password']");
    public SelenideElement loginButton = $x("//span[text()='Войти']");

    public LoginPage login(Users authToken) {
        elActions.input(loginInput, authToken.username)
                .input(passwordInput, authToken.password)
                .click(loginButton);
        return this;
    }
}
