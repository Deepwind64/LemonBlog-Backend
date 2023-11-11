package com.bluemsun.lemonserver.config;

import cn.hutool.core.util.ArrayUtil;
import com.bluemsun.lemoncommon.constant.HTTPConstant;
import com.bluemsun.lemonserver.interceptor.AdminTokenInterceptor;
import com.bluemsun.lemonserver.interceptor.InfoInterceptor;
import com.bluemsun.lemonserver.interceptor.MainTokenInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.servlet.config.annotation.*;

/**
 * @author deepwind
 */
@Configuration
@Slf4j
public class WebMvcConfig extends WebMvcConfigurationSupport {

    @Autowired
    private MainTokenInterceptor mainTokenInterceptor;
    @Autowired
    private InfoInterceptor infoInterceptor;
    @Value("${file.picpath}")
    private String picPath;
    @Value("${file.path}")
    private String filePath;

    /**
     * 注册自定义拦截器
     *
     * @param registry
     */
    @Override
    protected void addInterceptors(InterceptorRegistry registry) {
        log.info("开始注册自定义拦截器...");
        registry.addInterceptor(infoInterceptor)
                        .addPathPatterns("/**");
        // FIXME 优化user和userinfo的url设计
        registry.addInterceptor(mainTokenInterceptor)
                .addPathPatterns("/user/**","/userinfo/**","/admin/**","/blog/**","/column/**")
                .excludePathPatterns(ArrayUtil.addAll(HTTPConstant.NO_TOKEN_PATH,HTTPConstant.TOKEN_CHOOSE_PATH));
        registry.addInterceptor(new AdminTokenInterceptor())
                .addPathPatterns("/admin/**")
                .excludePathPatterns("/admin/login");
    }
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/pic/**").addResourceLocations("file:///"+picPath);
        registry.addResourceHandler("/file/**").addResourceLocations("file:///"+filePath);
        registry.addResourceHandler("/webpage/**").addResourceLocations("classpath:/static/webpage/");
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**").
                allowedOriginPatterns("*").
                allowedMethods("GET", "POST", "DELETE", "PUT").
                allowedHeaders("*").
                allowCredentials(true).
                exposedHeaders(HttpHeaders.SET_COOKIE).maxAge(3600L);
        // maxAge(3600)表明在3600秒内，不需要再发送预检验请求，可以缓存该结果
    }
}