package com.bluemsun.lemonserver.aspect;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.TimeInterval;
import cn.hutool.json.JSONUtil;
import com.bluemsun.lemoncommon.context.HttpContext;
import com.bluemsun.lemonpojo.entity.GlobalLog;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import jakarta.servlet.http.HttpServletRequest;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author deepwind
 * @version 1.0
 * 全局日志记录器
 */
@Slf4j
@Aspect
@Component
public class GlobalLogAspect extends BaseAspectSupport {
    /**
     * 定义切面Pointcut
     */
    @Pointcut("execution(public * com.bluemsun.lemonserver.controller.*.*(..))")
    public void log() {

    }

    @Around("log()")
    public Object doAround(ProceedingJoinPoint joinPoint) throws Throwable {

        GlobalLog globalLogBefore = new GlobalLog();
        //记录时间定时器
        TimeInterval timer = DateUtil.timer(true);
        HttpServletRequest request = HttpContext.getRequest();
        // 接口请求时间
        globalLogBefore.setStartTime(DateUtil.now());
        //请求链接
        globalLogBefore.setUrl(request.getRequestURL().toString());
        //请求方法GET,POST等
        globalLogBefore.setMethod(request.getMethod());
        //请求地址
        globalLogBefore.setIp(HttpContext.getIpAddress(request));
//        globalLogBefore.setIp(request.getRemoteAddr());
        log.info(JSONUtil.toJsonPrettyStr(globalLogBefore));

        //执行结果
        Object result = joinPoint.proceed();
        GlobalLog globalLogAfter = new GlobalLog();
        globalLogAfter.setResult(result);
        //执行消耗时间
        String endTime = timer.intervalPretty();
        globalLogAfter.setSpendTime(endTime);
        //执行参数
        Method method = resolveMethod(joinPoint);
        globalLogAfter.setParameter(getParameter(method, joinPoint.getArgs()));

        //接口描述
        if (method.isAnnotationPresent(ApiOperation.class)) {
            ApiOperation apiOperation = method.getAnnotation(ApiOperation.class);
            globalLogAfter.setDescription(apiOperation.value());
        }

        log.info(JSONUtil.toJsonPrettyStr(globalLogAfter));
        return result;

    }


    /**
     * 根据方法和传入的参数获取请求参数
     */
    private Object getParameter(Method method, Object[] args) {
        List<Object> argList = new ArrayList<>();
        Parameter[] parameters = method.getParameters();
        Map<String, Object> map = new HashMap<>();
        for (int i = 0; i < parameters.length; i++) {
            //将RequestBody注解修饰的参数作为请求参数
            RequestBody requestBody = parameters[i].getAnnotation(RequestBody.class);
            //将RequestParam注解修饰的参数作为请求参数
            RequestParam requestParam = parameters[i].getAnnotation(RequestParam.class);
            String key = parameters[i].getName();
            if (requestBody != null) {
                argList.add(args[i]);
            } else if (requestParam != null) {
                map.put(key, args[i]);
            } else {
                map.put(key, args[i]);
            }
        }
        if (map.size() > 0) {
            argList.add(map);
        }
        if (argList.size() == 0) {
            return null;
        } else if (argList.size() == 1) {
            return argList.get(0);
        } else {
            return argList;
        }
    }
}