package com.ts.common.entitites;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.ts.common.annotations.TypeId;
import com.ts.common.entitites.commonEntities.GeneralSlaId;
import com.ts.common.entitites.commonEntities.Udfs;
import com.ts.common.entitites.commonEntities.udfs.UdfSdModule;
import com.ts.common.entitites.commonEntities.udfs.UdfSdTaskCode;
import com.ts.common.entitites.sla.SlaTask;
import com.ts.common.utils.JsonUtils;
import com.ts.common.utils.RandomEntities;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.openqa.selenium.json.Json;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Aidar Askeev
 */
@Slf4j
public abstract class BaseEntity implements Serializable {
    private static String[] IGNORING_FIELDS = {""};

    public boolean isEquals(Object obj) {
        try {
            Assertions.assertThat(this)
                    .usingRecursiveComparison()
                    .ignoringFields(IGNORING_FIELDS)
                    .isEqualTo(obj);
            return true;
        } catch (AssertionError e) {
            log.error("Objects are not equals", e);
            return false;
        }
    }

    public boolean isEquals(Object obj, String... ignoringFileds) {
        try {
            Assertions.assertThat(this)
                    .usingRecursiveComparison()
                    .ignoringFields(ignoringFileds)
                    .isEqualTo(obj);
            return true;
        } catch (AssertionError e) {
            log.error("Objects are not equals", e);
            return false;
        }
    }

    public boolean isEqualsNoLog(Object obj) {
        try {
            Assertions.assertThat(this)
                    .usingRecursiveComparison()
                    .ignoringFields(IGNORING_FIELDS)
                    .isEqualTo(obj);
            return true;
        } catch (AssertionError e) {
            return false;
        }
    }

    public boolean isEqualsNoLog(Object obj, String... ignoringFileds) {
        try {
            Assertions.assertThat(this)
                    .usingRecursiveComparison()
                    .ignoringFields(ignoringFileds)
                    .isEqualTo(obj);
            return true;
        } catch (AssertionError e) {
            return false;
        }
    }

    private List<String> receiveChangeableFields() {
        List<Field> declaredFields = Arrays.asList(this.getClass().getDeclaredFields());
        return declaredFields.stream().filter(f -> f.isAnnotationPresent(TypeId.class)).map(Field::getName).collect(Collectors.toList());
    }

    public String removeField(String field) {
        ObjectNode jsonNode = JsonUtils.convertToJson(JsonUtils.convertToString(this));
        jsonNode.remove(field);
        return jsonNode.toString();
    }

    public String removeFields(List<String> fieldNames) {
        ObjectNode jsonNode = JsonUtils.convertToJson(JsonUtils.convertToString(this));
        fieldNames.forEach(s -> {
            jsonNode.remove(s);
        });
        return jsonNode.toString();
    }

    public String removeFields(String... fieldNames) {
        ObjectNode jsonNode = JsonUtils.convertToJson(JsonUtils.convertToString(this));
        for (int i = 0; i < fieldNames.length; i++) {
            jsonNode.remove(fieldNames[i]);
        }
        return jsonNode.toString();
    }

    public List<String> receiveAllFields() {
        List<Field> allFields = Arrays.asList(this.getClass().getDeclaredFields());
        return allFields.stream().map(Field::getName).collect(Collectors.toList());
    }

    public String removeAllFieldsExcept(String... fields) {
        List<String> allFields = receiveAllFields();
        log.info("all fields: {}", allFields.toString());
        List<String> acceptFields = Arrays.asList(fields);
        List<String> removedFields = allFields.stream().filter(a -> !acceptFields.contains(a)).collect(Collectors.toList());
        log.info(removedFields.toString());
        return removeFields(removedFields);
    }

    public static void main(String[] args) {
        SlaTask slaTask = RandomEntities.getSlaTask(GeneralSlaId.Fields.CHANGE_SD_MODULE);
        Udfs udfs = slaTask.getUdfs();
        UdfSdTaskCode udfSdTaskCode = RandomEntities.getUdfTaskCode(UdfSdTaskCode.Constants.ABNATTR.taskCodesId);
        UdfSdModule udfSdModule = RandomEntities.getUdfSdModule(UdfSdModule.Constants.NOTIFICATION_SERVICE.moduleIds);
        udfs.setUDF_SD_TASK_CODE(udfSdTaskCode);
        udfs.setUDF_SD_MODULE(udfSdModule);
        log.info(udfs.removeAllFieldsExcept("UDF_SD_TASK_CODE", "UDF_SD_MODULE"));
    }

}
