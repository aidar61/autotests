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
//        put(AUTHORIZATION, "Basic cm9vdDpwYXNzd29yZA==");
        put(CONTENT_TYPE, APPLICATION_JSON);
        put(CACHE_CONTROL, "no-cache");
        put(HOST, getDomainName(AppConfigProvider.STAND_URL));
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
    public static final String TASKS = "tasks";
    public static final String TAG = "tag";
    public static final String UPDATE = "update";
    public static final String INFO = "info";
    public static final String CREATE = "create";
    public static final String OPERATION = "operation";
    public static final String OPERATIONS = "operations";
    public static final String NUMBER = "";
    public static final String ID = "";
    public static final String MSG = "MSG_";
    public static final String TEST = "TEST";
    public static final String REST = "rest";
    public static final String WORKFLOW = "workflow";
    public static final String STATUS = "status";
    public static final String M_STATUSES = "mstatuses";
    public static final String M_STATUS = "mstatus";
    public static final String PERMISSIONS = "permissions";
    public static final String TRANSITION = "transition";
    public static final String PORTLET = "portlet";
    public static final String PERSONAL_QUEUE = "personalqueue";
    public static final String SLA_FEATURE_CONTROL = "slafeaturescontrol";
    public static final String GET_SLA_FEATURE_CONTROL = "getFeaturesControl";
    public static final String APP = "app";
    public static final String ACL = "acl";
    public static final String EFFECTIVE = "effective";
    public static final String TO_TASK = "toTask";
    public static final String UDF_VAL = "udfval";
    public static final String USER = "user";
    public static final String LIST = "list";
    public static final String SAVE = "save";
    public static final String UDF = "udf";
    public static final String LIST_VALUES = "listvalues";

    private static String getDomainName(String url) {
        String s = url.split("//")[1];
        String spl = s.split("/")[0];
        log.info("Host is: {}", spl);
        return spl;
    }
}
