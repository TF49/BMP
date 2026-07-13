package com.badminton.bmp.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
public class RateLimitException extends BusinessException {
    public RateLimitException(String message) {
        super(429, message);
    }
}
