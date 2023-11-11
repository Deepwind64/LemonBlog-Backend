package com.bluemsun.lemonserver.interceptor;

import com.bluemsun.lemoncommon.constant.HTTPConstant;
import com.bluemsun.lemoncommon.constant.MessageConstant;
import com.bluemsun.lemoncommon.context.UserContext;
import com.bluemsun.lemoncommon.exception.PermissionDeniedException;
import com.bluemsun.lemoncommon.exception.TokenException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * jwt令牌校验的拦截器
 * @author deepwind
 */
@Component
@Slf4j
public class AdminTokenInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if(UserContext.isAdmin()){
            return true;
        }
        throw new PermissionDeniedException(MessageConstant.PERMISSION_DENIED);
    }
}
