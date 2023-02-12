package com.ts.common.entitites.commonEntities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
public class GeneralSlaId {
    String id;

    public enum Fields {
        CAT_SLA_HELP("CAT_SLAHELP", "update"),
        RECEIVE_ANALIZE("MSG_SLAHELP_ANALIZE", "operation"),
        REQUEST_INFORMATION("MSG_SLAHELP_REQUESTINFO", "operation"),
        PROVIDE_CONSULT("MSG_SLAHELP_CONSULT", "operation"),
        CHANGE_SD_MODULE("MSG_SLAHELP_CHANGE_SD_MODULE", "operation"),
        CHANGE_AUTHOR("MSG_SLAHELP_CHANGEAUTHOR", "operation"),
        CHANGE_ATTRS("MSG_SLAHELP_CHANGEATTRS", "operation"),
        ADD_COMMENT("MSG_SLAHELP_OURCOMMENT", "operation"),
        CLOSE_SLAHELP("MSG_SLAHELP_CLOSE", "operation");
        public final String id;
        public final String type;

        Fields(String id, String type) {
            this.id = id;
            this.type = type;
        }
    }
}
