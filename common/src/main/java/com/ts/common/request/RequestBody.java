package com.ts.common.request;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.ts.common.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.List;


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


    public String convertToString() {
        return JsonUtils.convertToString(this);
    }
}
