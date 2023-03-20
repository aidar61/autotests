package com.ts.common.entitites.commonEntities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.ts.common.annotations.TypeId;
import com.ts.common.entitites.BaseEntity;
import com.ts.common.entitites.commonEntities.udf.UdfList;
import com.ts.common.entitites.commonEntities.udf.UdfUser;
import com.ts.common.entitites.commonEntities.udfs.*;
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
public class Udfs extends BaseEntity {
    @JsonInclude(JsonInclude.Include.NON_NULL)
//    @JsonProperty(defaultValue = "UDF_SD_TRUSTEDWATCHER")
    UdfSdTrustedWatcher udfSdTrustedWatcher;
    @JsonInclude(JsonInclude.Include.NON_NULL)
//    @JsonProperty(defaultValue = "UDF_WATCHER")
    UdfWatcher udfWatcher;
    @JsonInclude(JsonInclude.Include.NON_NULL)
//    @JsonProperty(defaultValue = "UDF_SD_TASK_CODE")
    @TypeId(type = "module")
    UdfSdTaskCode udfSdTaskCode;
    @JsonInclude(JsonInclude.Include.NON_NULL)
//    @JsonProperty(defaultValue = "UDF_SD_RELATED_TASK_CODES")
    UdfSdRelatedTaskCodes udfSdRelatedTaskCodes;
    @JsonInclude(JsonInclude.Include.NON_NULL)
//    @JsonProperty(defaultValue = "UDF_SD_MODULE")
    @TypeId(type = "module")
    UdfSdModule udfSdModule;
    //    @JsonProperty(defaultValue = "UDF_SLAHELP_COST")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    UdfSlaHelpCost udfSlaHelpCost;
    @JsonInclude(JsonInclude.Include.NON_NULL)
//    @JsonProperty(defaultValue = "UDF_SDFEATURE_GENUSE")
    UdfSdFeatureGenuse udfSdFeatureGenuse;
    @JsonInclude(JsonInclude.Include.NON_NULL)
//    @JsonProperty(defaultValue = "UDF_SLA_URGANCYHELP")
    UdfSlaUrgancyHelp udfSlaUrgancyHelp;
    @JsonInclude(JsonInclude.Include.NON_NULL)
//    @JsonProperty(defaultValue = "UDF_SD_HELPKOEF")
    UdfSdHelpkoef udfSdHelpkoef;
    @JsonInclude(JsonInclude.Include.NON_NULL)
//    @JsonProperty(defaultValue = "UDF_SD_LINKEDREQUEST")
    UdfSdLinkedRequest udfSdLinkedRequest;
    @JsonInclude(JsonInclude.Include.NON_NULL)
//    @JsonProperty(defaultValue = "UDF_SD_REPEATREQUEST")
    UdfSdRepeatRequest udfSdRepeatRequest;
    @JsonInclude(JsonInclude.Include.NON_NULL)
//    @JsonProperty(defaultValue = "UDF_SD_CLIENTWATCHERS")
    UdfSdClientWatchers udfSdClientWatchers;
    @JsonInclude(JsonInclude.Include.NON_NULL)
//    @JsonProperty(defaultValue = "UDF_BDKU_CONFIGURATION")
    UdfsBdkuConfiguration udfsBdkuConfiguration;
    @JsonInclude(JsonInclude.Include.NON_NULL)
//    @JsonProperty(defaultValue = "UDF_SD_REMOTEID")
    UdfSdRemoteId udfSdRemoteId;
    @JsonInclude(JsonInclude.Include.NON_NULL)
//    @JsonProperty(defaultValue = "UDF_SD_INITPERSON")
    UdfSdInitPerson udfSdInitPerson;
    @JsonInclude(JsonInclude.Include.NON_NULL)
//    @JsonProperty(defaultValue = "UDF_SD_NOTLIMITEDWORK")
    UdfSdNotLimitedWork udfSdNotLimitedWork;
    @JsonInclude(JsonInclude.Include.NON_NULL)
//    @JsonProperty(defaultValue = "UDF_WORKTASK_SOURCETYPE")
    UdfWorkTaskSourceType udfWorkTaskSourceType;
    @JsonInclude(JsonInclude.Include.NON_NULL)
//    @JsonProperty(defaultValue = "UDF_SD_PROVIDEDHELPDEADLINE")
    @TypeId(type = "date")
    UdfSdProvidedHelpDeadline udfSdProvidedHelpDeadline;
    @JsonInclude(JsonInclude.Include.NON_NULL)
//    @JsonProperty(defaultValue = "UDF_REGPROJECT")
    UdfRegProject udfRegProject;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    UdfUser udfUser;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    UdfList udfList;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    UdfList secondUdfList;

    public enum Fields {
        UDF_SD_TRUSTEDWATCHER("udfSdTrustedWatcher"),
        UDF_WATCHER("udfWatcher"),
        UDF_SD_TASK_CODE("udfSdTaskCode"),
        UDF_SD_RELATED_TASK_CODES("udfSdRelatedTaskCodes"),
        UDF_SD_MODULE("udfSdModule"),
        UDF_SLAHELP_COST("udfSlaHelpCost"),
        UDF_SDFEATURE_GENUSE("udfSdFeatureGenuse"),
        UDF_SLA_URGANCYHELP("udfSlaUrgancyHelp"),
        UDF_SD_HELPKOEF("udfSdHelpkoef"),
        UDF_SD_LINKEDREQUEST("udfSdLinkedRequest"),
        UDF_SD_REPEATREQUEST("udfSdRepeatRequest"),
        UDF_SD_CLIENTWATCHERS("udfSdClientWatchers"),
        UDF_BDKU_CONFIGURATION("udfsBdkuConfiguration"),
        UDF_SD_REMOTEID("udfSdRemoteId"),
        UDF_SD_INITPERSON("udfSdInitPerson"),
        UDF_SD_NOTLIMITEDWORK("udfSdNotLimitedWork"),
        UDF_WORKTASK_SOURCETYPE("udfWorkTaskSourceType"),
        UDF_SD_PROVIDEDHELPDEADLINE("udfSdProvidedHelpDeadline"),
        UDF_REGPROJECT("udfRegProject");
        public final String field;

        Fields(String field) {
            this.field = field;
        }
    }

    public enum UdfSd {
        UDF_SD_TRUSTEDWATCHER("UDF_SD_TRUSTEDWATCHER", USER),
        UDF_WATCHER("UDF_WATCHER", USER),
        UDF_ROLE_CURRENT("UDF_ROLE_CURRENT", USER),
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
        UDF_SD_AUTHORCLIENT_MSG("UDF_SD_AUTHORCLIENT_MSG", USER),
        UDF_SDBUG_PRIORITYBUG("UDF_SDBUG_PRIORITYBUG", LIST),
        UDF_SD_REMOTEACCESS("UDF_SD_REMOTEACCESS", LIST),
        UDF_SDFEATURE_TYPE("UDF_SDFEATURE_TYPE", LIST),
        UDF_SDFEATURE_PAYDCS("UDF_SDFEATURE_PAYDCS", LIST),
        STDT_HANDLER("STDT_HANDLER", USER),
        UDF_REGPROJECT("UDF_REGPROJECT", TASK);

        public final String udfId;
        public final Type type;

        UdfSd(String udfId, Type type) {
            this.udfId = udfId;
            this.type = type;
        }
    }
}
