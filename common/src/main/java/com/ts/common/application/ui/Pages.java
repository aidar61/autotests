package com.ts.common.application.ui;

import com.ts.common.ui.pages.FilterPage;
import com.ts.common.ui.pages.FormPage;
import com.ts.common.ui.pages.HomePage;
import com.ts.common.ui.pages.LoginPage;
import com.ts.common.ui.pages.portlets.PatchPage;
import lombok.Getter;

@Getter
public class Pages {
    private final LoginPage loginPage;
    private final HomePage homePage;
    private final FilterPage filterPage;
    @Getter
    private final PatchPage patchPage;
    @Getter
    private final FormPage formPage;

    public Pages() {
        this.loginPage = new LoginPage();
        this.homePage = new HomePage();
        this.filterPage = new FilterPage();
        this.patchPage = new PatchPage();
        this.formPage = new FormPage();

    }


}
