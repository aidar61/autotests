package com.ts.common.entitites.commonEntities.udfs;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.ts.common.entitites.commonEntities.ListValue;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.extern.jackson.Jacksonized;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

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
public class UdfSdTaskCode {
    String udfId;
    String type;
    ListValue[] listValue;
    public enum Constants{
        ABNATTR("818181df7d730063017d7302e40e0075");
        public final String taskCodesId;

        Constants(String taskCodesId) {
            this.taskCodesId = taskCodesId;
        }
    }
}
