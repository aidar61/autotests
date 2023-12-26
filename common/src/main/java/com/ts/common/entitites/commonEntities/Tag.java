package com.ts.common.entitites.commonEntities;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.ToString;


@ToString
@Builder
public class Tag {

    @JsonProperty
    String tag;

    @JsonProperty
    String privateTag;
}
