package com.ts.common.enums;

import com.ts.common.config.AppConfigProvider;

public enum Users {
    EMPLOYEE(AppConfigProvider.getUserConfig().username(), AppConfigProvider.getUserConfig().password()),
    CLIENT(AppConfigProvider.getUserConfig().clientUsername(), AppConfigProvider.getUserConfig().clientPassword());
    public final String username;
    public final String password;

    Users(String username, String password) {
        this.username = username;
        this.password = password;
    }
}
