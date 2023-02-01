package com.ts.common.config;

import org.aeonbits.owner.Config;

@Config.Sources("classpath:app.properties")
public interface AppConfig extends Config {
    @Config.Key("ts.rest.4.base.url")
    String baseUrl();
}
