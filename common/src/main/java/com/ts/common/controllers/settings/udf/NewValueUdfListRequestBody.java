package com.ts.common.controllers.settings.udf;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.ts.common.entitites.commonEntities.List;
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
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NewValueUdfListRequestBody extends RequestBody {
    String order;
    Boolean selectable;
    String name;
    String code;
    String userdata;

    public NewValueUdfListRequestBody(List list) {
        this.order = list.getOrder();
        this.selectable = list.getSelectable();
        this.name = list.getName();
        this.code = list.getCode();
        this.userdata = list.getUserData();
    }
}
