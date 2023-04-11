package com.ts.common.entitites.commonEntities.udfs;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ts.common.entitites.BaseEntity;
import com.ts.common.entitites.commonEntities.Task;
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
public class UdfSdModule extends BaseUdfs {
    @JsonProperty("udfid")
    String udfId;
    String type;
    Task[] taskValue;

    public enum Constants {
        ACCREDITIVES("818181b03c7fc013013c7fca7130041d"),
        NOTIFICATION_SERVICE("818181df62efd8e80162f1d00d3b5c41");
        public final String moduleIds;

        Constants(String moduleIds) {
            this.moduleIds = moduleIds;
        }
    }
}
