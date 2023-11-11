package com.bluemsun.lemoncommon.constant;

/**
 * @author deepwind
 */
public class EncryptionConstant {
    /**
     * jwt/token默认为1天过期，单位ms
     * 1000 * 60 * 60 * 24 * 15
     */
    public static final int JWT_EXPIRE_TIME = 1000 * 60 * 60 * 24;
    /**
     * 登录图片验证码过期时间默认为2分钟过期，单位 s
     * 120 s
     */
    public static final int VERIFY_CODE_EXPIRE_TIME = 60 * 2;
    public static final String JWT_KEY = "IWONDERHOWTOSOLVEIT";
    /**
     * 找回密码——验证码，有效期5分钟
     */
    public static final int FIND_PW_VERIFY_CODE_EXPIRE_TIME = 1000*60*5;


}
