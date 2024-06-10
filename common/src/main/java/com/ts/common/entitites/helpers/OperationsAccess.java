package com.ts.common.entitites.helpers;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.ts.common.entitites.BaseEntity;
import com.ts.common.entitites.commonEntities.GeneralSlaId;
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
public class OperationsAccess extends BaseEntity {
    GeneralSlaId operation;
    Boolean canEdit;
    Boolean canView;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    Boolean access;

    public OperationsAccess(Boolean access) {
        this.access = access;
    }
}
