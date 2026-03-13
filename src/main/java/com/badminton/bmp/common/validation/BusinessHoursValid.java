package com.badminton.bmp.common.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * 营业时间验证注解
 * 格式：HH:mm-HH:mm，例如：08:00-22:00
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = BusinessHoursValidator.class)
@Documented
public @interface BusinessHoursValid {

    String message() default "营业时间格式不正确，应为 HH:mm-HH:mm 格式，例如：08:00-22:00";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
