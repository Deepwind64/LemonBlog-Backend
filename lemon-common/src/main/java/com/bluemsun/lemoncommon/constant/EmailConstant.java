package com.bluemsun.lemoncommon.constant;

import cn.hutool.core.util.StrUtil;

/**
 * @author deepwind
 */
public class EmailConstant {
    /**
     * 注册邮件验证
     * 需要参数：用户名，确认注册链接，失效时间
     */
    public static final String REGISTER_VERIFY_TITLE = "柠檬轻博客注册验证";
    public static final String REGISTER_VERIFY_CONTENT = """
            <h1>柠檬轻博客注册验证</h1>
            <p>亲爱的用户{}，</p>
            <p style="padding-left: 2em;">感谢您的注册。</p>
            <p style="padding-left: 2em;">请点击 <a href="{}" style="font-weight: 600;">确认注册</a> 以完成注册。</p>
            <p style="padding-left: 2em;">该链接失效时间为{}</p>""";
    /**
     * 修改邮件地址验证
     * 需要参数：用户名，确认注册链接，失效时间
     */
    public static final String CHANGE_EMAIL_VERIFY_TITLE = "柠檬轻博客修改邮箱地址验证";
    public static final String CHANGE_EMAIL_VERIFY_CONTENT = """
            <h1>柠檬轻博客修改邮箱地址验证</h1>
            <p>亲爱的用户{}，</p>
            <p style="padding-left: 2em;">请点击 <a href="{}" style="font-weight: 600;">确认修改</a> 以完成邮箱修改。</p>
            <p style="padding-left: 2em;">该链接失效时间为{}</p>""";
    public static final String FINDPW_VERIFY_TITLE = "柠檬轻博客找回密码验证";
    public static final String FINDPW_VERIFY_CONTENT = """
            <h1>柠檬轻博客找回密码验证</h1>
            <p>亲爱的用户{}，</p>
            <p style="padding-left: 2em;">您的验证码为</p>
            <h2 style="text-align:center;">{}</h2>
            <p style="padding-left: 2em;">如果不是您发出的找回密码请求，请忽略本邮件</p>
            <p style="padding-left: 2em;">该链接有效期为5分钟，失效时间为{}""";

    /**
     * 为验证邮件内容填充可变的部分
     * @param username
     * @param verifyLink
     * @param expireTime
     * @return
     */
    public static String getRegisterVerifyContent(String username, String verifyLink, String expireTime) {
        return StrUtil.format(REGISTER_VERIFY_CONTENT,username,verifyLink,expireTime);
    }
    public static String getChangeEmailVerify(String username, String verifyLink, String expireTime) {
        return StrUtil.format(CHANGE_EMAIL_VERIFY_CONTENT,username,verifyLink,expireTime);
    }
    public static String getFindPwVerify(String username, String verifyCode, String expireTime) {
        return StrUtil.format(FINDPW_VERIFY_CONTENT,username,verifyCode,expireTime);
    }

}
