package com.ts.common.asserts;

import com.ts.common.entitites.BaseEntity;
import com.ts.common.entitites.tasks.GeneralTask;
import com.ts.common.enums.TaskStatuses;
import io.qameta.allure.Step;
import lombok.Data;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.testng.AssertJUnit;
import org.testng.asserts.Assertion;

import static org.testng.Assert.*;

@Slf4j
@Getter
public class TaskAsserts extends EntityAssert {

    public TaskAsserts(BaseEntity entity) {
        super(entity);
    }

    public TaskAsserts(GeneralTask task) {
        super(task);
    }

    public static TaskAsserts assertThat(BaseEntity entity) {
        return new TaskAsserts(entity);
    }

    @Step("Assert task: {0}")
    public static TaskAsserts assertThat(GeneralTask task) {
        return new TaskAsserts(task);
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

    public <T extends EntityAssert> T isNotEmpty(Object value) {
        assertNotNull(value);
        log.info("Value is not empty {}", value);
        return (T) this;
    }

    public TaskAsserts isCorrectStatus(TaskStatuses expectedTaskStatus) {
        assertEquals(super.entity.receiveTaskStatus()
                , expectedTaskStatus.name(), "Task Status is not valid");
        log.info("Task status is correct Actual: {}, Expected: {}"
                , super.entity.receiveTaskStatus(), expectedTaskStatus.name());
        return this;
    }
//    public TaskAsserts isCorrectTaskValues(GeneralTask expectedTask) {
//
//    }
}
