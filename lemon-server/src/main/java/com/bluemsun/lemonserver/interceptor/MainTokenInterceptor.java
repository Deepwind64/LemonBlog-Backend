package com.bluemsun.lemonserver.interceptor;

import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTUtil;
import com.bluemsun.lemoncommon.constant.EncryptionConstant;
import com.bluemsun.lemoncommon.constant.RedisConstant;
import com.bluemsun.lemoncommon.context.UserContext;
import com.bluemsun.lemoncommon.context.dto.BaseUserDTO;
import com.bluemsun.lemonserver.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.sql.Timestamp;

/**
 * jwt令牌校验的拦截器
 * @author deepwind
 */
@Component
@Slf4j
public class MainTokenInterceptor implements HandlerInterceptor {
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    @Autowired
    UserService userService;
    /**
     * 校验jwt
     *
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 判断当前拦截到的是Controller的方法还是其他资源
        if (!(handler instanceof HandlerMethod)) {
            // 非动态方法，直接放行
            return true;
        }

        String token = request.getHeader("token");
        String userId = request.getHeader("userId");
        if (token!=null && JWTUtil.verify(token, EncryptionConstant.JWT_KEY.getBytes())){
            final JWT jwt = JWTUtil.parseToken(token);
            Long id = Long.parseLong(jwt.getPayload("id").toString());
            boolean isAdmin = Boolean.parseBoolean(jwt.getPayload("isAdmin").toString());
            Timestamp expireTime = new Timestamp(Long.parseLong(jwt.getPayload("expire_time").toString()));

            if(expireTime.after(new Timestamp(System.currentTimeMillis()))){
                if (token.equals(redisTemplate.opsForValue().get(RedisConstant.USER_TOKEN_PREFIX + id.toString())) ||
                        token.equals(redisTemplate.opsForValue().get(RedisConstant.ADMIN_TOKEN_PREFIX + id))){
                    // 将用户id放入线程变量
                    BaseUserDTO baseUserDTO = BaseUserDTO.builder()
                            .userId(id)
                            .isAdmin(isAdmin)
                            .build();
                    UserContext.setBaseUser(baseUserDTO);
                    return true;
                }
            }else{
                // 处理失效情况
                userService.logout(id);
            }
        }
        response.setStatus(401);
        request.setAttribute("msg","token 无效");
        return false;
    }
}
