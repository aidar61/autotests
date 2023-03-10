package com.ts.common.entitites.commonEntities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.ts.common.entitites.BaseEntity;
import com.ts.common.entitites.commonEntities.udf.*;
import com.ts.common.enums.Type;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.extern.jackson.Jacksonized;
import lombok.extern.slf4j.Slf4j;

import static com.ts.common.enums.Type.*;

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
public class Udf extends BaseEntity {
    UdfDate udfDate;
    UdfList udfList;
    UdfTask udfTask;
    UdfUser udfUser;
    UdfString udfString;

    public enum UdfSd {
        UDF_SD_TRUSTEDWATCHER("UDF_SD_TRUSTEDWATCHER", USER),
        UDF_WATCHER("UDF_WATCHER", USER),
        UDF_SD_TASK_CODE("UDF_SD_TASK_CODE", LIST),
        UDF_SD_RELATED_TASK_CODES("UDF_SD_RELATED_TASK_CODES", MULTILIST),
        UDF_SD_MODULE("UDF_SD_MODULE", TASK),
        UDF_SLAHELP_COST("UDF_SLAHELP_COST", STRING),
        UDF_SDFEATURE_GENUSE("UDF_SDFEATURE_GENUSE", LIST),
        UDF_SLA_URGANCYHELP("UDF_SLA_URGANCYHELP", LIST),
        UDF_SD_HELPKOEF("UDF_SD_HELPKOEF", DOUBLE),
        UDF_SD_LINKEDREQUEST("UDF_SD_LINKEDREQUEST", TASK),
        UDF_SD_REPEATREQUEST("UDF_SD_REPEATREQUEST", TASK),
        UDF_SD_CLIENTWATCHERS("UDF_SD_CLIENTWATCHERS", STRING),
        UDF_BDKU_CONFIGURATION("UDF_BDKU_CONFIGURATION", TASK),
        UDF_SD_REMOTEID("UDF_SD_REMOTEID", STRING),
        UDF_SD_INITPERSON("UDF_SD_INITPERSON", STRING),
        UDF_SD_NOTLIMITEDWORK("UDF_SD_NOTLIMITEDWORK", LIST),
        UDF_WORKTASK_SOURCETYPE("UDF_WORKTASK_SOURCETYPE", LIST),
        UDF_SD_PROVIDEDHELPDEADLINE("UDF_SD_PROVIDEDHELPDEADLINE", DATE),
        UDF_REGPROJECT("UDF_REGPROJECT", TASK);

        public final String udfId;
        public final Type type;

        UdfSd(String udfId, Type type) {
            this.udfId = udfId;
            this.type = type;
        }
    }
}
