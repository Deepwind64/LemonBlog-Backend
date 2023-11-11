package com.bluemsun.lemonserver.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bluemsun.lemoncommon.constant.MessageConstant;
import com.bluemsun.lemoncommon.context.UserContext;
import com.bluemsun.lemoncommon.exception.PermissionDeniedException;
import com.bluemsun.lemonpojo.dto.*;
import com.bluemsun.lemonpojo.entity.User;

/**
 * @author deepwind
 */
public interface UserService extends IService<User> {
    /**
     * 数据检查，保存用户注册数据至数据库
     * @param userRegisterDTO
     * @return 用户id
     */
    long register(UserRegisterDTO userRegisterDTO);

    boolean checkPassword(String pw);

    /**
     * 邮箱登录，进行数据校验，更新登录状态
     * @param userEmailLoginDTO
     * @return 用户id
     */
    long login(UserEmailLoginDTO userEmailLoginDTO);
    /**
     * 邮箱登录，进行数据校验，更新登录状态
     * @param userNameLoginDTO
     * @return 用户id
     */
    long login(UserNameLoginDTO userNameLoginDTO);

    /**
     * 校验验证码
     *
     * @param verifyCode 待校验验证码
     * @param captchaOwner 验证码所有者
     * @return 验证码正确为true，反之为false
     */
    boolean checkVerifyCode(String verifyCode, String captchaOwner);

    void updateName(long userId, String name);

    void updateEmail(long userId, UpdateEmailDTO updateEmailDTO);

    void updatePassword(long userId, UpdatePasswordDTO updatePasswordDTO);

    void logout(long userId);

    /**
     * 权限校验函数，进行访问者身份判断
     * @param ownerId 资源所有者id
     */
    static void userAuthenticate(long ownerId) {
        // 能过token校验，用户必然存在
        if(UserContext.isAdmin()){
            return;
        }
        if(UserContext.getUserId()==ownerId){
            return;
        }
        throw new PermissionDeniedException(MessageConstant.PERMISSION_DENIED);
    }
    User checkEmail(String email);
    boolean isUserExistedById(long userId);

    void banUser(Long userId, Integer time, String reason);

    void recoverUser(Long userId);
}
