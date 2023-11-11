package com.bluemsun.lemoncommon.exception;

import com.bluemsun.lemoncommon.enumeration.ErrorCode;

public class InvalidArgsException extends BaseException{
    public InvalidArgsException() {
    }

    public InvalidArgsException(String msg) {
        super(msg, ErrorCode.FRONTEND_ERROR.getCode());
    }
}
