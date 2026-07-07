package com.campus.evaluation.common.core.constant;

/**
 * 系统常量
 */
public final class CommonConstants {

    private CommonConstants() {}

    /** 成功状态码 */
    public static final int SUCCESS_CODE = 200;

    /** 失败状态码 */
    public static final int FAIL_CODE = 500;

    /** 成功消息 */
    public static final String SUCCESS_MSG = "success";

    /** 逻辑删除-未删除 */
    public static final int NOT_DELETED = 0;

    /** 逻辑删除-已删除 */
    public static final int DELETED = 1;

    /** 请求头 - 租户ID */
    public static final String HEADER_TENANT_ID = "X-Tenant-Id";

    /** 请求头 - 学校ID */
    public static final String HEADER_SCHOOL_ID = "X-School-Id";

    /** 默认租户ID */
    public static final Long DEFAULT_TENANT_ID = 1L;
}
