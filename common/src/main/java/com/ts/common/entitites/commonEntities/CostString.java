package com.ts.common.entitites.commonEntities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ts.common.entitites.BaseEntity;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.extern.jackson.Jacksonized;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CostString extends BaseEntity {
    @JsonProperty(value = "cost")
    Integer cost;
    @JsonProperty(value = "budget1cat")
    Integer budgetFirst;
    @JsonProperty(value = "budget2cat")
    Integer budgetSecond;
    @JsonProperty(value = "budget3cat")
    Integer budgetThird;
}
