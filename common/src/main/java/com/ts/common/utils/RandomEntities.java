package com.ts.common.utils;

import com.ts.common.entitites.commonEntities.*;
import com.ts.common.entitites.commonEntities.udfs.*;
import com.ts.common.entitites.sla.SlaTask;
import com.ts.common.enums.Parents;

import java.io.File;

import static com.ts.common.enums.Parents.MTB;
import static com.ts.common.enums.Udfs.*;
import static com.ts.common.utils.RandomUtils.generateName;

public class RandomEntities {
    private static final String BDKU_JSON_PATH = "/Users/aidarka61/IdeaProjects/trackstudio-test/common/src/main/resources/data/mtbankBdkuConf.json";
    private static final String MODULE_JSON_PATH = "/Users/aidarka61/IdeaProjects/trackstudio-test/common/src/main/resources/data/moduleAkkConf.json";
    private static final File bdkuJsonFile = new File(BDKU_JSON_PATH);
    private static final File moduleJsonFile = new File(MODULE_JSON_PATH);

    private RandomEntities() {
    }

    public static SlaTask getSlaTask(GeneralSlaId.Fields iDs) {
        return SlaTask.builder()
                .category(getGeneralId(iDs))
                .operation(getGeneralId(iDs))
                .parent(getParent(MTB))
                .name(generateName())
                .description(generateName() + " description")
                .udfs(getFullUdfs())
                .attachments(new String[]{})
                .build();
    }

    public static Udfs getFullUdfs() {
        return Udfs.builder()
                .udfSdTrustedWatcher(getUdfSdTrustedWatcher())
                .udfWatcher(getUdfWatcher())
                .udfSdTaskCode(getUdfTaskCode())
                .udfSdRelatedTaskCodes(getUdfSdRelatedTaskCode())
                .udfSdModule(getUdfsModuleThrowsJson())
                .udfSlaHelpCost(getUdfSlaHelpCost())
                .udfSdFeatureGenuse(getUdfSdFeatureGenuse())
                .udfSlaUrgancyHelp(getUdfSlaUrgancyHelp())
                .udfSdHelpkoef(getUdfSdHelpkoef())
                .udfSdLinkedRequest(getUdfSdLinkedRequest())
                .udfSdRepeatRequest(getUdfSdRepeatRequest())
                .udfSdClientWatchers(getUdfSdClientWatchers())
                .udfsBdkuConfiguration(getBdkuThrowsJson())
                .udfSdRemoteId(getUdfSdRemoteId())
                .udfSdInitPerson(getUdfSdInitPerson())
                .udfSdNotLimitedWork(getUdfSdNotLimitedWork())
                .udfWorkTaskSourceType(getUdfWorkTaskSourceType())
                .udfRegProject(getUdfRegProject())
                .build();
    }

    public static UdfSdModule getUdfsModuleThrowsJson() {
        return JsonUtils.convertJsonToObject(moduleJsonFile, UdfSdModule.class);
    }

    public static UdfSdModule getUdfSdModule(String id) {
        return UdfSdModule.builder()
                .udfId(UDF_SD_MODULE.udfId)
                .type(UDF_SD_MODULE.type.name())
                .taskValue(new Task[]{new Task(id)})
                .build();
    }

    public static UdfsBdkuConfiguration getBdkuThrowsJson() {
        return JsonUtils.convertJsonToObject(bdkuJsonFile, UdfsBdkuConfiguration.class);
    }

    public static UdfSdTrustedWatcher getUdfSdTrustedWatcher() {
        return UdfSdTrustedWatcher.builder()
                .udfId(UDF_SD_TRUSTEDWATCHER.udfId)
                .type(UDF_SD_TRUSTEDWATCHER.type.name())
                .userValue(new UserValue[]{})
                .build();
    }

    public static UdfWatcher getUdfWatcher() {
        return UdfWatcher.builder()
                .udfId(UDF_WATCHER.udfId)
                .type(UDF_WATCHER.type.name())
                .userValue(new UserValue[]{})
                .build();
    }

    public static UdfSdTaskCode getUdfTaskCode() {
        return UdfSdTaskCode.builder()
                .udfId(UDF_SD_TASK_CODE.udfId)
                .type(UDF_SD_TASK_CODE.type.name())
                .listValue(new ListValue[]{})
                .build();
    }

    public static UdfSdTaskCode getUdfTaskCode(String id) {
        return UdfSdTaskCode.builder()
                .udfId(UDF_SD_TASK_CODE.udfId)
                .type(UDF_SD_TASK_CODE.type.name())
                .listValue(new ListValue[]{new ListValue(id)})
                .build();
    }

    public static UdfSdRelatedTaskCodes getUdfSdRelatedTaskCode() {
        return UdfSdRelatedTaskCodes.builder()
                .udfId(UDF_SD_RELATED_TASK_CODES.udfId)
                .type(UDF_SD_RELATED_TASK_CODES.type.name())
                .listValue(new ListValue[]{})
                .build();
    }

    public static UdfSlaHelpCost getUdfSlaHelpCost() {
        return UdfSlaHelpCost.builder()
                .udfId(UDF_SLAHELP_COST.udfId)
                .type(UDF_SLAHELP_COST.type.name())
                .build();
    }

    public static UdfSdFeatureGenuse getUdfSdFeatureGenuse() {
        return UdfSdFeatureGenuse.builder()
                .udfId(UDF_SDFEATURE_GENUSE.udfId)
                .type(UDF_SDFEATURE_GENUSE.type.name())
                .listValue(new ListValue[]{})
                .build();
    }

    public static UdfSlaUrgancyHelp getUdfSlaUrgancyHelp() {
        return UdfSlaUrgancyHelp.builder()
                .udfId(UDF_SLA_URGANCYHELP.udfId)
                .type(UDF_SLA_URGANCYHELP.type.name())
                .listValue(new ListValue[]{})
                .build();
    }

    public static UdfSdHelpkoef getUdfSdHelpkoef() {
        return UdfSdHelpkoef.builder()
                .udfId(UDF_SD_HELPKOEF.udfId)
                .type(UDF_SD_HELPKOEF.type.name())
                .build();
    }

    public static UdfSdLinkedRequest getUdfSdLinkedRequest() {
        return UdfSdLinkedRequest.builder()
                .udfId(UDF_SD_LINKEDREQUEST.udfId)
                .type(UDF_SD_LINKEDREQUEST.type.name())
                .taskValue(new Task[]{})
                .build();
    }

    public static UdfSdRepeatRequest getUdfSdRepeatRequest() {
        return UdfSdRepeatRequest.builder()
                .udfId(UDF_SD_REPEATREQUEST.udfId)
                .type(UDF_SD_REPEATREQUEST.type.name())
                .taskValue(new Task[]{})
                .build();
    }

    public static UdfSdClientWatchers getUdfSdClientWatchers() {
        return UdfSdClientWatchers.builder()
                .udfId(UDF_SD_CLIENTWATCHERS.udfId)
                .type(UDF_SD_CLIENTWATCHERS.type.name())
                .build();
    }

    public static UdfSdRemoteId getUdfSdRemoteId() {
        return UdfSdRemoteId.builder()
                .udfId(UDF_SD_REMOTEID.udfId)
                .type(UDF_SD_REMOTEID.type.name())
                .build();
    }

    public static UdfSdInitPerson getUdfSdInitPerson() {
        return UdfSdInitPerson.builder()
                .udfId(UDF_SD_INITPERSON.udfId)
                .type(UDF_SD_INITPERSON.type.name())
                .build();
    }

    public static UdfSdNotLimitedWork getUdfSdNotLimitedWork() {
        return UdfSdNotLimitedWork.builder()
                .udfId(UDF_SD_NOTLIMITEDWORK.udfId)
                .type(UDF_SD_NOTLIMITEDWORK.type.name())
                .listValue(new ListValue[]{})
                .build();
    }

    public static UdfWorkTaskSourceType getUdfWorkTaskSourceType() {
        return UdfWorkTaskSourceType.builder()
                .udfId(UDF_WORKTASK_SOURCETYPE.udfId)
                .type(UDF_WORKTASK_SOURCETYPE.type.name())
                .listValue(new ListValue[]{})
                .build();
    }

    public static UdfRegProject getUdfRegProject() {
        return UdfRegProject.builder()
                .udfId(UDF_REGPROJECT.udfId)
                .type(UDF_REGPROJECT.type.name())
                .taskValue(new Task[]{})
                .build();
    }

    public static Parent getParent(Parents parent) {
        return Parent.builder()
                .id(parent.id)
                .number(parent.tuskNumber)
                .build();
    }

    public static GeneralSlaId getGeneralId(GeneralSlaId.Fields category) {
        return GeneralSlaId.builder()
                .id(category.id)
                .build();
    }

}
