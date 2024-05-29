package com.ts.common.controllers.user;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.ts.common.entitites.commonEntities.*;
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
public class CreateUserRequestBody extends RequestBody {
    String login;
    String nameIntnl;
    String name;
    Boolean active;
    Role role;
    String company;
    Locale locale;
    Locale timezone;
    Task defaultTask;
    String parentId;
    User parent;
    Udfs[] udfs;

    public CreateUserRequestBody(User user) {
        this.login = user.getLogin();
        this.nameIntnl = user.getName();
        this.name = user.getName();
        this.active = user.getActive();
        this.role = user.getRole();
        this.company = user.getCompany();
        this.locale = user.getLocale();
        this.timezone = user.getTimezone();
        this.defaultTask = user.getDefaultTask();
        this.parentId = user.getParent().getId();
        this.parent = user.getParent();
        this.udfs = user.getUdfs();
    }
}
