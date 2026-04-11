package com.badminton.bmp.framework.web;

import com.badminton.bmp.common.Result;
import com.badminton.bmp.common.util.ErrorMessageSanitizer;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller 基类，统一返回成功和用户可读的错误信息。
 */
@RestController
public class BaseController {

    protected <T> Result<T> success(T data) {
        return Result.success(data);
    }

    protected <T> Result<T> error(String message) {
        return Result.error(ErrorMessageSanitizer.sanitize(message));
    }

    protected <T> Result<T> badRequest(String message) {
        return Result.badRequest(ErrorMessageSanitizer.sanitize(message));
    }

    protected <T> Result<T> operationError(String action) {
        return Result.error(action + "失败，请稍后重试");
    }

    protected <T> Result<T> operationError() {
        return Result.error("操作失败，请稍后重试");
    }

    protected <T> Result<T> runtimeError(RuntimeException e, String action) {
        String message = ErrorMessageSanitizer.sanitize(e.getMessage());
        if (!ErrorMessageSanitizer.isGenericOperationMessage(message)) {
            return Result.error(message);
        }
        return operationError(action);
    }

    protected <T> Result<T> runtimeError(RuntimeException e) {
        String message = ErrorMessageSanitizer.sanitize(e.getMessage());
        if (!ErrorMessageSanitizer.isGenericOperationMessage(message)) {
            return Result.error(message);
        }
        return operationError();
    }

    protected String userFriendlyErrorText(String message) {
        return ErrorMessageSanitizer.sanitize(message);
    }

    protected String userFriendlyErrorText(String action, Throwable throwable) {
        return ErrorMessageSanitizer.userFriendlyError(action, throwable);
    }
}
