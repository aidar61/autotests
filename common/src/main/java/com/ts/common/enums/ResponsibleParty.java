package com.ts.common.enums;

import lombok.Getter;

public enum ResponsibleParty {
    CLIENT("dfsdf"),
    SUPPLIER("818182d33920daa3013920dde2200027"); // поставщик
    @Getter
    private final String id;

    ResponsibleParty(String id) {
        this.id = id;
    }
}
