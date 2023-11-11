package com.bluemsun.lemoncommon.exception;

import com.bluemsun.lemoncommon.enumeration.ErrorCode;

public class WrongEmailException extends BaseException{
    public WrongEmailException() {
    }

    public WrongEmailException(String msg) {
        super(msg, ErrorCode.WRONG_ORIGIN_EMAIL.getCode());
    }
}
