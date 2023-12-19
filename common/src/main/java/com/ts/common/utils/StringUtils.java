package com.ts.common.utils;

import lombok.extern.slf4j.Slf4j;

import java.util.regex.Pattern;

@Slf4j
public class StringUtils {
    public static String getTaskNumber(String value) {
        var result = "";
        // Регулярное выражение для извлечения чисел в квадратных скобках
        var pattern = Pattern.compile("#(\\d+)|\\[(\\d+)]");
        var matcher = pattern.matcher(value);

        // Поиск совпадений
        while (matcher.find()) {
            result = matcher.group(1);
        }

        return result;
    }
}
