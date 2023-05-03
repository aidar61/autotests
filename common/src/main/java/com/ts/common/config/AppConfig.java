package com.ts.common.config;

import org.aeonbits.owner.Config;

@Config.Sources("classpath:app.properties")
public interface AppConfig extends Config {
    @Config.Key("ts.rest.base.url")
    String baseUrl();

    @Key("ts.stand")
    String stand();

    @Key("default.implicitly.wait")
    int implicitlyWait();

    @Key("default.implicitly.sleep")
    int implicitlySleep();

    @Key("headless")
    String headless();

    @Key("retry.number")
    int retriesNumber();
}
