package com.bluemsun.lemoncommon.enumeration;

/**
 * @author deepwind
 */

public enum ErrorCode {
    /**
     * 异常错误码
     */
    SUCCESS(0),
    INVALID_TOKEN(1001),
    PERMISSION_DENIED(1003),
    ACCOUNT_NOT_ENABLE(1004),
    ACCOUNT_FORBIDDEN(1005),
    ACCOUNT_QUIT_FOREVER(1006),
    LOGIN_TWICE(1007),
    ACCOUNT_NOT_LOGIN(1008),
    ACCOUNT_NOT_EXISTS_ERROR(1009),
    WRONG_PASSWORD(2002),
    OCCUPIED_ERROR(2004),
    EMAIL_VERIFY_ERROR(2005),
    PASSWORD_INVALID(2006),
    VERIFY_CODE_INVALID_ERROR(2007),
    WRONG_ORIGIN_EMAIL(2008),
    INVALID_FILE_ERROR(2009),
    FRONTEND_ERROR(4000),
    EMPTY_PARAM(4002),
    INVALID_RES_OPERATION(4003),
    RESOURCE_NOT_FOUND(4004),
    BACKEND_ERROR(5000),
    FILE_UPLOAD_FAILED(5001),
    SSE_ERROR(5003);


    ErrorCode(int code){
        this.code = code;
    }
    private final int code;

    public int getCode(){
        return this.code;
    }
}