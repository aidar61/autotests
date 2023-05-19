package com.ts.common.controllers;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ts.common.annotations.Create;
import com.ts.common.annotations.Mandatory;
import com.ts.common.annotations.TypeId;
import com.ts.common.entitites.commonEntities.*;
import com.ts.common.entitites.tasks.GeneralTask;
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
public class TaskRequestBody extends com.ts.common.request.RequestBody {
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
    @JsonProperty("description")
    String description;
    @TypeId(type = "operation")
    User handlerUser;
    @Mandatory
    Udfs udfs;
    Status finishStatus;
    @TypeId(type = "operation")
    @Create
    String[] attachments;

    public TaskRequestBody(GeneralTask slaTask) {
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
