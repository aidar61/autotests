package com.ts.common.enums;

public enum SlaType {

    SLA_HElP("SLAHELP"),

    SLA_BUG("SLABUG"),

    SLA_FEATURE("SLAFEATURE");
    public final String type;

    SlaType(String type) {
        this.type = type;
    }

}
