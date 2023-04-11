package com.ts.common.application.errors;

import lombok.Getter;

public enum TrackStudioErrors {
    REQUEST_INFO("Operation MSG_SLAHELP_REQUESTINFO not allowed for task #%s in state STATUS_SLAHELP_NEW by @root.");

    @Getter
    public final String value;

    TrackStudioErrors(String value) {
        this.value = value;
    }
}
