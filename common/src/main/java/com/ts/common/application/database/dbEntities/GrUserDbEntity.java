package com.ts.common.application.database.dbEntities;

import com.ts.common.entitites.commonEntities.User;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@ToString
public class GrUserDbEntity extends DbEntity {
    String user_id;
    String user_login;
    String user_name;

    public User mapTo() {
        return User.builder()
                .id(this.user_id)
                .login(this.user_login)
                .name(this.user_name)
                .build();
    }
}
