package com.ts.common.application;

import org.apache.commons.lang3.StringUtils;

import javax.ws.rs.core.MediaType;
import java.util.HashMap;
import java.util.Map;


import static com.ts.common.config.AppConfigProvider.BASE_URL;
import static org.apache.http.HttpHeaders.*;

/**
 * @author Aidar Askeev
 */
public class TrackStudioEndPoints {

    public static final Map<String, String> HEADERS_BASE_CONTROLLER = new HashMap<>() {{
        put(CONTENT_TYPE, MediaType.APPLICATION_JSON);
        put(HOST, getDomainName(BASE_URL));
    }};

    public static final Map<String, String> HEADERS_RESPONSE = new HashMap<>() {{
        put(CONTENT_TYPE, MediaType.APPLICATION_JSON);
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

    private static String getDomainName(String url) {
        return url.split("//")[1];
    }
}
