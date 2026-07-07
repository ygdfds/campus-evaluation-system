package com.campus.evaluation.common.log.aspect;

import com.campus.evaluation.common.log.annotation.OperationLog;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * 操作日志切面（骨架，后续可扩展写库）
 */
@Slf4j
@Aspect
@Component
public class OperationLogAspect {

    @Around("@annotation(operationLog)")
    public Object around(ProceedingJoinPoint joinPoint, OperationLog operationLog) throws Throwable {
        long startTime = System.currentTimeMillis();
        log.info("[操作日志] 开始: {} - {}", operationLog.type(), operationLog.value());
        try {
            Object result = joinPoint.proceed();
            long cost = System.currentTimeMillis() - startTime;
            log.info("[操作日志] 完成: {} - {}，耗时 {}ms", operationLog.type(), operationLog.value(), cost);
            return result;
        } catch (Throwable e) {
            log.error("[操作日志] 异常: {} - {}，错误: {}", operationLog.type(), operationLog.value(), e.getMessage());
            throw e;
        }
    }
}
