package com.ts.common.controllers;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
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
@JsonInclude(JsonInclude.Include.NON_NULL)
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
    @JsonProperty("shortname")
    String shortName;
    Status priority;
    @TypeId(type = "operation")
    User handlerUser;
    Resolution resolution;
    Boolean confirmed;
    @Mandatory
    Udfs udfs;
    Status finishStatus;
    @TypeId(type = "operation")
    @Create
    String[] attachments;

    public TaskRequestBody(GeneralTask generalTask) {
        this.id = generalTask.getId();
        this.category = generalTask.getCategory();
        this.operation = generalTask.getOperation();
        this.parent = generalTask.getParent();
        this.name = generalTask.getName();
        this.description = generalTask.getDescription();
        this.handlerUser = generalTask.getHandlerUser();
        this.udfs = generalTask.getUdfs();
        this.attachments = generalTask.getAttachments();
        this.priority = generalTask.getPriority();
        this.shortName = generalTask.getShortName();
        this.resolution = generalTask.getResolution();
        this.finishStatus = generalTask.getFinishStatus();
        this.confirmed = generalTask.getConfirmed();
    }

    public enum Fields {
        ID("id"),
        CATEGORY("category"),
        OPERATION("operation"),
        PARENT("parent"),
        NAME("name"),
        DESCRIPTION("description"),
        SHORT_NAME("shortName"),
        PRIORITY("priority"),
        RESOLUTION("resolution"),
        HANDLER_USER("handlerUser"),
        UDFS("udfs"),
        CONFIRMED("confirmed"),
        FINISH_STATUS("finishStatus"),
        ATTACHMENTS("attachments");
        public final String field;

        Fields(String field) {
            this.field = field;
        }
    }
}
