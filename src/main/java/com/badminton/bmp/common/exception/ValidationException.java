package com.badminton.bmp.common.exception;

/**
 * 验证异常类
 * 用于处理参数验证失败的情况
 */
public class ValidationException extends RuntimeException {

    public ValidationException(String message) {
        super(message);
    }

    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
