package com.campus.evaluation.common.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import java.util.Arrays;
import java.util.List;

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
            "/auth/captcha",
            "/files/*/preview"
    };

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SaInterceptor(handle -> {
            // 匹配所有路由，排除放行路径
            SaRouter.match("/**")
                    .notMatch(EXCLUDE_PATHS)
                    .check(r -> StpUtil.checkLogin());

            // 学校端用户管理接口需要 school_admin 角色
            List<String> adminPaths = Arrays.asList(
                    "/school/admin-users/**",
                    "/school/staff-users/**",
                    "/school/student-users/**",
                    "/school/roles/**"
            );
            SaRouter.match(adminPaths)
                    .check(r -> {
                        StpUtil.checkLogin();
                        StpUtil.checkRole("school_admin");
                    });

            // 审核接口仅 school_admin 可访问
            SaRouter.match("/evaluation/audits/**")
                    .check(r -> {
                        StpUtil.checkLogin();
                        StpUtil.checkRole("school_admin");
                    });
        })).addPathPatterns("/**");
    }
}
