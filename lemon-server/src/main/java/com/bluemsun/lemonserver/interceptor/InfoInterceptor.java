package com.bluemsun.lemonserver.interceptor;

import com.bluemsun.lemoncommon.context.HttpContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.HandlerInterceptor;
@Component
public class InfoInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 设置全局 request
        HttpContext.setRequest((ServletRequestAttributes) RequestContextHolder.getRequestAttributes());
        return true;
    }
}
