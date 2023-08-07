package com.ts.common.asserts;

import com.ts.common.entitites.commonEntities.Udfs;
import com.ts.common.entitites.commonEntities.udf.*;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Date;

import static org.testng.Assert.assertTrue;


public class UdfAssert {
    private Response response;

    public UdfAssert(Response response) {
        this.response = response;
    }

    public static UdfAssert assertThat(Response response) {
        return new UdfAssert(response);
    }

    public UdfAssert isCorrectUdfMemo(Udfs.UdfSd type, String expected) {
        var actual = new JsonPath(response.asString()).getObject("udfs." + type.udfId, UdfMemo.class).getStringValue();
        assertTrue(actual.equals(expected), type.udfId + " parameters is match: ");
        return this;
    }

    public UdfAssert isCorrectUdfDate(Udfs.UdfSd type, String date) {
        var udfDate = new JsonPath(response.asString()).getObject("udfs." + type.udfId, UdfDate.class).getDateValue();
        var udfFormattedDate = udfDate.substring(0, udfDate.indexOf('T'));
        var actual = getSimpleFormattedDate(udfFormattedDate);
        var expected = getSimpleFormattedDate(date);
        var result = actual.compareTo(expected);
        assertTrue(result == 0, type.udfId + " parameters is match: ");
        return this;
    }

    public UdfAssert isCorrectUdfDouble(Udfs.UdfSd type, double expected) {
        var actual = new JsonPath(response.asString()).getObject("udfs." + type.udfId, UdfDouble.class).getNumberValue();
        assertTrue(actual == expected, type.udfId + " parameters is match: ");
        return this;
    }

    public UdfAssert isCorrectUdfTask(Udfs.UdfSd type, String expected) {
        var actual = new JsonPath(response.asString()).getObject("udfs." + type.udfId, UdfTask.class).getTaskValue();
        System.out.println("actual: " + actual + ", expected: " + expected);
        assertTrue(Arrays.stream(actual).anyMatch(x -> x.getId().equals(expected)), type.udfId + " parameters is match: ");
        return this;
    }

    public UdfAssert isCorrectUdfList(Udfs.UdfSd type, String expected) {
        var actual = new JsonPath(response.asString()).getObject("udfs." + type.udfId, UdfList.class).getListValue();
        System.out.println("actual: " + actual + ", expected: " + expected);
        assertTrue(Arrays.stream(actual).anyMatch(x -> x.getId().equals(expected)), type.udfId + " parameters is match: ");
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
