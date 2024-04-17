package com.ts.common.config;

import org.aeonbits.owner.Config;
@Config.Sources("classpath:uiTest.properties")
public interface UiConfig extends Config {
    @Config.Key("ts.user.base")
    String baseUser();
}
