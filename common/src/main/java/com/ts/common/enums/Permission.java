package com.ts.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Permission {
    VIEW_OPERATION("V"),
    PROCESS_OPERATION("A"),
    HANDLE_OPERATION("B");
    private final String permission;
}
