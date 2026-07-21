package com.badminton.bmp.modules.coach.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface CoachStudentRelationMapper {

    @Select("SELECT COUNT(*) FROM biz_coach_student "
            + "WHERE coach_id = #{coachId} AND member_id = #{memberId} AND del_flag = 0")
    int existsActive(@Param("coachId") Long coachId, @Param("memberId") Long memberId);

    @Insert("INSERT INTO biz_coach_student "
            + "(coach_id, member_id, bind_type, del_flag, create_time, update_time) "
            + "VALUES (#{coachId}, #{memberId}, #{bindType}, 0, NOW(), NOW()) "
            + "ON DUPLICATE KEY UPDATE "
            + "bind_type = IF(del_flag = 1, #{bindType}, bind_type), "
            + "del_flag = 0, update_time = NOW()")
    int activate(@Param("coachId") Long coachId,
                 @Param("memberId") Long memberId,
                 @Param("bindType") String bindType);

    @Select("SELECT coach_id FROM biz_coach_student WHERE id = #{id} AND del_flag = 0")
    Long findCoachIdByRelationId(@Param("id") Long id);

    @Update("UPDATE biz_coach_student SET del_flag = 1, update_time = NOW() "
            + "WHERE id = #{id} AND del_flag = 0")
    int deactivate(@Param("id") Long id);
}
