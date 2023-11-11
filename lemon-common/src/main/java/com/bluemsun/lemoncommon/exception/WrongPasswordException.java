package com.bluemsun.lemoncommon.exception;

import com.bluemsun.lemoncommon.enumeration.ErrorCode;

public class WrongPasswordException extends BaseException{
    public WrongPasswordException() {
    }

    public WrongPasswordException(String msg) {
        super(msg, ErrorCode.WRONG_PASSWORD.getCode());
    }
}
