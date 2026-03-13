package com.badminton.bmp.modules.system.service.impl;

import com.badminton.bmp.modules.system.entity.UserSetting;
import com.badminton.bmp.modules.system.mapper.UserSettingMapper;
import com.badminton.bmp.modules.system.service.UserSettingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class UserSettingServiceImpl implements UserSettingService {

    @Autowired
    private UserSettingMapper userSettingMapper;

    @Override
    public List<UserSetting> getUserSettings(Long userId) {
        return userSettingMapper.findByUserId(userId);
    }

    @Override
    public String getSetting(Long userId, String key) {
        List<UserSetting> settings = userSettingMapper.findByUserId(userId);
        return settings.stream()
                .filter(s -> s.getSettingKey().equals(key))
                .map(UserSetting::getSettingValue)
                .findFirst()
                .orElse(null);
    }

    @Override
    @Transactional
    public void updateSetting(Long userId, String key, String value) {
        userSettingMapper.deleteByUserIdAndKey(userId, key);
        UserSetting setting = new UserSetting();
        setting.setUserId(userId);
        setting.setSettingKey(key);
        setting.setSettingValue(value);
        userSettingMapper.insert(setting);
    }

    @Override
    @Transactional
    public void batchUpdateSettings(Long userId, Map<String, String> settings) {
        settings.forEach((key, value) -> updateSetting(userId, key, value));
    }

    @Override
    @Transactional
    public void deleteAllSettings(Long userId) {
        userSettingMapper.deleteByUserId(userId);
    }

    @Override
    @Transactional
    public void deleteSetting(Long userId, String key) {
        userSettingMapper.deleteByUserIdAndKey(userId, key);
    }
}
