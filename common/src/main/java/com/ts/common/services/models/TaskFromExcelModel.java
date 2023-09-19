package com.ts.common.services.models;


import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class TaskFromExcelModel {
    String name;
    String description;
    String creator;
    String handler;
    String UDF_WATCHER;
    String UDF_CDP_BL;
    String UDF_SD_MODULE;
    String UDF_MIS_SERVICE;
    String UDF_BDKU_CONFIGURATION;
    String priority;
    String workTaskAnalysis;
}
