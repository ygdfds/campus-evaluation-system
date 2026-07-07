package com.campus.evaluation.common.security;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.exception.NotRoleException;
import com.campus.evaluation.common.core.domain.R;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Sa-Token 相关异常处理器
 * <p>
 * 优先级高于 GlobalExceptionHandler（@Order(0)），
 * 专门处理未登录、无权限、无角色异常
 */
@Slf4j
@RestControllerAdvice
@Order(0)
public class SaTokenExceptionHandler {

    /**
     * 未登录异常（401）
     */
    @ExceptionHandler(NotLoginException.class)
    public R<Void> handleNotLogin(NotLoginException e, HttpServletRequest request) {
        log.warn("未登录访问: {} - URI: {}", e.getMessage(), request.getRequestURI());
        return R.fail(401, "未登录或登录已过期，请重新登录");
    }

    /**
     * 无权限异常（403）
     */
    @ExceptionHandler(NotPermissionException.class)
    public R<Void> handleNotPermission(NotPermissionException e, HttpServletRequest request) {
        log.warn("无权限访问: {} - URI: {}", e.getMessage(), request.getRequestURI());
        return R.fail(403, "没有操作权限");
    }

    /**
     * 无角色异常（403）
     */
    @ExceptionHandler(NotRoleException.class)
    public R<Void> handleNotRole(NotRoleException e, HttpServletRequest request) {
        log.warn("无角色权限: {} - URI: {}", e.getMessage(), request.getRequestURI());
        return R.fail(403, "没有角色权限");
    }
}
