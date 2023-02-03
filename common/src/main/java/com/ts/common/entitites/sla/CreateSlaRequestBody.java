package com.ts.common.entitites.sla;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
public class CreateSlaRequestBody {
    Category category;
    String name;
    String description;
    Udfs udfs;

    public CreateSlaRequestBody(SlaTask slaTask) {
        this.category = slaTask.getCategory();
        this.name = slaTask.getName();
        this.description = slaTask.getDescription();
        this.udfs = slaTask.getUdfs();
    }
}
