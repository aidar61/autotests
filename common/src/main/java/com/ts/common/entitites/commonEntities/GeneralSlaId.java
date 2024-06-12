package com.ts.common.entitites.commonEntities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.ts.common.entitites.BaseEntity;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.extern.jackson.Jacksonized;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Builder
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GeneralSlaId extends BaseEntity {
    String id;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    String action;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    String name;

    public enum Fields {
        CAT("CAT_SLAHELP", "update"),
        RECEIVE_ANALIZE("MSG_SLAHELP_ANALIZE", "operation"),
        REQUEST_INFORMATION("MSG_SLAHELP_REQUESTINFO", "operation"),
        PROVIDE_CONSULT("MSG_SLAHELP_CONSULT", "operation"),
        CHANGE_SD_MODULE("MSG_SLAHELP_CHANGE_SD_MODULE", "operation"),
        CHANGE_AUTHOR("MSG_SLAHELP_CHANGEAUTHOR", "operation"),
        CHANGE_ATTRS("MSG_SLAHELP_CHANGEATTRS", "operation"),
        ADD_COMMENT("MSG_SLAHELP_OURCOMMENT", "operation"),
        PROVIDE_INFO("MSG_SLAHELP_PROVIDEINFO", "operation"),
        CLOSE_SLAHELP("MSG_SLAHELP_CLOSE", "operation");
        public final String id;
        public final String type;

        Fields(String id, String type) {
            this.id = id;
            this.type = type;
        }
    }
}
