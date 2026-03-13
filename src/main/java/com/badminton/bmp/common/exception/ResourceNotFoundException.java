package com.badminton.bmp.common.exception;

/**
 * 资源未找到异常类
 * 用于处理资源不存在的情况
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
