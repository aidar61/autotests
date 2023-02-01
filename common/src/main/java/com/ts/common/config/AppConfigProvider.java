package com.ts.common.config;

import org.aeonbits.owner.ConfigFactory;

public class AppConfigProvider {
    private static AppConfig config;
    public static final String BASE_URL = get().baseUrl();

    private AppConfigProvider() {
        throw new IllegalStateException("AppConfigProvider should never be instantiated");
    }

    public static AppConfig get() {
        if (config == null) {
            config = ConfigFactory.create(AppConfig.class, System.getProperties());
        }
        return config;
    }
}
