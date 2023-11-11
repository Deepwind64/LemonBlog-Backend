package com.bluemsun.lemoncommon.enumeration;

/**
 * @author deepwind
 */

public enum BlogStatus {
    /**
     * 博客状态
     */
    UNCHECK(0,"未审核"),
    PASS(1, "审核通过");
    BlogStatus(int code, String msg){
        this.msg = msg;
        this.code = code;
    }
    private final String msg;
    private final int code;
    public String getMsg(){
        return this.msg;
    }
    public int getCode(){return this.code;}
}
