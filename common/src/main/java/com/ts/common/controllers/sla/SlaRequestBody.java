package com.ts.common.controllers.sla;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.ts.common.annotations.Mandatory;
import com.ts.common.annotations.TypeId;
import com.ts.common.entitites.commonEntities.GeneralSlaId;
import com.ts.common.entitites.commonEntities.Parent;
import com.ts.common.entitites.commonEntities.Status;
import com.ts.common.entitites.commonEntities.Udfs;
import com.ts.common.entitites.sla.SlaTask;
import com.ts.common.request.RequestBody;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.extern.jackson.Jacksonized;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = false)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SlaRequestBody extends RequestBody {
    @Mandatory
    @TypeId(type = "category")
    GeneralSlaId category;
    @Mandatory
    @TypeId(type = "operation")
    GeneralSlaId operation;
    Parent parent;
    String name;
    @Mandatory
    String description;
    Udfs udfs;
    Status finishStatus;
    @Mandatory
    String[] attachments;

    public SlaRequestBody(SlaTask slaTask) {
        this.category = slaTask.getCategory();
        this.operation = slaTask.getOperation();
        this.parent = slaTask.getParent();
        this.name = slaTask.getName();
        this.description = slaTask.getDescription();
        this.udfs = slaTask.getUdfs();
        this.attachments = slaTask.getAttachments();
    }

    public enum Fields {
        CATEGORY("category"),
        OPERATION("operation");
        public final String field;

        Fields(String field) {
            this.field = field;
        }
    }
}
