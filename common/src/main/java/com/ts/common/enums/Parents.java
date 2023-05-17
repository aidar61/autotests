package com.ts.common.enums;

public enum Parents {
    RYSGAL_BANK("818181df80d4285f0180f4976ad649a5", "1329503"),
    MTB("8a8181df6bb563e5016bb720665a5ce5", "925540");
    public final String id;
    public final String tuskNumber;

    Parents(String id, String tuskNumber) {
        this.id = id;
        this.tuskNumber = tuskNumber;
    }
}
