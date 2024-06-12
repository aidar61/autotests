package com.ts.common.entitites.commonEntities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ts.common.entitites.BaseEntity;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.extern.jackson.Jacksonized;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Role extends BaseEntity {
    String id;
    String name;
    @JsonProperty(value = "owner")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    User owner;

    public enum Constants {
        ANALYTIC("Аналитик"),
        CLIENT("Клиент"),
        EMPLOYEE("Сотрудник"),
        ACCOUNT_MANAGER("Account-менеджер"),
        CLIENT_MANAGER("Менеджер клиента"),
        MANAGER_REQUEST("Менеджер запроса"),
        ROLE_SUPPORT_MEMBER("Участник проекта сопровождения");
        @Getter
        private final String role;

        Constants(String role) {
            this.role = role;
        }
    }

    @Getter
    public enum RoleConstants {
        ROLE_SUPPORT_MANAGER("ROLE_SUPPORT_MANAGER", "Менеджер клиента"),
        ROLE_SUPPORT_MEMBER("ROLE_SUPPORT_MEMBER", "Участник проекта сопровождения"),
        ROLE_SDFEATURE_ANALYSIS_MANAGER("ROLE_SDFEATURE_ANALYSIS_MANAGER", "Менеджер по анализу доработок"),
        ROLE_SDFEATURE_IMPL_MANAGER("ROLE_SDFEATURE_IMPL_MANAGER", "Менеджер запроса"),
        ROLE_CONTRACT_EMP("ROLE_CONTRACT_EMP", "Ведение контрактов"),
        ROLE_TASK_ANALITIC("ROLE_TASK_ANALITIC", "Аналитик"),
        ROLE_ORGANIZATION("ROLE_ORGANIZATION", "Организация"),
        ROLE_DEP("ROLE_DEP", "Подразделение"),
        ROLE_WORKER("ROLE_WORKER", "Сотрудник"),
        ROLE_SUPPORT_COSTMANAGER("ROLE_SUPPORT_COSTMANAGER", "Account-Manager"),
        ROLE_TASK_MANAGER("ROLE_TASK_MANAGER", "Менеджер проекта"),
        ROLE_TASK_PARTICIPANT("ROLE_TASK_PARTICIPANT", "Участник проекта"),
        ROLE_GROUP_TECHNOLOGYSERVICE("ROLE_GROUP_TECHNOLOGYSERVICE", "Технолог"),
        ROLE_CLIENT("ROLE_CLIENT", "Клиент");

        private final String id;
        private final String name;

        RoleConstants(String id, String name) {
            this.id = id;
            this.name = name;
        }

    }
}
