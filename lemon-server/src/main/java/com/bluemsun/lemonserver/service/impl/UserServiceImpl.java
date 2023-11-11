package com.bluemsun.lemonserver.service.impl;

import cn.hutool.core.date.BetweenFormatter;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.extra.mail.MailUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bluemsun.lemoncommon.constant.*;
import com.bluemsun.lemoncommon.enumeration.ErrorCode;
import com.bluemsun.lemoncommon.enumeration.UserAccountStatus;
import com.bluemsun.lemoncommon.enumeration.UserLoginStatus;
import com.bluemsun.lemoncommon.exception.*;
import com.bluemsun.lemonpojo.dto.*;
import com.bluemsun.lemonpojo.entity.Message;
import com.bluemsun.lemonpojo.entity.Userinfo;
import com.bluemsun.lemonpojo.entity.User;
import com.bluemsun.lemonserver.dao.UserMapper;
import com.bluemsun.lemonserver.service.MessageService;
import com.bluemsun.lemonserver.service.UserService;
import com.bluemsun.lemonserver.service.UserinfoService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper,User> implements UserService {
    @Resource
    UserMapper userMapper;
    @Resource
    UserinfoService userinfoService;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Resource
    MessageService messageService;
    @Override
    public long register(UserRegisterDTO userRegisterDTO) {
        String email = userRegisterDTO.getEmail();
        String name = userRegisterDTO.getName();
        String password = userRegisterDTO.getPassword();
        // FIXME 加入 not enable 判断，否则会造成死账户问题
        // 数据存在性校验
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_email",email);
        if (getOne(queryWrapper)!=null){
            // Email Occupied
            throw new OccupiedException(MessageConstant.EMAIL + email);
        }
        queryWrapper.clear();
        queryWrapper.eq("user_name",name);
        if (getOne(queryWrapper)!=null){
            // Name Occupied
            throw new OccupiedException(MessageConstant.NAME + name);
        }
        // 密码合法性校验，合法则保存
        if (checkPassword(password)) {
            // TODO 密码加密
            User user = User.builder()
                    .email(email)
                    .name(name)
                    .password(SecureUtil.sha1(password))
                    .accountStatus(UserAccountStatus.NOT_ENABLE.ordinal())
                    .build();
            save(user);
            long userId = user.getId();
            // TODO 在注册时创建信息记录
            Userinfo userinfo = Userinfo.builder().
                    userId(userId).
                    avatarId(DefaultConstant.DEFAULT_AVATAR_ID).
                    build();
            userinfoService.save(userinfo);
            log.info("注册：用户id: {}, 昵称: {}, 邮箱: {}", user.getId(), name, email);
            return userId;
        }
        return -1;
    }
    @Override
    public boolean checkVerifyCode(String verifyCode, String captchaOwner) {
        String captchaCode = (String) redisTemplate.opsForValue().get(captchaOwner);
        if (captchaCode!=null && !"".equals(captchaCode)){
            // 使用后废弃验证码
            redisTemplate.delete(captchaOwner);
        }
        if(verifyCode.equals(captchaCode)){
            return true;
        }
        throw new VerifyCodeInavalidException(MessageConstant.VERIFY_FAILED);
    }
    @Override
    public boolean checkPassword(String pw){
        // 至少8位密码，要求包含数字和字母
        if (pw.length() >= 8 && pw.matches(".*\\d+.*") && pw.matches(".*[a-zA-Z]+.*")){return true;}
        else {
            // Invalid Password
            throw new PasswordInvalidException(MessageConstant.PASSWORD_INVALID);
        }
    }

    @Override
    public long login(UserEmailLoginDTO userEmailLoginDTO) {
        // 按邮箱查询用户是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_email",userEmailLoginDTO.getEmail());
        User user = userMapper.selectOne(queryWrapper);
        String password = userEmailLoginDTO.getPassword();
        return universalLogin(user, password);
    }
    @Override
    public long login(UserNameLoginDTO userNameLoginDTO) {
        // 按用户名查询用户是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_name",userNameLoginDTO.getName());
        User user = userMapper.selectOne(queryWrapper);
        String password = userNameLoginDTO.getPassword();
        return universalLogin(user, password);
    }
    private long universalLogin(User user, String password){
        if (user==null){
            throw new AccountNotExistsException(MessageConstant.ACCOUNT_NOT_FOUND);
        }
        // 阻止重复登录
//        if (redisTemplate.opsForValue().get(user.getId().toString())!=null){
//            throw new AlreadyLoginException("用户"+user.getName()+MessageConstant.ALREADY_LOGIN);
//        }

        // 存在则校验密码
        if (verifyPassword(user,password)){
            // 检查账户状态
            if (checkAccountStatus(user)){
                // 更新登录状态
                user.setLastLoginTime(new Date());
                user.setLoginStatus(UserLoginStatus.LOGIN_IN.ordinal());
                userMapper.updateById(user);
                return user.getId();
            }
        }
        return -1;
    }
    private boolean checkAccountStatus(User user){
        int status;
        if(redisTemplate.opsForValue().get(RedisConstant.BAN_PREFIX + user.getId())==null){
            status = user.getAccountStatus();
            if(status==UserAccountStatus.FORBIDDEN.ordinal()){
                // redis无数据说明已解封，修改账户状态为正常
                user.setAccountStatus(UserAccountStatus.NORMAL.ordinal());
                updateById(user);
                status = UserAccountStatus.NORMAL.ordinal();
            }
        }else{
            // 发现在redis中有封禁数据
            status = UserAccountStatus.FORBIDDEN.ordinal();
        }
        UserAccountStatus userAccountStatus = UserAccountStatus.class.getEnumConstants()[status];
        switch (userAccountStatus){
            case NORMAL -> {
                return true;
            }
            case NOT_ENABLE -> {
                throw new AccountStatusException(ErrorCode.ACCOUNT_NOT_ENABLE,MessageConstant.ACCOUNT_NOT_ENABLE);
            }
            case FORBIDDEN -> {
                Long between = redisTemplate.opsForValue().getOperations().getExpire(RedisConstant.BAN_PREFIX + user.getId().toString());
                String message = StrUtil.format(MessageConstant.ACCOUNT_FORBIDDEN,DateUtil.formatBetween(between, BetweenFormatter.Level.MINUTE));
                throw new AccountStatusException(ErrorCode.ACCOUNT_FORBIDDEN,message);
            }
            case QUIT_FOREVER -> {
                throw new AccountStatusException(ErrorCode.ACCOUNT_QUIT_FOREVER,MessageConstant.ACCOUNT_QUIT_FOREVER);
            }
            default -> {
                return false;
            }
        }
    }

    @Override
    public void updateName(long userId, String name) {
        User user = userMapper.selectById(userId);
        // 检查是否存在重名
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_name",name);
        User nameCheckUser = userMapper.selectOne(queryWrapper);
        if (nameCheckUser!=null){
            throw new OccupiedException(MessageConstant.NAME);
        }
        // 更新用户名
        user.setName(name);
        userMapper.updateById(user);
    }

    @Override
    public void updateEmail(long userId, UpdateEmailDTO updateEmailDTO) {
        User user = userMapper.selectById(userId);
        String originEmail = updateEmailDTO.getOriginEmail();
        String newEmail = updateEmailDTO.getNewEmail();
        // 看看源邮箱和密码是不是对的上
        if (verifyEmail(user, originEmail) && verifyPassword(user, updateEmailDTO.getPassword())){
            // FIXME 解决上一次邮箱链接还未点击就发送下一个链接造成的冲突问题
            // FIXME 老邮箱未验证可能导致盗号风险
            // 检查是否存在重复邮箱
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("user_email",newEmail);
            User nameCheckUser = userMapper.selectOne(queryWrapper);
            if (nameCheckUser!=null){
                throw new OccupiedException(MessageConstant.EMAIL);
            }
            // 用uuid标识，有效期一天
            String uuid = IdUtil.simpleUUID();
            redisTemplate.opsForValue().set(uuid,newEmail);
            DateTime failureTime = DateUtil.tomorrow();

            // 发送验证邮件
            String name = user.getName();
            String varifyLink =UrlConstant.EMAIL_SERVER_LOCATION+"/user/email/"+userId+"/"+uuid;
            MailUtil.send(newEmail, EmailConstant.CHANGE_EMAIL_VERIFY_TITLE,
                    EmailConstant.getChangeEmailVerify(name,varifyLink, DateUtil.formatDateTime(failureTime)),true);

            log.info(StrUtil.format("注册验证码已发送至{}",newEmail));
        }
    }
    private boolean verifyPassword(User user, String password){
        if (user.getPassword().equals(SecureUtil.sha1(password))){return true;}
        else{
            throw new WrongPasswordException(MessageConstant.PASSWORD_ERROR);
        }
    }
    private boolean verifyEmail(User user, String email){
        if (user.getEmail().equals(email)){return true;}
        else{
            throw new WrongEmailException(MessageConstant.ORIGIN_EMAIL_ERROR);
        }
    }

    @Override
    public User checkEmail(String email) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_email",email);
        User user = userMapper.selectOne(queryWrapper);
        if(user==null){
            throw new AccountNotExistsException(MessageConstant.ACCOUNT_NOT_FOUND);
        }
        return user;
    }

    @Override
    public void updatePassword(long userId, UpdatePasswordDTO updatePasswordDTO) {
        User user = userMapper.selectById(userId);
        String oldPassword = updatePasswordDTO.getOriginPassword();
        String newPassword = updatePasswordDTO.getNewPassword();
        // 检查原密码对错和新密码合法性
        if (verifyPassword(user,oldPassword) && checkPassword(newPassword)){
            user.setPassword(SecureUtil.sha1(newPassword));
            userMapper.updateById(user);
        }
    }

    @Override
    public boolean isUserExistedById(long userId) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id",userId);
        return userMapper.exists(queryWrapper);
    }

    @Override
    public void logout(long userId) {
        // 修改登录状态
        User user = userMapper.selectById(userId);
        if (user!=null){
            if (user.getLoginStatus()==UserLoginStatus.LOGIN_OUT.ordinal()) {
                throw new AccountNotLoginException(MessageConstant.ACCOUNT_NOT_LOGIN);
            }
            user.setLoginStatus(UserLoginStatus.LOGIN_OUT.ordinal());
            userMapper.updateById(user);
            // 删除token
            redisTemplate.delete(String.valueOf(userId));
        }
        else{
            throw new AccountNotExistsException(MessageConstant.ACCOUNT_NOT_FOUND);
        }
    }

    @Override
    public void banUser(Long userId, Integer time, String reason) {
        // 强制下线
        try{
            logout(userId);
        }catch (AccountNotLoginException ignored){}

        // 封禁数据存入redis
        redisTemplate.opsForValue().set(RedisConstant.BAN_PREFIX + userId,1,time,TimeUnit.HOURS);
        // 修改账户状态
        updateById(User.builder().id(userId).accountStatus(UserAccountStatus.FORBIDDEN.ordinal()).build());
        // 发出通知
        String title = StrUtil.format(MessageConstant.BAN_USER_UNCOMPLETED,reason,time.toString());
        messageService.send(Message.builder()
                .title(title)
                .content("")
                .receiverId(userId)
                .senderId(DefaultConstant.SYSTEM_USER_ID)
                .createTime(new Date()).build());
        // 释放SSE
        messageService.close(userId);
    }

    @Override
    public void recoverUser(Long userId) {
        // 封禁数据从redis删除
        redisTemplate.delete(RedisConstant.BAN_PREFIX + userId);
        // 修改账户状态
        updateById(User.builder().id(userId).accountStatus(UserAccountStatus.NORMAL.ordinal()).build());
    }
}
