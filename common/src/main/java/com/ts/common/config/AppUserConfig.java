package com.ts.common.config;

import org.aeonbits.owner.Config;

@Config.Sources("classpath:user.properties")
public interface AppUserConfig extends Config {
    @Config.Key("username")
    String username();

    @Key("second_username")
    String secondUsername();

    @Config.Key("password")
    String password();

    @Key("passwordProd")
    String passwordProd();

    @Key("clientUsername")
    String clientUsername();

    @Key("role.organization")
    String roleOrganization();

    @Key("role.client")
    String roleClient();

    @Key("role.dep")
    String roleDep();

    @Key("project.manager")
    String projectManager();

    @Key("project.participant")
    String projectParticipant();

    @Key("manager.client")
    String managerClient();

    @Key("manager.supplier")
    String managerSupplier();

    @Key("analytic")
    String analytic();

    @Key("manager.account")
    String managerAccount();

    @Key("manager.feature")
    String managerFeature();

    @Key("manager.request")
    String managerRequest();

    @Key("manager.emp")
    String managerEmp();
}
