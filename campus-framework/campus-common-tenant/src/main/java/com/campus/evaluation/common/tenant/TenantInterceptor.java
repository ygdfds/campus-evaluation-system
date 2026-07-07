package com.campus.evaluation.common.tenant;

import com.campus.evaluation.common.core.constant.CommonConstants;
import com.campus.evaluation.common.security.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 租户拦截器
 * <p>
 * 优先从 Sa-Token 登录用户获取 tenantId，
 * 如未登录则回退到请求头 X-Tenant-Id / X-School-Id
 */
@Slf4j
@Component
public class TenantInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        TenantContext context = new TenantContext();

        // 优先从登录用户获取
        Long loginTenantId = SecurityUtils.getTenantId();
        Long loginSchoolId = SecurityUtils.getSchoolId();

        if (loginTenantId != null) {
            context.setTenantId(loginTenantId);
        } else {
            String tenantIdStr = request.getHeader(CommonConstants.HEADER_TENANT_ID);
            if (tenantIdStr != null && !tenantIdStr.isEmpty()) {
                context.setTenantId(Long.parseLong(tenantIdStr));
            } else {
                context.setTenantId(CommonConstants.DEFAULT_TENANT_ID);
            }
        }

        if (loginSchoolId != null) {
            context.setSchoolId(loginSchoolId);
        } else {
            String schoolIdStr = request.getHeader(CommonConstants.HEADER_SCHOOL_ID);
            if (schoolIdStr != null && !schoolIdStr.isEmpty()) {
                context.setSchoolId(Long.parseLong(schoolIdStr));
            }
        }

        TenantContextHolder.set(context);
        log.debug("租户上下文已设置: tenantId={}, schoolId={}", context.getTenantId(), context.getSchoolId());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        TenantContextHolder.clear();
    }
}
