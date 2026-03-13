package com.badminton.bmp.modules.court.mapper;

import com.badminton.bmp.modules.court.entity.Court;
import org.apache.ibatis.annotations.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 场地Mapper接口
 * 用于数据库操作
 */
@Mapper
public interface CourtMapper {

    /**
     * 根据ID查找场地
     * @param id 场地ID
     * @return 场地对象
     */
    @Select("SELECT c.*, v.venue_name FROM sys_court c " +
            "LEFT JOIN sys_venue v ON c.venue_id = v.id " +
            "WHERE c.id = #{id} AND c.del_flag = 0")
    Court findById(@Param("id") Long id);

    /**
     * 查找所有场地（支持条件筛选和分页）
     * @param venueId 场馆ID（可选）
     * @param status 状态（可选）
     * @param keyword 关键词（匹配场地编号或名称）
     * @param offset 偏移量
     * @param limit 限制数量
     * @return 场地列表
     */
    @Select("<script>" +
            "SELECT c.*, v.venue_name FROM sys_court c " +
            "LEFT JOIN sys_venue v ON c.venue_id = v.id " +
            "WHERE c.del_flag = 0 " +
            "<if test='venueId != null'> AND c.venue_id = #{venueId} </if>" +
            "<if test='status != null'> AND c.status = #{status} </if>" +
            "<if test='keyword != null and keyword != \"\"'> " +
            "AND (c.court_code LIKE CONCAT('%', #{keyword}, '%') " +
            "OR c.court_name LIKE CONCAT('%', #{keyword}, '%')) </if>" +
            "ORDER BY c.venue_id, c.court_code " +
            "LIMIT #{offset}, #{limit}" +
            "</script>")
    List<Court> findAll(@Param("venueId") Long venueId,
                        @Param("status") Integer status,
                        @Param("keyword") String keyword,
                        @Param("offset") int offset,
                        @Param("limit") int limit);

    /**
     * 统计场地数量（支持条件筛选）
     * @param venueId 场馆ID（可选）
     * @param status 状态（可选）
     * @param keyword 关键词（匹配场地编号或名称）
     * @return 场地数量
     */
    @Select("<script>" +
            "SELECT COUNT(*) FROM sys_court c " +
            "WHERE c.del_flag = 0 " +
            "<if test='venueId != null'> AND c.venue_id = #{venueId} </if>" +
            "<if test='status != null'> AND c.status = #{status} </if>" +
            "<if test='keyword != null and keyword != \"\"'> " +
            "AND (c.court_code LIKE CONCAT('%', #{keyword}, '%') " +
            "OR c.court_name LIKE CONCAT('%', #{keyword}, '%')) </if>" +
            "</script>")
    int count(@Param("venueId") Long venueId,
              @Param("status") Integer status,
              @Param("keyword") String keyword);

    /**
     * 插入新场地
     * @param court 场地对象
     * @return 影响的行数
     */
    @Insert("INSERT INTO sys_court (court_code, venue_id, court_name, billing_type, " +
            "price_per_hour, status, create_time, update_time, del_flag) " +
            "VALUES (#{courtCode}, #{venueId}, #{courtName}, #{billingType}, " +
            "#{pricePerHour}, #{status}, #{createTime}, #{updateTime}, 0)")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Court court);

    /**
     * 更新场地信息
     * @param court 场地对象
     * @return 影响的行数
     */
    @Update("UPDATE sys_court SET court_code = #{courtCode}, venue_id = #{venueId}, " +
            "court_name = #{courtName}, billing_type = #{billingType}, " +
            "price_per_hour = #{pricePerHour}, status = #{status}, " +
            "update_time = #{updateTime} WHERE id = #{id} AND del_flag = 0")
    int update(Court court);

    /**
     * 物理删除场地
     * 说明：直接删除记录，触发外键 ON DELETE 规则
     * @param id 场地ID
     * @return 影响的行数
     */
    @Delete("DELETE FROM sys_court WHERE id = #{id}")
    int deleteById(@Param("id") Long id);

    /**
     * 更新场地状态
     * @param id 场地ID
     * @param status 新状态
     * @return 影响的行数
     */
    @Update("UPDATE sys_court SET status = #{status}, update_time = NOW() " +
            "WHERE id = #{id} AND del_flag = 0")
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    /**
     * 检查同一场馆下是否存在相同编号的场地
     * @param venueId 场馆ID
     * @param courtCode 场地编号
     * @param excludeId 排除的场地ID（用于更新时排除自身）
     * @return 存在返回true，否则返回false
     */
    @Select("<script>" +
            "SELECT COUNT(*) > 0 FROM sys_court " +
            "WHERE venue_id = #{venueId} AND court_code = #{courtCode} AND del_flag = 0 " +
            "<if test='excludeId != null'> AND id != #{excludeId} </if>" +
            "</script>")
    boolean existsByVenueIdAndCode(@Param("venueId") Long venueId,
                                   @Param("courtCode") String courtCode,
                                   @Param("excludeId") Long excludeId);

    /**
     * 统计指定场馆下的场地数量
     * @param venueId 场馆ID
     * @return 场地数量
     */
    @Select("SELECT COUNT(*) FROM sys_court WHERE venue_id = #{venueId} AND del_flag = 0")
    int countByVenueId(@Param("venueId") Long venueId);

    /**
     * 统计各状态的场地数量
     * @return 各状态数量
     */
    @Select("SELECT CAST(status AS SIGNED) AS status, COUNT(*) as count FROM sys_court " +
            "WHERE del_flag = 0 GROUP BY status")
    List<java.util.Map<String, Object>> countByStatus();

    /**
     * 统计所有场地数量
     * @return 场地总数
     */
    @Select("SELECT COUNT(*) FROM sys_court WHERE del_flag = 0")
    int countAll();

    /**
     * 查询指定场地在指定日期的所有有效预约（联表查询会员信息）
     * 有效预约状态：1-待支付，2-已支付，3-进行中
     * @param courtId 场地ID
     * @param bookingDate 预约日期
     * @return 预约信息列表（包含会员信息）
     */
    @Select("SELECT " +
            "b.id AS booking_id, " +
            "b.start_time, " +
            "b.end_time, " +
            "b.status, " +
            "m.member_name, " +
            "m.member_type, " +
            "m.member_level " +
            "FROM biz_booking b " +
            "LEFT JOIN sys_member m ON b.member_id = m.id " +
            "WHERE b.court_id = #{courtId} " +
            "AND b.booking_date = #{bookingDate} " +
            "AND b.status IN (1, 2, 3) " +
            "AND b.del_flag = 0 " +
            "ORDER BY b.start_time ASC")
    List<Map<String, Object>> findTodayBookingUsers(@Param("courtId") Long courtId,
                                                     @Param("bookingDate") LocalDate bookingDate);

    /**
     * 批量查询多个场地在指定日期的预约数量
     * @param courtIds 场地ID列表
     * @param bookingDate 预约日期
     * @return 场地ID与预约数量的映射
     */
    @Select("<script>" +
            "SELECT court_id, COUNT(*) AS booking_count " +
            "FROM biz_booking " +
            "WHERE court_id IN " +
            "<foreach item='id' collection='courtIds' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach> " +
            "AND booking_date = #{bookingDate} " +
            "AND status IN (1, 2, 3) " +
            "AND del_flag = 0 " +
            "GROUP BY court_id" +
            "</script>")
    List<Map<String, Object>> countTodayBookingsByCourtIds(@Param("courtIds") List<Long> courtIds,
                                                           @Param("bookingDate") LocalDate bookingDate);

    /**
     * 统计指定日期新增场地数量（按 create_time）
     */
    @Select("<script>" +
            "SELECT COUNT(*) FROM sys_court " +
            "WHERE del_flag = 0 AND DATE(create_time) = #{date} " +
            "<if test='venueId != null'> AND venue_id = #{venueId} </if>" +
            "</script>")
    int countCreatedOnDate(@Param("venueId") Long venueId,
                           @Param("date") java.time.LocalDate date);
}
