package com.ts.common.entitites.helpers;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ts.common.entitites.BaseEntity;
import com.ts.common.entitites.commonEntities.Category;
import com.ts.common.entitites.commonEntities.GeneralSlaId;
import com.ts.common.entitites.commonEntities.Status;
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
public class Transition extends BaseEntity {
    String id;
    String startId;
    String finishId;
    String mstatusId;
    GeneralSlaId mstatus;
    Boolean def;
    @JsonProperty(value = "ctrstatusSet")
    Category[] tasks;
    Status start;
    Status finish;
}
