package com.ts.common.ui.pages;

import com.ts.common.ui.driver.BrowserManager;
import com.ts.common.ui.driver.WebElementActions;

public abstract class AbstractBasePage {
    private WebElementActions actions;
    protected BrowserManager browser;

    public WebElementActions actions() {
        if (actions == null) actions = new WebElementActions();
        return actions;
    }
}
