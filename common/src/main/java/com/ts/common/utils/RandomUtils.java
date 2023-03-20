package com.ts.common.utils;

import com.github.javafaker.Faker;
import com.ts.common.enums.ComSlaOperations;

import static com.ts.common.application.controllers.TrackStudioEndPoints.TEST;

public class RandomUtils {
    private static final Faker faker = new Faker();

    public static String generateName() {
        return faker.name().firstName();
    }

    public static String generateComment() {
        return faker.commerce().productName();
    }

    public static String generateDescriptionForOperation(ComSlaOperations comSlaOperations) {
        return String.format(comSlaOperations.id, TEST);
    }

    public static int generateRandomNumberBetween(int min, int max) {
        return faker.number().numberBetween(min, max);
    }
}
