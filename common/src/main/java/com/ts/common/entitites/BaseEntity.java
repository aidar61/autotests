package com.ts.common.entitites;

import com.ts.common.annotations.TypeId;
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

}
