package com.badminton.bmp.common.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * 状态值验证注解
 * 验证状态值是否在允许的范围内
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = StatusValidator.class)
@Documented
public @interface StatusValid {

    String message() default "状态值不合法";

    int[] allowedValues() default {};

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
