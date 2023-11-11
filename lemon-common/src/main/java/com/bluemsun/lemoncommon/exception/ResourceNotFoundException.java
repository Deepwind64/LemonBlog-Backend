package com.bluemsun.lemoncommon.exception;

import com.bluemsun.lemoncommon.enumeration.ErrorCode;

public class ResourceNotFoundException extends BaseException{
    public ResourceNotFoundException() {
    }

    public ResourceNotFoundException(String msg) {
        super(msg, ErrorCode.RESOURCE_NOT_FOUND.getCode());
    }
}
