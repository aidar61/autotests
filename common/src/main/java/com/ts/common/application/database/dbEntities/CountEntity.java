package com.ts.common.application.database.dbEntities;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@ToString
@Slf4j
public class CountEntity extends DbEntity {
    @JsonProperty("COUNT(*)")
    Integer count;

    @Override
    public Integer receiveCount() {
        return getCount();
    }
}
