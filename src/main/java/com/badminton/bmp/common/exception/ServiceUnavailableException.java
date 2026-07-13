package com.badminton.bmp.common.exception;

import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
public class ServiceUnavailableException extends BusinessException {
    private final String traceId;

    public ServiceUnavailableException(String message) {
        super(503, message);
        this.traceId = UUID.randomUUID().toString().replace("-", "");
    }

    public String getTraceId() {
        return traceId;
    }
}
