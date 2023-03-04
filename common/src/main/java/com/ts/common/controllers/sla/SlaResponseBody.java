package com.ts.common.controllers.sla;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.ts.common.entitites.commonEntities.Category;
import com.ts.common.entitites.commonEntities.Status;
import com.ts.common.request.ResponseBody;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.extern.jackson.Jacksonized;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SlaResponseBody extends ResponseBody {
    String id;
    String description;
    String name;
    String number;
    Category category;
    Status status;
}
