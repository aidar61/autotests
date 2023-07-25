package com.ts.common.application.database.dbEntities;

import com.ts.common.entitites.commonEntities.Task;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import java.net.URISyntaxException;
import java.util.Arrays;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@ToString
@Slf4j
public class GrTaskDbEntity extends DbEntity {
    String task_id;
    String task_name;
    String task_status;
    String task_number;
    String task_path;
    String task_parent;

    public Task mapTo() {
        return Task.builder()
                .id(this.task_id)
                .number(this.task_number)
                .build();
    }

    public Task receiveParentTask() {
        return Task.builder()
                .id(this.task_parent)
                .number(extractParentTaskFromTaskPath())
                .build();
    }

    public String extractParentTaskFromTaskPath() {
        String[] pathSegments = this.task_path.split("/");
        if (pathSegments.length >= 2) {
            String preLastEndpoint = pathSegments[pathSegments.length - 2];
            log.info("Parent task is {}", preLastEndpoint);
            return preLastEndpoint;
        }
        String firstEndPoint = pathSegments[0];
        log.info("Parent task is {}", firstEndPoint);
        return firstEndPoint;
    }
}
