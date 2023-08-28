package com.ts.common.entitites.commonEntities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ts.common.entitites.BaseEntity;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.extern.jackson.Jacksonized;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MultiList extends BaseEntity {
    String id;
    @JsonProperty("userdata0")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    String userData0;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    String userData;
//    UserDataJson userdataJson;

    public MultiList(String id) {
        this.id = id;
//        this.userdataJson = userdataJson;
    }

}
