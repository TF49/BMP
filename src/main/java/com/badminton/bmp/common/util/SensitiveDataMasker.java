package com.badminton.bmp.common.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Pattern;

/**
 * 敏感数据脱敏工具类
 * 用于审计日志中的敏感信息掩码处理
 */
public class SensitiveDataMasker {

    private static final Logger log = LoggerFactory.getLogger(SensitiveDataMasker.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    // 手机号正则（中国大陆）
    private static final Pattern PHONE_PATTERN = Pattern.compile("1[3-9]\\d{9}");
    
    // 身份证号正则（18位）
    private static final Pattern ID_CARD_PATTERN = Pattern.compile("\\d{17}[\\dXx]");
    
    // 银行卡号正则（13-19位）
    private static final Pattern BANK_CARD_PATTERN = Pattern.compile("\\d{13,19}");

    /**
     * 对JSON字符串中的敏感字段进行脱敏
     * 
     * @param jsonData 原始JSON数据
     * @return 脱敏后的JSON字符串
     */
    public static String maskSensitiveFields(String jsonData) {
        if (jsonData == null || jsonData.trim().isEmpty()) {
            return jsonData;
        }

        try {
            JsonNode rootNode = objectMapper.readTree(jsonData);
            if (rootNode.isObject()) {
                ObjectNode objectNode = (ObjectNode) rootNode;
                maskObjectNode(objectNode);
                return objectMapper.writeValueAsString(objectNode);
            }
            return jsonData;
        } catch (Exception e) {
            log.warn("敏感数据脱敏失败，返回原始数据: {}", e.getMessage());
            return jsonData;
        }
    }

    /**
     * 递归处理JSON对象节点
     */
    private static void maskObjectNode(ObjectNode node) {
        node.fieldNames().forEachRemaining(fieldName -> {
            JsonNode fieldValue = node.get(fieldName);
            
            // 根据字段名判断是否需要脱敏
            String lowerFieldName = fieldName.toLowerCase();
            
            if (fieldValue.isTextual()) {
                String originalValue = fieldValue.asText();
                String maskedValue = maskFieldByName(lowerFieldName, originalValue);
                if (!originalValue.equals(maskedValue)) {
                    node.put(fieldName, maskedValue);
                }
            } else if (fieldValue.isObject()) {
                // 递归处理嵌套对象
                maskObjectNode((ObjectNode) fieldValue);
            } else if (fieldValue.isArray()) {
                // 处理数组（暂不支持，可根据需要扩展）
                // 数组中的对象也需要脱敏
            }
        });
    }

    /**
     * 根据字段名和值进行脱敏
     */
    private static String maskFieldByName(String fieldName, String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }

        // 手机号字段
        if (fieldName.contains("phone") || fieldName.contains("mobile") || fieldName.contains("tel")) {
            return maskPhone(value);
        }

        // 身份证号字段
        if (fieldName.contains("idcard") || fieldName.contains("idno") || 
            fieldName.contains("identitycard") || fieldName.contains("identity_card")) {
            return maskIdCard(value);
        }

        // 银行卡号字段
        if (fieldName.contains("bankcard") || fieldName.contains("cardno") || 
            fieldName.contains("bank_card") || fieldName.contains("accountno")) {
            return maskBankCard(value);
        }

        // 邮箱字段
        if (fieldName.contains("email") || fieldName.contains("mail")) {
            return maskEmail(value);
        }

        // 姓名字段（仅脱敏中间字）
        if (fieldName.contains("name") && !fieldName.contains("username") && 
            !fieldName.contains("nickname") && value.length() >= 2 && value.length() <= 10) {
            return maskName(value);
        }

        // 密码字段（完全隐藏）
        if (fieldName.contains("password") || fieldName.contains("passwd") || fieldName.contains("pwd")) {
            return "******";
        }

        return value;
    }

    /**
     * 手机号脱敏: 138****1234
     */
    public static String maskPhone(String phone) {
        if (phone == null || phone.length() != 11) {
            return phone;
        }
        if (PHONE_PATTERN.matcher(phone).matches()) {
            return phone.substring(0, 3) + "****" + phone.substring(7);
        }
        return phone;
    }

    /**
     * 身份证号脱敏: 110101********1234
     */
    public static String maskIdCard(String idCard) {
        if (idCard == null || idCard.length() != 18) {
            return idCard;
        }
        if (ID_CARD_PATTERN.matcher(idCard).matches()) {
            return idCard.substring(0, 6) + "********" + idCard.substring(14);
        }
        return idCard;
    }

    /**
     * 银行卡号脱敏: 6222 **** **** 1234
     */
    public static String maskBankCard(String bankCard) {
        if (bankCard == null || bankCard.length() < 13 || bankCard.length() > 19) {
            return bankCard;
        }
        if (BANK_CARD_PATTERN.matcher(bankCard).matches()) {
            int len = bankCard.length();
            // 保留前4位和后4位
            String prefix = bankCard.substring(0, 4);
            String suffix = bankCard.substring(len - 4);
            int maskLen = len - 8;
            String mask = "*".repeat(Math.max(0, maskLen));
            return prefix + " " + mask + " " + suffix;
        }
        return bankCard;
    }

    /**
     * 邮箱脱敏: abc****@example.com
     */
    public static String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return email;
        }
        String[] parts = email.split("@");
        if (parts.length != 2) {
            return email;
        }
        String username = parts[0];
        String domain = parts[1];
        
        if (username.length() <= 3) {
            return username.charAt(0) + "***@" + domain;
        } else {
            return username.substring(0, 3) + "****@" + domain;
        }
    }

    /**
     * 姓名脱敏: 张*、张*三、欧阳**
     */
    public static String maskName(String name) {
        if (name == null || name.length() < 2) {
            return name;
        }
        
        int len = name.length();
        if (len == 2) {
            // 两个字: 张*
            return name.charAt(0) + "*";
        } else if (len == 3) {
            // 三个字: 张*三
            return name.charAt(0) + "*" + name.charAt(2);
        } else {
            // 四个字及以上: 欧阳**, 司马**
            return name.substring(0, 2) + "*".repeat(len - 2);
        }
    }

    /**
     * 地址脱敏（保留省市，隐藏详细地址）
     * 例: 北京市朝阳区****
     */
    public static String maskAddress(String address) {
        if (address == null || address.length() < 6) {
            return address;
        }
        
        // 简单实现：保留前6个字符（通常是省市区），其余用****代替
        if (address.length() <= 10) {
            return address.substring(0, 4) + "****";
        } else {
            return address.substring(0, 6) + "****";
        }
    }

    /**
     * 通用脱敏方法（自动识别）
     * 根据内容模式自动判断类型并脱敏
     */
    public static String maskAuto(String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }

        // 检测手机号
        if (PHONE_PATTERN.matcher(value).matches()) {
            return maskPhone(value);
        }

        // 检测身份证号
        if (ID_CARD_PATTERN.matcher(value).matches()) {
            return maskIdCard(value);
        }

        // 检测银行卡号
        if (BANK_CARD_PATTERN.matcher(value).matches()) {
            return maskBankCard(value);
        }

        // 检测邮箱
        if (value.contains("@") && value.contains(".")) {
            return maskEmail(value);
        }

        return value;
    }

    /**
     * 检查字符串是否包含敏感信息
     */
    public static boolean containsSensitiveInfo(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }

        return PHONE_PATTERN.matcher(text).find() ||
               ID_CARD_PATTERN.matcher(text).find() ||
               BANK_CARD_PATTERN.matcher(text).find() ||
               text.contains("password") ||
               text.contains("密码");
    }

    /**
     * 批量脱敏字符串中的所有敏感信息
     */
    public static String maskAllSensitiveInfo(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        String result = text;

        // 脱敏手机号
        result = PHONE_PATTERN.matcher(result).replaceAll(match -> {
            String phone = match.group();
            return maskPhone(phone);
        });

        // 脱敏身份证号
        result = ID_CARD_PATTERN.matcher(result).replaceAll(match -> {
            String idCard = match.group();
            return maskIdCard(idCard);
        });

        // 脱敏银行卡号
        result = BANK_CARD_PATTERN.matcher(result).replaceAll(match -> {
            String bankCard = match.group();
            return maskBankCard(bankCard);
        });

        return result;
    }
}
