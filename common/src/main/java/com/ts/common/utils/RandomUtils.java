package com.ts.common.utils;

import com.github.javafaker.Faker;

public class RandomUtils {
    private static final Faker faker = new Faker();

    public static String generateName() {
        return faker.name().firstName();
    }
}
