package com.ts.common.entitites.commonEntities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
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

    public enum Constants {
        CLIENT("Клиент"),
        EMPLOYEE("Сотрудник"),
        ACCOUNT_MANAGER("Account-менеджер"),
        CLIENT_MANAGER("Менеджер клиента");
        @Getter
        private final String role;

        Constants(String role) {
            this.role = role;
        }
    }
}
