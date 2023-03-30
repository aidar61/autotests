package com.ts.common.enums;

import com.ts.common.config.AppConfigProvider;

public enum Users {
    EMPLOYEE(AppConfigProvider.getUserConfig().username(), AppConfigProvider.getUserConfig().password()),
    FIRST_CLIENT(AppConfigProvider.getUserConfig().firstClientUserName(), AppConfigProvider.getUserConfig().firstClientPassword()),
    CLIENT(AppConfigProvider.getUserConfig().clientUsername(), AppConfigProvider.getUserConfig().clientPassword());
    public final String username;
    public final String password;

    Users(String username, String password) {
        this.username = username;
        this.password = password;
    }
}
