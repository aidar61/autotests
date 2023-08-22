package com.ts.common.asserts;


import com.ts.common.config.AppConfigProvider;
import com.ts.common.entitites.commonEntities.Status;
import com.ts.common.entitites.commonEntities.Udfs;
import com.ts.common.entitites.commonEntities.User;
import com.ts.common.entitites.commonEntities.udf.*;
import com.ts.common.entitites.tasks.Task;
import com.ts.common.enums.TaskStatuses;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Date;
import java.util.stream.Collectors;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

@Slf4j
public class CommonAssert {
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

    public CommonAssert isCorrectTaskDescription(String expectedDescription, String parentId) {
        var task = response.as(Task.class);
        var actualLink = Jsoup.parse(task.getDescription()).select("a[href]").first().attr("href");
        var expectedLink = AppConfigProvider.STAND_URL + "/app/task/" + parentId;
        var actualDescription = task.getDescription().substring(0, expectedDescription.length());
        assertTrue((actualLink.equals(expectedLink) && expectedDescription.equals(actualDescription)), expectedDescription + " parameters is match: ");
        return this;
    }

    public CommonAssert isCorrectUdfDate(Udfs.UdfSd type, String date) {
        var udfDate = new JsonPath(response.asString()).getObject("udfs." + type.udfId, UdfDate.class).getDateValue();
        var udfFormattedDate = udfDate.substring(0, udfDate.indexOf('T'));
        var actual = getSimpleFormattedDate(udfFormattedDate);
        var expected = getSimpleFormattedDate(date);
        var result = actual.compareTo(expected);
        assertEquals(result, 0, type.udfId + " parameters is match: ");
        return this;
    }

    public CommonAssert isCorrectUdfDouble(Udfs.UdfSd type, Integer expected) {
        var actual = new JsonPath(response.asString()).getObject("udfs." + type.udfId, UdfDouble.class).getNumberValue();
        assertEquals(actual, expected, type.udfId + " parameters is match: ");
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
}
