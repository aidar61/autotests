package com.ts.common.entitites.tasks;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ts.common.asserts.ApiAsserts;
import com.ts.common.controllers.TaskResponseBody;
import com.ts.common.entitites.BaseEntity;
import com.ts.common.entitites.commonEntities.*;
import com.ts.common.entitites.commonEntities.Task;
import com.ts.common.enums.TaskType;
import com.ts.common.utils.InitEntities;
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
@ToString
public class GeneralTask extends BaseEntity {
    public static ApiAsserts TaskAsserts;
    TaskType taskType;
    String id;
    String number;
    GeneralSlaId category;
    GeneralSlaId operation;
    Parent parent;
    String name;
    String description;
    String shortName;
    User handlerUser;
    User submitterUser;
    Udfs udfs;
    //    String udfsString;
    @JsonProperty("status")
    Status finishStatus;
    Status priority;
    Resolution resolution;
    String statusName;
    @JsonProperty("mstatusName")
    String mstatusName;
    String priorityName;
    Boolean confirmed;
    String[] attachments;

    public GeneralTask(TaskResponseBody taskResponseBody) {
        this.id = taskResponseBody.getId();
        this.number = taskResponseBody.getNumber();
        this.name = taskResponseBody.getName();
        this.description = taskResponseBody.getDescription();
        this.finishStatus = taskResponseBody.getFinishStatus();
        this.handlerUser = taskResponseBody.getHandlerUser();
        this.submitterUser = taskResponseBody.getSubmitterUser();
//        this.udfsString = taskResponseBody.getUdfs();
    }


    public void refreshUdf() {
        this.udfs = InitEntities.refreshUdf();
    }

    public void refreshUdf(Udfs udf) {
        this.udfs = InitEntities.refreshUdf();
        setUdfs(udf);
    }

    public void refreshTask() {
        this.resolution = null;
        this.handlerUser = null;
        this.finishStatus = null;
        this.confirmed = null;
        this.description = null;
        this.udfs = null;
        this.shortName = null;
        this.name = null;
    }

    public com.ts.common.entitites.commonEntities.Task to() {
        return Task.builder()
                .id(this.id)
                .number(this.number)
                .build();
    }

    @Override
    public Object receiveTaskStatus() {
        return getFinishStatus().getId();
    }

    public Object receiveShortName() {
        return getShortName();
    }

    public Object receiveName() {
        return getName();
    }
}
