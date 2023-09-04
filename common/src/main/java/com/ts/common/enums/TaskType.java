package com.ts.common.enums;

public enum TaskType {

    SLA_HElP("SLAHELP"),

    SLA_BUG("SLABUG"),

    SLA_FEATURE("SLAFEATURE"),
    POTENTIAL_GAP("POTENTIALGAP"),
    SOL_SELECTED("SOLSELECTED"),
    GAP_SOLUTION("GAPSOLUTION"),
    SD_HELP("SDHELP"),
    GAP("GAP"),
    ADVICE("ADVICE"),
    SANCTION("SANCTION"),
    CONFIRMATION("CONFIRMATION"),
    RELEASE_MODULE("RELEASEMODULE"),
    DEV_TASK("DEVTASK"),
    WORK_TASK("WORKTASK"),
    BUG_TASK("BUGTASK"),

    RELEASE("RELEASE");

    public final String type;

    TaskType(String type) {
        this.type = type;
    }

    public enum WorkTask {
        ADMIN_JOB("ADMINJOB"),
        OUT_COMMUNICATION("OUTCOMMUNICATION"),
        IN_COMMUNICATION("INCOMMUNICATION"),
        HELP_DESK_TASK("HELPDESKTASK"),
        LOCALIZATION("LOCALIZATION"),
        MIGRATION("MIGRATION"),
        AUTOTEST("AUTOTEST"),
        ANAL_TASK("ANALTASK"),
        CONSTR_ANAL_TASK("CONSTRANALTASK"),
        ACCEPT_TASK("ACCEPTTASK"),
        DEV_AUTOTEST("DEVAUTOTEST"),
        REG_TEST_TASK("REGTESTTASK"),
        TEST_TASK("TESTTASK"),
        QA_JOB("QAJOB"),
        MAINTENANCE("MAINTENANCE"),
        INFRASTRUCTURE("INFRASTRUCTURE"),
        CONSTRUCTOR("CONSTRUCTOR"),
        TEACH_CLIENT("TEACHCLIENT"),
        TEACH_ROLE("TEACHROLE"),
        FIRST_ANAL("FIRSTANAL"),
        BUSINESS_PROC("BUSINESPROC"),
        SUPPORT("SUPPORT"),
        PO_PROJECT("POPROJECT"),
        PERSONAL_JOB("PERSONALJOB"),
        BUSINESS_ANAL("BUSINESSANAL"),
        MANAGERS("MANAGERS"),
        DOC_TASK("DOCTASK");
        public final String type;

        WorkTask(String type) {
            this.type = type;
        }
    }


}
