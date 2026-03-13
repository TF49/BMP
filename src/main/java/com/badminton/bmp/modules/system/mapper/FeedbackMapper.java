package com.badminton.bmp.modules.system.mapper;

import com.badminton.bmp.modules.system.entity.Feedback;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;

@Mapper
public interface FeedbackMapper {

    @Insert("INSERT INTO biz_feedback (user_id, content, contact, create_time) VALUES (#{userId}, #{content}, #{contact}, #{createTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Feedback feedback);
}
