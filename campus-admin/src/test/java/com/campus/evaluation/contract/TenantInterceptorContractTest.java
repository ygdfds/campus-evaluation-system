package com.campus.evaluation.contract;

import com.campus.evaluation.common.core.exception.BusinessException;
import com.campus.evaluation.common.security.SecurityUtils;
import com.campus.evaluation.common.tenant.TenantContext;
import com.campus.evaluation.common.tenant.TenantContextHolder;
import com.campus.evaluation.common.tenant.TenantInterceptor;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mockStatic;

class TenantInterceptorContractTest {

    private final TenantInterceptor interceptor = new TenantInterceptor();

    @AfterEach
    void clearContext() {
        TenantContextHolder.clear();
    }

    @Test
    void publicApiUsesServletPathWhenApplicationHasContextPath() throws Exception {
        MockHttpServletRequest request = request("/api/auth/login", "/api", "/auth/login");
        HttpServletResponse response = new MockHttpServletResponse();

        assertThat(interceptor.preHandle(request, response, new Object())).isTrue();
        assertThat(TenantContextHolder.get()).isNull();
    }

    @Test
    void healthApiUsesServletPathWhenApplicationHasContextPath() throws Exception {
        MockHttpServletRequest request = request("/api/health", "/api", "/health");
        HttpServletResponse response = new MockHttpServletResponse();

        assertThat(interceptor.preHandle(request, response, new Object())).isTrue();
        assertThat(TenantContextHolder.get()).isNull();
    }

    @Test
    void protectedApiStillRejectsUnauthenticatedRequest() {
        MockHttpServletRequest request = request("/api/school/profile/current", "/api", "/school/profile/current");
        HttpServletResponse response = new MockHttpServletResponse();

        try (var security = mockStatic(SecurityUtils.class)) {
            security.when(SecurityUtils::isLogin).thenReturn(false);
            assertThatThrownBy(() -> interceptor.preHandle(request, response, new Object()))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(error -> {
                        BusinessException business = (BusinessException) error;
                        assertThat(business.getCode()).isEqualTo(401);
                        assertThat(business.getErrKey()).isEqualTo("UNAUTHORIZED");
                    });
        }
    }

    @Test
    void afterCompletionAlwaysClearsTenantContext() throws Exception {
        TenantContextHolder.set(new TenantContext());

        interceptor.afterCompletion(
                request("/api/school/profile/current", "/api", "/school/profile/current"),
                new MockHttpServletResponse(),
                new Object(),
                null);

        assertThat(TenantContextHolder.get()).isNull();
    }

    private MockHttpServletRequest request(String uri, String contextPath, String servletPath) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI(uri);
        request.setContextPath(contextPath);
        request.setServletPath(servletPath);
        return request;
    }
}
