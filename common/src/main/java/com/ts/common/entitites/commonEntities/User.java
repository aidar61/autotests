package com.ts.common.entitites.commonEntities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.ts.common.entitites.BaseEntity;
import io.qameta.allure.internal.shadowed.jackson.annotation.JsonIgnore;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.extern.jackson.Jacksonized;
import lombok.extern.slf4j.Slf4j;
import org.sonatype.guice.bean.reflect.IgnoreSetters;

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
    Boolean active;

    public User(String id, String login, String name) {
        this.id = id;
        this.login = login;
        this.name = name;
    }

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
        FREELANCERS("818181b03d111b94013d11e324e82aac", "freelancers", "ВНЕШТАТНЫЕ СОТРУДНИКИ"),
        KASENOVA_MARIJAN("8a8181df89d741260189decd938f2603", "m.kassenova@tengebank.uz", "Касенова Маржан Нуртаевна"),
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
