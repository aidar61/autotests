package com.ts.common.entitites.commonEntities.udf;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ts.common.entitites.BaseEntity;
import com.ts.common.entitites.commonEntities.List;
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
public class UdfList extends BaseEntity {
    @JsonProperty("udfid")
    String udfId;
    String type;
    List[] listValue;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("userdata0")
    String userData;
}
