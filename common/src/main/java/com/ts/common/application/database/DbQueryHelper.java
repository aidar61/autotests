package com.ts.common.application.database;

public class DbQueryHelper {
    public enum Operators {
        OR("OR"),
        AND("AND"),
        EQUAL("="),
        LIKE("LIKE");

        public final String operator;

        Operators(String operator) {
            this.operator = operator;
        }
    }
}
