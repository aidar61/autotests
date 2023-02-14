package com.ts.common.entitites.sla;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.ts.common.entitites.BaseEntity;
import com.ts.common.entitites.commonEntities.*;
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
    User handlerUser;
    Udfs udfs;
    Status status;
    String[] attachments;
}
