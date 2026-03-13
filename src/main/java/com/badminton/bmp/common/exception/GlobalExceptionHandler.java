package com.badminton.bmp.common.exception;

import com.badminton.bmp.common.Result;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * 全局异常处理器
 * 统一处理异常并返回统一格式
 * 参照CMS实现方式
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @Value("${spring.profiles.active:dev}")
    private String activeProfile;

    /**
     * 处理业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public Result<Object> handleBusinessException(BusinessException e) {
        log.error("业务异常: {}", e.getMessage(), e);
        return new Result<>(e.getCode(), e.getMessage(), null);
    }

    /**
     * 处理验证异常
     */
    @ExceptionHandler(ValidationException.class)
    public Result<Object> handleValidationException(ValidationException e) {
        log.error("验证异常: {}", e.getMessage(), e);
        return Result.badRequest(e.getMessage());
    }

    /**
     * 处理资源未找到异常
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public Result<Object> handleResourceNotFoundException(ResourceNotFoundException e) {
        log.error("资源未找到: {}", e.getMessage(), e);
        return Result.notFound(e.getMessage());
    }

    /**
     * 处理Bean Validation失败异常（@Valid注解）
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Object> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        log.error("参数验证失败", e);
        String errorMessage = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return Result.badRequest("参数验证失败: " + errorMessage);
    }

    /**
     * 处理参数验证失败异常（@Validated注解）
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public Result<Object> handleConstraintViolationException(ConstraintViolationException e) {
        log.error("参数约束验证失败", e);
        String errorMessage = e.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining("; "));
        return Result.badRequest("参数验证失败: " + errorMessage);
    }

    /**
     * 处理参数绑定失败异常
     */
    @ExceptionHandler(BindException.class)
    public Result<Object> handleBindException(BindException e) {
        log.error("参数绑定失败", e);
        String errorMessage = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return Result.badRequest("参数绑定失败: " + errorMessage);
    }

    /**
     * 处理HTTP请求方法不支持异常
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public Result<Object> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        log.error("HTTP请求方法不支持: {}", e.getMessage(), e);
        return new Result<>(405, "请求方法不支持: " + e.getMethod(), null);
    }

    /**
     * 处理HTTP媒体类型不支持异常
     */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public Result<Object> handleHttpMediaTypeNotSupportedException(HttpMediaTypeNotSupportedException e) {
        log.error("HTTP媒体类型不支持: {}", e.getMessage(), e);
        return new Result<>(415, "媒体类型不支持: " + e.getContentType(), null);
    }

    /**
     * 处理HTTP消息不可读异常
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Result<Object> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        log.error("HTTP消息不可读", e);
        return Result.badRequest("请求参数格式错误");
    }

    /**
     * 处理Spring Security的AccessDeniedException
     */
    @ExceptionHandler(AccessDeniedException.class)
    public Result<Object> handleAccessDeniedException(AccessDeniedException e) {
        log.error("权限不足: {}", e.getMessage(), e);
        return Result.forbidden("权限不足，无法访问该资源");
    }

    /**
     * 处理其他异常
     */
    @ExceptionHandler(Exception.class)
    public Result<Object> handleException(Exception e) {
        log.error("系统异常", e);
        // 生产环境隐藏详细错误信息
        if ("prod".equals(activeProfile)) {
            return Result.error("系统内部错误，请联系管理员");
        }
        return Result.error("系统内部错误：" + e.getMessage());
    }
}
