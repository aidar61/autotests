package com.ts.common.entitites.commonEntities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
@FieldDefaults(level = AccessLevel.PRIVATE)
public class User extends BaseEntity {
    String id;
    String login;
    String name;

    //    boolean active;
//    String status;
//    int childrenCount;
//    int wrongAuthAttempt;
//    boolean authBlocked;
//    boolean freelancer;
    @Getter
    public enum Constants {
        ABDULLAEV_BAHODIR("818181df7edb763f017ee28d995a3ba6", "babdullayev", "Абдуллаев Баходир");
        public final String id;
        public final String login;
        public final String name;

        Constants(String id, String login, String name) {
            this.id = id;
            this.login = login;
            this.name = name;
        }
    }
}
