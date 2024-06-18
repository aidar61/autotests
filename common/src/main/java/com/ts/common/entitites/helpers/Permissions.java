package com.ts.common.entitites.helpers;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.ts.common.entitites.BaseEntity;
import com.ts.common.entitites.commonEntities.Role;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Permissions extends BaseEntity {
    Role role;
    String[] permissions;
    String viewOperation; //Разрешен просмотр
    String processOperation; //Может выполнить
    String handleOperation; //Разрешено быть ответственным

    @Override
    public Permissions receivePermissions() {
        return this;
    }
}
