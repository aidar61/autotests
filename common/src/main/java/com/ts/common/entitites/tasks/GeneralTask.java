package com.ts.common.entitites.tasks;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ts.common.controllers.TaskResponseBody;
import com.ts.common.entitites.BaseEntity;
import com.ts.common.entitites.commonEntities.*;
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
    Udfs udfs;
    @JsonProperty("status")
    Status finishStatus;
    Status priority;
    String statusName;
    String priorityName;
    String[] attachments;

    public GeneralTask(TaskResponseBody taskResponseBody) {
        this.id = taskResponseBody.getId();
        this.number = taskResponseBody.getNumber();
        this.name = taskResponseBody.getName();
        this.description = taskResponseBody.getDescription();
        this.finishStatus = taskResponseBody.getFinishStatus();
    }

    public void refreshUdf() {
        this.udfs = InitEntities.refreshUdf();
    }

    public void refreshUdf(Udfs udf) {
        this.udfs = InitEntities.refreshUdf();
        setUdfs(udf);
    }

    @Override
    public Object receiveTaskStatus() {
        return getFinishStatus();
    }

    public Object receiveShortName() {
        return getShortName();
    }

    public Object receiveName() {
        return getName();
    }

}
