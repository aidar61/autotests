package com.ts.common.enums;

import lombok.Getter;

public enum Resolutions {
    RESOLUTION_DEPENDS_ON_ANOTHER_TASK("818181a82399260001239ee1001409b1"),
    RESOLUTION_AWAITS_UNTIL_DATE("818181a820cd840f0120f7c2ae8b3748"),

    //Работа приостановлена на неопределённый срок
    RESOLUTION_WORK_SUSPENDED_INDEFINITELY("818181a820cd840f0120f7c39810374c"),
    ANSWER_IN_CODE("818184d9750684970175072aa59f0003"),
    CANNOT_BE_COMPLETED_WITHIN_THE_SPECIFIED_TIME_FRAME("818181a81faa73ff011fad7b2473015f"),
    NOT_IN_MY_PROFESSIONAL_SKILL("818181a81faa73ff011fad7b0c38015e"),
    CANNOT_ANSWER("818181872b473a3e012b5309e71914ac");

    @Getter
    private final String id;

    Resolutions(String id) {
        this.id = id;
    }
}
