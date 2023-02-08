package com.ts.common.entitites.commonEntities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ts.common.entitites.BaseEntity;
import com.ts.common.entitites.commonEntities.udfs.*;
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
public class Udfs extends BaseEntity {
    @JsonProperty("UDF_SD_TRUSTEDWATCHER")
    UdfSdTrustedWatcher udfSdTrustedWatcher;
    @JsonProperty("UDF_WATCHER")
    UdfWatcher udfWatcher;
    @JsonProperty("UDF_SD_TASK_CODE")
    UdfSdTaskCode udfSdTaskCode;
    @JsonProperty("UDF_SD_RELATED_TASK_CODES")
    UdfSdRelatedTaskCodes udfSdRelatedTaskCodes;
    @JsonProperty("UDF_SD_MODULE")
    UdfSdModule module;
    @JsonProperty("UDF_SLAHELP_COST")
    UdfSlaHelpCost udfSlaHelpCost;
    @JsonProperty("UDF_SDFEATURE_GENUSE")
    UdfSdFeatureGenuse udfSdFeatureGenuse;
    @JsonProperty("UDF_SLA_URGANCYHELP")
    UdfSlaUrgancyHelp udfSlaUrgancyHelp;
    @JsonProperty("UDF_SD_HELPKOEF")
    UdfSdHelpkoef udfSdHelpkoef;
    @JsonProperty("UDF_SD_LINKEDREQUEST")
    UdfSdLinkedRequest udfSdLinkedRequest;
    @JsonProperty("UDF_SD_REPEATREQUEST")
    UdfSdRepeatRequest udfSdRepeatRequest;
    @JsonProperty("UDF_SD_CLIENTWATCHERS")
    UdfSdClientWatchers udfSdClientWatchers;
    @JsonProperty("UDF_BDKU_CONFIGURATION")
    UdfsBdkuConfiguration bdku;
    @JsonProperty("UDF_SD_REMOTEID")
    UdfSdRemoteId udfSdRemoteId;
    @JsonProperty("UDF_SD_INITPERSON")
    UdfSdInitPerson udfSdInitPerson;
    @JsonProperty("UDF_SD_NOTLIMITEDWORK")
    UdfSdNotLimitedWork udfSdNotLimitedWork;
    @JsonProperty("UDF_WORKTASK_SOURCETYPE")
    UdfWorkTaskSourceType udfWorkTaskSourceType;
    @JsonProperty("UDF_REGPROJECT")
    UdfRegProject udfRegProject;

}
