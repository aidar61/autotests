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
public class TaskV2 extends BaseEntity {
    @Mandatory
    String id;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    String number;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    String userdata0;

    public TaskV2(String id, String number, String userdata0) {
        this.id = id;
        this.number = number;
        this.userdata0 = userdata0;

    }
}
