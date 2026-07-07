package com.campus.evaluation.common.log.aspect;

import com.campus.evaluation.common.log.annotation.OperationLog;
import com.campus.evaluation.common.log.service.OperationLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * 操作日志切面
 * <p>
 * 拦截 @OperationLog 注解的方法，自动将操作记录写入 log_operation_log 表。
 * 日志写入失败不影响主业务流程。
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class OperationLogAspect {

    private final OperationLogService operationLogService;

    @Around("@annotation(operationLog)")
    public Object around(ProceedingJoinPoint joinPoint, OperationLog operationLog) throws Throwable {
        String module = resolveModule(operationLog, joinPoint);
        String action = operationLog.type();
        String content = operationLog.value();

        // 尝试从方法参数中提取业务对象信息
        TargetInfo targetInfo = extractTargetInfo(joinPoint);

        long startTime = System.currentTimeMillis();
        log.info("[操作日志] 开始: {} - {}", action, content);
        try {
            Object result = joinPoint.proceed();
            long cost = System.currentTimeMillis() - startTime;
            log.info("[操作日志] 完成: {} - {}，耗时 {}ms", action, content, cost);

            // 成功写入
            operationLogService.save(
                    module, action,
                    targetInfo.type, targetInfo.id,
                    content, "success", null
            );
            return result;
        } catch (Throwable e) {
            long cost = System.currentTimeMillis() - startTime;
            log.error("[操作日志] 异常: {} - {}，耗时 {}ms，错误: {}", action, content, cost, e.getMessage());

            // 失败写入
            operationLogService.save(
                    module, action,
                    targetInfo.type, targetInfo.id,
                    content, "fail", e.getMessage()
            );
            throw e;
        }
    }

    /**
     * 解析模块名：优先使用注解中的 module，否则从包名推断
     */
    private String resolveModule(OperationLog operationLog, ProceedingJoinPoint joinPoint) {
        if (operationLog.module() != null && !operationLog.module().isEmpty()) {
            return operationLog.module();
        }
        // 从类所在包名推断：com.campus.evaluation.school.controller → school
        String className = joinPoint.getTarget().getClass().getName();
        String[] parts = className.split("\\.");
        for (int i = 0; i < parts.length; i++) {
            if ("controller".equals(parts[i]) && i > 0) {
                return parts[i - 1];
            }
        }
        return "unknown";
    }

    /**
     * 从方法参数中提取 @PathVariable 或名为 id 的 @RequestParam
     */
    private TargetInfo extractTargetInfo(ProceedingJoinPoint joinPoint) {
        TargetInfo info = new TargetInfo();
        try {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();
            Annotation[][] paramAnnotations = method.getParameterAnnotations();
            String[] paramNames = signature.getParameterNames();
            Object[] args = joinPoint.getArgs();

            for (int i = 0; i < args.length; i++) {
                // 检查 @PathVariable
                for (Annotation ann : paramAnnotations[i]) {
                    if (ann instanceof PathVariable pv) {
                        String name = pv.value().isEmpty() ? (paramNames != null ? paramNames[i] : "") : pv.value();
                        if ("id".equals(name) && args[i] instanceof Long) {
                            info.id = (Long) args[i];
                        }
                    }
                }
            }

            // 从类名推断 targetType：TeachingOrgController → teaching_org
            String controllerName = joinPoint.getTarget().getClass().getSimpleName();
            if (controllerName.endsWith("Controller")) {
                String base = controllerName.substring(0, controllerName.length() - "Controller".length());
                info.type = camelToSnake(base);
            }
        } catch (Exception e) {
            log.debug("[操作日志] 提取目标信息失败: {}", e.getMessage());
        }
        return info;
    }

    private String camelToSnake(String s) {
        if (s == null || s.isEmpty()) return s;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (Character.isUpperCase(c)) {
                if (i > 0) sb.append('_');
                sb.append(Character.toLowerCase(c));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /** 业务目标信息 */
    private static class TargetInfo {
        String type;
        Long id;
    }
}
