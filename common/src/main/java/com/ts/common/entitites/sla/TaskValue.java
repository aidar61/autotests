package com.ts.common.entitites.sla;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
@AllArgsConstructor
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TaskValue extends BaseEntity {
    String id;
    int abudget;
    int actualBudget;
    String name;
    String number;
    String shortname;
    String categoryLink;
    Category category;
    Status status;
    String statusName;
    int childrenCount;
    boolean hasChildren;
    int messageCount;
    boolean hasAttachments;
    boolean onSight;
    Udfs udfs;
    String stateLink;
    String statusColor;
    String taskLink;
    boolean sortMessageAsc;
    boolean selectable;
    boolean canEditHandler;
    String $$key;
    boolean $visible;
    boolean $firstInGroup;
    boolean $selected;
    boolean invalid;
}
