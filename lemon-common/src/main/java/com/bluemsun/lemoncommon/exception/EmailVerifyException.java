package com.bluemsun.lemoncommon.exception;

import com.bluemsun.lemoncommon.enumeration.ErrorCode;

public class EmailVerifyException extends BaseException {
    public EmailVerifyException() {
    }

    public EmailVerifyException(String msg) {
        super("邮箱" + msg, ErrorCode.EMAIL_VERIFY_ERROR.getCode());
    }
}
