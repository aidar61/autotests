package com.ts.common.config;

import org.aeonbits.owner.ConfigFactory;

public class AppConfigProvider {
    private static AppConfig config;
    private static AppDb db;
    private static AppUserConfig userConfig;
    public static final String STAND = get().stand();
    private static String BASE_URL = get().baseUrl();
    public static final String STAND_URL = formIntegrationUrl();
    private static String DB_BASE_URL = AppConfigProvider.getDbConfig().url();
    public static final String DB_URL = formDBUrl();
    public static final int IMPLICITLY_WAIT_SEC = get().implicitlyWait();
    public static final int IMPLICITLY_SLEEP_MS = get().implicitlySleep();

    public static void main(String[] args) {
        System.out.println(DB_URL);
        System.out.println(STAND_URL);
    }

    private AppConfigProvider() {
        throw new IllegalStateException("AppConfigProvider should never be instantiated");
    }

    public static String formIntegrationUrl() {
        if (STAND.equals("ts-integration")) {
            BASE_URL = "http://ts-integration.dev.colvir.ru/TrackStudio";
            return BASE_URL;
        } else {
            return BASE_URL.replace("null", STAND);
        }
    }

    public static String formDBUrl() {
        if (STAND.equals("ts-integration")) {
            DB_BASE_URL = "jdbc:oracle:thin:@//ts-integration.dev.colvir.ru:1521/colvirdb";
            return DB_BASE_URL;
        } else {
            return DB_BASE_URL.replace("null", STAND);
        }
    }

    public static AppConfig get() {
        if (config == null) {
            config = ConfigFactory.create(AppConfig.class, System.getProperties());
        }
        return config;
    }

    public static AppDb getDbConfig() {
        if (db == null) {
            db = ConfigFactory.create(AppDb.class, System.getProperties());
        }
        return db;
    }

    public static AppUserConfig getUserConfig() {
        if (userConfig == null) {
            userConfig = ConfigFactory.create(AppUserConfig.class, System.getProperties());
        }
        return userConfig;
    }
}
