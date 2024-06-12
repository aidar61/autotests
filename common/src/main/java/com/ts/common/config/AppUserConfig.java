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
    String role_organization();

    @Key("role.client")
    String role_client();

    @Key("role.client.2")
    String role_client_2();

    @Key("role.dep")
    String role_dep();

    @Key("at.task.manager")
    String at_task_manager(); //Менеджер проекта

    @Key("at.task.manager.2")
    String at_task_manager_2(); //Менеджер проекта

    @Key("at.task.participant")
    String at_task_participant(); //Участник проекта

    @Key("at.task.participant.2")
    String at_task_participant_2(); //Участник проекта

    @Key("at.support.manager")
    String at_support_manager(); // менеджер клиента

    @Key("at.suppliermanager")
    String at_suppliermanager();// Менеджер по взаимодействию с поставщиком

    @Key("at.task.analitic")
    String at_task_analitic(); //Аналитик

    @Key("at.support.costmanager")
    String at_support_costmanager(); //Account-менеджер

    @Key("at.sdfeature.analysis.manager")
    String at_sdfeature_analysis_manager();//Менеджер по анализу доработок

    @Key("at.sdfeature.impl.manager")
    String at_sdfeature_impl_manager();// Менеджер запроса

    @Key("at.contract.emp")
    String at_contract_emp(); // Ведение контрактов

    @Key("at.group.technologyservice")
    String at_group_technologyservice(); // Технолог
}
