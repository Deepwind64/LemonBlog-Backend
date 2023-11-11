package com.bluemsun.lemoncommon.exception;

import com.bluemsun.lemoncommon.enumeration.ErrorCode;

public class VerifyCodeInavalidException extends BaseException{
    public VerifyCodeInavalidException() {
    }

    public VerifyCodeInavalidException(String msg) {
        super("验证码" + msg, ErrorCode.VERIFY_CODE_INVALID_ERROR.getCode());
    }
}
