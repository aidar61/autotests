package com.ts.common.config;

import org.aeonbits.owner.Config;

@Config.Sources("classpath:db.properties")
public interface AppDb extends Config {
    @Key("url")
    String url();

    @Key("username")
    String username();

    @Key("password")
    String password();

    @Key("driver")
    String driver();
}
