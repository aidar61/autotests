package com.ts.common.enums;

public enum Parents {
    RYSGAL_BANK("818181df80d4285f0180f4976ad649a5", "1329503"),
    RELEASE("818181df584b901d01584d61ecfd7c17", "592578"),
    OPERATION_ACC_CLIENT("818181b03c7fc013013c7fca5f5b0359", "186585"),
    MTB("8a8181df6bb563e5016bb720665a5ce5", "925540");
    public final String id;
    public final String tuskNumber;

    Parents(String id, String tuskNumber) {
        this.id = id;
        this.tuskNumber = tuskNumber;
    }
}
