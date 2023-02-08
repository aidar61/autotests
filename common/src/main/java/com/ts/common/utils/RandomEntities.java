package com.ts.common.utils;

import com.ts.common.entitites.commonEntities.*;
import com.ts.common.entitites.commonEntities.udfs.UdfSdHelpkoef;
import com.ts.common.entitites.commonEntities.udfs.UdfSdModule;
import com.ts.common.entitites.commonEntities.udfs.UdfSdTrustedWatcher;
import com.ts.common.entitites.commonEntities.udfs.UdfsBdkuConfiguration;
import com.ts.common.entitites.sla.SlaTask;
import com.ts.common.enums.Categories;
import com.ts.common.enums.Parents;
import org.testng.annotations.Test;

import java.io.File;

import static com.ts.common.enums.Categories.CAT_SLA_HELP;
import static com.ts.common.enums.Parents.MTB;
import static com.ts.common.utils.RandomUtils.generateName;

public class RandomEntities {
    private static final String BDKU_JSON_PATH = "/Users/aidarka61/IdeaProjects/trackstudio-test/common/src/main/resources/data/mtbankBdkuConf.json";
    private static final String MODULE_JSON_PATH = "/Users/aidarka61/IdeaProjects/trackstudio-test/common/src/main/resources/data/moduleAkkConf.json";
    private static final File bdkuJsonFile = new File(BDKU_JSON_PATH);
    private static final File moduleJsonFile = new File(MODULE_JSON_PATH);

    private RandomEntities() {
    }

    public static Udfs getUdfsWithAkkModuleAndBdkuMTBank() {
        return Udfs.builder()
                .module(getUdfsModuleThrowsJson())
                .bdku(getBdkuThrowsJson())
                .build();
    }

    public static Udfs getFullUdfs() {
        return Udfs.builder()
                .module(getUdfsModuleThrowsJson())
                .bdku(getBdkuThrowsJson())
                .build();
    }

    public static UdfSdModule getUdfsModuleThrowsJson() {
        return JsonUtils.convertJsonToObject(moduleJsonFile, UdfSdModule.class);
    }

    public static UdfsBdkuConfiguration getBdkuThrowsJson() {
        return JsonUtils.convertJsonToObject(bdkuJsonFile, UdfsBdkuConfiguration.class);
    }

    public static UdfSdTrustedWatcher getUdfSdTrustedWatcher(com.ts.common.enums.Udfs udfs) {
        return UdfSdTrustedWatcher.builder()
                .udfId(udfs.UDF_SD_TRUSTEDWATCHER.udfId)
                .type(udfs.)
    }

    public static Parent getParent(Parents parent) {
        return Parent.builder()
                .id(parent.id)
                .number(parent.tuskNumber)
                .build();
    }

    public static CategoryId getCategory(Categories category) {
        return CategoryId.builder()
                .id(category.id)
                .build();
    }

    public static SlaTask getSlaTask() {
        return SlaTask.builder()
                .category(getCategory(CAT_SLA_HELP))
                .parent(getParent(MTB))
                .name(generateName())
                .description(generateName() + " description")
                .udfs(getUdfsWithAkkModuleAndBdkuMTBank())
                .build();
    }

    @Test
    public void test() {
        com.ts.common.enums.Udfs[] values = com.ts.common.enums.Udfs.values();

    }

}
