package com.badminton.bmp.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * 状态值验证器
 * 验证状态值是否在允许的范围内
 */
public class StatusValidator implements ConstraintValidator<StatusValid, Integer> {

    private Set<Integer> allowedValues;

    @Override
    public void initialize(StatusValid constraintAnnotation) {
        this.allowedValues = new HashSet<>();
        for (int value : constraintAnnotation.allowedValues()) {
            allowedValues.add(value);
        }
    }

    @Override
    public boolean isValid(Integer value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        return allowedValues.isEmpty() || allowedValues.contains(value);
    }
}
