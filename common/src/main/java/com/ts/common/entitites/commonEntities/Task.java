package com.ts.common.entitites.commonEntities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.ts.common.annotations.Mandatory;
import com.ts.common.entitites.BaseEntity;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.extern.jackson.Jacksonized;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Builder
@Getter
@Setter
@ToString
@NoArgsConstructor
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Task extends BaseEntity {
    @Mandatory
    String id;
    //    @JsonInclude(JsonInclude.Include.NON_NULL)
//    int abudget;
//    @JsonInclude(JsonInclude.Include.NON_NULL)
//    int actualBudget;
//    @JsonInclude(JsonInclude.Include.NON_NULL)
//    String name;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    String number;
//    @JsonInclude(JsonInclude.Include.NON_NULL)
//    String shortname;
//    @JsonInclude(JsonInclude.Include.NON_NULL)
//    String categoryLink;
//    @JsonInclude(JsonInclude.Include.NON_NULL)
//    Category category;
//    @JsonInclude(JsonInclude.Include.NON_NULL)
//    Status status;
//    @JsonInclude(JsonInclude.Include.NON_NULL)
//    String statusName;
//    @JsonInclude(JsonInclude.Include.NON_NULL)
//    int childrenCount;
//    @JsonInclude(JsonInclude.Include.NON_NULL)
//    boolean hasChildren;
//    @JsonInclude(JsonInclude.Include.NON_NULL)
//    int messageCount;
//    @JsonInclude(JsonInclude.Include.NON_NULL)
//    boolean hasAttachments;
//    @JsonInclude(JsonInclude.Include.NON_NULL)
//    boolean onSight;
//    @JsonInclude(JsonInclude.Include.NON_NULL)
//    String stateLink;
//    @JsonInclude(JsonInclude.Include.NON_NULL)
//    String statusColor;
//    @JsonInclude(JsonInclude.Include.NON_NULL)
//    String taskLink;
//    @JsonInclude(JsonInclude.Include.NON_NULL)
//    boolean sortMessageAsc;
//    @JsonInclude(JsonInclude.Include.NON_NULL)
//    boolean selectable;
//    @JsonInclude(JsonInclude.Include.NON_NULL)
//    boolean canEditHandler;
//    @JsonInclude(JsonInclude.Include.NON_NULL)
//    String $$key;
//    @JsonInclude(JsonInclude.Include.NON_NULL)
//    boolean $visible;
//    @JsonInclude(JsonInclude.Include.NON_NULL)
//    boolean $firstInGroup;
//    @JsonInclude(JsonInclude.Include.NON_NULL)
//    boolean $selected;
//    @JsonInclude(JsonInclude.Include.NON_NULL)
//    boolean invalid;

    public Task(String id, String number) {
        this.id = id;
        this.number = number;
    }

    public enum Constants {
        CORE("818181b03c7fc013013c7fca70500413", "186615"),
        APP_SERVER("818181b03c88913a013c889ce00b0557", "187245"),
        MTBANK("818181df581d378801581f74aad8098a", "589152"),
        NOTIFICATION_SERVICE("818181df62efd8e80162f1d00d3b5c41", ""),
        APNG("8a8181df717ac73501719cbc94596f2f", "1032095"),
        HEAD_BOOK("818181b03c7fc013013c7fca6d1803ea", "186609"),
        SERVICE_DESK("818181a822ee6d820122f4f8e43207ae", "8860"),
        TASK_TS_DEV("818181a81e9d8b28011e9d9811150002", "2405"),
        ONE_HUNGRED("818180a04638d7db01463db877d746a9", "287919"), // Рассчетный фронт офис
        FRONT_OFFICE("818180a04638d7db01463db877d746a9", "287919"),
        RYSGAL_BANK("818180a050c582480150c952631449fe", "463982"),

        WORKTASK_TESTTASK("818181df7ddf2212017de2ae35ec1c12", "1279796"),
        KZ_KZI("818181df64257a61016427680a2f5d1b", "788781"),
        CUSTOMER_REQUEST("8a8181df879af7cb01879d663123091e", "1441326"), //Двойное начисление штрафа по гарантиям в первый день просрочки
        AKKREDITIVES("818181b03c7fc013013c7fca7130041d", "186616");
        public final String id;
        public final String number;

        Constants(String id, String number) {
            this.id = id;
            this.number = number;
        }
    }
}
