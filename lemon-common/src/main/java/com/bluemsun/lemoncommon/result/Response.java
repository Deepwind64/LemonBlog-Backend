package com.bluemsun.lemoncommon.result;

import lombok.Data;

@Data
public class Response {
    private int code;

    private String msg;

    private Object data;

    public Response() {
        super();
    }

    public Response(int code, String msg, Object data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public static Response success(Object data) {
        return new Response(0, "success", data);
    }

    public static Response failure(int errCode, String errorMessage) {
        return new Response(errCode, errorMessage, null);
    }
}
