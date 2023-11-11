package com.bluemsun.lemoncommon.context;

import com.bluemsun.lemoncommon.constant.MessageConstant;
import com.bluemsun.lemoncommon.exception.TokenException;
import com.bluemsun.lemoncommon.context.dto.BaseUserDTO;
import org.springframework.stereotype.Component;

/**
 * 用户信息上下文，用于把token中的userId带回
 * @author deepwind
 */
@Component
public class UserContext {
    private static final ThreadLocal<BaseUserDTO> user = new ThreadLocal<>();

    private UserContext() {
    }

    public static Long getUserId() {
        return getUser().getUserId();
    }
    public static boolean isAdmin(){
        return getUser().getIsAdmin();
    }

    public static BaseUserDTO getUser() {
        BaseUserDTO baseUser = user.get();
        if (null == baseUser) {
            throw new TokenException(MessageConstant.TOKEN_INVALID);
        }
        return baseUser;
    }
    public static void setBaseUser(BaseUserDTO baseUser) {
        user.set(baseUser);
    }

    public static void remove() {
        user.remove();
    }

}