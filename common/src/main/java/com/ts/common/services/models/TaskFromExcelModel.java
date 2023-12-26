package com.ts.common.services.models;

import com.ts.common.entitites.commonEntities.Tag;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
@Builder
public class TaskFromExcelModel {
    String name;
    String description;
    String creator;
    String handler;
    String UDF_WATCHER;
    String UDF_CDP_BL;
    String UDF_SD_MODULE;
    String UDF_MIS_SERVICE;
    String priority;
    String UDF_WORKTASK_ANALYSIS; //Предварительный анализ
    //    String UDF_WORKTASK_PLANFD; //Планируемая дата начала
//    String UDF_WORKTASK_PLANTD; //Планируемая дата завершения
//    String UDF_WORKTASK_PLANBUDGET; //Оценка трудоемко
    String UDF_BDKU_CONFIGURATION;
    String UDF_SD_LINKEDREQUEST;
    String status;
    String file;
    String tag;
    String clientWatcher;
}
