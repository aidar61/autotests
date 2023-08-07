package com.ts.common.controllers;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
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
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@ToString
public class TaskResponseBody extends com.ts.common.request.ResponseBody {
    String id;
    String description;
    String name;
    @JsonProperty("shortname")
    String shortName;
    String number;
    Category category;
    @JsonProperty("status")
    Status finishStatus;
    String statusName;
    User handlerUser;
    User submitterUser;
    String priorityName;
    @JsonProperty("task")
    GeneralTask task;
    @JsonProperty("message")
    GeneralTask message;
    Resolution resolution;
//    String udfs;

    @Override
    public Object receiveTaskStatus() {
        if (this.task != null) {
            return task.getFinishStatus().getId();
        }
        return getFinishStatus().getId();
    }

    @Override
    public Object receiveHandlerUser() {
        return this.message.getHandlerUser();
    }


    @Override
    public Object receiveTaskResolution() {
        return this.message.getResolution().getId();
    }

//    @Override
//    public Object receiveUdf() {
//        return this.getUdfs();
//    }
}
