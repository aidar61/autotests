package com.ts.common.config;

import org.aeonbits.owner.ConfigFactory;

public class AppConfigProvider {
    private static AppConfig config;
    public static final String BASE_URL = get().baseUrl();
    public static final int IMPLICITLY_WAIT_SEC = get().implicitlyWait();
    public static final int IMPLICITLY_SLEEP_MS = get().implicitlySleep();

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
