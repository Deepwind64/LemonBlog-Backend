package com.bluemsun.lemoncommon.exception;

import com.bluemsun.lemoncommon.enumeration.ErrorCode;

/**
 * @author deepwind
 */
public class InvalidFileException extends BaseException{
    public InvalidFileException() {
    }

    public InvalidFileException(String msg) {
        super(msg, ErrorCode.INVALID_FILE_ERROR.getCode());
    }
}
