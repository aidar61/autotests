package com.ts.common.application.controllers;

import com.ts.common.config.AppConfigProvider;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.WILDCARD;
import static org.apache.http.HttpHeaders.*;

/**
 * @author Aidar Askeev
 */
@Slf4j
public class TrackStudioEndPoints {

    public static final Map<String, String> HEADERS_BASE_CONTROLLER = new HashMap<>() {{
        put(AUTHORIZATION, "Basic cm9vdDpwYXNzd29yZA==");
        put(CONTENT_TYPE, APPLICATION_JSON);
        put(CACHE_CONTROL, "no-cache");
        put(HOST, getDomainName(AppConfigProvider.BASE_URL));
        put(ACCEPT_ENCODING, "gzip, deflate, br");
        put(ACCEPT, WILDCARD);
    }};

    public static final Map<String, String> HEADERS_RESPONSE = new HashMap<>() {{
        put(CONTENT_TYPE, APPLICATION_JSON);
        put(DATE, StringUtils.EMPTY);
        put(VARY, StringUtils.EMPTY);
    }};

    public static final Map<String, String> HEADERS_EMPTY_RESPONSE = new HashMap<>() {{
        put(DATE, StringUtils.EMPTY);
        put(VARY, StringUtils.EMPTY);
        put(CONTENT_LENGTH, StringUtils.EMPTY);
    }};

    public static final Map<String, String> HEADERS_DELETE_RESPONSE = new HashMap<>() {{
        put(DATE, StringUtils.EMPTY);
        put(VARY, StringUtils.EMPTY);
        put(SERVER, StringUtils.EMPTY);
    }};

    public static final String AUTHORIZATION_HEADER = AUTHORIZATION;
    public static final String TASK = "task";
    public static final String UPDATE = "update";
    public static final String INFO = "info";
    public static final String CREATE = "create";
    public static final String OPERATION = "operation";
    public static final String NUMBER = "";
    public static final String ID = "";
    public static final String MSG = "MSG_";

    private static String getDomainName(String url) {
        String s = url.split("//")[1];
        String spl = s.split("/")[0];
        log.info("Host is: {}", spl);
        return spl;
    }
}
