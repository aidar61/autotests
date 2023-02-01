package com.ts.common.entitites;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;

import java.io.Serializable;

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
}
