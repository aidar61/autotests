package com.ts.common.utils;

import com.ts.common.entitites.sla.Task;
import com.ts.common.entitites.sla.UdfSdModule;
import com.ts.common.entitites.sla.Udfs;

import static com.ts.common.utils.RandomUtils.*;

public class RandomEntities {

    private RandomEntities() {
    }

    public static Task getTask() {
        return Task.builder()
                .name(generateName())
                .description(generateName())
                .udfs(getUdfs())
                .build();
    }

    public static Udfs getUdfs() {
        return Udfs.builder()
                .module(getUdfsModule())
                .build();
    }

    public static UdfSdModule getUdfsModule() {
        return UdfSdModule.builder().build();
    }
}
