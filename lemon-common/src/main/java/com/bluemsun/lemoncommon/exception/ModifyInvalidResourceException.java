package com.bluemsun.lemoncommon.exception;

import com.bluemsun.lemoncommon.enumeration.ErrorCode;

public class ModifyInvalidResourceException extends BaseException{
    public ModifyInvalidResourceException() {
    }

    public ModifyInvalidResourceException(String msg) {
        super(msg, ErrorCode.INVALID_RES_OPERATION.getCode());
    }
}
