package com.campus.evaluation.common.web;

import com.campus.evaluation.common.core.domain.R;
import com.campus.evaluation.common.core.exception.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.stream.Collectors;

/**
 * 全局异常处理器
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<R<Void>> handleBusinessException(BusinessException e, HttpServletRequest request) {
        log.warn("业务异常: {} - URI: {}", e.getMessage(), request.getRequestURI());
        return response(e.getCode(), R.fail(e.getCode(), e.getMessage(), e.getErrKey()));
    }

    /**
     * 参数校验异常（@RequestBody）
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<R<Void>> handleValidationException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        log.warn("参数校验失败: {}", message);
        return response(400, R.fail(400, message, "VALIDATION_FAILED"));
    }

    /**
     * 参数绑定异常
     */
    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<R<Void>> handleBindException(BindException e) {
        String message = e.getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        log.warn("参数绑定失败: {}", message);
        return response(400, R.fail(400, message, "VALIDATION_FAILED"));
    }

    /**
     * 请求方法不支持
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public ResponseEntity<R<Void>> handleMethodNotSupported(HttpRequestMethodNotSupportedException e) {
        return response(405, R.fail(405, "不支持的请求方法: " + e.getMethod(), "METHOD_NOT_ALLOWED"));
    }

    /**
     * 404
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<R<Void>> handleNoHandlerFound(NoHandlerFoundException e) {
        return response(404, R.fail(404, "请求资源不存在", "NOT_FOUND"));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<R<Void>> handleMaxUploadSizeExceeded(MaxUploadSizeExceededException e) {
        return response(422, R.fail(422, "上传文件超过大小限制", "FILE_TOO_LARGE"));
    }

    /**
     * 兜底异常处理
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<R<Void>> handleException(Exception e, HttpServletRequest request) {
        log.error("系统异常: URI={}, error={}", request.getRequestURI(), e.getMessage(), e);
        return response(500, R.fail(500, "系统内部错误，请联系管理员", "INTERNAL_ERROR"));
    }

    private ResponseEntity<R<Void>> response(int code, R<Void> body) {
        HttpStatus status = HttpStatus.resolve(code);
        return ResponseEntity.status(status != null ? status : HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}
