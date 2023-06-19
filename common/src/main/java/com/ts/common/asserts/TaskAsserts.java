package com.ts.common.asserts;

import com.ts.common.entitites.BaseEntity;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.testng.AssertJUnit;
import org.testng.asserts.Assertion;

import static org.testng.Assert.assertEquals;

@Slf4j
public class TaskAsserts extends EntityAssert {
    public TaskAsserts(BaseEntity entity) {
        super(entity);
    }

    public static TaskAsserts assertThat(BaseEntity entity) {
        return new TaskAsserts(entity);
    }

    public TaskAsserts isCorrectName(String expectedCode, String expectedPrefix) {
        String expected = String.format("%s-%s-1.0.0", expectedCode, expectedPrefix);
        Object actual = super.entity.receiveName();
        AssertJUnit.assertEquals(expected, actual);
        log.info("Task name is correct: Expected {}, Actual {}", expected, actual);
        return this;
    }

    public TaskAsserts isCorrectShortName(String expectedPrefix) {
        String expected = String.format("%s-1.0.0", expectedPrefix);
        Object actual = super.entity.receiveShortName();
        AssertJUnit.assertEquals(expected, actual);
        log.info("Task name is correct: Expected {}, Actual {}", expected, actual);
        return this;
    }
}
