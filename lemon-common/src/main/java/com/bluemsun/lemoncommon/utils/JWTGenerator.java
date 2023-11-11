package com.bluemsun.lemoncommon.utils;

import cn.hutool.jwt.JWTUtil;
import com.bluemsun.lemoncommon.constant.EncryptionConstant;

import java.io.Serial;
import java.util.HashMap;
import java.util.Map;

/**
 * @author deepwind
 */
public class JWTGenerator {
    public static String getJWT(Long expireTime, Long userId, boolean isAdmin){

        Map<String, Object> map = new HashMap<>() {
            @Serial
            private static final long serialVersionUID = 1L;
            {
                put("id", Integer.parseInt(userId.toString()));
                put("expire_time",expireTime);
                put("isAdmin",isAdmin);
            }
        };
        return JWTUtil.createToken(map, EncryptionConstant.JWT_KEY.getBytes());
    }
}
