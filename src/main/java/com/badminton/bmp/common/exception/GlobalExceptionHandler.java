package com.badminton.bmp.common.exception;

import com.badminton.bmp.common.Result;
import com.badminton.bmp.common.util.ErrorMessageSanitizer;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

import java.util.stream.Collectors;

/**
 * 全局异常处理，统一给前端返回用户可理解的错误信息。
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BusinessException.class)
    public Result<Object> handleBusinessException(BusinessException e) {
        log.error("业务异常: {}", e.getMessage(), e);
        return new Result<>(e.getCode(), ErrorMessageSanitizer.sanitize(e.getMessage()), null);
    }

    @ExceptionHandler(ValidationException.class)
    public Result<Object> handleValidationException(ValidationException e) {
        log.error("验证异常: {}", e.getMessage(), e);
        return Result.badRequest(ErrorMessageSanitizer.sanitize(e.getMessage()));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public Result<Object> handleResourceNotFoundException(ResourceNotFoundException e) {
        log.error("资源未找到: {}", e.getMessage(), e);
        return Result.notFound(ErrorMessageSanitizer.sanitize(e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Object> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        log.error("参数验证失败", e);
        String errorMessage = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return Result.badRequest("参数验证失败: " + errorMessage);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public Result<Object> handleConstraintViolationException(ConstraintViolationException e) {
        log.error("参数约束验证失败", e);
        String errorMessage = e.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining("; "));
        return Result.badRequest("参数验证失败: " + errorMessage);
    }

    @ExceptionHandler(BindException.class)
    public Result<Object> handleBindException(BindException e) {
        log.error("参数绑定失败", e);
        String errorMessage = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return Result.badRequest("参数绑定失败: " + errorMessage);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public Result<Object> handleHandlerMethodValidationException(HandlerMethodValidationException e) {
        log.error("方法参数验证失败", e);
        String errorMessage = e.getAllValidationResults().stream()
                .flatMap(result -> result.getResolvableErrors().stream())
                .map(error -> error.getDefaultMessage() != null ? error.getDefaultMessage() : error.toString())
                .collect(Collectors.joining("; "));
        if (errorMessage == null || errorMessage.trim().isEmpty()) {
            errorMessage = "请求参数校验失败";
        }
        return Result.badRequest("参数验证失败: " + errorMessage);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public Result<Object> handleIllegalArgumentException(IllegalArgumentException e) {
        log.error("参数异常: {}", e.getMessage(), e);
        return Result.badRequest(ErrorMessageSanitizer.sanitize(e.getMessage()));
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public Result<Object> handleMissingRequestHeaderException(MissingRequestHeaderException e) {
        log.error("缺少请求头: {}", e.getHeaderName(), e);
        return Result.unauthorized("登录信息已失效，请重新登录");
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public Result<Object> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        log.error("HTTP请求方法不支持: {}", e.getMessage(), e);
        return new Result<>(405, "请求方式不支持", null);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public Result<Object> handleHttpMediaTypeNotSupportedException(HttpMediaTypeNotSupportedException e) {
        log.error("HTTP媒体类型不支持: {}", e.getMessage(), e);
        return new Result<>(415, "请求内容类型不支持", null);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Result<Object> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        log.error("HTTP消息不可读", e);
        return Result.badRequest("请求参数格式错误");
    }

    @ExceptionHandler(AccessDeniedException.class)
    public Result<Object> handleAccessDeniedException(AccessDeniedException e) {
        log.error("权限不足: {}", e.getMessage(), e);
        return Result.forbidden("权限不足，无法访问该资源");
    }

    @ExceptionHandler(Exception.class)
    public Result<Object> handleException(Exception e) {
        log.error("系统异常", e);
        return Result.error("系统繁忙，请稍后重试");
    }
}
