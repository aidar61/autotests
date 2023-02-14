package com.ts.common.application.database.dbEntities;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@ToString
public class GrTaskDbEntity extends DbEntity {
    String task_id;
    String task_shortname;
    String task_name;
    String task_status;
}
