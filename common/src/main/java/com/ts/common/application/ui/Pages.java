package com.ts.common.application.ui;

import com.ts.common.ui.pages.FilterPage;
import com.ts.common.ui.pages.HomePage;
import com.ts.common.ui.pages.LoginPage;
import lombok.Getter;

@Getter
public class Pages {
    private final LoginPage loginPage;
    private final HomePage homePage;
    private final FilterPage filterPage;

    public Pages() {
        this.loginPage = new LoginPage();
        this.homePage = new HomePage();
        this.filterPage = new FilterPage();
    }
}
