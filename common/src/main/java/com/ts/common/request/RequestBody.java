package com.ts.common.request;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.ts.common.annotations.Create;
import com.ts.common.annotations.Mandatory;
import com.ts.common.annotations.TypeId;
import com.ts.common.controllers.TaskRequestBody;
import com.ts.common.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.util.ArrayList;
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

    public List<String> receiveAllFields() {
        List<Field> allFields = Arrays.asList(this.getClass().getDeclaredFields());
        return allFields.stream().map(Field::getName).collect(Collectors.toList());
    }

    public List<String> receiveCreateFields() {
        List<Field> fields = Arrays.asList(this.getClass().getDeclaredFields());
        return fields.stream().filter(s -> s.isAnnotationPresent(Create.class)).map(Field::getName).collect(Collectors.toList());
    }

    private List<Field> receiveChangeableFields() {
        List<Field> fields = Arrays.asList(this.getClass().getDeclaredFields());
        return fields.stream().filter(f -> f.isAnnotationPresent(TypeId.class)).collect(Collectors.toList());
    }

    private List<String> receiveTypesFieldWithName(String annotValue) {
        List<Field> changeableFields = receiveChangeableFields();
        return changeableFields.stream().filter(cf -> cf.getAnnotation(TypeId.class).type().equals(annotValue)).map(Field::getName).collect(Collectors.toList());
    }

    public String keepMandatoryAndCreateFields() {
        List<String> createFields = receiveCreateFields();
        List<String> mandatoryFields = receiveMandatoryFields();
        List<String> allFields = receiveAllFields();
        allFields.removeAll(createFields);
        allFields.removeAll(mandatoryFields);
        return removeFields(allFields);
    }

    public String keepMandatoryAndCreateFieldsAnd(TaskRequestBody.Fields... extraFields) {
        List<String> createFields = receiveCreateFields();
        List<String> mandatoryFields = receiveMandatoryFields();
        List<String> allFields = receiveAllFields();
        allFields.removeAll(createFields);
        allFields.removeAll(mandatoryFields);
        List<String> extraFieldsList = new ArrayList<>();
        Arrays.stream(extraFields).forEach(f -> extraFieldsList.add(f.field));
        allFields.removeAll(extraFieldsList);
        return removeFields(allFields);
    }

    public String keepFields(String... fields) {
        List<String> allFields = receiveAllFields();
        allFields.removeAll(Arrays.asList(fields));
        return removeFields(allFields);
    }

    public String keepFields(TaskRequestBody.Fields... fields) {
        List<String> fieldsList = new ArrayList<>();
        Arrays.stream(fields).forEach(f -> fieldsList.add(f.field));
        List<String> allFields = receiveAllFields();
        allFields.removeAll(fieldsList);
        return removeFields(allFields);
    }
}
