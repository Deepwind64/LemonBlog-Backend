package com.bluemsun.lemoncommon.constant;

public class HTTPConstant {
    /**
     * Cookie 上下文路径，用于控制cookie路径
     */
    public static final String COOKIE_CONTEXT_PATH = "/lemon-blog/";
    public static final String[] NO_TOKEN_PATH = {"/user/login",
            "/user/register",
            "/user/register/*/*",
            "/user/verifyCode",
            "/user/email/*/*",
            "/user/email/check",
            "/user/password/verifyCode",
            "/user/password/recovery",
            "/user/test",
            "/userinfo/*/blog",
            "/blog/basicInfo/*",
            "/blog/view/*",
            "/blog/*/comments",
            "/column/user/*",
            "/column/*/view",
            "/column/label",
            "/admin/login"};
    public static final String[] TOKEN_CHOOSE_PATH = {"/userinfo/*",
    "/userinfo/avatar/*"};
}
