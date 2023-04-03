package com.ts.common.application.controllers;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * @author Aidar Askeev
 */
@Data
@AllArgsConstructor
@Builder
public class AuthToken implements Cloneable {
    private String user;
    private String password;

    public AuthToken clone() {
        try {
            return (AuthToken) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }
}
