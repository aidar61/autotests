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
        MTBANK("818181df581d378801581f74aad8098a", "589152"),
        NOTIFICATION_SERVICE("818181df62efd8e80162f1d00d3b5c41", ""),
        HEAD_BOOK("818181b03c7fc013013c7fca6d1803ea", "186609"),
        SERVICE_DESK("818181a822ee6d820122f4f8e43207ae","8860"),
        AKKREDITIVES("818181b03c7fc013013c7fca7130041d", "186616");
        public final String id;
        public final String number;

        Constants(String id, String number) {
            this.id = id;
            this.number = number;
        }
    }
}
