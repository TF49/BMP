package com.badminton.bmp.modules.booking.mapper;

import com.badminton.bmp.modules.booking.entity.Booking;
import org.apache.ibatis.annotations.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

/**
 * 场地预约Mapper接口
 */
@Mapper
public interface BookingMapper {

    /**
     * 根据ID查询预约记录（联表查询会员、场地、场馆信息）
     */
    @Select("SELECT b.*, m.member_name FROM biz_booking b " +
            "LEFT JOIN sys_member m ON b.member_id = m.id " +
            "WHERE b.id = #{id} AND b.del_flag = 0")
    Booking findById(@Param("id") Long id);

    /**
     * 分页查询预约记录（支持条件筛选，联表查询）
     */
    @Select("<script>" +
            "SELECT b.*, m.member_name FROM biz_booking b " +
            "LEFT JOIN sys_member m ON b.member_id = m.id " +
            "WHERE b.del_flag = 0 " +
            "<if test='memberId != null'> AND b.member_id = #{memberId} </if>" +
            "<if test='status != null'> AND b.status = #{status} </if>" +
            "<if test='venueId != null'> AND EXISTS (" +
            "SELECT 1 FROM biz_booking_court bc " +
            "LEFT JOIN sys_court c ON bc.court_id = c.id " +
            "WHERE bc.booking_id = b.id AND c.venue_id = #{venueId}) </if>" +
            "<if test='courtId != null'> AND EXISTS (" +
            "SELECT 1 FROM biz_booking_court bc " +
            "WHERE bc.booking_id = b.id AND bc.court_id = #{courtId}) </if>" +
            "<if test='startDate != null'> AND b.booking_date &gt;= #{startDate} </if>" +
            "<if test='endDate != null'> AND b.booking_date &lt;= #{endDate} </if>" +
            "<if test='keyword != null and keyword != \"\"'> " +
            "AND (b.booking_no LIKE CONCAT('%', #{keyword}, '%') " +
            "OR m.member_name LIKE CONCAT('%', #{keyword}, '%') " +
            "OR EXISTS (SELECT 1 FROM biz_booking_court bc " +
            "LEFT JOIN sys_court c ON bc.court_id = c.id " +
            "LEFT JOIN sys_venue v ON c.venue_id = v.id " +
            "WHERE bc.booking_id = b.id AND (c.court_name LIKE CONCAT('%', #{keyword}, '%') " +
            "OR v.venue_name LIKE CONCAT('%', #{keyword}, '%')))) </if>" +
            "ORDER BY b.create_time DESC " +
            "LIMIT #{offset}, #{limit}" +
            "</script>")
    List<Booking> findAll(@Param("venueId") Long venueId,
                          @Param("memberId") Long memberId,
                          @Param("courtId") Long courtId,
                          @Param("status") Integer status,
                          @Param("keyword") String keyword,
                          @Param("startDate") LocalDate startDate,
                          @Param("endDate") LocalDate endDate,
                          @Param("offset") int offset,
                          @Param("limit") int limit);

    /**
     * 统计预约记录数量（支持条件筛选）
     */
    @Select("<script>" +
            "SELECT COUNT(*) FROM biz_booking b " +
            "LEFT JOIN sys_member m ON b.member_id = m.id " +
            "WHERE b.del_flag = 0 " +
            "<if test='memberId != null'> AND b.member_id = #{memberId} </if>" +
            "<if test='status != null'> AND b.status = #{status} </if>" +
            "<if test='venueId != null'> AND EXISTS (" +
            "SELECT 1 FROM biz_booking_court bc LEFT JOIN sys_court c ON bc.court_id = c.id " +
            "WHERE bc.booking_id = b.id AND c.venue_id = #{venueId}) </if>" +
            "<if test='courtId != null'> AND EXISTS (" +
            "SELECT 1 FROM biz_booking_court bc WHERE bc.booking_id = b.id AND bc.court_id = #{courtId}) </if>" +
            "<if test='startDate != null'> AND b.booking_date &gt;= #{startDate} </if>" +
            "<if test='endDate != null'> AND b.booking_date &lt;= #{endDate} </if>" +
            "<if test='keyword != null and keyword != \"\"'> " +
            "AND (b.booking_no LIKE CONCAT('%', #{keyword}, '%') " +
            "OR m.member_name LIKE CONCAT('%', #{keyword}, '%') " +
            "OR EXISTS (SELECT 1 FROM biz_booking_court bc " +
            "LEFT JOIN sys_court c ON bc.court_id = c.id " +
            "LEFT JOIN sys_venue v ON c.venue_id = v.id " +
            "WHERE bc.booking_id = b.id AND (c.court_name LIKE CONCAT('%', #{keyword}, '%') " +
            "OR v.venue_name LIKE CONCAT('%', #{keyword}, '%')))) </if>" +
            "</script>")
    int count(@Param("venueId") Long venueId,
              @Param("memberId") Long memberId,
              @Param("courtId") Long courtId,
              @Param("status") Integer status,
              @Param("keyword") String keyword,
              @Param("startDate") LocalDate startDate,
              @Param("endDate") LocalDate endDate);

    /**
     * 插入预约记录
     */
    @Insert("INSERT INTO biz_booking (booking_no, member_id, court_id, booking_mode, booking_date, start_time, end_time, " +
            "actual_start_time, actual_end_time, duration, order_amount, payment_method, payment_status, " +
            "status, remark, create_time, update_time, del_flag) " +
            "VALUES (#{bookingNo}, #{memberId}, #{courtId}, #{bookingMode}, #{bookingDate}, #{startTime}, #{endTime}, " +
            "#{actualStartTime}, #{actualEndTime}, #{duration}, #{orderAmount}, #{paymentMethod}, " +
            "#{paymentStatus}, #{status}, #{remark}, #{createTime}, #{updateTime}, 0)")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Booking booking);

    /**
     * 更新预约记录
     */
    @Update("UPDATE biz_booking SET member_id = #{memberId}, court_id = #{courtId}, booking_mode = #{bookingMode}, booking_date = #{bookingDate}, " +
            "start_time = #{startTime}, end_time = #{endTime}, actual_start_time = #{actualStartTime}, " +
            "actual_end_time = #{actualEndTime}, duration = #{duration}, order_amount = #{orderAmount}, " +
            "payment_method = #{paymentMethod}, payment_status = #{paymentStatus}, status = #{status}, " +
            "remark = #{remark}, update_time = #{updateTime} WHERE id = #{id} AND del_flag = 0")
    int update(Booking booking);

    /**
     * 逻辑删除预约记录
     */
    @Update("UPDATE biz_booking SET del_flag = 1, update_time = NOW() WHERE id = #{id}")
    int deleteById(@Param("id") Long id);

    /**
     * 更新预约状态
     */
    @Update("UPDATE biz_booking SET status = #{status}, update_time = NOW() " +
            "WHERE id = #{id} AND del_flag = 0")
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    /**
     * 查询以指定前缀开头的所有预约单号（含已软删除记录，用于生成唯一单号）
     * booking_no 有唯一约束，软删除行仍占号，生成新单号时需考虑
     */
    @Select("SELECT booking_no FROM biz_booking WHERE booking_no LIKE CONCAT(#{prefix}, '%')")
    List<String> findBookingNosByPrefix(@Param("prefix") String prefix);

    /**
     * 检查预约单号是否存在
     */
    @Select("<script>" +
            "SELECT COUNT(*) > 0 FROM biz_booking " +
            "WHERE booking_no = #{bookingNo} AND del_flag = 0 " +
            "<if test='excludeId != null'> AND id != #{excludeId} </if>" +
            "</script>")
    boolean existsByBookingNo(@Param("bookingNo") String bookingNo,
                             @Param("excludeId") Long excludeId);

    /**
     * 按状态统计预约数量
     */
    @Select("SELECT CAST(status AS SIGNED) AS status, COUNT(*) as count FROM biz_booking " +
            "WHERE del_flag = 0 GROUP BY status")
    List<Map<String, Object>> countByStatus();

    /**
     * 统计总预约数
     */
    @Select("SELECT COUNT(*) FROM biz_booking WHERE del_flag = 0")
    int countAll();

    /**
     * 查询已过结束时间的“进行中”预约（用于定时任务自动改为已完成）
     * 条件：status=3 且 (booking_date < 今日 或 (booking_date=今日 且 end_time <= 当前时间))
     */
    @Select("SELECT * FROM biz_booking WHERE status = 3 AND del_flag = 0 " +
            "AND (booking_date < #{today} OR (booking_date = #{today} AND end_time <= #{nowTime}))")
    List<Booking> findOverdueInProgressBookings(@Param("today") LocalDate today,
                                               @Param("nowTime") LocalTime nowTime);

    /**
     * 查询已到开始时间的“已支付”预约（用于定时任务推进为进行中）
     * 条件：status=2 且 (booking_date < 今日 或 (booking_date=今日 且 start_time <= 当前时间))
     */
    @Select("SELECT * FROM biz_booking WHERE status = 2 AND del_flag = 0 " +
            "AND (booking_date < #{today} OR (booking_date = #{today} AND start_time <= #{nowTime}))")
    List<Booking> findDuePaidBookings(@Param("today") LocalDate today,
                                      @Param("nowTime") LocalTime nowTime);

    /**
     * 按日期统计预订量（用于趋势图）
     */
    @Select("<script>" +
            "SELECT b.booking_date AS date, COUNT(*) AS cnt FROM biz_booking b " +
            "WHERE b.del_flag = 0 AND b.status != 0 " +
            "<if test='venueId != null'> AND EXISTS (" +
            "SELECT 1 FROM biz_booking_court bc LEFT JOIN sys_court c ON bc.court_id = c.id " +
            "WHERE bc.booking_id = b.id AND c.venue_id = #{venueId}) </if>" +
            "AND b.booking_date &gt;= #{startDate} AND b.booking_date &lt;= #{endDate} " +
            "GROUP BY b.booking_date ORDER BY b.booking_date" +
            "</script>")
    List<Map<String, Object>> countByDate(@Param("venueId") Long venueId,
                                         @Param("startDate") LocalDate startDate,
                                         @Param("endDate") LocalDate endDate);

    /**
     * 统计指定日期的预订量（排除已取消）
     */
    @Select("<script>" +
            "SELECT COUNT(*) FROM biz_booking b WHERE b.del_flag = 0 AND b.status != 0 AND b.booking_date = #{bookingDate} " +
            "<if test='venueId != null'> AND EXISTS (" +
            "SELECT 1 FROM biz_booking_court bc LEFT JOIN sys_court c ON bc.court_id = c.id " +
            "WHERE bc.booking_id = b.id AND c.venue_id = #{venueId}) </if>" +
            "</script>")
    int countByBookingDate(@Param("venueId") Long venueId,
                           @Param("bookingDate") LocalDate bookingDate);

    /**
     * 统计指定日期各小时段的预订量（用于高峰时段）
     */
    @Select("<script>" +
            "SELECT HOUR(b.start_time) AS hour, COUNT(*) AS cnt " +
            "FROM biz_booking b WHERE b.del_flag = 0 AND b.status != 0 AND b.booking_date = #{bookingDate} " +
            "<if test='venueId != null'> AND EXISTS (" +
            "SELECT 1 FROM biz_booking_court bc LEFT JOIN sys_court c ON bc.court_id = c.id " +
            "WHERE bc.booking_id = b.id AND c.venue_id = #{venueId}) </if>" +
            "GROUP BY HOUR(b.start_time) " +
            "ORDER BY cnt DESC" +
            "</script>")
    List<Map<String, Object>> countStartHourForDate(@Param("venueId") Long venueId,
                                                    @Param("bookingDate") LocalDate bookingDate);

    /**
     * 按场馆 + 日期统计预订量（用于 Dashboard 各场馆预订趋势）
     */
    @Select("<script>" +
            "SELECT c.venue_id AS venueId, v.venue_name AS venueName, b.booking_date AS date, COUNT(DISTINCT b.id) AS cnt " +
            "FROM biz_booking b " +
            "LEFT JOIN biz_booking_court bc ON bc.booking_id = b.id " +
            "LEFT JOIN sys_court c ON bc.court_id = c.id " +
            "LEFT JOIN sys_venue v ON c.venue_id = v.id " +
            "WHERE b.del_flag = 0 AND b.status != 0 " +
            "<if test='venueId != null'> AND c.venue_id = #{venueId} </if>" +
            "AND b.booking_date &gt;= #{startDate} AND b.booking_date &lt;= #{endDate} " +
            "GROUP BY c.venue_id, v.venue_name, b.booking_date " +
            "ORDER BY c.venue_id, b.booking_date" +
            "</script>")
    List<Map<String, Object>> countByVenueAndDate(@Param("venueId") Long venueId,
                                                 @Param("startDate") LocalDate startDate,
                                                 @Param("endDate") LocalDate endDate);

    /**
     * 最近预订（用于最近活动）
     */
    @Select("<script>" +
            "SELECT b.id, b.create_time, MIN(c.court_name) AS court_name, MIN(c.court_code) AS court_code " +
            "FROM biz_booking b " +
            "LEFT JOIN biz_booking_court bc ON bc.booking_id = b.id " +
            "LEFT JOIN sys_court c ON bc.court_id = c.id " +
            "WHERE b.del_flag = 0 " +
            "<if test='venueId != null'> AND c.venue_id = #{venueId} </if>" +
            "GROUP BY b.id, b.create_time " +
            "ORDER BY b.create_time DESC LIMIT #{limit}" +
            "</script>")
    List<Map<String, Object>> findRecentBookings(@Param("venueId") Long venueId,
                                                 @Param("limit") int limit);

    /**
     * 按日期 + 小时统计预订量（用于热力图：day 为星期几 0=周一..6=周日，hour 为 8-21）
     * 返回：date, hour, cnt
     */
    @Select("<script>" +
            "SELECT b.booking_date AS date, HOUR(b.start_time) AS hour, COUNT(*) AS cnt " +
            "FROM biz_booking b WHERE b.del_flag = 0 AND b.status != 0 " +
            "<if test='venueId != null'> AND EXISTS (" +
            "SELECT 1 FROM biz_booking_court bc LEFT JOIN sys_court c ON bc.court_id = c.id " +
            "WHERE bc.booking_id = b.id AND c.venue_id = #{venueId}) </if>" +
            "AND b.booking_date &gt;= #{startDate} AND b.booking_date &lt;= #{endDate} " +
            "AND HOUR(b.start_time) &gt;= 8 AND HOUR(b.start_time) &lt;= 21 " +
            "GROUP BY b.booking_date, HOUR(b.start_time) ORDER BY b.booking_date, hour" +
            "</script>")
    List<Map<String, Object>> countByHourAndDate(@Param("venueId") Long venueId,
                                                  @Param("startDate") LocalDate startDate,
                                                  @Param("endDate") LocalDate endDate);
}
