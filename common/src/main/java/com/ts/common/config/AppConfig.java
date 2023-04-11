package com.ts.common.config;

import org.aeonbits.owner.Config;
import org.checkerframework.checker.units.qual.K;
import org.checkerframework.checker.units.qual.Length;

@Config.Sources("classpath:app.properties")
public interface AppConfig extends Config {
    @Config.Key("ts.rest.4.base.url")
    String baseUrl();

    @Key("default.implicitly.wait")
    int implicitlyWait();

    @Key("default.implicitly.sleep")
    int implicitlySleep();

    @Key("headless")
    String headless();
    @Key("retry.number")
    int retriesNumber();
}
