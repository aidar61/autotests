package com.ts.common.tests;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.ts.common.entitites.BaseEntity;
import com.ts.common.entitites.commonEntities.udf.UdfDouble;
import com.ts.common.entitites.commonEntities.udf.UdfUser;
import com.ts.common.serializers.UdfDoubleArraySerializer;
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
public class Udf extends BaseEntity {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonSerialize(using = UdfDoubleArraySerializer.class)
    @JsonUnwrapped
    UdfDouble[] udfDoubles;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    UdfUser udfUsers;

    public void generateUdfDoubles(UdfDouble... udfDoubles) {
        setUdfDoubles(udfDoubles);
    }
}
