package com.ts.common.entitites.commonEntities.udfs;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.ts.common.entitites.commonEntities.TaskValue;
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
public class UdfSdRepeatRequest {
    String udfId;
    String type;
    List<TaskValue> taskValue;
}
