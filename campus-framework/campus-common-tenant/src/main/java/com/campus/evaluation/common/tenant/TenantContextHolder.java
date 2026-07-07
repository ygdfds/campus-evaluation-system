package com.campus.evaluation.common.tenant;

/**
 * 租户上下文持有者（基于 ThreadLocal）
 */
public final class TenantContextHolder {

    private TenantContextHolder() {}

    private static final ThreadLocal<TenantContext> CONTEXT = new ThreadLocal<>();

    /**
     * 设置租户上下文
     */
    public static void set(TenantContext context) {
        CONTEXT.set(context);
    }

    /**
     * 获取租户上下文
     */
    public static TenantContext get() {
        return CONTEXT.get();
    }

    /**
     * 获取当前租户ID
     */
    public static Long getTenantId() {
        TenantContext ctx = CONTEXT.get();
        return ctx != null ? ctx.getTenantId() : null;
    }

    /**
     * 获取当前学校ID
     */
    public static Long getSchoolId() {
        TenantContext ctx = CONTEXT.get();
        return ctx != null ? ctx.getSchoolId() : null;
    }

    /**
     * 清除租户上下文
     */
    public static void clear() {
        CONTEXT.remove();
    }
}
