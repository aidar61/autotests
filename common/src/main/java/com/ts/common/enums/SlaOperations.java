package com.ts.common.enums;

public enum SlaOperations {
    TOPRECOST("%S_TOPRECOST"); //начать предварительный анализ

    public final String id;

    SlaOperations(String id) {
        this.id = id;
    }
}
