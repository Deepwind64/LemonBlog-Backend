package com.bluemsun.lemoncommon.exception;

import com.bluemsun.lemoncommon.enumeration.ErrorCode;

public class PasswordInvalidException extends BaseException{
    public PasswordInvalidException() {
    }
    public PasswordInvalidException(String msg) {
        super(msg, ErrorCode.PASSWORD_INVALID.getCode());
    }
}
