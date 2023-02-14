package com.ts.common.application.controllers;

import lombok.Data;

/**
 * @author Aidar Askeev
 */
@Data
public class AuthToken {
    private String user;
    private String password;
}
