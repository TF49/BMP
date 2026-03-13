package com.badminton.bmp.modules.system.mapper;

import com.badminton.bmp.modules.system.entity.UserSetting;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface UserSettingMapper {

    @Select("SELECT * FROM sys_user_setting WHERE user_id = #{userId}")
    List<UserSetting> findByUserId(@Param("userId") Long userId);

    @Select("SELECT * FROM sys_user_setting WHERE user_id = #{userId} AND setting_key = #{settingKey}")
    UserSetting findByUserIdAndKey(@Param("userId") Long userId, @Param("settingKey") String settingKey);

    @Delete("DELETE FROM sys_user_setting WHERE user_id = #{userId}")
    int deleteByUserId(@Param("userId") Long userId);

    @Insert("INSERT INTO sys_user_setting (user_id, setting_key, setting_value) VALUES (#{userId}, #{settingKey}, #{settingValue})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(UserSetting setting);

    @Update("UPDATE sys_user_setting SET setting_value = #{settingValue} WHERE user_id = #{userId} AND setting_key = #{settingKey}")
    int updateByUserIdAndKey(UserSetting setting);

    @Delete("DELETE FROM sys_user_setting WHERE user_id = #{userId} AND setting_key = #{settingKey}")
    int deleteByUserIdAndKey(@Param("userId") Long userId, @Param("settingKey") String settingKey);
}
