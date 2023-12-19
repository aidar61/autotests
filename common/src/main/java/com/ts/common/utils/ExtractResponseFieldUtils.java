package com.ts.common.utils;

import com.ts.common.entitites.BaseEntity;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;

@Slf4j
public class ExtractResponseFieldUtils {

    private Response response;

    public ExtractResponseFieldUtils(Response response) {
        this.response = response;
    }

    public static ExtractResponseFieldUtils extractThat(Response response) {
        return new ExtractResponseFieldUtils(response);
    }

    public <T extends BaseEntity> T extractByPath(String path, Class<T> type) {
        T object = null;
        try {
            object = new JsonPath(response.asString()).getObject(path, type);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        Assertions.assertThat(object)
                .withFailMessage("Cannot extract object of type %s because this field don't exist", path)
                .isNotNull();
        log.info("Extracted field value {} of type {}", object.toString(), path);
        return object;
    }
}
