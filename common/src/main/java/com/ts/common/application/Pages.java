package com.ts.common.application;

import com.ts.common.ui.pages.FilterPage;
import com.ts.common.ui.pages.HomePage;
import com.ts.common.ui.pages.LoginPage;
import com.ts.common.ui.pages.portlets.PatchPage;
import lombok.Getter;

public class Pages {
    @Getter
    private final LoginPage loginPage;
    @Getter
    private final HomePage homePage;
    @Getter
    private final FilterPage filterPage;
    @Getter
    private final PatchPage patchPage;

    public Pages() {
        this.loginPage = new LoginPage();
        this.homePage = new HomePage();
        this.filterPage = new FilterPage();
        this.patchPage = new PatchPage();
    }


}
