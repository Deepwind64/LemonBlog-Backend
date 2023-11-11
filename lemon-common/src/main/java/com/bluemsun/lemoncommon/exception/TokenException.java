package com.bluemsun.lemoncommon.exception;

import com.bluemsun.lemoncommon.enumeration.ErrorCode;

public class TokenException extends BaseException{
    public TokenException() {
    }

    public TokenException(String msg) {
        super(msg, ErrorCode.INVALID_TOKEN.getCode());
    }
}
