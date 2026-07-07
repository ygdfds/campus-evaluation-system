package com.campus.evaluation.common.log.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 操作日志注解
 * <p>
 * 标注在 Controller 方法上，AOP 切面自动记录操作日志到 log_operation_log 表。
 * 日志写入失败不影响主流程。
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OperationLog {

    /** 操作描述 */
    String value() default "";

    /** 操作类型（CREATE / UPDATE / DELETE / OTHER） */
    String type() default "OTHER";

    /** 所属模块（school / file / auth / platform 等） */
    String module() default "";
}
