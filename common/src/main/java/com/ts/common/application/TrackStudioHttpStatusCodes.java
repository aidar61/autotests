package com.ts.common.application;

import lombok.Getter;
import org.apache.http.HttpStatus;

@Getter
public enum TrackStudioHttpStatusCodes {
    HTTP_OK(HttpStatus.SC_OK),
    HTTP_CREATED(HttpStatus.SC_CREATED),
    HTTP_NO_CONTENT(HttpStatus.SC_NO_CONTENT),
    HTTP_BAD_REQUEST(HttpStatus.SC_BAD_REQUEST),
    HTTP_NOT_FOUND(HttpStatus.SC_NOT_FOUND),
    HTTP_NOT_ALLOWED(HttpStatus.SC_METHOD_NOT_ALLOWED);
    private int value;

    TrackStudioHttpStatusCodes(int value) {
        this.value = value;
    }
}