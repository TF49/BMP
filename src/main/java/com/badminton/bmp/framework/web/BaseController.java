package com.badminton.bmp.framework.web;

import com.badminton.bmp.common.Result;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller基类
 * 参照CMS实现方式
 */
@RestController
public class BaseController {
    
    protected <T> Result<T> success(T data) {
        return Result.success(data);
    }
    
    protected <T> Result<T> error(String message) {
        return Result.error(message);
    }
}
