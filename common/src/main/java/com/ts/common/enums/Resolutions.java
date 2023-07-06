package com.ts.common.enums;

import lombok.Getter;

public enum Resolutions {
    ANSWER_IN_CODE("818184d9750684970175072aa59f0003"),
    CANNOT_ANSWER("818181872b473a3e012b5309e71914ac");

    @Getter
    private final String id;

    Resolutions(String id) {
        this.id = id;
    }
}
