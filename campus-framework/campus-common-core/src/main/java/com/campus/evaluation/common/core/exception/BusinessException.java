package com.campus.evaluation.common.core.exception;

import com.campus.evaluation.common.core.domain.ErrorCode;
import lombok.Getter;

/**
 * 业务异常
 */
@Getter
public class BusinessException extends RuntimeException {

    /** 错误码 */
    private final int code;

    /** Stable machine-readable error key. */
    private final String errKey;

    public BusinessException(String message) {
        super(message);
        this.code = 500;
        this.errKey = "INTERNAL_ERROR";
    }

    public BusinessException(int code, String message) {
        this(code, message, null);
    }

    public BusinessException(int code, String message, String errKey) {
        super(message);
        this.code = code;
        this.errKey = errKey;
    }

    public BusinessException(ErrorCode errorCode) {
        this(errorCode, null);
    }

    public BusinessException(ErrorCode errorCode, String detail) {
        super(errorCode.getMessage() + ": " + detail);
        this.code = errorCode.getCode();
        this.errKey = errorCode.name();
    }

    public BusinessException(ErrorCode errorCode, String errKey, String detail) {
        super(detail != null ? detail : errorCode.getMessage());
        this.code = errorCode.getCode();
        this.errKey = errKey != null ? errKey : errorCode.name();
    }
}
