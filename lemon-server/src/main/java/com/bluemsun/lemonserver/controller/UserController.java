package com.bluemsun.lemonserver.controller;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.ShearCaptcha;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.extra.mail.MailUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.bluemsun.lemoncommon.constant.*;
import com.bluemsun.lemoncommon.enumeration.UserAccountStatus;
import com.bluemsun.lemoncommon.exception.AccountNotExistsException;
import com.bluemsun.lemoncommon.exception.EmailVerifyException;
import com.bluemsun.lemoncommon.exception.InvalidArgsException;
import com.bluemsun.lemoncommon.exception.VerifyCodeInavalidException;
import com.bluemsun.lemoncommon.utils.GenRandomPassword;
import com.bluemsun.lemoncommon.utils.JWTGenerator;
import com.bluemsun.lemoncommon.utils.VerifyCodeGenerator;
import com.bluemsun.lemonpojo.dto.*;
import com.bluemsun.lemonpojo.entity.User;
import com.bluemsun.lemonpojo.vo.LoginTicketVO;
import com.bluemsun.lemonserver.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jakarta.annotation.Resource;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.MediaType;
import static com.bluemsun.lemonserver.service.UserService.userAuthenticate;

import java.io.IOException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/user")
@Slf4j
@Api("用户账户管理相关接口")
public class UserController {
    @Resource
    UserService userService;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @PostMapping("/register")
    @ApiOperation( value = "用户注册", produces = MediaType.APPLICATION_JSON_VALUE)
    public void register(@RequestBody @Validated UserRegisterDTO userRegisterDTO){
        // 注册逻辑
        long userId = userService.register(userRegisterDTO);

        // 生成验证链接
        String varifyString = IdUtil.simpleUUID();
        String varifyLink = UrlConstant.EMAIL_SERVER_LOCATION+"/user/register/"+userId+"/"+varifyString;

        // key：{id}+"verify"，24小时过期
        redisTemplate.opsForValue().set("verifyId " + userId,varifyString,1, TimeUnit.DAYS);
        DateTime failureTime = DateUtil.tomorrow();

        // 发送验证邮件
        String email = userRegisterDTO.getEmail();
        String name = userRegisterDTO.getName();
        MailUtil.send(email,EmailConstant.REGISTER_VERIFY_TITLE,
                EmailConstant.getRegisterVerifyContent(name,varifyLink, DateUtil.formatDateTime(failureTime)),true);

        log.info(StrUtil.format("注册验证码已发送至{}",email));

    }
    @ApiOperation("注册验证链接")
    @GetMapping("/register/{id}/{varifyString}")
    public void verifyRegister(@PathVariable("id") Integer id,@PathVariable("varifyString") String varifyString){
        String key = "verifyId " + id;
        // 获取原验证字符串
        String trueVerrifyString = (String) redisTemplate.opsForValue().get(key);
        // 比较，更新账户状态
        if(varifyString.equals(trueVerrifyString)){
            User user = User.builder()
                    .id(id.longValue())
                    .accountStatus(UserAccountStatus.NORMAL.ordinal())
                    .build();
            userService.updateById(user);
            // 清理验证码
            redisTemplate.delete(key);
        }else{
            throw new EmailVerifyException(MessageConstant.VERIFY_FAILED);
        }
    }
    @GetMapping("/verifyCode")
    public void getVerifyCode(HttpServletResponse response) throws IOException {
        // 用一段随机字符串代表验证码的归属
        String captchaOwner = GenRandomPassword.getPasswordOne(20);
        // 用Cookie保存归属信息
        ResponseCookie cookie = ResponseCookie.from("captchaOwner", captchaOwner)
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .maxAge(EncryptionConstant.VERIFY_CODE_EXPIRE_TIME)
                .build();
        response.setHeader(HttpHeaders.SET_COOKIE,cookie.toString());

        //定义图形验证码的长、宽、验证码字符数、干扰线宽度
        ShearCaptcha captcha = CaptchaUtil.createShearCaptcha(150, 40, 4, 3);
        //图形验证码写出，可以写出到文件，也可以写出到流
        captcha.write(response.getOutputStream());
        //获取验证码中的文字内容
        String verifyCode = captcha.getCode();

        // 将验证码存入Redis，有效期2分钟
        redisTemplate.opsForValue().set(captchaOwner, verifyCode, EncryptionConstant.VERIFY_CODE_EXPIRE_TIME, TimeUnit.SECONDS);
        log.info("验证码信息 {} , {}",captchaOwner,verifyCode);
    }
    @PostMapping("/login")
    public LoginTicketVO login(@RequestBody @Validated UserLoginDTO userLoginDTO, @CookieValue("captchaOwner") String captchaOwner){
        // 校验验证码
        String verifyCode = userLoginDTO.getVerifyCode();
        userService.checkVerifyCode(verifyCode, captchaOwner);

        Long userId;
        // 登录逻辑
        if (userLoginDTO.getEmail()!=null){
            // 邮箱登录
            UserEmailLoginDTO userEmailLoginDTO = new UserEmailLoginDTO();
            BeanUtil.copyProperties(userLoginDTO, userEmailLoginDTO);
            userId = userService.login(userEmailLoginDTO);
        } else if (userLoginDTO.getName()!=null) {
            // 用户名登录
            UserNameLoginDTO userNameLoginDTO = new UserNameLoginDTO();
            BeanUtil.copyProperties(userLoginDTO, userNameLoginDTO);
            userId = userService.login(userNameLoginDTO);
        }else {
            throw new InvalidArgsException(MessageConstant.INVALID_ARGS);
        }

        // 产生jwt令牌
        Long expireTime = System.currentTimeMillis() + EncryptionConstant.JWT_EXPIRE_TIME;
        boolean isAdmin = false;
        String token = JWTGenerator.getJWT(expireTime,userId,isAdmin);
        //  将 token 存入redis
        redisTemplate.opsForValue().set(RedisConstant.USER_TOKEN_PREFIX + userId,token,1,TimeUnit.DAYS);

        LoginTicketVO loginTicketVO = LoginTicketVO.builder()
                .userId(userId)
                .token(token)
                .userAccountStatus(UserAccountStatus.NORMAL.ordinal())
                .expireTime(expireTime)
                .build();

        String name = userService.getById(userId).getName();
        log.info("用户{},id: {}登录成功",name,userId);
        return loginTicketVO;
    }
    @ApiOperation("修改用户名验证链接")
    @PutMapping("/name/{userId}")
    public void updateName(@PathVariable("userId") long userId, @RequestBody @Validated UpdateNameDTO updateNameDTO){
        userAuthenticate(userId);
        userService.updateName(userId, updateNameDTO.getName());
        log.info("id: {}的用户，新用户名为 {}", userId, updateNameDTO.getName());
    }
    @ApiOperation("修改邮箱")
    @PutMapping("/email/{userId}")
    public void updateEmail(@PathVariable("userId") long userId, @RequestBody @Validated UpdateEmailDTO updateEmailDTO){
        userAuthenticate(userId);
        userService.updateEmail(userId, updateEmailDTO);
        log.info("id: {}的用户，新邮箱为{}", userId, updateEmailDTO.getNewEmail());
    }

    @ApiOperation("修改邮箱验证链接")
    @GetMapping("/email/{userId}/{uid}")
    public void verifyRegister(@PathVariable("userId") Long userId,@PathVariable("uid") String uuid){
        // 获取新邮箱地址
        String newEmail = (String) redisTemplate.opsForValue().get(uuid);
        User user = userService.getById(userId);
        // uuid能对应到邮箱，说明已经发过更新
        if(newEmail!=null){
            user.setEmail(newEmail);
            userService.updateById(user);
            // 删除缓存的新邮箱
            redisTemplate.delete(uuid);
        }else{
            throw new EmailVerifyException(MessageConstant.VERIFY_FAILED);
        }
    }
//    @Auth("hasAuth()")
    @ApiOperation("返回邮箱提示信息")
    @GetMapping("/email-tip/{userId}")
    public String updateEmail(@PathVariable("userId") Long userId){
        userAuthenticate(userId);
        String email = userService.getById(userId).getEmail();
        String[] emailPiece = email.split("@");
        // 取前三，如果长度不够返回最大长度-1
        int maxLength = emailPiece[0].length()>=3 ? 3 : emailPiece[0].length()-1;
        return emailPiece[0].substring(0,maxLength)+"***"+"@"+emailPiece[1];
    }
    @ApiOperation("检查邮箱存在性")
    @GetMapping("/email/check")
    public void checkEmail(String email){
        userService.checkEmail(email);
    }
    @ApiOperation("找回密码——邮箱验证码")
    @GetMapping("/password/verifyCode")
    public void getEmailVerifyCode(String email){
        // 检查邮箱是否存在
        User user = userService.checkEmail(email);
        // 产生验证码
        String verifyCode = VerifyCodeGenerator.generateVerifyCode(4,null).toUpperCase();
        // 验证码存入redis
        redisTemplate.opsForValue().set(email, verifyCode,5, TimeUnit.MINUTES);
        // 发送验证码邮件
        Long failureTime = EncryptionConstant.FIND_PW_VERIFY_CODE_EXPIRE_TIME + System.currentTimeMillis();
        MailUtil.send(email,EmailConstant.FINDPW_VERIFY_TITLE,
                EmailConstant.getFindPwVerify(user.getName(),verifyCode, DateUtil.date(failureTime).toTimeStr()),true);
        log.info("找回密码：邮箱{}，验证码{}", email, verifyCode);
    }
    @PostMapping("/password/recovery")
    public void recoverPassword(@RequestBody @Validated RecoverPasswordDTO recoverPasswordDTO){
        String email = recoverPasswordDTO.getEmail();
        String password = recoverPasswordDTO.getPassword();
        String verifyCode = recoverPasswordDTO.getVerifyCode();
        // 检查新密码合法性
        userService.checkPassword(password);
        // 校验验证码和邮箱
        if(verifyCode.equals(redisTemplate.opsForValue().get(email))){
            User user = userService.checkEmail(email);
            user.setPassword(SecureUtil.sha1(password));
            userService.updateById(user);
            redisTemplate.delete(email);
            log.info("找回密码成功，id：{}，邮箱：{}，新密码：{}",user.getId(),email,password);
        }else{
            throw new VerifyCodeInavalidException(MessageConstant.VERIFY_FAILED);
        }
    }

    @ApiOperation("修改密码")
    @PutMapping("/password/{userId}")
    public void updatePassword(@PathVariable("userId") long userId, @RequestBody @Validated UpdatePasswordDTO updatePasswordDTO){
        userAuthenticate(userId);
        userService.updatePassword(userId, updatePasswordDTO);
        // TODO 给注册邮箱发送警告信息，附带重置密码链接
        log.info("id: {}的用户，新密码为{}", userId, updatePasswordDTO.getNewPassword());
    }
    @ApiOperation("登出")
    @GetMapping("/logout/{userId}")
    public void logout(@PathVariable("userId") long userId, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        userAuthenticate(userId);
        userService.logout(userId);
        log.info("id: {}的用户已登出", userId);
    }
}
