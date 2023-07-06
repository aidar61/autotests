package com.ts.common.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.ts.common.entitites.BaseEntity;
import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;

@Slf4j
public class JsonUtils {
    private final static ObjectMapper objectMapper = new ObjectMapper();

    public static <T extends BaseEntity> T convertJsonToObject(File json, Class<T> tClass) {
        try {
            return objectMapper.readValue(json, tClass);
        } catch (IOException e) {
            log.error("Can not convert json to OBJECT: ", e);
        }
        return null;
    }

    public static String convertToString(Object obj) {
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        String json = StringUtils.EMPTY;
        try {
            json = ow.writeValueAsString(obj).replace("\r", StringUtils.EMPTY).replace("\n", StringUtils.EMPTY);

        } catch (JsonProcessingException e) {
            log.error("Can not parse object: ", e);
        }
        return json;
    }

    public static ObjectNode convertToJson(String body) {
        try {
            return (ObjectNode) objectMapper.readTree(body);
        } catch (JsonProcessingException e) {
            log.error("Can not convert to JSON: ", e);
        }
        return null;
    }

    public static <T> T deserialize(Response response, Class<T> type) {
        try {
            return response
                    .then()
                    .extract()
                    .body()
                    .as(type);
        } catch (Exception e) {
            log.error("Can not parse object", e);
            return null;
        }
    }

    public static <T> T deserialize(String json, Class<T> tClass) {
        try {
            return objectMapper.readValue(json, tClass);
        } catch (IOException e) {
            log.error("Can not convert json to OBJECT: ", e);
        }
        return null;
    }
}
