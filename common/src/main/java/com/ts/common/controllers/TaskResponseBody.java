package com.ts.common.controllers;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ts.common.entitites.commonEntities.Category;
import com.ts.common.entitites.commonEntities.Status;
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
public class TaskResponseBody extends com.ts.common.request.ResponseBody {
    String id;
    String description;
    String name;
    String number;
    Category category;
    @JsonProperty("status")
    Status finishStatus;
}
