package com.ts.common.entitites;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.ts.common.annotations.TypeId;
import com.ts.common.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;

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
    private static String[] IGNORING_FIELDS = {"description, slaType, category, operation, parent, handlerUser, udfs, attachments, description, finishStatus", "submitterUser"};

    public boolean isEquals(Object obj) {
        try {
            Assertions.assertThat(this)
                    .usingRecursiveComparison()
                    .ignoringActualNullFields()
                    .ignoringExpectedNullFields()
                    .ignoringFields(IGNORING_FIELDS)
                    .isEqualTo(obj);
            return true;
        } catch (AssertionError e) {
            log.error("Objects are not equals", e);
            return false;
        }
    }

    public boolean isEquals(Object obj, String... ignoringFields) {
        try {
            Assertions.assertThat(this)
                    .usingRecursiveComparison()
                    .ignoringFields(ignoringFields)
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


    public String removeField(String field) {
        ObjectNode jsonNode = JsonUtils.convertToJson(JsonUtils.convertToString(this));
        jsonNode.remove(field);
        return jsonNode.toString();
    }

    public String removeFields(List<String> fieldNames) {
        ObjectNode jsonNode = JsonUtils.convertToJson(JsonUtils.convertToString(this));
        fieldNames.forEach(s -> {
            assert jsonNode != null;
            jsonNode.remove(s);
        });
        assert jsonNode != null;
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

    public List<String> receiveOptionalFields() {
        List<Field> fields = Arrays.asList(this.getClass().getDeclaredFields());
        return fields.stream().filter(s -> !s.isAnnotationPresent(TypeId.class)).map(Field::getName).collect(Collectors.toList());
    }

    private List<Field> receiveChangeableFields() {
        List<Field> fields = Arrays.asList(this.getClass().getDeclaredFields());
        return fields.stream().filter(f -> f.isAnnotationPresent(TypeId.class)).collect(Collectors.toList());
    }

    private List<String> receiveTypesFieldWithName(String annotValue) {
        List<Field> changeableFields = receiveChangeableFields();
        return changeableFields.stream().filter(cf -> cf.getAnnotation(TypeId.class).type().equals(annotValue)).map(Field::getName).collect(Collectors.toList());
    }

    public String removeOptionalAndTypeFieldWithName(String annotName) {
        List<String> optionalFields = receiveOptionalFields();
        List<String> typeFields = receiveTypesFieldWithName(annotName);
        typeFields.addAll(optionalFields);
        return removeFields(typeFields);
    }

    public String keepTypeFieldWithName(String annotName) {
        List<String> typeFields = receiveTypesFieldWithName(annotName);
        List<String> allFields = receiveAllFields();
        allFields.removeAll(typeFields);
        return removeFields(allFields);
    }

    public String keepFields(String... fields) {
        List<String> allFields = receiveAllFields();
        allFields.removeAll(Arrays.asList(fields));
        return removeFields(allFields);
    }

    public Object receiveTaskStatus() {
        return null;
    }

    public Object receiveShortName() {
        return null;
    }

    public Object receiveName() {
        return null;
    }

    public Object receiveTaskNumber() {
        return null;
    }

    public Object receiveHandlerUser() {
        return null;
    }

    public Object receiveTaskResolution() {
        return null;
    }

    public Object receiveUdf() {
        return null;
    }

    public Integer receiveCount() {
        return null;
    }
}
