package com.bluemsun.lemoncommon.exception;

import com.bluemsun.lemoncommon.enumeration.ErrorCode;

public class SseException extends BaseException{
    public SseException() {
    }

    public SseException(String msg) {
        super(msg, ErrorCode.SSE_ERROR.getCode());
    }
}
