package com.bluemsun.lemoncommon.exception;

import com.bluemsun.lemoncommon.enumeration.ErrorCode;

public class AccountNotExistsException extends BaseException{
    public AccountNotExistsException() {
    }

    public AccountNotExistsException(String msg) {
        super(msg,ErrorCode.ACCOUNT_NOT_EXISTS_ERROR.getCode());
    }
}
