package com.bluemsun.lemoncommon.exception;

import com.bluemsun.lemoncommon.enumeration.ErrorCode;

/**
 * 业务异常
 */
public class BaseException extends RuntimeException {
    private int errorCode;

    public BaseException() {
    }

    public BaseException(String msg, int errorCode) {
        super(msg);
        this.errorCode = errorCode;
    }

    public int getErrorCode() {
        return errorCode;
    }
    public void setErrorCode(int code){
        this.errorCode = code;
    }
}
