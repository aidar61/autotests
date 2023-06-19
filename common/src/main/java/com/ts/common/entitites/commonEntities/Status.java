package com.ts.common.entitites.commonEntities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
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
public class Status extends BaseEntity {
    String id;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    String color;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    String name;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    String workflowId;
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    boolean defaultStart;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    String image;
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    int order;
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    boolean start;
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    boolean finish;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("trname")
    String trName;
}
