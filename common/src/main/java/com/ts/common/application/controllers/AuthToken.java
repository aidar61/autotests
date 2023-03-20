package com.ts.common.application.controllers;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author Aidar Askeev
 */
@Data
@AllArgsConstructor
public class AuthToken {
    private String user;
    private String password;
}
