package com.badminton.bmp.common;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 通用分页响应构建器，替代各 Controller 中重复的 Map 手动拼装。
 */
public final class PageResult {

    private PageResult() {}

    public static Map<String, Object> of(List<?> data, int total, int page, int size) {
        Map<String, Object> result = new HashMap<>();
        result.put("data", data);
        result.put("total", total);
        result.put("page", page);
        result.put("size", size);
        result.put("pages", (total + size - 1) / size);
        return result;
    }
}
