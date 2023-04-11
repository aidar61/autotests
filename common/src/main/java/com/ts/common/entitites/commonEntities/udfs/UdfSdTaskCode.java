package com.ts.common.entitites.commonEntities.udfs;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
public class UdfSdTaskCode extends BaseUdfs{
    String udfId;
    String type;
    List[] listValue;
    public enum Constants{
        ABNATTR("818181df7d730063017d7302e40e0075");
        public final String taskCodesId;

        Constants(String taskCodesId) {
            this.taskCodesId = taskCodesId;
        }
    }
}
