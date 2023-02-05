package com.ts.common.entitites.sla;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.ts.common.entitites.commonEntities.Category;
import com.ts.common.entitites.commonEntities.Parent;
import com.ts.common.entitites.commonEntities.Udfs;
import com.ts.common.request.RequestBody;
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
public class CreateSlaRequestBody extends RequestBody {
    Category category;
    Parent parent;
    String name;
    String description;
    Udfs udfs;

    public CreateSlaRequestBody(SlaTask slaTask) {
        this.category = slaTask.getCategory();
        this.parent = slaTask.getParent();
        this.name = slaTask.getName();
        this.description = slaTask.getDescription();
        this.udfs = slaTask.getUdfs();
    }
}
