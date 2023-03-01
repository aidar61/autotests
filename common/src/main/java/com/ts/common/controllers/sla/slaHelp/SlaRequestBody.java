package com.ts.common.controllers.sla.slaHelp;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.ts.common.annotations.Create;
import com.ts.common.annotations.Mandatory;
import com.ts.common.annotations.TypeId;
import com.ts.common.entitites.commonEntities.*;
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
    @TypeId(type = "operation")
    String id;
    @Create
    GeneralSlaId category;
    @TypeId(type = "operation")
    GeneralSlaId operation;
    @Create
    Parent parent;
    @Create
    String name;
    @Mandatory
    String description;
    @TypeId(type = "operation")
    User handlerUser;
    @Mandatory
    Udfs udfs;
    Status finishStatus;
    @TypeId(type = "operation")
    String[] attachments;

    public SlaRequestBody(SlaTask slaTask) {
        this.id = slaTask.getId();
        this.category = slaTask.getCategory();
        this.operation = slaTask.getOperation();
        this.parent = slaTask.getParent();
        this.name = slaTask.getName();
        this.description = slaTask.getDescription();
        this.handlerUser = slaTask.getHandlerUser();
        this.udfs = slaTask.getUdfs();
        this.attachments = slaTask.getAttachments();
    }

    public enum Fields {
        ID("id"),
        CATEGORY("category"),
        OPERATION("operation"),
        PARENT("parent"),
        NAME("name"),
        DESCRIPTION("description"),
        HANDLER_USER("handlerUser"),
        UDFS("udfs"),
        FINISH_STATUS("finishStatus"),
        ATTACHMENTS("attachments");
        public final String field;

        Fields(String field) {
            this.field = field;
        }
    }
}
