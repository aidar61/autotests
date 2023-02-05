package com.ts.common.entitites.sla;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.ts.common.entitites.BaseEntity;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.extern.jackson.Jacksonized;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = false)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateSlaResponseBody extends BaseEntity {
    String number;
    String id;
}
