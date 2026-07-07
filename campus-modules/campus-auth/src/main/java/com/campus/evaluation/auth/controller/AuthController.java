package com.campus.evaluation.auth.controller;

import com.campus.evaluation.auth.domain.dto.LoginRequest;
import com.campus.evaluation.auth.domain.vo.CurrentUserVO;
import com.campus.evaluation.auth.domain.vo.LoginResponse;
import com.campus.evaluation.auth.domain.vo.PermissionVO;
import com.campus.evaluation.auth.service.AuthService;
import com.campus.evaluation.common.core.domain.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.Map;

/**
 * 认证授权控制器
 */
@Tag(name = "认证授权", description = "登录、登出、用户信息、权限查询")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 登录
     */
    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public R<LoginResponse> login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        LoginResponse response = authService.login(request, httpRequest);
        return R.ok(response);
    }

    /**
     * 登出
     */
    @Operation(summary = "用户登出")
    @PostMapping("/logout")
    public R<Void> logout() {
        authService.logout();
        return R.ok(null, "登出成功");
    }

    /**
     * 获取当前用户信息
     */
    @Operation(summary = "获取当前用户信息")
    @GetMapping("/me")
    public R<CurrentUserVO> me() {
        CurrentUserVO vo = authService.getCurrentUser();
        return R.ok(vo);
    }

    /**
     * 获取当前用户权限信息
     */
    @Operation(summary = "获取权限信息")
    @GetMapping("/permissions")
    public R<PermissionVO> permissions() {
        PermissionVO vo = authService.getPermissions();
        return R.ok(vo);
    }

    /**
     * 获取前端路由（预留接口）
     */
    @Operation(summary = "获取前端路由（预留）")
    @GetMapping("/routes")
    public R<Map<String, Object>> routes() {
        // 预留接口，返回空数据
        return R.ok(Collections.singletonMap("routes", Collections.emptyList()));
    }

    /**
     * 验证码（预留接口）
     */
    @Operation(summary = "获取验证码（预留）")
    @GetMapping("/captcha")
    public R<Map<String, Object>> captcha() {
        // 预留接口，暂不启用验证码
        return R.ok(Collections.singletonMap("captchaEnabled", false));
    }
}
