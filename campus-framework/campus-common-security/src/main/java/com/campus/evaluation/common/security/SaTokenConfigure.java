package com.campus.evaluation.common.security;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Sa-Token 路由拦截配置
 * <p>
 * 放行健康检查、接口文档、登录接口，其余接口默认需要登录
 */
@Configuration
public class SaTokenConfigure implements WebMvcConfigurer {

    /** 放行路径列表 */
    private static final String[] EXCLUDE_PATHS = {
            "/health/**",
            "/doc.html",
            "/webjars/**",
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-resources/**",
            "/favicon.ico",
            "/auth/login",
            "/auth/captcha"
    };

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SaInterceptor(handle -> {
            // 匹配所有路由，排除放行路径
            SaRouter.match("/**")
                    .notMatch(EXCLUDE_PATHS)
                    .check(r -> StpUtil.checkLogin());
        })).addPathPatterns("/**");
    }
}
