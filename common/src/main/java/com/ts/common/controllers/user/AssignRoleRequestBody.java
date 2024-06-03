package com.ts.common.controllers.user;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.ts.common.entitites.commonEntities.Role;
import com.ts.common.entitites.commonEntities.User;
import com.ts.common.entitites.tasks.GeneralTask;
import com.ts.common.entitites.user.AssignRole;
import com.ts.common.request.RequestBody;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.extern.jackson.Jacksonized;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@FieldDefaults(level = AccessLevel.PRIVATE)
@ToString
public class AssignRoleRequestBody extends RequestBody {
    AssignRole toTask;
    AssignRole forUser;
    AssignRole assignedRole;
    Boolean override;

    public AssignRoleRequestBody(GeneralTask task, User user, Role.RoleConstants role) {
        this.toTask = AssignRole.builder().toTaskNumber(task.getNumber()).build();
        this.forUser = AssignRole.builder().forUserLogin(user.getLogin()).build();
        this.assignedRole = AssignRole.builder().roleId(role.getId()).build();
        this.override = false;
    }
}
