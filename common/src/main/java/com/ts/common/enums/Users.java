package com.ts.common.enums;

import com.ts.common.config.AppConfigProvider;

public enum Users {
    EMPLOYEE(AppConfigProvider.getUserConfig().username(), AppConfigProvider.getUserConfig().password()),
    CLIENT(AppConfigProvider.getUserConfig().clientUsername(), AppConfigProvider.getUserConfig().password()),
    SECOND_EMPLOYEE(AppConfigProvider.getUserConfig().secondUsername(), AppConfigProvider.getUserConfig().password()),
    ROOT("root", AppConfigProvider.getUserConfig().password());
    public final String username;
    public final String password;

    Users(String username, String password) {
        this.username = username;
        this.password = password;
    }
}
