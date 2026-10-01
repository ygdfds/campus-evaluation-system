package com.campus.evaluation.common.core.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 错误码枚举
 */
@Getter
@AllArgsConstructor
public enum ErrorCode {

    SUCCESS(200, "操作成功"),
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未认证"),
    FORBIDDEN(403, "无权限"),
    NOT_FOUND(404, "资源不存在"),
    METHOD_NOT_ALLOWED(405, "不支持的请求方法"),
    CONFLICT(409, "资源状态冲突"),
    UNPROCESSABLE_ENTITY(422, "请求无法处理"),
    INTERNAL_ERROR(500, "系统内部错误"),
    DB_ERROR(500, "数据库操作异常"),
    SERVICE_UNAVAILABLE(503, "服务暂不可用");

    private final int code;
    private final String message;
}
