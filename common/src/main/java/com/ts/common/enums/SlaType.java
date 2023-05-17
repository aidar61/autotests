package com.ts.common.enums;

public enum SlaType {

    SLA_HElP("SLAHELP"),

    SLA_BUG("SLABUG"),

    SLA_FEATURE("SLAFEATURE"),
    POTENTIAL_GAP("POTENTIALGAP"),
    GAP("GAP");
    public final String type;

    SlaType(String type) {
        this.type = type;
    }

}
