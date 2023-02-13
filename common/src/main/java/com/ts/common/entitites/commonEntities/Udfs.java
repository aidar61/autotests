package com.ts.common.entitites.commonEntities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
    //    @JsonProperty(value = "UDF_SD_TRUSTEDWATCHER")
    UdfSdTrustedWatcher UDF_SD_TRUSTEDWATCHER;
    //    @JsonProperty(value = "UDF_WATCHER")
    UdfWatcher UDF_WATCHER;
    //    @JsonProperty(value = "UDF_SD_TASK_CODE")
    UdfSdTaskCode UDF_SD_TASK_CODE;
    //    @JsonProperty(value = "UDF_SD_RELATED_TASK_CODES")
    UdfSdRelatedTaskCodes UDF_SD_RELATED_TASK_CODES;
    //    @JsonProperty(value = "UDF_SD_MODULE")
    UdfSdModule UDF_SD_MODULE;
    //    @JsonProperty(value = "UDF_SLAHELP_COST")
    UdfSlaHelpCost UDF_SLAHELP_COST;
    //    @JsonProperty(value = "UDF_SDFEATURE_GENUSE")
    UdfSdFeatureGenuse UDF_SDFEATURE_GENUSE;
    //    @JsonProperty(value = "UDF_SLA_URGANCYHELP")
    UdfSlaUrgancyHelp UDF_SLA_URGANCYHELP;
    //    @JsonProperty(value = "UDF_SD_HELPKOEF")
    UdfSdHelpkoef UDF_SD_HELPKOEF;
    //    @JsonProperty(value = "UDF_SD_LINKEDREQUEST")
    UdfSdLinkedRequest UDF_SD_LINKEDREQUEST;
    //    @JsonProperty(value = "UDF_SD_REPEATREQUEST")
    UdfSdRepeatRequest UDF_SD_REPEATREQUEST;
    //    @JsonProperty(value = "UDF_SD_CLIENTWATCHERS")
    UdfSdClientWatchers UDF_SD_CLIENTWATCHERS;
    //    @JsonProperty(value = "UDF_BDKU_CONFIGURATION")
    UdfsBdkuConfiguration UDF_BDKU_CONFIGURATION;
    //    @JsonProperty(value = "UDF_SD_REMOTEID")
    UdfSdRemoteId UDF_SD_REMOTEID;
    //    @JsonProperty(value = "UDF_SD_INITPERSON")/
    UdfSdInitPerson UDF_SD_INITPERSON;
    //    @JsonProperty(value = "UDF_SD_NOTLIMITEDWORK")
    UdfSdNotLimitedWork UDF_SD_NOTLIMITEDWORK;
    //    @JsonProperty(value = "UDF_WORKTASK_SOURCETYPE")
    UdfWorkTaskSourceType UDF_WORKTASK_SOURCETYPE;
    //    @JsonProperty(value = "UDF_REGPROJECT")
    UdfRegProject UDF_REGPROJECT;
}
