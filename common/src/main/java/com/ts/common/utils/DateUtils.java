package com.ts.common.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateUtils {
    public static String getCurrentDate(int days) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        if (days > 0) {
            return LocalDateTime.now().plusDays(days).format(dateTimeFormatter);
        } else {
            return LocalDate.now().format(dateTimeFormatter);
        }
    }
}
