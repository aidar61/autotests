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
public class AuthToken {
    private String user;
    private String password;
}
