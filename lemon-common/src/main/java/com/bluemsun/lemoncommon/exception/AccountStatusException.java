package com.bluemsun.lemoncommon.exception;

import com.bluemsun.lemoncommon.enumeration.ErrorCode;

public class AccountStatusException extends BaseException{
    public AccountStatusException() {
    }

    public AccountStatusException(ErrorCode errorCode,String msg) {
        super(msg, errorCode.getCode());
    }
}
