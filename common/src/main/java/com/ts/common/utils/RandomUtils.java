package com.ts.common.utils;

import com.github.javafaker.Faker;
import com.ts.common.enums.Operations;

import static com.ts.common.application.controllers.TrackStudioEndPoints.TEST;

public class RandomUtils {
    private static final Faker faker = new Faker();

    public static String generateName() {
        return "TEST " + faker.name().firstName();
    }

    public static String generateComment() {
        return faker.commerce().productName();
    }

    public static String generateDescriptionForOperation(Operations operations) {
        return String.format(operations.id, TEST);
    }

    public static String generateCodeShortName() {
        return faker.number().digits(7);
    }

    public static String generateUrl() {
        return "htps://" + faker.internet().domainSuffix() + faker.internet().domainName() + "." + faker.internet().domainWord();
    }

    public static String generateString() {
        return faker.chuckNorris().fact();
    }

    public static String generatePrefix() {
        return faker.name().prefix().replace(".", "_").toUpperCase() + faker.name().firstName().toUpperCase();
    }

    public static String generateEmail() {
        return faker.internet().emailAddress();
    }

    public static int generateRandomNumberBetween(int min, int max) {
        return faker.number().numberBetween(min, max);
    }

    public static void main(String[] args) {
        System.out.println(generateUrl());
    }
}
