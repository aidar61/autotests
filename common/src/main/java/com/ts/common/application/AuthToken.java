package com.ts.common.application;

import lombok.Data;

/**
 * @author Aidar Askeev
 */
@Data
public class AuthToken {
    private String token;
    private String expirationTime;
    private String refreshToken;
}
