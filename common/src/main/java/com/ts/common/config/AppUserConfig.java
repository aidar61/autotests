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
    String projectManager(); //Менеджер проекта

    @Key("project.participant")
    String projectParticipant(); //Участник проекта

    @Key("manager.client")
    String managerClient(); // менеджер клиента

    @Key("manager.supplier")
    String managerSupplier();// Менеджер по взаимодействию с поставщиком

    @Key("analytic")
    String analytic(); //Аналитик

    @Key("manager.account")
    String managerAccount(); //Account-менеджер

    @Key("manager.feature")
    String managerFeature();//Менеджер по анализу доработок

    @Key("manager.request")
    String managerRequest();// Менеджер запроса

    @Key("manager.emp")
    String managerEmp(); // Ведение контрактов
}
