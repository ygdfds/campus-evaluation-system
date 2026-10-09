package com.campus.evaluation.common.security;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class SaTokenConfigure implements WebMvcConfigurer {

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
            "/auth/schools",
            "/auth/verify-identity",
            "/auth/forgot-password",
            };

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SaInterceptor(handle -> {
            SaRouter.match("/**")
                    .notMatch(EXCLUDE_PATHS)
                    .check(r -> StpUtil.checkLogin());

            SaRouter.match("/school/**")
                    .check(r -> {
                        StpUtil.checkLogin();
                        StpUtil.checkRole("school_admin");
                    });

            SaRouter.match("/evaluation/audits/**")
                    .check(r -> {
                        StpUtil.checkLogin();
                        StpUtil.checkRole("school_admin");
                    });

            List<String> studentPaths = List.of(
                    "/student/evaluation/tasks",
                    "/student/evaluation/tasks/**",
                    "/student/evaluation/submissions",
                    "/student/evaluation/submissions/**"
            );
            SaRouter.match(studentPaths)
                    .check(r -> {
                        StpUtil.checkLogin();
                        StpUtil.checkRole("student");
                    });
        })).addPathPatterns("/**");
    }
}
