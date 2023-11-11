package com.bluemsun.lemoncommon.exception;

import com.bluemsun.lemoncommon.enumeration.ErrorCode;

public class AccountNotLoginException extends BaseException{
    public AccountNotLoginException() {
    }

    public AccountNotLoginException(String msg) {
        super(msg, ErrorCode.ACCOUNT_NOT_LOGIN.getCode());
    }
}
