package com.ts.common.asserts;

import com.ts.common.entitites.BaseEntity;
import com.ts.common.entitites.commonEntities.*;
import com.ts.common.entitites.commonEntities.udf.*;
import com.ts.common.entitites.tasks.Task;
import com.ts.common.enums.TaskStatuses;
import io.qameta.allure.Step;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.jsoup.Jsoup;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Date;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.testng.Assert.assertEquals;

@Slf4j
public class CommonAssert {
    private Response response;

    public CommonAssert(Response response) {
        this.response = response;
    }


    public static CommonAssert assertThat(Response response) {
        return new CommonAssert(response);
    }

    @Step("[ASSERT] ({0}) Checking task field, Expected is {1}")
    public CommonAssert isCorrectTaskField(String description, String expectedFieldValue, String path) {
        String actualFieldValue = new JsonPath(response.asString()).getString(path);
        assertEquals(actualFieldValue, expectedFieldValue, description + " parameters is match: ");
        log.info(description + " is correct Actual {}, Expected {}", actualFieldValue, expectedFieldValue);
        return this;
    }

    @Step("[ASSERT] Checking task {0}, Expected is {1}")
    public CommonAssert isCorrectTaskUser(String description, String expected, String path) {
        var actual = new JsonPath(response.asString()).getList(path, String.class);
        Assertions.assertThat(actual)
                .withFailMessage("Code is not correct expected %s, actual %s", expected,
                        actual)
                .anyMatch(x -> x.equals(expected));
        return this;
    }

    @Step("[ASSERT] Checking subtask status with category: {0}, Expected: {1}")
    public CommonAssert isCorrectSubTasksStatus(String category, TaskStatuses expected) {
        var actual = new JsonPath(response.asString())
                .getList("tasks", Task.class)
                .stream()
                .filter(s -> s.getCategory().getId().equals(category))
                .map(s -> s.receiveTaskStatus())
                .collect(Collectors.toList());
        Assertions
                .assertThat(actual)
                .withFailMessage("Code is not correct expected %s, actual %s", expected, actual)
                .anyMatch(x -> x.equals(expected.toString()));
        return this;
    }

    @Step("[ASSERT] Checking subtask {0} user, Expected: {1}")
    public CommonAssert isCorrectSubTasksUser(String userType, String expected) {
        var actual = JsonPath.from(response.asString()).getList("tasks." + userType, User.class);
        Assertions
                .assertThat(actual)
                .withFailMessage("User is not correct expected %s, actual %s", expected, actual)
                .anyMatch(x -> x.getLogin().equals(expected.toString()));
        log.info("Subtask {} Actual {}, Expected {}", userType, actual.stream().map(s -> s.getLogin()).collect(Collectors.joining("|")), expected);
        return this;
    }


    @Step("[ASSERT] Checking udf memo type of {0}, Expected is {1}")
    public CommonAssert isCorrectUdfMemo(Udfs.UdfSd type, String expected) {
        var actual = extractUdfField(type, UdfMemo.class).getStringValue();
        assertEquals(actual, expected, type.udfId + " parameters is match: ");
        return this;
    }

    @Step("[ASSERT] Checking subtask created, Expected is {0}")
    public CommonAssert isSubtaskCreated(String expected) {
        var actual = new JsonPath(response.asString()).getList("tasks", Task.class);
        Assertions
                .assertThat(actual)
                .withFailMessage("Code is not correct expected %s, actual %s", expected, actual)
                .anyMatch(x -> x.getCategory().getId().equals(expected));

        log.info("Subtask created with Actual {} category, Expected {}", actual.stream().map(s -> s.getCategory().getId()).collect(Collectors.joining("|")), expected);
        return this;
    }

    @Step("[ASSERT] Checking subtask created, Expected category {0} created with {1} count")
    public CommonAssert isSubtaskCreatedWithCount(String expectedCategory, int expectedCount) {
        var actual = new JsonPath(response.asString())
                .getList("tasks", Task.class)
                .stream()
                .filter(x -> x.getCategory().getId().equals(expectedCategory))
                .collect(Collectors.toList());
        assertEquals(actual.size(), expectedCount, expectedCategory + " size is match: ");
        return this;
    }

    @Step("[ASSERT] Checking udf memo type of {0}, Expected is {1}")
    public CommonAssert isCorrectUdfString(Udfs.UdfSd type, String expected) {
        var actual = extractUdfField(type, UdfString.class).getStringValue();
        assertEquals(actual, expected, type.udfId + " parameters is match: ");
        return this;
    }

    @Step("[ASSERT] ({0}) Checking udf string type {1}, Expected is {2}")
    public CommonAssert isCorrectStringField(String description, Udfs.UdfSd type, String expected) {
        var stringType = extractUdfField(type, UdfString.class);
        assertEquals(stringType.getStringValue(), expected, description + " parameters is match: ");
        log.info(description + " is correct Actual {}, Expected {}", stringType.getStringValue(), expected);
        return this;
    }

    @Step("[ASSERT] Checking subtask status with category: {0}, Expected: {1}")
    public CommonAssert isCorrectSubTaskStatus(String category, TaskStatuses expectedStatus) {
        var tasks = new JsonPath(response.asString()).getList("tasks", Task.class);
        var actual = tasks.stream().filter(s -> s.getCategory().getId().equals(category)).findFirst().get();
        assertEquals(expectedStatus.toString(), actual.getFinishStatus().getId(), expectedStatus + " parameters is match: ");
        return this;
    }


    @Step("[ASSERT] Checking task status, Expected: {0}")
    public CommonAssert isCorrectTaskStatus(TaskStatuses expectedStatus) {
        var actualStatus = new JsonPath(response.asString()).getObject("status", Status.class);
        assertEquals(expectedStatus.toString(), actualStatus.getId(), expectedStatus + " parameters is match: ");
        return this;
    }

    @Step("[ASSERT] Checking task category, Expected: {0}")
    public CommonAssert isCorrectTaskCategory(String expectedCategory) {
        var actualStatus = new JsonPath(response.asString()).getObject("category", GeneralSlaId.class);
        assertEquals(expectedCategory, actualStatus.getId(), expectedCategory + " parameters is match: ");
        return this;
    }

    @Step("[ASSERT] Checking for create task with category: {0}")
    public CommonAssert isTaskNotCreate(String expected) {
        var actual = new JsonPath(response.asString()).getList("tasks", Task.class);
        Assertions
                .assertThat(actual)
                .withFailMessage("Code is not correct expected %s, actual %s", expected, actual)
                .anyMatch(x -> !x.getCategory().getId().equals(expected));
        return this;
    }

    @Step("[ASSERT] Checking task handler user, Expected: {0}")
    public CommonAssert isCorrectHandlerUser(String expectedLogin) {
        var task = response.as(Task.class);
        assertEquals(expectedLogin, task.getHandlerUser().getLogin(), expectedLogin + " parameters is match: ");
        return this;
    }

    @Step("[ASSERT] Checking task submitter user, Expected: {0}")
    public CommonAssert isCorrectSubmitterUser(String expectedLogin) {
        var submitterUser = new JsonPath(response.asString()).getObject("submitterUser", User.class);
        assertEquals(expectedLogin, submitterUser.getLogin(), expectedLogin + " parameters is match: ");
        log.info("Submitter is correct Actual {}, Expected {}", submitterUser.getLogin(), expectedLogin);
        return this;
    }

    @Step("[ASSERT] Checking task name, Expected: {0}")
    public CommonAssert isCorrectTaskName(String expectedName) {
        var task = response.as(Task.class);
        assertEquals(expectedName, task.getName(), expectedName + " parameters is match: ");
        return this;
    }

    @Step("[ASSERT] Checking task description, Expected: {0}")
    public CommonAssert isCorrectTaskDescription(String expectedDescription) {
        var task = response.as(Task.class);
        var actualDescription = task.getDescription().substring(0, expectedDescription.length());
        assertEquals(actualDescription, expectedDescription, expectedDescription + " parameters is match: ");
        return this;
    }

    @Step("[ASSERT] Checking is link contains task ID {}")
    public CommonAssert isCorrectTaskLink(String parentId) {
        var task = response.as(Task.class);
        var actualLink = Jsoup.parse(task.getDescription()).select("a[href]").first().attr("href");
        Assertions.assertThat(actualLink)
                .withFailMessage("Url %s is not contain task number %s", actualLink, parentId)
                .contains(parentId);
        log.info("{} link is contains correct task number {}", actualLink, parentId);
        return this;
    }

    @Step("[ASSERT] Checking udf date type of {0}, Expected is: {1}")
    public CommonAssert isCorrectUdfDate(Udfs.UdfSd type, String expectedDate) {
        String udfDate = extractUdfField(type, UdfDate.class).getDateValue();
        var actualDate = udfDate.substring(0, udfDate.indexOf('T'));
        Assertions.assertThat(actualDate)
                .withFailMessage("Date on field %s is not correct, Actual %s Expected %s", type.udfId, actualDate, expectedDate)
                .contains(expectedDate);
        log.info("{} is correct Actual {}, Expected {}", type, actualDate, expectedDate);
        return this;
    }

    @Step("[ASSERT] ({0}) Checking udf double type of {1}, Expected is {2}")
    public CommonAssert isCorrectUdfInteger(String description, Udfs.UdfSd type, Integer expected) {
        Integer actual = Objects.requireNonNull(extractUdfField(type, UdfInteger.class)).getNumberValue();
        assertEquals(actual, expected, type.udfId + " parameters is non match: ");
        log.info("{}: {} is correct Actual {}, Expected {}", description, type, actual, expected);
        return this;
    }

    @Step("[ASSERT] ({0}) Checking udf double type of {1}, Expected is {2}")
    public CommonAssert isCorrectUdfDouble(String description, Udfs.UdfSd type, Integer expected) {
        Integer actual = extractUdfField(type, UdfDouble.class).getNumberValue();
        assertEquals(actual, expected, type.udfId + " parameters is match: ");
        log.info("{}: {} is correct Actual {}, Expected {}", description, type, actual, expected);
        return this;
    }

    @Step("[ASSERT] Checking udf task type of {0} is correct, Expected: {1}")
    public CommonAssert isCorrectUdfTask(Udfs.UdfSd type, com.ts.common.entitites.commonEntities.Task.Constants expected) {
        var actual = new JsonPath(response.asString()).getObject("udfs." + type.udfId, UdfTask.class).getTaskValue();
        Assertions
                .assertThat(actual)
                .withFailMessage("Code is not correct expected %s, actual %s", expected.number, actual)
                .anyMatch(x -> x.getNumber().equals(expected.number));
        log.info("Task number is correct Actual: {}, Expected: {}"
                , Arrays.stream(actual).map(com.ts.common.entitites.commonEntities.Task::getNumber).collect(Collectors.joining("|")), expected);
        return this;
    }

    @Step("[ASSERT] Checking udf task type of {0} is correct, Expected: {1}")
    public CommonAssert isCorrectUdfTask(Udfs.UdfSd type, String expected) {
        var actual = new JsonPath(response.asString()).getObject("udfs." + type.udfId, UdfTask.class).getTaskValue();
        Assertions
                .assertThat(actual)
                .withFailMessage("Code is not correct expected %s, actual %s", expected, actual)
                .anyMatch(x -> x.getNumber().equals(expected));
        log.info("Task number is correct Actual: {}, Expected: {}"
                , Arrays.stream(actual).map(com.ts.common.entitites.commonEntities.Task::getNumber).collect(Collectors.joining("|")), expected);
        return this;
    }

    @Step("[ASSERT] Checking udf list type of {0} is correct, Expected: {1}")
    public CommonAssert isCorrectUdfList(Udfs.UdfSd type, String expected) {
        var actual = new JsonPath(response.asString()).getObject("udfs." + type.udfId, UdfList.class).getListValue();
        Assertions
                .assertThat(actual)
                .withFailMessage("Udf type %s: Code is not correct expected %s, actual %s", type.udfId, expected, Arrays.stream(actual).map(List::getId).collect(Collectors.joining("|")))
                .anyMatch(x -> x.getId().equals(expected));
        log.info("{} is correct, Actual {}, Expected {}", type.udfId, Arrays.stream(actual).map(List::getId).collect(Collectors.joining("|")), expected);
        return this;
    }


    @Step("[ASSERT] Checking response error message is correct, Expected: {0}")
    public CommonAssert isCorrectErrorMessage(String expectedMessage) {
        String actual = new JsonPath(response.asString()).getString("message");
        assertEquals(actual, expectedMessage, " parameters is match: ");
        log.info("Message is correct, Actual {}, Expected {}", actual, expectedMessage);
        return this;
    }

    @Step("[ASSERT] Checking response message {0}  is correct, Expected: {1}")
    public CommonAssert isCorrectMessageField(String field, String expectedMessage) {
        String actual = new JsonPath(response.asString()).getString("message." + field).substring(0, expectedMessage.length());
        assertEquals(actual, expectedMessage, " parameters is match: ");
        log.info("Message is correct, Actual {}, Expected {}", actual, expectedMessage);
        return this;
    }

    @Step("[ASSERT] Checking udf multi list type of {0} is correct, Expected: {1}")
    public CommonAssert isCorrectUdfMultiList(Udfs.UdfSd type, String expected) {

        var actual = extractUdfField(type, UdfMultiList.class).getListValue();
        Assertions
                .assertThat(actual)
                .withFailMessage("Code is not correct expected %s, actual %s", expected, actual)
                .anyMatch(x -> x.getId().equals(expected));
        return this;
    }

    @Step("[ASSERT] Checking udf {0} with id {1} review mode, Expected: {2}")
    public CommonAssert isCorrectReviewMode(Udfs.UdfSd type, String id, String reviewMode) {
        var actual = extractUdfField(type, UdfMultiList.class).getListValue();
        var listValue = Arrays.stream(actual).filter(x -> x.getId().equals(id)).findFirst().get();
        var actualReviewMode = new JsonPath(listValue.getUserData0()).getString("reviewmode");
        assertEquals(actualReviewMode, reviewMode, actualReviewMode + " parameters is match: ");
        return this;
    }


    @Step("[ASSERT] Checking udf {0} with id {1} PRGCode, Expected: {2}")
    public CommonAssert isCorrectPrgCode(Udfs.UdfSd type, String id, String prgCode) {
        var actual = extractUdfField(type, UdfMultiList.class).getListValue();
        var listValue = Arrays.stream(actual).filter(x -> x.getId().equals(id)).findFirst().get();
        var actualPrgCode = new JsonPath(listValue.getUserData0()).getString("prgcode");
        assertEquals(actualPrgCode, prgCode, actualPrgCode + " parameters is match: ");
        return this;
    }

    @Step("[ASSERT] Checking udfUser type of {0} is correct, Expected user: {1}")
    public CommonAssert isCorrectUdfUSer(Udfs.UdfSd type, String expected) {
        var actual = extractUdfField(type, UdfUser.class).getUserValue();
        Assertions
                .assertThat(actual)
                .withFailMessage("Code is not correct expected %s, actual %s", expected, actual)
                .anyMatch(x -> x.getLogin().equals(expected));
        return this;
    }

    @Step("[ASSERT] Checking udfUser type of {0} is correct, Expected user: {1}")
    public CommonAssert isCorrectUdfUSer(Udfs.UdfSd type, User expected) {
        User actualUser = extractUdfField(type, UdfUser.class).getUserValue()[0];
        assertEquals(actualUser.getLogin(), expected.getLogin(), "Users is not match");
        return this;
    }

    @Step("[ASSERT] Checking udf list type of {0} is correct, Expected: {1}")
    public CommonAssert isCorrectUdfListCode(Udfs.UdfSd type, String expected) {
        var actual = extractUdfField(type, UdfListAdditional.class).getListValue();
        log.info("actual: " + Arrays.toString(actual) + ", expected: " + expected);
        Assertions.assertThat(actual)
                .withFailMessage("Code is not correct expected %s, actual %s", expected, Arrays.toString(actual))
                .anyMatch(x -> x.getCode().equals(expected));
        return this;
    }

    @Step("[ASSERT] Checking udf double type of {0} is correct, Expected: {1}")
    public CommonAssert isCorrectUDfDouble(Udfs.UdfSd firstDoubleUdfType, Udfs.UdfSd secondDoubleUdfType) {
        var firstPlanBudgetValue = new JsonPath(response.asString()).getDouble("udfs." + firstDoubleUdfType + ".numberValue");
        var planBudgetValue = new JsonPath(response.asString()).getDouble("udfs." + secondDoubleUdfType + ".numberValue");
        assertEquals(firstPlanBudgetValue, planBudgetValue, "Value is not match");
        return this;
    }

    private OffsetDateTime getFormattedDate(String date) {
        Instant instant = Instant.parse(date);
        return instant.atOffset(ZoneOffset.UTC);
    }

    private Date getSimpleFormattedDate(String date) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            return dateFormat.parse(date);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    private <T extends BaseEntity> T extractUdfField(Udfs.UdfSd udfType, Class<T> type) {
        T object = null;
        try {
            object = new JsonPath(response.asString()).getObject("udfs." + udfType.udfId, type);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        Assertions.assertThat(object)
                .withFailMessage("Cannot extract object of type %s because this field don't exist", udfType.udfId)
                .isNotNull();
        log.info("Extracted field value {} of type {}", object.toString(), udfType.udfId);
        return object;
    }
}