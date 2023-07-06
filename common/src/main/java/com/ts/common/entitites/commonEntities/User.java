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
        ARUTYANIN_YURIY("818181df6936d3710169388776323be0", "yarutyunyan", "Арутюнян Юрий"),
        ARTEMEVA_MARINA("8a8181df6ecdd573016ed62eb51107bc", "martemyeva", "Артемьева Марина"),
        BABUSHKIN_IVAN("818181a81e88c49f011e96ab25d401a9", "ibabushkin", "Бабушкин Иван"),
        ALTUNIN_NIKOLAY("818180a0550e6cfc015512ed27c13dca", "naltunin", "Алтунин Николай"),
        AKSENOV_ANDREY("818181df7ec16c9b017ed65c3d2f4401", "aaxyonov@mtbank.by", "Аксёнов Андрей"),
        ABDULLAEV_BAHODIR("818181df7edb763f017ee28d995a3ba6", "babdullayev", "Абдуллаев Баходир"),
        QA("818181df610695cb016108349b590ba8", "qa", "qa");
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
