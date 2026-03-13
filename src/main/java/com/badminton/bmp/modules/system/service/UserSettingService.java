package com.badminton.bmp.modules.system.service;

import com.badminton.bmp.modules.system.entity.UserSetting;
import java.util.List;
import java.util.Map;

/**
 * 用户设置服务接口
 */
public interface UserSettingService {

    /**
     * 获取用户所有设置
     */
    List<UserSetting> getUserSettings(Long userId);

    /**
     * 获取用户特定设置值
     */
    String getSetting(Long userId, String settingKey);

    /**
     * 保存或更新用户设置
     */
    void updateSetting(Long userId, String settingKey, String settingValue);

    /**
     * 批量保存用户设置
     */
    void batchUpdateSettings(Long userId, Map<String, String> settings);

    /**
     * 删除用户特定设置
     */
    void deleteSetting(Long userId, String settingKey);

    /**
     * 删除用户所有设置
     */
    void deleteAllSettings(Long userId);
}
