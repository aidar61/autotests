package com.ts.common.entitites.commonEntities.udf;

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
@JsonInclude(JsonInclude.Include.NON_NULL)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UdfString extends BaseEntity {
    @JsonProperty("udfid")
    String udfId;
    String type;
    String stringValue;
    @JsonProperty("udfdata0")
    String udfData;

    public enum Constants {
        COST("{\"cost\":100,\"budget1cat\":4,\"budget2cat\":4,\"budget3cat\":4}");
        @Getter
        public final String value;

        Constants(String value) {
            this.value = value;
        }
    }
}
