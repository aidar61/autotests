package com.ts.common.enums;

public enum ComSlaOperations {
    CAT("CAT_%S", "update"),
    REMOVE_REQUEST("%S_REMOVEREQUEST", "operation");
    public final String id;
    public final String type;

    ComSlaOperations(String id, String type) {
        this.id = id;
        this.type = type;
    }
}
