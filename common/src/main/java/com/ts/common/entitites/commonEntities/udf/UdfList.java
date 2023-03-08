package com.ts.common.entitites.commonEntities.udf;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.ts.common.entitites.commonEntities.List;
import com.ts.common.entitites.commonEntities.udfs.BaseUdfs;
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
public class UdfList extends BaseUdfs {
    String udfId;
    String type;
    List[] listValue;
}
