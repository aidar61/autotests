package com.ts.common.enums;

public enum TaskType {

    SLA_HElP("SLAHELP"),

    SLA_BUG("SLABUG"),

    SLA_FEATURE("SLAFEATURE"),
    POTENTIAL_GAP("POTENTIALGAP"),
    SOL_SELECTED("SOLSELECTED"),
    GAP_SOLUTION("GAPSOLUTION"),
    GAP("GAP"),
    RELEASE_MODULE("RELEASEMODULE"),
    RELEASE("RELEASE");
    public final String type;

    TaskType(String type) {
        this.type = type;
    }

}
