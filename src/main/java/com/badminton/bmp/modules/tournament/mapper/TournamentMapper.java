package com.badminton.bmp.modules.tournament.mapper;

import com.badminton.bmp.modules.tournament.entity.Tournament;
import org.apache.ibatis.annotations.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface TournamentMapper {
    @Select("SELECT t.*, IFNULL(v.venue_name, '') as venue_name FROM biz_tournament t " +
            "LEFT JOIN sys_venue v ON t.venue_id = v.id " +
            "WHERE t.id = #{id} AND t.del_flag = 0")
    Tournament findById(@Param("id") Long id);

    @Select("<script>" +
            "SELECT t.*, IFNULL(v.venue_name, '') as venue_name FROM biz_tournament t " +
            "LEFT JOIN sys_venue v ON t.venue_id = v.id " +
            "WHERE t.del_flag = 0 " +
            "<if test='venueId != null'> AND t.venue_id = #{venueId} </if>" +
            "<if test='status != null'> AND t.status = #{status} </if>" +
            "<if test='excludeCancelled != null and excludeCancelled'> AND t.status != 0 </if>" +
            "<if test='type != null and type != \"\"'> AND t.tournament_type = #{type} </if>" +
            "<if test='keyword != null and keyword != \"\"'> " +
            "AND t.tournament_name LIKE CONCAT('%', #{keyword}, '%') </if>" +
            "<if test='startTime != null'> AND t.tournament_start &gt;= #{startTime} </if>" +
            "<if test='endTime != null'> AND t.tournament_start &lt;= #{endTime} </if>" +
            "ORDER BY t.tournament_start DESC " +
            "LIMIT #{offset}, #{limit}" +
            "</script>")
    List<Tournament> findAll(@Param("venueId") Long venueId,
                             @Param("status") Integer status,
                             @Param("excludeCancelled") Boolean excludeCancelled,
                             @Param("type") String type,
                             @Param("keyword") String keyword,
                             @Param("startTime") LocalDateTime startTime,
                             @Param("endTime") LocalDateTime endTime,
                             @Param("offset") int offset,
                             @Param("limit") int limit);

    @Select("<script>" +
            "SELECT COUNT(*) FROM biz_tournament t WHERE t.del_flag = 0 " +
            "<if test='venueId != null'> AND t.venue_id = #{venueId} </if>" +
            "<if test='status != null'> AND t.status = #{status} </if>" +
            "<if test='excludeCancelled != null and excludeCancelled'> AND t.status != 0 </if>" +
            "<if test='type != null and type != \"\"'> AND t.tournament_type = #{type} </if>" +
            "<if test='keyword != null and keyword != \"\"'> " +
            "AND t.tournament_name LIKE CONCAT('%', #{keyword}, '%') </if>" +
            "<if test='startTime != null'> AND t.tournament_start &gt;= #{startTime} </if>" +
            "<if test='endTime != null'> AND t.tournament_start &lt;= #{endTime} </if>" +
            "</script>")
    int count(@Param("venueId") Long venueId,
              @Param("status") Integer status,
              @Param("excludeCancelled") Boolean excludeCancelled,
              @Param("type") String type,
              @Param("keyword") String keyword,
              @Param("startTime") LocalDateTime startTime,
              @Param("endTime") LocalDateTime endTime);

    @Insert("INSERT INTO biz_tournament (tournament_name, tournament_type, venue_id, max_participants, " +
            "current_participants, registration_start, registration_end, tournament_start, tournament_end, " +
            "entry_fee, prize_info, description, status, create_time, update_time, del_flag) " +
            "VALUES (#{tournamentName}, #{tournamentType}, #{venueId}, #{maxParticipants}, " +
            "#{currentParticipants}, #{registrationStart}, #{registrationEnd}, #{tournamentStart}, #{tournamentEnd}, " +
            "#{entryFee}, #{prizeInfo}, #{description}, #{status}, #{createTime}, #{updateTime}, 0)")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Tournament tournament);

    @Update("UPDATE biz_tournament SET tournament_name = #{tournamentName}, tournament_type = #{tournamentType}, " +
            "venue_id = #{venueId}, max_participants = #{maxParticipants}, " +
            "registration_start = #{registrationStart}, registration_end = #{registrationEnd}, " +
            "tournament_start = #{tournamentStart}, tournament_end = #{tournamentEnd}, " +
            "entry_fee = #{entryFee}, prize_info = #{prizeInfo}, description = #{description}, status = #{status}, " +
            "update_time = #{updateTime} WHERE id = #{id} AND del_flag = 0")
    int update(Tournament tournament);

    @Update("UPDATE biz_tournament SET del_flag = 1, update_time = NOW() WHERE id = #{id}")
    int deleteById(@Param("id") Long id);

    @Update("UPDATE biz_tournament SET status = #{status}, update_time = NOW() WHERE id = #{id} AND del_flag = 0")
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    @Update("UPDATE biz_tournament SET current_participants = #{currentParticipants}, update_time = NOW() " +
            "WHERE id = #{id} AND del_flag = 0")
    int updateCurrentParticipants(@Param("id") Long id, @Param("currentParticipants") Integer currentParticipants);

    @Select("SELECT CAST(status AS SIGNED) AS status, COUNT(*) as count FROM biz_tournament WHERE del_flag = 0 GROUP BY status")
    List<Map<String, Object>> countByStatus();

    @Select("SELECT COUNT(*) FROM biz_tournament WHERE del_flag = 0")
    int countAll();

    /**
     * 定时任务：将「报名中」且已到比赛开始时间的赛事改为「进行中」
     * 条件：status=1 且 tournament_start <= 当前时间
     */
    @Update("UPDATE biz_tournament SET status = 2, update_time = NOW() WHERE del_flag = 0 AND status = 1 " +
            "AND tournament_start <= #{now}")
    int batchUpdateEnrollingToOngoing(@Param("now") LocalDateTime now);

    /**
     * 定时任务：将「进行中」且已过比赛结束时间的赛事改为「已结束」
     * 条件：status=2 且 tournament_end <= 当前时间
     */
    @Update("UPDATE biz_tournament SET status = 3, update_time = NOW() WHERE del_flag = 0 AND status = 2 " +
            "AND tournament_end <= #{now}")
    int batchUpdateOngoingToFinished(@Param("now") LocalDateTime now);
}
