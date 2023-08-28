package com.ts.common.asserts;


import com.ts.common.config.AppConfigProvider;
import com.ts.common.entitites.BaseEntity;
import com.ts.common.entitites.commonEntities.List;
import com.ts.common.entitites.commonEntities.Status;
import com.ts.common.entitites.commonEntities.Udfs;
import com.ts.common.entitites.commonEntities.User;
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
import static org.testng.Assert.assertTrue;

@Slf4j
public class CommonAssert {
    //TODO ВМЕСТО sout log, ПО ШАБЛОНУ метода isCorrectUdfDate(Udfs.UdfSd type, String expectedDate) или isCorrectUdfUSer(Udfs.UdfSd type, User expected)
    //TODO Стараться сравнивать объекты используя метод BaseEntity.class isEquals()
    //TODO ДОБАВИТЬ НА КАЖДЫЙ МЕТОД @STEP ПО ШАБЛОНУ :
    /**
     * @Step("[ASSERT] Checking udf type of {0}, Expected is: {1}")
     */
    private Response response;

    public CommonAssert(Response response) {
        this.response = response;
    }


    public static CommonAssert assertThat(Response response) {
        return new CommonAssert(response);
    }

    public CommonAssert isCorrectSubTasksStatus(String category, TaskStatuses expectedStatus) {
        var tasks = new JsonPath(response.asString()).getList("tasks", Task.class).stream().filter(s -> s.getCategory().getId().equals(category)).collect(Collectors.toList());
        assertTrue(tasks.stream().allMatch(s -> s.receiveTaskStatus().equals(expectedStatus.toString())), expectedStatus + " parameters is match: ");
        return this;
    }

    public CommonAssert isCorrectSubTasksSubmitUser(String expectedUserLogin) {
        var actualSubmitUsers = JsonPath.from(response.asString()).getList("tasks.submitterUser.login");
        assertTrue(actualSubmitUsers
                        .stream()
                        .allMatch(s -> s.equals(expectedUserLogin)),
                expectedUserLogin + " parameters is match: ");
        log.info("Task status is correct Actual: {}, Expected: {}"
                , actualSubmitUsers.stream().findFirst().get(), expectedUserLogin);
        return this;
    }


    public CommonAssert isCorrectUdfMemo(Udfs.UdfSd type, String expected) {
        var actual = new JsonPath(response.asString()).getObject("udfs." + type.udfId, UdfMemo.class).getStringValue();
        assertEquals(actual, expected, type.udfId + " parameters is match: ");
        return this;
    }

    public CommonAssert isCorrectSubTaskStatus(String category, TaskStatuses expectedStatus) {
        var tasks = new JsonPath(response.asString()).getList("tasks", Task.class);
        var actual = tasks.stream().filter(s -> s.getCategory().getId().equals(category)).findFirst().get();
        assertEquals(expectedStatus.toString(), actual.getFinishStatus().getId(), expectedStatus + " parameters is match: ");
        return this;
    }

    public CommonAssert isCorrectTaskStatus(TaskStatuses expectedStatus) {
        var actualStatus = new JsonPath(response.asString()).getObject("status", Status.class);
        assertEquals(expectedStatus.toString(), actualStatus.getId(), expectedStatus + " parameters is match: ");
        return this;
    }

    public CommonAssert isTaskNotCreate(String category) {
        var tasks = new JsonPath(response.asString()).getList("tasks", Task.class);
        assertTrue(!tasks.stream().anyMatch(s -> s.getCategory().getId().equals(category)), category + " is not create: ");
        return this;
    }

    public CommonAssert isCorrectHandlerUser(String expectedLogin) {
        var task = response.as(Task.class);
        assertEquals(expectedLogin, task.getHandlerUser().getLogin(), expectedLogin + " parameters is match: ");
        return this;
    }

    public CommonAssert isCorrectSubmitterUser(String expectedLogin) {
        var submitterUser = new JsonPath(response.asString()).getObject("submitterUser", User.class);
        assertEquals(expectedLogin, submitterUser.getLogin(), expectedLogin + " parameters is match: ");
        return this;
    }

    public CommonAssert isCorrectTaskName(String expectedName) {
        var task = response.as(Task.class);
        assertEquals(expectedName, task.getName(), expectedName + " parameters is match: ");
        return this;
    }

    public CommonAssert isCorrectTaskDescription(String expectedDescription) {
        var task = response.as(Task.class);
        var actualDescription = task.getDescription().substring(0, expectedDescription.length());
        assertEquals(actualDescription, expectedDescription, expectedDescription + " parameters is match: ");
        return this;
    }

    public CommonAssert isCorrectTaskLink(String parentId) {
        var task = response.as(Task.class);
        var actualLink = Jsoup.parse(task.getDescription()).select("a[href]").first().attr("href");
        var expectedLink = AppConfigProvider.STAND_URL + "/app/task/" + parentId;
        assertEquals(actualLink, expectedLink, expectedLink + " parameters is match: ");
        return this;
    }

    @Step("[ASSERT] Checking udf date type of {0}, Expected is: {1}")
    public CommonAssert isCorrectUdfDate(Udfs.UdfSd type, String expectedDate) {
        String udfDate = extractUdfField(type, UdfDate.class).getDateValue();
        var actualDate = udfDate.substring(0, udfDate.indexOf('T'));
        Assertions.assertThat(actualDate)
                .contains(expectedDate)
                .withFailMessage("Date on field %s is not correct, Actual %s Expected %s", type.udfId, actualDate, expectedDate);
        log.info("{} is correct Actual {}, Expected {}", type, actualDate, expectedDate);
        return this;
    }

    @Step("[ASSERT] Checking udf double type of {0}, Expected is {1}")
    public CommonAssert isCorrectUdfDouble(Udfs.UdfSd type, Integer expected) {
        Integer actual = extractUdfField(type, UdfDouble.class).getNumberValue();
        assertEquals(actual, expected, type.udfId + " parameters is match: ");
        log.info("{} is correct Actual {}, Expected {}", type, actual, expected);
        return this;
    }

    public CommonAssert isCorrectUdfTask(Udfs.UdfSd type, String expected) {
        var actual = new JsonPath(response.asString()).getObject("udfs." + type.udfId, UdfTask.class).getTaskValue();
        System.out.println("actual: " + actual + ", expected: " + expected);
        assertTrue(Arrays.stream(actual).anyMatch(x -> x.getId().equals(expected)), type.udfId + " parameters is match: ");
        return this;
    }

    public CommonAssert isCorrectUdfList(Udfs.UdfSd type, String expected) {
        var actual = new JsonPath(response.asString()).getObject("udfs." + type.udfId, UdfList.class).getListValue();
        System.out.println("actual: " + actual + ", expected: " + expected);
        assertTrue(Arrays.stream(actual).anyMatch(x -> x.getId().equals(expected)), type.udfId + " parameters is match: ");
        return this;
    }
    @Step("[ASSERT] Checking udf list type of {0} is correct, Expected: {1}")
    public CommonAssert isCorrectUdfList(Udfs.UdfSd type, List expected) {
        List actual = new JsonPath(response.asString()).getObject("udfs." + type.udfId, UdfList.class).getListValue()[0];
        assertTrue(actual.isEquals(expected), "List is not match");
        log.info("List is correct, Actual {}, Expected {}", actual, expected);
        return this;
    }

    public CommonAssert isCorrectUdfMultiList(Udfs.UdfSd type, String expected) {
        var actual = new JsonPath(response.asString()).getObject("udfs." + type.udfId, UdfMultiList.class).getListValue();
        System.out.println("actual: " + actual + ", expected: " + expected);
        assertTrue(Arrays.stream(actual).anyMatch(x -> x.getId().equals(expected)), type.udfId + " parameters is match: ");
        return this;
    }

    public CommonAssert isCorrectReviewMode(Udfs.UdfSd type, String id, String reviewMode) {
        var actual = new JsonPath(response.asString()).getObject("udfs." + type.udfId, UdfMultiList.class).getListValue();
        var listValue = Arrays.stream(actual).filter(x -> x.getId().equals(id)).findFirst().get();
        var actualReviewMode = new JsonPath(listValue.getUserData0()).getString("reviewmode");
        assertEquals(actualReviewMode, reviewMode, actualReviewMode + " parameters is match: ");
        return this;
    }


    public CommonAssert isCorrectPrgCode(Udfs.UdfSd type, String id, String prgCode) {
        var actual = new JsonPath(response.asString()).getObject("udfs." + type.udfId, UdfMultiList.class).getListValue();
        var listValue = Arrays.stream(actual).filter(x -> x.getId().equals(id)).findFirst().get();
        var actualPrgCode = new JsonPath(listValue.getUserData0()).getString("prgcode");
        assertEquals(actualPrgCode, prgCode, actualPrgCode + " parameters is match: ");
        return this;
    }

    public CommonAssert isCorrectUdfUSer(Udfs.UdfSd type, String expected) {
        var actual = new JsonPath(response.asString()).getObject("udfs." + type.udfId, UdfUser.class).getUserValue();
        System.out.println("actual: " + actual + ", expected: " + expected);
        assertTrue(Arrays.stream(actual).anyMatch(x -> x.getLogin().equals(expected)), type.udfId + " parameters is match: ");
        return this;
    }

    @Step("[ASSERT] Checking udfUser type of {0} is correct, Expected user: {1}")
    public CommonAssert isCorrectUdfUSer(Udfs.UdfSd type, User expected) {
        User actualUser = extractUdfField(type, UdfUser.class).getUserValue()[0];
        assertTrue(actualUser.isEquals(expected), "Users is not match");
        log.info("Users is correct Actual {}, Expected {}", actualUser, expected);
        return this;
    }

    public CommonAssert isCorrectUdfListCode(Udfs.UdfSd type, String expected) {
        var actual = new JsonPath(response.asString()).getObject("udfs." + type.udfId, UdfListAdditional.class).getListValue();
        System.out.println("actual: " + actual + ", expected: " + expected);
        assertTrue(Arrays.stream(actual).anyMatch(x -> x.getCode().equals(expected)), type.udfId + " parameters is match: ");
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

    private <T extends BaseEntity> T extractUdfField(Udfs.UdfSd udfType, Class<T> clazz) {
        T object = null;
        try {
            object = new JsonPath(response.asString()).getObject("udfs." + udfType.udfId, clazz);
            return object;
        } catch (NullPointerException e) {
            e.printStackTrace();
            log.info("Cannot extract object of type {}, because this field don't exist", udfType.udfId);
        }
        return object;
    }
}
