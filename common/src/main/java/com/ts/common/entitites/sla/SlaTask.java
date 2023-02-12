package com.ts.common.entitites.sla;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ts.common.entitites.BaseEntity;
import com.ts.common.entitites.commonEntities.GeneralSlaId;
import com.ts.common.entitites.commonEntities.Parent;
import com.ts.common.entitites.commonEntities.Status;
import com.ts.common.entitites.commonEntities.Udfs;
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
@FieldDefaults(level = AccessLevel.PRIVATE)
@ToString
public class SlaTask extends BaseEntity {
    String id;
    String number;
    GeneralSlaId category;
    GeneralSlaId operation;
    Parent parent;
    String name;
    String description;
    Udfs udfs;
    Status status;
    String[] attachments;
}
