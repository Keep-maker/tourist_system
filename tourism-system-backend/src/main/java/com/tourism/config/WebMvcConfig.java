package com.tourism.config;

import com.tourism.common.LoginInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC配置类
 * 注册登录拦截器，配置拦截与放行规则
 * 
 * 一体化单体项目不需要跨域配置(前后端同源)
 * 静态资源放行static目录
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private LoginInterceptor loginInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginInterceptor)
                // 拦截所有 /api/** 接口
                .addPathPatterns("/api/**")
                // 放行登录接口和注册接口(无需token)
                .excludePathPatterns(
                        "/api/admin/login",
                        "/api/admin/register",
                        // 静态资源文件
                        "/**/*.html",
                        "/**/*.js",
                        "/**/*.css",
                        "/**/*.png",
                        "/**/*.jpg",
                        "/**/*.jpeg",
                        "/**/*.gif",
                        "/**/*.ico",
                        "/**/*.svg",
                        "/**/*.woff",
                        "/**/*.woff2",
                        "/**/*.ttf",
                        // 其他
                        "/error",
                        "/favicon.ico"
                );
    }
}
