package com.badminton.bmp.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

/**
 * 身份证号验证器
 */
public class IdCardValidator implements ConstraintValidator<IdCardValid, String> {

    private static final Pattern ID_CARD_15_PATTERN = Pattern.compile("^[1-9]\\d{7}((0\\d)|(1[0-2]))(([0|1|2]\\d)|3[0-1])\\d{3}$");
    private static final Pattern ID_CARD_18_PATTERN = Pattern.compile("^[1-9]\\d{5}[1-9]\\d{3}((0\\d)|(1[0-2]))(([0|1|2]\\d)|3[0-1])\\d{3}([0-9Xx])$");

    private static final int[] WEIGHT = {7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2};
    private static final char[] CHECK_CODE = {'1', '0', 'X', '9', '8', '7', '6', '5', '4', '3', '2'};

    @Override
    public void initialize(IdCardValid constraintAnnotation) {
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.trim().isEmpty()) {
            return true; // 空值由 @NotBlank 处理
        }

        value = value.trim().toUpperCase();

        // 验证15位身份证
        if (value.length() == 15) {
            return ID_CARD_15_PATTERN.matcher(value).matches();
        }

        // 验证18位身份证
        if (value.length() == 18) {
            if (!ID_CARD_18_PATTERN.matcher(value).matches()) {
                return false;
            }

            // 验证校验码
            return validateCheckCode(value);
        }

        return false;
    }

    /**
     * 验证18位身份证的校验码
     */
    private boolean validateCheckCode(String idCard) {
        try {
            int sum = 0;
            for (int i = 0; i < 17; i++) {
                sum += Character.getNumericValue(idCard.charAt(i)) * WEIGHT[i];
            }
            char checkCode = CHECK_CODE[sum % 11];
            return checkCode == idCard.charAt(17);
        } catch (Exception e) {
            return false;
        }
    }
}
