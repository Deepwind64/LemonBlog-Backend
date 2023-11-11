package com.bluemsun.lemoncommon.exception;

import com.bluemsun.lemoncommon.constant.MessageConstant;
import com.bluemsun.lemoncommon.enumeration.ErrorCode;

/**
 * @author deepwind
 */
public class OccupiedException extends BaseException{
    public OccupiedException() {
    }

    public OccupiedException(String msg) {
        super(msg +" "+MessageConstant.ALREADY_EXISTS, ErrorCode.OCCUPIED_ERROR.getCode());
    }

}
