package com.bluemsun.lemoncommon.exception;

import com.bluemsun.lemoncommon.enumeration.ErrorCode;

public class PermissionDeniedException extends BaseException{
    public PermissionDeniedException() {
    }

    public PermissionDeniedException(String msg) {
        super(msg, ErrorCode.PERMISSION_DENIED.getCode());
    }
}
