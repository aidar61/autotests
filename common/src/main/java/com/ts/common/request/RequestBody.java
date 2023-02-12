package com.ts.common.request;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.ts.common.annotations.Mandatory;
import com.ts.common.annotations.TypeId;
import com.ts.common.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


/**
 * @author Aidar Askeev
 */
@Slf4j
public abstract class RequestBody {
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

    public List<String> receiveMandatoryFields() {
        List<Field> fields = Arrays.asList(this.getClass().getDeclaredFields());
        return fields.stream().filter(s -> s.isAnnotationPresent(Mandatory.class)).map(Field::getName).collect(Collectors.toList());
    }

    public List<String> receiveOptionalFields() {
        List<Field> fields = Arrays.asList(this.getClass().getDeclaredFields());
        return fields.stream().filter(s -> !s.isAnnotationPresent(Mandatory.class)).map(Field::getName).collect(Collectors.toList());
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

    public String removeTypeFieldWithName(String annotName) {
        List<String> typeFields = receiveTypesFieldWithName(annotName);
        return removeFields(typeFields);
    }


}
