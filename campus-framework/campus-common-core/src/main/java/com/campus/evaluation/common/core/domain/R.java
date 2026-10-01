package com.campus.evaluation.common.core.domain;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * 统一响应对象
 *
 * @param <T> 数据类型
 */
@Data
public class R<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 状态码 */
    private int code;

    /** 消息 */
    private String message;

    /** 数据 */
    private T data;

    /** Response creation time in the API contract timezone. */
    private String timestamp;

    /** Stable machine-readable error key. */
    private String errKey;

    private static final DateTimeFormatter TIMESTAMP_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private R() {}

    public static <T> R<T> ok() {
        return ok(null);
    }

    public static <T> R<T> ok(T data) {
        R<T> r = new R<>();
        r.setCode(200);
        r.setMessage("success");
        r.setData(data);
        r.setTimestamp(now());
        return r;
    }

    public static <T> R<T> ok(T data, String message) {
        R<T> r = new R<>();
        r.setCode(200);
        r.setMessage(message);
        r.setData(data);
        r.setTimestamp(now());
        return r;
    }

    public static <T> R<T> fail(String message) {
        R<T> r = new R<>();
        r.setCode(500);
        r.setMessage(message);
        r.setData(null);
        r.setTimestamp(now());
        return r;
    }

    public static <T> R<T> fail(int code, String message) {
        R<T> r = new R<>();
        r.setCode(code);
        r.setMessage(message);
        r.setData(null);
        r.setTimestamp(now());
        return r;
    }

    public static <T> R<T> fail(ErrorCode errorCode) {
        return fail(errorCode.getCode(), errorCode.getMessage(), errorCode.name());
    }

    public static <T> R<T> fail(int code, String message, String errKey) {
        R<T> r = fail(code, message);
        r.setErrKey(errKey);
        return r;
    }

    private static String now() {
        return LocalDateTime.now(ZoneId.of("Asia/Shanghai")).format(TIMESTAMP_FORMATTER);
    }
}
