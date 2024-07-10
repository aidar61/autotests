package com.ts.common.controllers.portlet;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.ts.common.entitites.commonEntities.UserProjectAssign;
import com.ts.common.request.RequestBody;
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
public class UserProjectAssignRequestBody extends RequestBody {
    String userId;
    String projectCode;
    String stageId;
    String fromStr;
    String toStr;
    Integer occupancy;

    public UserProjectAssignRequestBody(UserProjectAssign assign) {
        this.userId = assign.getUser().getId();
        this.projectCode = assign.getProjectCode();
        this.stageId = assign.getStageId();
        this.fromStr = assign.getFromStr();
        this.toStr = assign.getToStr();
        this.occupancy = assign.getOccupancy();
    }

}
