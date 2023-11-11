package com.bluemsun.lemoncommon.exception;

import com.bluemsun.lemoncommon.enumeration.ErrorCode;

public class AlreadyLoginException extends BaseException{
    public AlreadyLoginException() {
    }

    public AlreadyLoginException(String msg) {
        super(msg,ErrorCode.LOGIN_TWICE.getCode());
    }
}
