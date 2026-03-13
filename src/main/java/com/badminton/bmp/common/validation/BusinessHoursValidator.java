package com.badminton.bmp.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

/**
 * 营业时间验证器
 */
public class BusinessHoursValidator implements ConstraintValidator<BusinessHoursValid, String> {

    private static final Pattern BUSINESS_HOURS_PATTERN =
        Pattern.compile("^([01]\\d|2[0-3]):[0-5]\\d-([01]\\d|2[0-3]):[0-5]\\d$");

    @Override
    public void initialize(BusinessHoursValid constraintAnnotation) {
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.trim().isEmpty()) {
            return true; // 空值由 @NotBlank 处理
        }

        if (!BUSINESS_HOURS_PATTERN.matcher(value).matches()) {
            return false;
        }

        // 验证开始时间小于结束时间
        String[] parts = value.split("-");
        if (parts.length != 2) {
            return false;
        }

        try {
            String[] startParts = parts[0].split(":");
            String[] endParts = parts[1].split(":");

            int startHour = Integer.parseInt(startParts[0]);
            int startMinute = Integer.parseInt(startParts[1]);
            int endHour = Integer.parseInt(endParts[0]);
            int endMinute = Integer.parseInt(endParts[1]);

            int startMinutes = startHour * 60 + startMinute;
            int endMinutes = endHour * 60 + endMinute;

            return startMinutes < endMinutes;
        } catch (Exception e) {
            return false;
        }
    }
}
