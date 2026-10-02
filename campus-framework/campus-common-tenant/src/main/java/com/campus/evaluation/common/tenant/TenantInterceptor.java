package com.campus.evaluation.common.tenant;

import com.campus.evaluation.common.core.constant.CommonConstants;
import com.campus.evaluation.common.core.exception.BusinessException;
import com.campus.evaluation.common.security.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.List;
import java.util.Objects;

@Slf4j
@Component
public class TenantInterceptor implements HandlerInterceptor {

    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    private static final List<String> PUBLIC_PATHS = List.of(
            "/health/**",
            "/doc.html",
            "/webjars/**",
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-resources/**",
            "/favicon.ico",
            "/auth/**"
    );

    private static final List<String> PLATFORM_PATHS = List.of(
            "/admin/**",
            "/platform/**",
            "/system/**"
    );

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        TenantContextHolder.clear();

        String uri = request.getRequestURI();
        if (matches(uri, PUBLIC_PATHS)) {
            return true;
        }

        if (!SecurityUtils.isLogin()) {
            throw new BusinessException(401, "Login required or expired", "UNAUTHORIZED");
        }

        if (matches(uri, PLATFORM_PATHS)) {
            ensurePlatformAccess();
            setTenantContext(SecurityUtils.getTenantId(), SecurityUtils.getSchoolId());
            return true;
        }

        Long loginTenantId = SecurityUtils.getTenantId();
        Long loginSchoolId = SecurityUtils.getSchoolId();
        if (loginTenantId == null) {
            throw new BusinessException(403, "Tenant context is required", "TENANT_REQUIRED");
        }

        Long headerTenantId = parseHeader(request, CommonConstants.HEADER_TENANT_ID);
        if (headerTenantId != null && !Objects.equals(headerTenantId, loginTenantId)) {
            throw new BusinessException(403, "Cross-tenant access is forbidden", "TENANT_FORBIDDEN");
        }

        Long headerSchoolId = parseHeader(request, CommonConstants.HEADER_SCHOOL_ID);
        if (headerSchoolId != null && loginSchoolId != null && !Objects.equals(headerSchoolId, loginSchoolId)) {
            throw new BusinessException(403, "Cross-school access is forbidden", "SCHOOL_FORBIDDEN");
        }

        setTenantContext(loginTenantId, loginSchoolId);
        log.debug("Tenant context set: tenantId={}, schoolId={}", loginTenantId, loginSchoolId);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        TenantContextHolder.clear();
    }

    private void setTenantContext(Long tenantId, Long schoolId) {
        TenantContext context = new TenantContext();
        context.setTenantId(tenantId);
        context.setSchoolId(schoolId);
        TenantContextHolder.set(context);
    }

    private void ensurePlatformAccess() {
        if (!SecurityUtils.hasRole("system_admin") && !SecurityUtils.hasPermission("system:access")) {
            throw new BusinessException(403, "Platform API requires system permission", "PLATFORM_FORBIDDEN");
        }
    }

    private Long parseHeader(HttpServletRequest request, String headerName) {
        String value = request.getHeader(headerName);
        if (!StringUtils.hasText(value)) {
            return null;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException ex) {
            throw new BusinessException(400, "Invalid " + headerName + " header", "INVALID_TENANT_HEADER");
        }
    }

    private boolean matches(String uri, List<String> patterns) {
        return patterns.stream().anyMatch(pattern -> PATH_MATCHER.match(pattern, uri));
    }
}
