package com.ts.common.config;

import org.aeonbits.owner.Config;

@Config.Sources("classpath:user.properties")
public interface AppUserConfig extends Config {
    @Config.Key("username")
    String username();

    @Config.Key("password")
    String password();

    @Key("clientUsername")
    String clientUsername();

}
